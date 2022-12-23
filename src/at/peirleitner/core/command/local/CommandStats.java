package at.peirleitner.core.command.local;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import at.peirleitner.core.Core;
import at.peirleitner.core.SpigotMain;
import at.peirleitner.core.util.user.PredefinedMessage;
import at.peirleitner.core.util.user.User;

/**
 * Display player statistics inside a GUI
 * 
 * @since 1.0.17
 * @author Markus Peirleitner (Rengobli)
 *
 */
public class CommandStats implements CommandExecutor {

	public CommandStats() {
		SpigotMain.getInstance().getCommand("stats").setExecutor(this);
	}

	@Override
	public boolean onCommand(CommandSender cs, Command cmd, String arg, String[] args) {

		if (!(cs instanceof Player)) {
			cs.sendMessage(
					Core.getInstance().getLanguageManager().getMessage(PredefinedMessage.ACTION_REQUIRES_PLAYER));
			return true;
		}

		Player p = (Player) cs;
		User user = Core.getInstance().getUserSystem().getUser(p.getUniqueId());

		SpigotMain.getInstance().getLocalGuiManager().getStatisticMainGUI(user).open(p);
		return true;

	}

}
