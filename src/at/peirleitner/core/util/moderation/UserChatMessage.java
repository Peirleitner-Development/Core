package at.peirleitner.core.util.moderation;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import at.peirleitner.core.Core;
import at.peirleitner.core.util.LogType;
import at.peirleitner.core.util.database.SaveType;
import at.peirleitner.core.util.database.TableType;
import at.peirleitner.core.util.user.User;

/**
 * Chat Message sent by a {@link User}
 * 
 * @since 1.0.14
 * @author Markus Peirleitner (Rengobli)
 *
 */
public class UserChatMessage {

	private int id;
	private UUID uuid;
	private String message;
	private long sent;
	private int saveTypeID;
	private UserChatMessageType type;
	private String recipient;
	private String metaData;

	public UserChatMessage() {
		
	}
	
	public UserChatMessage(int id, UUID uuid, String message, long sent, int saveTypeID, UserChatMessageType type,
			String recipient, String metaData) {
		this.id = id;
		this.uuid = uuid;
		this.message = message;
		this.sent = sent;
		this.saveTypeID = saveTypeID;
		this.type = type;
		this.recipient = recipient;
		this.metaData = metaData;
	}

	public final int getID() {
		return id;
	}

	public final void setID(int id) {
		this.id = id;
	}

	public final UUID getUUID() {
		return uuid;
	}

	public final void setUUID(UUID uuid) {
		this.uuid = uuid;
	}

	public final String getMessage() {
		return message;
	}

	public final void setMessage(String message) {
		this.message = message;
	}

	public final long getSent() {
		return sent;
	}

	public final void setSent(long sent) {
		this.sent = sent;
	}

	public final SaveType getSaveType() {
		return Core.getInstance().getSaveTypeByID(this.getSaveTypeID());
	}

	public final int getSaveTypeID() {
		return saveTypeID;
	}

	public final void setSaveTypeID(int saveTypeID) {
		this.saveTypeID = saveTypeID;
	}

	public final UserChatMessageType getType() {
		return type;
	}

	public final void setType(UserChatMessageType type) {
		this.type = type;
	}

	public final String getRecipient() {
		return recipient;
	}

	/**
	 * 
	 * @return {@link User} created from {@link #getRecipient()}
	 * @since 1.0.14
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final User getRecipientUser() {
		return this.isRecipientUser() ? Core.getInstance().getUserSystem().getUser(UUID.fromString(this.getRecipient())) : null;
	}

	public final void setRecipient(String recipient) {
		this.recipient = recipient;
	}

	public final boolean hasRecipient() {
		return this.getRecipient() == null ? false : true;
	}

	public final boolean isRecipientUser() {

		if (!this.hasRecipient())
			return false;

		try {

			UUID uuid = UUID.fromString(this.getRecipient());
			
			if(uuid != null) {
				return true;
			} else {
				return false;
			}

		} catch (IllegalArgumentException ex) {
			// Recipient is not a valid UUID
			return false;
		}

	}
	
	
	
	public final String getMetaData() {
		return metaData;
	}

	public final void setMetaData(String metaData) {
		this.metaData = metaData;
	}
	
	public final boolean hasMetaData() {
		return this.getMetaData() == null ? false : true;
	}

	public final ChatLog getChatLog() {
		
		try {
			
			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection().prepareStatement("SELECT * FROM " + TableType.MODERATION_CHATLOGS.getTableName(true) + " WHERE message = ?");
			stmt.setInt(1, this.getID());
			
			ResultSet rs = stmt.executeQuery();
			
			if(rs.next()) {
				return Core.getInstance().getModerationSystem().getChatLogByResultSet(rs);
			} else {
				// This message has not been flagged
				return null;
			}
			
		} catch (SQLException e) {
			Core.getInstance().log(getClass(), LogType.ERROR, "Could not get ChatLog for UserChatMessage '" + this.getID() + "'/SQL: " + e.getMessage());
			return null;
		}
		
	}
	
	public final boolean hasChatLog() {
		return this.getChatLog() == null ? false : true;
	}

	@Override
	public String toString() {
		return "UserChatMessage [id=" + id + ", uuid=" + uuid + ", message=" + message + ", sent=" + sent
				+ ", saveTypeID=" + saveTypeID + ", type=" + type + ", recipient=" + recipient + ", metaData=" + metaData + "]";
	}
	
	

}
