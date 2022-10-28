package at.peirleitner.core.listener.local;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import at.peirleitner.core.Core;
import at.peirleitner.core.SpigotMain;
import at.peirleitner.core.api.local.LogMessageCreateEvent;
import at.peirleitner.core.command.local.CommandLog;
import at.peirleitner.core.util.LogType;

/**
 * @since 1.0.8
 * @author Markus Peirleitner (Rengobli)
 *
 */
public class LogMessageCreateListener implements Listener {

	public LogMessageCreateListener() {
		SpigotMain.getInstance().getServer().getPluginManager().registerEvents(this, SpigotMain.getInstance());
	}

	@EventHandler
	public void onLogMessageCreate(LogMessageCreateEvent e) {

		String pluginName = e.getPluginName();
		Class<?> c = e.getRepresentedClass();
		LogType logType = e.getLogType();
		String message = e.getMessage();

		if (logType == LogType.DEBUG && !Core.getInstance().getSettingsManager()
				.isSetting(Core.getInstance().getPluginName(), "manager.settings.send-debug-logs-in-chat")) {
			return;
		}

		final String logMessage = "[" + pluginName + "/"
				+ (c == null ? "?" : Core.getInstance().logWithSimpleClassNames() ? c.getSimpleName() : c.getName())
				+ "/" + logType.toString() + "] " + message;

		for (Player all : Bukkit.getOnlinePlayers()) {

			if (CommandLog.LOG_LIST.contains(all.getUniqueId())) {
				all.sendMessage(Core.getInstance().getLanguageManager().getNotifyPrefix() + logMessage);
			}

		}

	}

}
