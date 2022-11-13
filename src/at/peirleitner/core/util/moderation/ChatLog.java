package at.peirleitner.core.util.moderation;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

import javax.annotation.Nonnull;

import at.peirleitner.core.Core;
import at.peirleitner.core.system.ModerationSystem;
import at.peirleitner.core.util.DiscordWebHookType;
import at.peirleitner.core.util.LogType;
import at.peirleitner.core.util.database.TableType;
import at.peirleitner.core.util.user.User;

/**
 * System logged {@link UserChatMessage}s. A ChatLog will automatically be
 * created on Reports or Chat-Content-Filter triggers
 * 
 * @since 1.0.14
 * @author Markus Peirleitner (Rengobli)
 * @see UserChatMessageFlag
 */
public final class ChatLog {

	private int id;
	private long created;
	private UUID creator;
	private String comment;

	public ChatLog() {
		this.id = -1;
		this.created = -1;
		this.creator = null;
		this.comment = null;
	}

	/**
	 * 
	 * @return Unique ID of this ChatLog
	 * @since 1.0.14
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final int getID() {
		return id;
	}

	public final void setID(int id) {
		this.id = id;
	}

	/**
	 * 
	 * @return TimeStamp of ChatLog creation
	 * @since 1.0.14
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final long getCreated() {
		return created;
	}

	public final void setCreated(long created) {
		this.created = created;
	}

	/**
	 * 
	 * @return UUID of this ChatLogs' Creator
	 * @since 1.0.14
	 * @author Markus Peirleitner (Rengobli)
	 * @see #isSystemGenerated()
	 * @apiNote May be <code>null</code> if issued by the CONSOLE
	 */
	public final UUID getCreator() {
		return creator;
	}
	
	public final String getCreatorName() {
		return this.isSystemGenerated() ? "ModerationSystem" : Core.getInstance().getUserSystem().getUser(this.getCreator()).getLastKnownName();
	}

	public final void setCreator(UUID creator) {
		this.creator = creator;
	}

	/**
	 * 
	 * @return If this ChatLog has been generated by the CONSOLE
	 * @since 1.0.14
	 * @author Markus Peirleitner (Rengobli)
	 * @see #getCreator()
	 */
	public final boolean isSystemGenerated() {
		return this.getCreator() == null;
	}

	/**
	 * 
	 * @return Comment that has been assigned to this ChatLog
	 * @since 1.0.14
	 * @author Markus Peirleitner (Rengobli)
	 * @see #hasComment()
	 * @apiNote This will return an empty String if no comment has been associated
	 *          with this ChatLog
	 */
	public final String getComment() {
		return this.comment == null ? "" : this.comment;
	}

	public final void setComment(String comment) {
		this.comment = comment;
	}

	/**
	 * 
	 * @return If this ChatLog has a comment associated with it
	 * @since 1.0.14
	 * @author Markus Peirleitner (Rengobli)
	 * @see #getComment()
	 */
	public final boolean hasComment() {
		return this.getComment() == null ? false : true;
	}

	/**
	 * 
	 * @return Messages associated with this ChatLog
	 * @since 1.0.14
	 * @author Markus Peirleitner (Rengobli)
	 * @see #getFlaggedMessages()
	 */
	public final Collection<UserChatMessage> getMessages() {

		Collection<UserChatMessage> messages = new ArrayList<>();

		try {

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection().prepareStatement("SELECT * FROM "
					+ TableType.MODERATION_CHATLOGS_MESSAGES.getTableName(true) + " WHERE chatLog = ?");
			stmt.setInt(1, this.getID());

			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {

				int id = rs.getInt(1);
//				int chatLog = rs.getInt(2);
				UUID uuid = UUID.fromString(rs.getString(3));
				String message = rs.getString(4);
				Collection<UserChatMessageFlag> flags = (rs.getString(5) == null ? new ArrayList<>()
						: Core.getInstance().getModerationSystem().fromString(rs.getString(5)));
				long sent = rs.getLong(6);

				UserChatMessage ucm = new UserChatMessage();
				ucm.setID(id);
				// ChatLog is not part of this Object
				ucm.setUUID(uuid);
				ucm.setMessage(message);
				ucm.setFlags(flags);
				ucm.setCreated(sent);

				messages.add(ucm);

			}

			return messages;

		} catch (SQLException e) {
			Core.getInstance().log(getClass(), LogType.ERROR,
					"Could not get Messages from ChatLog '" + this.getID() + "'/SQL: " + e.getMessage());
			return null;
		}

	}

	/**
	 * 
	 * @return Messages that have been flagged by the {@link ModerationSystem}
	 * @since 1.0.14
	 * @author Markus Peirleitner (Rengobli)
	 * @see #getMessages()
	 */
	public final Collection<UserChatMessage> getFlaggedMessages() {

		Collection<UserChatMessage> messages = new ArrayList<>();

		try {

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection()
					.prepareStatement("SELECT * FROM " + TableType.MODERATION_CHATLOGS_MESSAGES.getTableName(true)
							+ " WHERE chatLog = ? AND flags NOT NULL");
			stmt.setInt(1, this.getID());

			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {

				int id = rs.getInt(1);
//				int chatLog = rs.getInt(2);
				UUID uuid = UUID.fromString(rs.getString(3));
				String message = rs.getString(4);
				Collection<UserChatMessageFlag> flags = (rs.getString(5) == null ? new ArrayList<>()
						: Core.getInstance().getModerationSystem().fromString(rs.getString(5)));
				long sent = rs.getLong(6);

				UserChatMessage ucm = new UserChatMessage();
				ucm.setID(id);
				// ChatLog is not part of this Object
				ucm.setUUID(uuid);
				ucm.setMessage(message);
				ucm.setFlags(flags);
				ucm.setCreated(sent);

				messages.add(ucm);

			}

			return messages;

		} catch (SQLException e) {
			Core.getInstance().log(getClass(), LogType.ERROR,
					"Could not get flagged Messages from ChatLog '" + this.getID() + "'/SQL: " + e.getMessage());
			return null;
		}

	}

	public final boolean hasFlaggedMessages() {
		return this.getFlaggedMessages().isEmpty() ? false : true;
	}

	/**
	 * As long as a ChatLog is not reviewed, the associated User can not send any
	 * chat messages.
	 * 
	 * @param user   - User reviewing this ChatLog
	 * @param result - Result of reviewing
	 * @return If the Review has been inserted
	 */
	public final boolean review(@Nonnull User user, @Nonnull ChatLogReviewResult result) {

		if (this.hasReview()) {
			user.sendMessage(Core.getInstance().getPluginName(),
					"system.moderation.chatLog.review.error.already-has-review", Arrays.asList("" + this.getID()),
					true);
			return false;
		}

		try {

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection()
					.prepareStatement("INSERT INTO " + TableType.MODERATION_CHATLOGS_REVIEWS.getTableName(true)
							+ " (chatLog, staff, review, reviewed) VALUES (?, ?, ?, ?);");
			stmt.setInt(1, this.getID());
			stmt.setString(2, user.getUUID().toString());
			stmt.setString(3, result.toString());
			stmt.setLong(4, System.currentTimeMillis());

			int updated = stmt.executeUpdate();

			if (updated == 1) {

				user.sendMessage(Core.getInstance().getPluginName(), "system.moderation.chatLog.review.success",
						Arrays.asList("" + this.getID(), result.toString()), true);
				Core.getInstance().getLanguageManager().notifyStaff(Core.getInstance().getPluginName(),
						"notify.system.moderation.chatLog.review",
						Arrays.asList(user.getDisplayName(), "" + this.getID(), result.toString()), true);
				Core.getInstance().createWebhook(user.getLastKnownName() + " reviewed the ChatLog " + this.getID()
						+ ". Result: " + result.toString(), DiscordWebHookType.STAFF_NOTIFICATION);

				Core.getInstance().log(getClass(), LogType.DEBUG, "User '" + user.getUUID().toString()
						+ "' reviewed the ChatLog '" + this.getID() + "' with result '" + result.toString() + "'.");
				return true;
			} else {
				Core.getInstance().log(getClass(), LogType.WARNING, "Could not review ChatLog '" + this.getID()
						+ "': Updated row count returned '" + updated + "' where 1 was expected.");
				return false;
			}

		} catch (SQLException e) {
			Core.getInstance().log(getClass(), LogType.ERROR,
					"Could not review ChatLog '" + this.getID() + "'/SQL: " + e.getMessage());
			return false;
		}

	}

	/**
	 * 
	 * @return Review as stated by the staff
	 * @since 1.0.14
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final ChatLogReviewResult getReviewResult() {

		try {

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection()
					.prepareStatement("SELECT review FROM " + TableType.MODERATION_CHATLOGS_REVIEWS.getTableName(true)
							+ " WHERE chatLog = ?");
			stmt.setInt(1, this.getID());

			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {

				ChatLogReviewResult result = ChatLogReviewResult.valueOf(rs.getString(1));
				return result;

			} else {
//				Core.getInstance().log(getClass(), LogType.DEBUG,
//						"ChatLog '" + this.getID() + "' does not have a review result.");
				return null;
			}

		} catch (SQLException | IllegalArgumentException e) {
			Core.getInstance().log(getClass(), LogType.ERROR,
					"Could not get review result of the ChatLog '" + this.getID() + "'/SQL: " + e.getMessage());
			return null;
		}

	}

	/**
	 * 
	 * @return If this ChatLog has yet been reviewed by the staff
	 * @since 1.0.14
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final boolean hasReview() {
		return this.getReviewResult() == null ? false : true;
	}

}