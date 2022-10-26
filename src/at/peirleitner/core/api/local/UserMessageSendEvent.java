package at.peirleitner.core.api.local;

import java.util.List;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import at.peirleitner.core.manager.LanguageManager;
import at.peirleitner.core.util.RunMode;
import at.peirleitner.core.util.user.PredefinedMessage;
import at.peirleitner.core.util.user.User;

/**
 * Triggered when a message is sent towards a {@link User} using the
 * {@link LanguageManager} while the {@link RunMode} is set to
 * {@link RunMode#LOCAL}.
 * 
 * @since 1.0.7
 * @author Markus Peirleitner (Rengobli)
 *
 */
public class UserMessageSendEvent extends Event implements Cancellable {

	private static final HandlerList HANDLERS = new HandlerList();
	private final String pluginName;
	private final String key;
	private final List<String> replacements;
	private final boolean prefix;
	private boolean cancelled;

	public UserMessageSendEvent(String pluginName, String key, List<String> replacements, boolean prefix) {
		this.pluginName = pluginName;
		this.key = key;
		this.replacements = replacements;
		this.prefix = prefix;
		this.cancelled = false;
	}

	public final String getPluginName() {
		return pluginName;
	}

	public final String getKey() {
		return key;
	}

	public final List<String> getReplacements() {
		return replacements;
	}

	public final boolean isPrefix() {
		return prefix;
	}

	public final boolean isPredefinedMessage() {

		for (PredefinedMessage pdm : PredefinedMessage.values()) {
			if (pdm.getPath().equals(this.getKey())) {
				return true;
			}
		}

		return false;
	}

	public final PredefinedMessage getPredefinedMessage() {

		PredefinedMessage msg = null;

		for (PredefinedMessage pdm : PredefinedMessage.values()) {
			if (pdm.getPath().equals(this.getKey())) {
				msg = pdm;
			}
		}

		return msg;
	}

	@Override
	public final boolean isCancelled() {
		return cancelled;
	}

	@Override
	public final void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS;
	}

	@Override
	public HandlerList getHandlers() {
		return HANDLERS;
	}

}
