package at.peirleitner.core.system;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import at.peirleitner.core.Core;
import at.peirleitner.core.util.CoreSystem;
import at.peirleitner.core.util.LogType;
import at.peirleitner.core.util.database.TableType;
import at.peirleitner.core.util.moderation.ChatLog;
import at.peirleitner.core.util.moderation.ChatLogReviewResult;
import at.peirleitner.core.util.moderation.UserChatMessage;
import at.peirleitner.core.util.moderation.UserChatMessageFlag;
import at.peirleitner.core.util.moderation.UserChatMessageType;
import at.peirleitner.core.util.user.User;

/**
 * System used to moderate a {@link User}'s behavior
 * 
 * @since 1.0.14
 * @author Markus Peirleitner (Rengobli)
 * @see UserChatMessage
 * @see UserChatMessageFlag
 *
 */
public class ModerationSystem implements CoreSystem {

	private Collection<String> cachedBlockedPhrases;
	private Collection<String> cachedAllowedDomains;
	private Collection<String> blockedDomains;
	private HashMap<UUID, Integer> lastMessage;

	public ModerationSystem() {

		// Initialize
		this.createTable();
		this.cachedBlockedPhrases = new ArrayList<>();
		this.cachedAllowedDomains = new ArrayList<>();
		this.blockedDomains = new ArrayList<>();
		this.lastMessage = new HashMap<>();

		// Load
		this.reload();

	}

	public final Collection<String> getBlockedDomains() {
		return this.blockedDomains;
	}

	/**
	 * 
	 * @return Last message sent by this {@link User}. This will be cleared on
	 *         server leaving. The Integer refers to the unique ID of the
	 *         {@link UserChatMessage}.
	 * @since 1.0.14
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final HashMap<UUID, Integer> getLastMessage() {
		return lastMessage;
	}

	public final void reload() {

		if (Core.getInstance().getMySQL() == null || !Core.getInstance().getMySQL().isConnected()) {
			Core.getInstance().log(getClass(), LogType.DEBUG,
					"Not loading ModerationSystem cache data since MySQL Connection has not yet been established.");
			return;
		}

		this.getCachedBlockedPhrases().clear();
		this.getCachedBlockedPhrases().addAll(this.getBlockedPhrasesFromDatabase());
		Core.getInstance().log(getClass(), LogType.DEBUG,
				"Loaded " + this.getCachedBlockedPhrases().size() + " blocked Phrases from Database.");

		this.getCachedAllowedDomains().clear();
		this.getCachedAllowedDomains().addAll(this.getAllowedDomainsFromDatabase());
		Core.getInstance().log(getClass(), LogType.DEBUG,
				"Loaded " + this.getCachedAllowedDomains().size() + " allowed Domains from Database.");

//		this.getCachedChatLogs().clear();
//		this.getCachedChatLogs().addAll(this.getChatLogsFromDatabase());
//		Core.getInstance().log(getClass(), LogType.INFO,
//				"Loaded " + this.getCachedChatLogs().size() + " ChatLogs from Database.");

		try (InputStream inputStream = this.getClass().getResourceAsStream("/domains.txt");
				InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
				BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
				Stream<String> lines = bufferedReader.lines();) {
			lines.forEach(line -> this.getBlockedDomains().add(line));
		} catch (IOException e) {
			Core.getInstance().log(this.getClass(), LogType.ERROR,
					"Could not load blocked Domains from file 'domains.txt' in folder 'main/resources': "
							+ e.getMessage());
			return;
		}

		Core.getInstance().log(this.getClass(), LogType.DEBUG,
				"Loaded " + this.getBlockedDomains().size() + " blocked Domain Extensions");

	}

	/**
	 * 
	 * @param id - Unique ID of the {@link UserChatMessage}
	 * @return Message object or <code>null</code> if none can be found or an error
	 *         occurs
	 * @since 1.0.14
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final UserChatMessage getChatMessage(@Nonnull int id) {

		try {

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection()
					.prepareStatement("SELECT * FROM " + TableType.MODERATION_LOG_USER_MESSAGES.getTableName(true) + " WHERE id = ?");
			stmt.setInt(1, id);
			
			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {

				UserChatMessage ucm = this.getChatMessageByResultSet(rs);
				return ucm;

			} else {
				// None with the given ID
				return null;
			}

		} catch (SQLException e) {
			Core.getInstance().log(getClass(), LogType.ERROR,
					"Could not get Chat Message for ID '" + id + "'/SQL: " + e.getMessage());
			return null;
		}

	}

	/**
	 * Log a {@link UserChatMessage} towards the Database.
	 * 
	 * @param message - Message sent by the {@link User}
	 * @return Unique ID of this message as of incremented in the Database or -1 if
	 *         an error occurs.
	 */
	public final int logChatMessageToDatabase(@Nonnull UserChatMessage message) {

		try {

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection().prepareStatement(
					"INSERT INTO " + TableType.MODERATION_LOG_USER_MESSAGES.getTableName(true)
							+ " (uuid, message, sent, saveType, type, recipient) VALUES (?, ?, ?, ?, ?, ?);",
					Statement.RETURN_GENERATED_KEYS);
			stmt.setString(1, message.getUUID().toString());
			stmt.setString(2, message.getMessage());
			stmt.setLong(3, message.getSent());
			stmt.setInt(4, message.getSaveTypeID());
			stmt.setString(5, message.getType().toString());
			stmt.setString(6, message.getRecipient());
			
			stmt.executeUpdate();

			ResultSet rs = stmt.getGeneratedKeys();
			rs.next();

			return rs.getInt(1);

		} catch (SQLException e) {
			Core.getInstance().log(getClass(), LogType.ERROR,
					"Could not log ChatMessage '" + message.toString() + "' to Database/SQL: " + e.getMessage());
			return -1;
		}

	}

	public final Collection<UserChatMessageFlag> checkMessage(@Nonnull UUID uuid, @Nonnull String message) {

		List<UserChatMessageFlag> flags = new ArrayList<>(UserChatMessageFlag.values().length);
		UserChatMessage lastMessage = null;

		if (this.getLastMessage().containsKey(uuid)) {
			lastMessage = this.getChatMessage(this.getLastMessage().get(uuid));
		}

		// Spam
		if (lastMessage != null && lastMessage.getMessage().equalsIgnoreCase(message)) {
			flags.add(UserChatMessageFlag.SPAM);
		}

		// Caps
		int length = message.length();
		int caps = 0;
		double maxAllowed = 75.0;

		for (int i = 0; i < length; i++) {

			char c = message.charAt(i);

			if (Character.isUpperCase(c)) {
				caps++;
			}

		}

		if (caps > 0) {

			int capsPercentage = (length / caps);

			if (capsPercentage > maxAllowed) {
				flags.add(UserChatMessageFlag.CAPS);
			}

		}

		// Blocked Phrase
		for (String s : this.getBlockedPhrases()) {
			if (message.toLowerCase().contains(s.toLowerCase())) {
				flags.add(UserChatMessageFlag.BLOCKED_PHRASE);
				break;
			}
		}

		// Advertising
		for (String s : this.getBlockedDomains()) {
			if (message.toLowerCase().contains("." + s.toLowerCase())) {

				for (String allowed : this.getAllowedDomains()) {
					if (message.contains(allowed)) {
						continue;
					}
				}

				flags.add(UserChatMessageFlag.ADVERTISING);
				break;
			}
		}

		// Cooldown
		if (lastMessage != null) {

			long nextUsage = lastMessage.getSent() + (1000L * this.getChatCooldown());

			if (nextUsage > System.currentTimeMillis()) {
				flags.add(UserChatMessageFlag.COOLDOWN);
			}

		}

		return flags;
	}

	public final int getChatCooldown() {
		return Integer.valueOf(Core.getInstance().getSettingsManager().getSetting(Core.getInstance().getPluginName(),
				"system.moderation.chat-cooldown"));
	}

	public final Collection<String> getBlockedPhrases() {
		return this.getCachedBlockedPhrases().isEmpty() ? this.getBlockedPhrasesFromDatabase()
				: this.getCachedBlockedPhrases();
	}

	private final Collection<String> getCachedBlockedPhrases() {
		return this.cachedBlockedPhrases;
	}

	private final Collection<String> getBlockedPhrasesFromDatabase() {

		Collection<String> phrases = new ArrayList<>();

		try {

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection()
					.prepareStatement("SELECT * FROM " + TableType.MODERATION_BLOCKED_PHRASES.getTableName(true));
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {

				String phrase = rs.getString(1);
				phrases.add(phrase);

			}

		} catch (SQLException e) {
			Core.getInstance().log(getClass(), LogType.ERROR,
					"Could not get blocked phrases from Database/SQL: " + e.getMessage());

		}

		return phrases;

	}

	public final Collection<String> getAllowedDomains() {
		return this.getCachedAllowedDomains().isEmpty() ? this.getAllowedDomainsFromDatabase()
				: this.getCachedAllowedDomains();
	}

	private final Collection<String> getCachedAllowedDomains() {
		return this.cachedAllowedDomains;
	}

	private final Collection<String> getAllowedDomainsFromDatabase() {

		Collection<String> domains = new ArrayList<>();

		try {

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection()
					.prepareStatement("SELECT * FROM " + TableType.MODERATION_ALLOWED_DOMAINS.getTableName(true));
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {

				String domain = rs.getString(1);
				domains.add(domain);

			}

		} catch (SQLException e) {
			Core.getInstance().log(getClass(), LogType.ERROR,
					"Could not get allowed domains from Database/SQL: " + e.getMessage());

		}

		return domains;

	}

	public final String toString(@Nonnull Collection<UserChatMessageFlag> flags) {

		StringBuilder sb = new StringBuilder();

		for (UserChatMessageFlag flag : flags) {
			sb.append(flag.toString() + ";");
		}

		return sb.toString();
	}

	public final Collection<UserChatMessageFlag> fromString(@Nonnull String message) {

		Collection<UserChatMessageFlag> flags = new ArrayList<>();
		String[] string = message.split(";");

		for (String s : string) {
			flags.add(UserChatMessageFlag.valueOf(s));
		}

		return flags;
	}

	public final ChatLog createChatLog(@Nonnull UserChatMessage message,
			@Nonnull Collection<UserChatMessageFlag> flags) {

		if (this.hasActiveChatLog(message.getUUID())) {
			Core.getInstance().log(getClass(), LogType.DEBUG, "Not creating new ChatLog of User '"
					+ message.getUUID().toString() + "' since an active one does already exist.");
			return null;
		}

		try {

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection().prepareStatement("INSERT INTO "
					+ TableType.MODERATION_CHATLOGS.getTableName(true) + " (message, flags) VALUES (?, ?);",
					Statement.RETURN_GENERATED_KEYS);
			stmt.setInt(1, message.getID());
			stmt.setString(2, this.toString(flags));
			stmt.executeUpdate();

			ResultSet rs = stmt.getGeneratedKeys();
			rs.next();

			int id = rs.getInt(1);

			ChatLog chatLog = new ChatLog();
			chatLog.setID(id);
			chatLog.setMessageID(message.getID());
			chatLog.setFlags(flags);
			chatLog.setStaff(null);
			chatLog.setReviewed(-1);
			chatLog.setResult(null);

//			this.getCachedChatLogs().add(chatLog);
			Core.getInstance().log(getClass(), LogType.DEBUG,
					"Created ChatLog '" + chatLog.getID() + "' for ChatMessage '" + chatLog.getMessageID() + "'.");

			return chatLog;

		} catch (SQLException e) {
			Core.getInstance().log(getClass(), LogType.ERROR, "Could not create new ChatLog of User '"
					+ message.getUUID().toString() + "'/SQL: " + e.getMessage());
			return null;
		}

	}

	/**
	 * 
	 * @param id - ChatLog ID
	 * @return ChatLog with the given ID or <code>null</code> if none can be found
	 * @since 1.0.14
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final ChatLog getChatLog(@Nonnull int id) {

		try {

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection().prepareStatement(
					"SELECT * FROM " + TableType.MODERATION_CHATLOGS.getTableName(true) + " WHERE id = ?");
			stmt.setInt(1, id);

			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {

				return this.getChatLogByResultSet(rs);

			} else {
				// No ChatLog
				return null;
			}

		} catch (SQLException e) {
			Core.getInstance().log(getClass(), LogType.ERROR,
					"Could not get ChatLog with ID '" + id + "' from Database/SQL: " + e.getMessage());
			return null;
		}

	}

	/**
	 * 
	 * @return All existing ChatLogs inside the Database
	 * @since 1.0.14
	 * @author Markus Peirleitner (Rengobli)
	 * @see #getChatLogs(UUID)
	 * @see TableType#MODERATION_CHATLOGS
	 */
	public final Collection<ChatLog> getChatLogs() {

		Collection<ChatLog> chatLogs = new ArrayList<>();

		try {

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection()
					.prepareStatement("SELECT * FROM " + TableType.MODERATION_CHATLOGS.getTableName(true));
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {

				ChatLog cl = this.getChatLogByResultSet(rs);
				chatLogs.add(cl);

			}

			return chatLogs;

		} catch (SQLException e) {
			Core.getInstance().log(getClass(), LogType.ERROR,
					"Could not get ChatLogs from Database/SQL: " + e.getMessage());
			return null;
		}

	}

	/**
	 * 
	 * @param rs - ResultSet
	 * @return ChatLog Object as of the ResultSet
	 * @throws SQLException If an error with this {@link ResultSet} occurs
	 * @since 1.0.14
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final ChatLog getChatLogByResultSet(@Nonnull ResultSet rs) throws SQLException {

		int id = rs.getInt(1);
		int messageID = rs.getInt(2);
		Collection<UserChatMessageFlag> flags = this.fromString(rs.getString(3));
		UUID staff = (rs.getString(4) == null ? null : UUID.fromString(rs.getString(4)));
		long reviewed = rs.getLong(5);
		ChatLogReviewResult result = (rs.getString(6) == null ? null : ChatLogReviewResult.valueOf(rs.getString(6)));

		return new ChatLog(id, messageID, flags, staff, reviewed, result);
	}

	public final UserChatMessage getChatMessageByResultSet(@Nonnull ResultSet rs) throws SQLException {

		int id = rs.getInt(1);
		UUID uuid = UUID.fromString(rs.getString(2));
		String message = rs.getString(3);
		long sent = rs.getLong(4);
		int saveType = rs.getInt(5);
		UserChatMessageType type = UserChatMessageType.valueOf(rs.getString(6));
		String recipient = rs.getString(7);

		UserChatMessage ucm = new UserChatMessage(id, uuid, message, sent, saveType, type, recipient);

		return ucm;
	}

	/**
	 * 
	 * @param uuid - UUID to get the ChatLogs for
	 * @return All ChatLogs that have been issued against this User
	 * @since 1.0.14
	 * @author Markus Peirleitner (Rengobli)
	 * @see #getChatLogs()
	 */
	public final Collection<ChatLog> getChatLogs(@Nonnull UUID uuid) {

		Collection<ChatLog> chatLogs = new ArrayList<>();

		for(ChatLog cl : this.getChatLogs()) {
			if(cl.getChatMessage().getUUID().equals(uuid)) {
				chatLogs.add(cl);
			}
		}
		
		return chatLogs;
	}

	public final ChatLog getActiveChatLog(@Nonnull UUID uuid) {

		Collection<ChatLog> chatLogs = this.getChatLogs(uuid);

		// Return if no ChatLogs can be found
		if (chatLogs.isEmpty()) {
			return null;
		}

		for (ChatLog cl : chatLogs) {
			if (!cl.isReviewed()) {
				return cl;
			}
		}

		return null;
	}

	/**
	 * 
	 * @param uuid - UUID to get the ChatLogs for
	 * @return If the given User does have an active {@link ChatLog} issued against
	 *         them. A ChatLog is considered 'active' as long as it does not exist
	 *         inside the {@link TableType#MODERATION_CHATLOGS_REVIEWS} table.
	 * @since 1.0.14
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final boolean hasActiveChatLog(@Nonnull UUID uuid) {
		return this.getActiveChatLog(uuid) == null ? false : true;
	}

	@Override
	public void createTable() {

		try {

			PreparedStatement stmt = null;
			List<String> statements = new ArrayList<>();

			statements.add("CREATE TABLE IF NOT EXISTS " + TableType.MODERATION_BLOCKED_PHRASES.getTableName(true)
					+ " (" + "phrase VARCHAR(30) NOT NULL, " + "added BIGINT(255) NOT NULL DEFAULT '"
					+ System.currentTimeMillis() + "', " + "staff CHAR(36), " + "PRIMARY KEY(phrase));");

			statements.add("CREATE TABLE IF NOT EXISTS " + TableType.MODERATION_ALLOWED_DOMAINS.getTableName(true)
					+ " (" + "domain VARCHAR(80) NOT NULL, " + "added BIGINT(255) NOT NULL DEFAULT '"
					+ System.currentTimeMillis() + "', " + "staff CHAR(36), " + "PRIMARY KEY(domain));");

			statements.add("CREATE TABLE IF NOT EXISTS " + TableType.MODERATION_LOG_USER_MESSAGES.getTableName(true)
					+ " (" + "id INT AUTO_INCREMENT NOT NULL, " + "uuid CHAR(36) NOT NULL, "
					+ "message VARCHAR(150) NOT NULL, " + "sent BIGINT(255) NOT NULL DEFAULT '"
					+ System.currentTimeMillis() + "', " + "saveType INT NOT NULL, " + "type ENUM('"
					+ UserChatMessageType.PUBLIC.toString() + "', '" + UserChatMessageType.PRIVATE.toString() + "'), "
					+ "recipient VARCHAR(50), " + "PRIMARY KEY (id), " + "FOREIGN KEY (saveType) REFERENCES "
					+ TableType.SAVE_TYPE.getTableName(true) + "(id));");

			statements.add("CREATE TABLE IF NOT EXISTS " + TableType.MODERATION_CHATLOGS.getTableName(true) + " ("
					+ "id INT AUTO_INCREMENT NOT NULL, " + "message INT NOT NULL, " + "flags VARCHAR(150) NOT NULL, "
					+ "staff CHAR(36), " + "reviewed BIGINT(255) NOT NULL DEFAULT '-1', " + "result ENUM('"
					+ ChatLogReviewResult.JUSTIFIED.toString() + "', '" + ChatLogReviewResult.NOT_JUSTIFIED.toString()
					+ "'), " + "PRIMARY KEY (id), " + "FOREIGN KEY (message) REFERENCES "
					+ TableType.MODERATION_LOG_USER_MESSAGES.getTableName(true) + "(id));");

			for (String s : statements) {

				stmt = Core.getInstance().getMySQL().getConnection().prepareStatement(s);
				stmt.execute();

			}

		} catch (SQLException e) {
			Core.getInstance().log(getClass(), LogType.ERROR,
					"Could not create tables for ModerationSystem/SQL: " + e.getMessage());
			return;
		}
	}

	@Override
	public TableType getTableType() {
		return null;
	}

}
