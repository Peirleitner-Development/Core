package at.peirleitner.core.command.local;

import java.util.Arrays;

import javax.annotation.Nonnull;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import at.peirleitner.core.Core;
import at.peirleitner.core.SpigotMain;
import at.peirleitner.core.util.GlobalUtils;
import at.peirleitner.core.util.MOTD;
import at.peirleitner.core.util.user.CorePermission;
import at.peirleitner.core.util.user.Language;
import at.peirleitner.core.util.user.PredefinedMessage;
import at.peirleitner.core.util.user.User;
import net.md_5.bungee.api.ChatColor;

/**
 * Change the current
 * <strong>M</strong>essage<strong>O</strong>f<strong>T</strong>he<strong>D</strong>ay
 * (Text on the multiplayer server list)
 * 
 * @since 1.0.4
 * @author Markus Peirleitner (Rengobli)
 *
 */
public class CommandMotd implements CommandExecutor {

	public CommandMotd() {
		SpigotMain.getInstance().getCommand("motd").setExecutor(this);
	}

	@Override
	public boolean onCommand(CommandSender cs, Command cmd, String arg, String[] args) {

		if (!cs.hasPermission(CorePermission.COMMAND_MOTD.getPermission())) {
			cs.sendMessage(Core.getInstance().getLanguageManager().getMessage(PredefinedMessage.NO_PERMISSION));
			return true;
		}

		if (args.length == 0) {

			MOTD motd = Core.getInstance().getMotdSystem().getMOTD();

			// Should not be able to trigger, catch anyways if manipulated by the server
			// administrator.
			if (motd == null) {
				Core.getInstance().getLanguageManager().sendMessage(cs, Core.getInstance().getPluginName(),
						"command.motd.info.no-motd-set", null, true);
				return true;
			}

			Core.getInstance().getLanguageManager().sendMessage(cs, Core.getInstance().getPluginName(),
					"command.motd.info.success",
					Arrays.asList(motd.getFirstLine(), motd.getSecondLine(), motd.getStaffName(), GlobalUtils.getFormatedDate(motd.getChanged())),
					true);

			return true;

		} else if (args.length == 1 && args[0].equalsIgnoreCase("update")) {
			
			if(!Core.getInstance().getMotdSystem().isCachingEnabled()) {
				Core.getInstance().getLanguageManager().sendMessage(cs, Core.getInstance().getPluginName(), "command.motd.update.error.caching-disabled", null, true);
				return true;
			}
			
			MOTD motd = Core.getInstance().getMotdSystem().getMOTD();

			Core.getInstance().getLanguageManager().sendMessage(cs, Core.getInstance().getPluginName(), "command.motd.update." + (motd == null ? "error.cant-get-motd" : "success"), null, true);
			return true;
			
		} else if (args.length > 1 && args[0].equalsIgnoreCase("set")) {

			StringBuilder sb = new StringBuilder();

			for (int i = 1; i < args.length; i++) {
				sb.append(args[i] + " ");
			}

			Player p = null;
			User user = null;
			if (cs instanceof Player) {
				p = (Player) cs;
				user = Core.getInstance().getUserSystem().getUser(p.getUniqueId());
			}

			boolean updated = Core.getInstance().getMotdSystem().update(p == null ? null : p.getUniqueId(),
					sb.toString());

			if (!updated) {
				Core.getInstance().getLanguageManager().sendMessage(cs, Core.getInstance().getPluginName(),
						"command.motd.set.error.sql", null, true);
				return true;
			}

			MOTD motd = Core.getInstance().getMotdSystem().getMOTD();

			String line1 = ChatColor.translateAlternateColorCodes('&', motd.getFirstLine());
			String line2 = ChatColor.translateAlternateColorCodes('&', motd.getSecondLine());

			Core.getInstance().getLanguageManager().sendMessage(cs, Core.getInstance().getPluginName(),
					"command.motd.set.success", Arrays.asList(line1, line2), true);

			Core.getInstance().getLanguageManager().notifyStaff(Core.getInstance().getPluginName(),
					"notify.motd.update", Arrays.asList(user == null ? "CONSOLE" : user.getDisplayName(), line1, line2),
					false);

		} else {
			this.sendHelp(cs);
		}

		return true;
	}

	private final void sendHelp(@Nonnull CommandSender cs) {
		cs.sendMessage(Core.getInstance().getLanguageManager().getMessage(Core.getInstance().getPluginName(),
				Language.ENGLISH, "command.motd.syntax", null));
	}

}
