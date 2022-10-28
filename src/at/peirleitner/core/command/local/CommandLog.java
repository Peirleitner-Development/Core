package at.peirleitner.core.command.local;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import at.peirleitner.core.Core;
import at.peirleitner.core.SpigotMain;
import at.peirleitner.core.api.local.LogMessageCreateEvent;
import at.peirleitner.core.util.LogType;
import at.peirleitner.core.util.user.CorePermission;
import at.peirleitner.core.util.user.PredefinedMessage;
import at.peirleitner.core.util.user.User;

/**
 * Toggle display of log messages
 * 
 * @since 1.0.8
 * @author Markus Peirleitner (Rengobli)
 * @see LogType
 * @see Core#log(Class, LogType, String)
 * @see Core#log(String, Class, LogType, String)
 * @see Core#logWithSimpleClassNames()
 * @see LogMessageCreateEvent
 */
public class CommandLog implements CommandExecutor {

	public CommandLog() {
		SpigotMain.getInstance().getCommand("log").setExecutor(this);
	}

	public static Collection<UUID> LOG_LIST = new ArrayList<>();

	@Override
	public boolean onCommand(CommandSender cs, Command cmd, String arg, String[] args) {

		if (!(cs instanceof Player)) {
			cs.sendMessage(
					Core.getInstance().getLanguageManager().getMessage(PredefinedMessage.ACTION_REQUIRES_PLAYER));
			return true;
		}

		if (!cs.hasPermission(CorePermission.COMMAND_LOG.getPermission())) {
			cs.sendMessage(Core.getInstance().getLanguageManager().getMessage(PredefinedMessage.NO_PERMISSION));
			return true;
		}

		Player p = (Player) cs;
		User user = Core.getInstance().getUserSystem().getUser(p.getUniqueId());

		if (!LOG_LIST.contains(p.getUniqueId())) {
			LOG_LIST.add(p.getUniqueId());
			user.sendMessage(Core.getInstance().getPluginName(), "command.log.success.on", null, true);
			return true;
		} else {
			LOG_LIST.remove(p.getUniqueId());
			user.sendMessage(Core.getInstance().getPluginName(), "command.log.success.off", null, true);
			return true;
		}

	}

}
