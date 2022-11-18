package at.peirleitner.core.api.local;

import javax.annotation.Nonnull;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import at.peirleitner.core.util.moderation.ChatLog;
import at.peirleitner.core.util.user.User;

/**
 * <strong>Note: Currently unused</strong><br>
 * Triggered when a {@link User}s chat message has been flagged and a restriction is forced
 * 
 * @since 1.0.14
 * @author Markus Peirleitner (Rengobli)
 *
 */
public class UserChatMessageFlagEvent extends Event {

	private static final HandlerList HANDLERS = new HandlerList();
	private ChatLog chatLog;

	public UserChatMessageFlagEvent(@Nonnull ChatLog chatLog) {
		this.chatLog = chatLog;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS;
	}

	@Override
	public HandlerList getHandlers() {
		return HANDLERS;
	}

	public final ChatLog getChatLog() {
		return chatLog;
	}

	@Override
	public String toString() {
		return "UserChatMessageFlagEvent[chatLog=" + chatLog + "]";
	}

}
