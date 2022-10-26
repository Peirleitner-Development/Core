package at.peirleitner.core.api.local;

import javax.annotation.Nonnull;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import at.peirleitner.core.Core;
import at.peirleitner.core.manager.LanguageManager;
import at.peirleitner.core.util.user.PredefinedMessage;

/**
 * Triggered when gaining a {@link PredefinedMessage} using
 * {@link LanguageManager#getMessage(PredefinedMessage)}. You may manipulate the
 * result using {@link #setMessage(String)}.
 * 
 * @since 1.0.7
 * @author Markus Peirleitner (Rengobli)
 */
public class UserMessageGetEvent extends Event {

	private static final HandlerList HANDLERS = new HandlerList();
	private final PredefinedMessage predefinedMessage;
	private String message;

	public UserMessageGetEvent(PredefinedMessage predefinedMessage) {
		this.predefinedMessage = predefinedMessage;
		this.message = Core.getInstance().getLanguageManager().getMessage(Core.getInstance().getPluginName(),
				Core.getInstance().getDefaultLanguage(), predefinedMessage.getPath(), null);
	}

	public final PredefinedMessage getPredefinedMessage() {
		return this.predefinedMessage;
	}

	public final void setMessage(@Nonnull String message) {
		this.message = message;
	}

	public final String getMessage() {
		return this.message;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS;
	}

	@Override
	public HandlerList getHandlers() {
		return HANDLERS;
	}

}
