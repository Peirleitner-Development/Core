package at.peirleitner.core.command.local;

import java.util.Arrays;

import javax.annotation.Nonnull;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import at.peirleitner.core.Core;
import at.peirleitner.core.SpigotMain;
import at.peirleitner.core.util.user.CorePermission;
import at.peirleitner.core.util.user.Language;
import at.peirleitner.core.util.user.PredefinedMessage;
import at.peirleitner.core.util.user.User;

public class CommandTeleport implements CommandExecutor {

	public CommandTeleport() {
		SpigotMain.getInstance().getCommand("teleport").setExecutor(this);
	}

	@Override
	public boolean onCommand(CommandSender cs, Command cmd, String arg, String[] args) {

		if (args.length == 1) {

			if (!(cs instanceof Player)) {
				cs.sendMessage(
						Core.getInstance().getLanguageManager().getMessage(PredefinedMessage.ACTION_REQUIRES_PLAYER));
				return true;
			}

			Player p = (Player) cs;

			if (!cs.hasPermission(CorePermission.COMMAND_TELEPORT_SELF.getPermission())) {
				cs.sendMessage(Core.getInstance().getLanguageManager().getMessage(PredefinedMessage.NO_PERMISSION));
				return true;
			}

			Player target = Bukkit.getPlayer(args[0]);

			if (target == null) {
				cs.sendMessage(
						Core.getInstance().getLanguageManager().getMessage(PredefinedMessage.TARGET_PLAYER_NOT_FOUND));
				return true;
			}
			
			if(p == target) {
				cs.sendMessage(
						Core.getInstance().getLanguageManager().getMessage(PredefinedMessage.CANT_TARGET_YOURSELF));
				return true;
			}

			p.teleport(target.getLocation());

			User targetUser = Core.getInstance().getUserSystem().getUser(target.getUniqueId());

			Core.getInstance().getLanguageManager().sendMessage(cs, Core.getInstance().getPluginName(),
					"command.teleport.self.success", Arrays.asList(targetUser.getDisplayName()), true);
			return true;

		} else if (args.length == 2) {

			if (!cs.hasPermission(CorePermission.COMMAND_TELEPORT_OTHER.getPermission())) {
				cs.sendMessage(Core.getInstance().getLanguageManager().getMessage(PredefinedMessage.NO_PERMISSION));
				return true;
			}

			Player target1 = Bukkit.getPlayer(args[0]);
			Player target2 = Bukkit.getPlayer(args[1]);

			if (target1 == null || target2 == null) {
				cs.sendMessage(
						Core.getInstance().getLanguageManager().getMessage(PredefinedMessage.TARGET_PLAYER_NOT_FOUND));
				return true;
			}

			User sender = null;

			if (cs instanceof Player) {
				Player p = (Player) cs;
				sender = Core.getInstance().getUserSystem().getUser(p.getUniqueId());
			}

			User t1 = Core.getInstance().getUserSystem().getUser(target1.getUniqueId());
			User t2 = Core.getInstance().getUserSystem().getUser(target2.getUniqueId());
			
			if(sender != null && sender == t1 || sender != null && sender == t2) {
				cs.sendMessage(
						Core.getInstance().getLanguageManager().getMessage(PredefinedMessage.CANT_TARGET_YOURSELF));
				return true;
			}
			
			if(target1 == target2) {
				Core.getInstance().getLanguageManager().sendMessage(cs, Core.getInstance().getPluginName(), "command.teleport.error.target1-cant-be-target2", null, true);
				return true;
			}

			target1.teleport(target2.getLocation());

			Core.getInstance().getLanguageManager().sendMessage(cs, Core.getInstance().getPluginName(),
					"command.teleport.other.sender.success", Arrays.asList(t1.getDisplayName(), t2.getDisplayName()),
					true);
			t1.sendMessage(Core.getInstance().getPluginName(), "command.teleport.other.target1.success",
					Arrays.asList(sender == null ? "CONSOLE" : sender.getDisplayName(), t2.getDisplayName()), false);
			t2.sendMessage(Core.getInstance().getPluginName(), "command.teleport.other.target2.success",
					Arrays.asList(sender == null ? "CONSOLE" : sender.getDisplayName(), t1.getDisplayName()), false);

			return true;

		} else {
			this.sendHelp(cs);
			return true;
		}
	}

	private final void sendHelp(@Nonnull CommandSender cs) {
		cs.sendMessage(Core.getInstance().getLanguageManager().getMessage(Core.getInstance().getPluginName(),
				Language.ENGLISH, "command.teleport.syntax", null));
	}

}
