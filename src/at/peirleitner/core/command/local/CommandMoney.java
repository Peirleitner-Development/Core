package at.peirleitner.core.command.local;

import java.util.Arrays;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import at.peirleitner.core.Core;
import at.peirleitner.core.SpigotMain;
import at.peirleitner.core.util.user.PredefinedMessage;
import at.peirleitner.core.util.user.User;

public class CommandMoney implements CommandExecutor {

	public CommandMoney() {
		SpigotMain.getInstance().getCommand("money").setExecutor(this);
	}

	@Override
	public boolean onCommand(CommandSender cs, Command cmd, String arg, String[] args) {

		if (!(cs instanceof Player)) {
			cs.sendMessage(
					Core.getInstance().getLanguageManager().getMessage(PredefinedMessage.ACTION_REQUIRES_PLAYER));
			return false;
		}

		final Player p = (Player) cs;
		final User user = Core.getInstance().getUserSystem().getUser(p.getUniqueId());
		final double economy = Core.getInstance().getEconomySystem().getEconomy(p.getUniqueId(),
				Core.getInstance().getSettingsManager().getSaveType());

		user.sendMessage(Core.getInstance().getPluginName(), "command.money.own-balance",
				Arrays.asList("" + economy, "" + Core.getInstance().getEconomySystem().getChar()), true);

		return true;
	}

}
