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
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import at.peirleitner.core.Core;
import at.peirleitner.core.util.CoreSystem;
import at.peirleitner.core.util.LogType;
import at.peirleitner.core.util.database.TableType;
import at.peirleitner.core.util.moderation.ChatLog;
import at.peirleitner.core.util.moderation.ChatLogReviewResult;
import at.peirleitner.core.util.moderation.UserChatMessage;
import at.peirleitner.core.util.moderation.UserChatMessageFlag;
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
	private Collection<UserChatMessage> cachedMessages;
	private Collection<String> blockedDomains;

	public ModerationSystem() {

		// Initialize
		this.createTable();
		this.cachedBlockedPhrases = new ArrayList<>();
		this.cachedAllowedDomains = new ArrayList<>();
		this.cachedMessages = new ArrayList<>();
		this.blockedDomains = new ArrayList<>();

		// Load
		this.reload();

	}

	public final Collection<String> getBlockedDomains() {
		return this.blockedDomains;
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

	public final Collection<UserChatMessage> getCachedMessages() {
		return this.cachedMessages;
	}

	public final Collection<UserChatMessage> getCachedMessages(@Nonnull UUID uuid) {

		Collection<UserChatMessage> messages = new ArrayList<>();

		for (UserChatMessage msg : this.getCachedMessages()) {
			if (msg.getUUID().equals(uuid)) {
				messages.add(msg);
			}
		}

		return messages;
	}

	public final Collection<UserChatMessage> getCachedMessages(@Nonnull UUID uuid, @Nonnull int lastMinutes) {

		Collection<UserChatMessage> messages = new ArrayList<>();
		long maxAllowed = System.currentTimeMillis() - (1000L * 60 * lastMinutes);

		for (UserChatMessage msg : this.getCachedMessages()) {

			if (msg.getUUID().equals(uuid) && msg.getCreated() >= maxAllowed) {
				messages.add(msg);
			}

		}

		return messages;
	}

	public final UserChatMessage getLatestCachedMessage(@Nonnull UUID uuid) {

		Collection<UserChatMessage> messages = this.getCachedMessages(uuid);

		if (messages.isEmpty()) {
			return null;
		}

		UserChatMessage latest = null;
		long greatest = -1;

		for (UserChatMessage ucm : messages) {
			if (ucm.getCreated() > greatest) {
				greatest = ucm.getCreated();
				latest = ucm;
			}
		}

		return latest;
	}

	public final Collection<UserChatMessageFlag> checkMessage(@Nonnull UUID uuid, @Nonnull String message) {

		List<UserChatMessageFlag> flags = new ArrayList<>(UserChatMessageFlag.values().length);
		UserChatMessage lastMessage = this.getLatestCachedMessage(uuid);

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

			long nextUsage = lastMessage.getCreated() + (1000L * this.getChatCooldown());

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

	public final ChatLog createChatLog(@Nullable UUID creator, @Nonnull UUID target, @Nullable String comment) {

		if (this.hasActiveChatLog(target)) {
			Core.getInstance().log(getClass(), LogType.DEBUG, "Not creating new ChatLog of User '" + target.toString()
					+ "' since an active one does already exist.");
			return null;
		}

		// Get messages sent from this User of the last ten minutes
		Collection<UserChatMessage> messages = this.getCachedMessages(target);

		try {

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection().prepareStatement("INSERT INTO "
					+ TableType.MODERATION_CHATLOGS.getTableName(true) + " (creator, comment) VALUES (?, ?);",
					Statement.RETURN_GENERATED_KEYS);
			stmt.setString(1, creator == null ? null : creator.toString());
			stmt.setString(2, comment);
			stmt.executeUpdate();

			ResultSet rs = stmt.getGeneratedKeys();
			rs.next();

			int id = rs.getInt(1);

			ChatLog chatLog = new ChatLog();
			chatLog.setID(id);
			chatLog.setCreated(System.currentTimeMillis());
			chatLog.setCreator(creator);
			chatLog.setComment(comment);

//			this.getCachedChatLogs().add(chatLog);
			Core.getInstance().log(getClass(), LogType.DEBUG,
					"Created ChatLog '" + chatLog.getID() + "', uploading messages to Database (" + messages.size() + ")..");

			for (UserChatMessage ucm : messages) {

				stmt = Core.getInstance().getMySQL().getConnection().prepareStatement(
						"INSERT INTO " + TableType.MODERATION_CHATLOGS_MESSAGES.getTableName(true)
								+ " (chatLog, uuid, message, flags, sent) VALUES (?, ?, ?, ?, ?);",
						Statement.RETURN_GENERATED_KEYS);
				stmt.setInt(1, chatLog.getID());
				stmt.setString(2, ucm.getUUID().toString());
				stmt.setString(3, ucm.getMessage());
				stmt.setString(4, ucm.hasFlags() ? this.toString(ucm.getFlags()) : null);
				stmt.setLong(5, ucm.getCreated());

				stmt.executeUpdate();
				rs = stmt.getGeneratedKeys();
				rs.next();

				ucm.setID(rs.getInt(1));

			}

			Core.getInstance().log(getClass(), LogType.DEBUG,
					"Fully uploaded all logged messages (" + messages.size() + ") towards the Database.");
			return chatLog;

		} catch (SQLException e) {
			Core.getInstance().log(getClass(), LogType.ERROR,
					"Could not create new ChatLog of User '" + target.toString() + "'/SQL: " + e.getMessage());
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

				return this.getByResultSet(rs);

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

				ChatLog cl = this.getByResultSet(rs);
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
	private final ChatLog getByResultSet(@Nonnull ResultSet rs) throws SQLException {

		int id = rs.getInt(1);
		long created = rs.getLong(2);
		UUID creator = (rs.getString(3) == null ? null : UUID.fromString(rs.getString(3)));
		String comment = rs.getString(4);

		ChatLog chatLog = new ChatLog();
		chatLog.setID(id);
		chatLog.setCreated(created);
		chatLog.setCreator(creator);
		chatLog.setComment(comment);

		return chatLog;
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

//		// Check Cache if available
//		if(!this.getCachedChatLogs().isEmpty()) {
//			
//			for(ChatLog cl : this.getCachedChatLogs()) {
//				if(cl.getMessage().getUUID().equals(uuid)) {
//					chatLogs.add(cl);
//				}
//			}
//			
//			return chatLogs;
//		}

		// Check from Database if Cache is empty
		try {

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection().prepareStatement(
					"SELECT chatLog FROM " + TableType.MODERATION_CHATLOGS_MESSAGES.getTableName(true) + " WHERE uuid = ?");
			stmt.setString(1, uuid.toString());

			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {

				ChatLog cl = this.getChatLog(rs.getInt(1));
				chatLogs.add(cl);

			}

//			this.getCachedChatLogs().addAll(chatLogs);
			return chatLogs;

		} catch (SQLException e) {
			Core.getInstance().log(getClass(), LogType.ERROR,
					"Could not get ChatLogs for User '" + uuid.toString() + "'/SQL: " + e.getMessage());
			return null;
		}

	}


	public final ChatLog getActiveChatLog(@Nonnull UUID uuid) {

		Collection<ChatLog> chatLogs = this.getChatLogs(uuid);

		// Return if no ChatLogs can be found
		if (chatLogs.isEmpty()) {
			return null;
		}

		for (ChatLog cl : chatLogs) {
			if (!cl.hasReview()) {
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

			statements.add("CREATE TABLE IF NOT EXISTS " + TableType.MODERATION_CHATLOGS.getTableName(true) + " ("
					+ "id INT AUTO_INCREMENT NOT NULL, " + "created BIGINT(255) NOT NULL DEFAULT '"
					+ System.currentTimeMillis() + "', " + "creator CHAR(36), "
					+ "comment VARCHAR(200), " + "PRIMARY KEY(id));");

			statements.add("CREATE TABLE IF NOT EXISTS " + TableType.MODERATION_CHATLOGS_MESSAGES.getTableName(true)
					+ " (" + "id INT AUTO_INCREMENT NOT NULL, " + "chatLog INT NOT NULL, " + "uuid CHAR(36) NOT NULL, "
					+ "message VARCHAR(150) NOT NULL, " + "flags VARCHAR(150), "
					+ "sent BIGINT(255) NOT NULL, " + "PRIMARY KEY (id), " + "FOREIGN KEY (chatLog) REFERENCES "
					+ TableType.MODERATION_CHATLOGS.getTableName(true) + "(id));");

			statements.add("CREATE TABLE IF NOT EXISTS " + TableType.MODERATION_CHATLOGS_REVIEWS.getTableName(true)
					+ " (" + "chatLog INT NOT NULL, " + "staff CHAR(36) NOT NULL, " + "review ENUM('"
					+ ChatLogReviewResult.JUSTIFIED.toString() + "', '" + ChatLogReviewResult.NOT_JUSTIFIED.toString()
					+ "') NOT NULL, " + "reviewed BIGINT(255) NOT NULL DEFAULT '" + System.currentTimeMillis() + "', "
					+ "PRIMARY KEY (chatLog));");

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
