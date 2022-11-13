package at.peirleitner.core.util.moderation;

/**
 * System logged {@link UserChatMessage}. A ChatLog will automatically be
 * created on Reports or Chat-Content-Filter triggers
 * 
 * @since 1.0.14
 * @author Markus Peirleitner (Rengobli)
 * @see UserChatMessageFlag
 */
public final class ChatLog {

	private final int id;
	private final UserChatMessage message;

	public ChatLog(int id, UserChatMessage message) {
		this.id = id;
		this.message = message;
	}

	public final int getID() {
		return id;
	}

	public final UserChatMessage getMessage() {
		return message;
	}

}
