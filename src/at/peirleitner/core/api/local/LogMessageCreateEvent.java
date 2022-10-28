package at.peirleitner.core.api.local;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import at.peirleitner.core.Core;
import at.peirleitner.core.util.LogType;

/**
 * Triggered when creating a new Log message using
 * {@link Core#log(Class, LogType, String)}
 * 
 * @since 1.0.8
 * @author Markus Peirleitner (Rengobli)
 *
 */
public class LogMessageCreateEvent extends Event {

	private static final HandlerList HANDLERS = new HandlerList();
	private final String pluginName;
	private final Class<?> c;
	private final LogType logType;
	private final String message;

	public LogMessageCreateEvent(String pluginName, Class<?> c, LogType logType, String message) {
		this.pluginName = pluginName;
		this.c = c;
		this.logType = logType;
		this.message = message;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS;
	}

	public final HandlerList getHandlers() {
		return HANDLERS;
	}

	public final String getPluginName() {
		return pluginName;
	}

	public final Class<?> getRepresentedClass() {
		return c;
	}

	public final LogType getLogType() {
		return logType;
	}

	public final String getMessage() {
		return message;
	}

}
