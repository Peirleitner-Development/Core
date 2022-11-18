package at.peirleitner.core.util.moderation;

import java.util.Collection;
import java.util.UUID;

import javax.annotation.Nonnull;

import at.peirleitner.core.Core;
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
	private int messageID;
	private Collection<UserChatMessageFlag> flags;
	private UUID staff;
	private long reviewed;
	private ChatLogReviewResult result;

	public ChatLog() {

	}

	public ChatLog(int id, int messageID, Collection<UserChatMessageFlag> flags, UUID staff, long reviewed,
			ChatLogReviewResult result) {
		this.id = id;
		this.messageID = messageID;
		this.flags = flags;
		this.staff = staff;
		this.reviewed = reviewed;
		this.result = result;
	}

	public final int getID() {
		return id;
	}

	public final void setID(int id) {
		this.id = id;
	}

	public final int getMessageID() {
		return messageID;
	}

	public final void setMessageID(int messageID) {
		this.messageID = messageID;
	}

	public final UserChatMessage getChatMessage() {
		return Core.getInstance().getModerationSystem().getChatMessage(this.getMessageID());
	}

	public final Collection<UserChatMessageFlag> getFlags() {
		return flags;
	}

	public final void setFlags(Collection<UserChatMessageFlag> flags) {
		this.flags = flags;
	}

	public final UUID getStaff() {
		return staff;
	}

	public final void setStaff(UUID staff) {
		this.staff = staff;
	}

	public final long getReviewed() {
		return reviewed;
	}

	public final void setReviewed(long reviewed) {
		this.reviewed = reviewed;
	}

	public final ChatLogReviewResult getResult() {
		return result;
	}

	public final void setResult(ChatLogReviewResult result) {
		this.result = result;
	}

	public final boolean review(@Nonnull User user, @Nonnull ChatLogReviewResult result) {
		return false;
	}

	public final boolean isReviewed() {
		return this.getResult() == null ? false : true;
	}

	@Override
	public String toString() {
		return "ChatLog [id=" + id + ", messageID=" + messageID + ", flags=" + flags + ", staff=" + staff
				+ ", reviewed=" + reviewed + ", result=" + result + "]";
	}

}
