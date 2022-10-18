package at.peirleitner.core.command.local;

import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

import javax.annotation.Nonnull;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import at.peirleitner.core.Core;
import at.peirleitner.core.SpigotMain;
import at.peirleitner.core.system.MaintenanceSystem;
import at.peirleitner.core.util.user.CorePermission;
import at.peirleitner.core.util.user.Language;
import at.peirleitner.core.util.user.PredefinedMessage;
import at.peirleitner.core.util.user.User;
import net.md_5.bungee.api.ChatColor;

public class CommandMaintenance implements CommandExecutor {

	public CommandMaintenance() {
		SpigotMain.getInstance().getCommand("maintenance").setExecutor(this);
	}

	@Override
	public boolean onCommand(CommandSender cs, Command cmd, String arg, String[] args) {

		if (!cs.hasPermission(CorePermission.COMMAND_MAINTENANCE.getPermission())) {
			cs.sendMessage(Core.getInstance().getLanguageManager().getMessage(PredefinedMessage.NO_PERMISSION));
			return true;
		}

		if (args.length == 1) {

			if (args[0].equalsIgnoreCase("list")) {

				Collection<UUID> list = this.getMaintenanceSystem().getWhitelisted();

				if (list.isEmpty()) {
					Core.getInstance().getLanguageManager().sendMessage(cs, Core.getInstance().getPluginName(),
							"command.maintenance.list.success.empty", null, true);
				} else {

					Core.getInstance().getLanguageManager().sendMessage(cs, Core.getInstance().getPluginName(),
							"command.maintenance.list.success.listing-in-chat", null, true);
					for (UUID uuid : list) {

						User user = Core.getInstance().getUserSystem().getUser(uuid);
						cs.sendMessage(ChatColor.DARK_GRAY + "- " + ChatColor.GRAY
								+ (user == null ? uuid.toString() : user.getDisplayName()));

					}

				}

			} else if (args[0].equalsIgnoreCase("on")) {

				boolean updated = this.getMaintenanceSystem().setMaintenance(true);

				// Kick all players that are not bypassing maintenance mode
				if (updated) {

					for (Player all : Bukkit.getOnlinePlayers()) {

						if (!all.hasPermission(CorePermission.BYPASS_MAINTENANCE.getPermission())) {

							User user = Core.getInstance().getUserSystem().getUser(all.getUniqueId());
							all.kickPlayer(Core.getInstance().getLanguageManager().getMessage(
									Core.getInstance().getPluginName(), user.getLanguage(), "maintenance.kick",
									Arrays.asList(Core.getInstance().getSettingsManager().getServerName(),
											Core.getInstance().getSettingsManager().getServerWebsite())));

						}

					}

				}

				Core.getInstance().getLanguageManager().sendMessage(cs, Core.getInstance().getPluginName(),
						"command.maintenance.on." + (updated ? "success" : "error"), null, true);

				if (updated) {
					Core.getInstance().getLanguageManager().notifyStaff(Core.getInstance().getPluginName(),
							"maintenance.broadcast.on", Arrays.asList(cs.getName()), true);
				}

				return true;

			} else if (args[0].equalsIgnoreCase("off")) {

				boolean updated = this.getMaintenanceSystem().setMaintenance(false);

				Core.getInstance().getLanguageManager().sendMessage(cs, Core.getInstance().getPluginName(),
						"command.maintenance.off." + (updated ? "success" : "error"), null, true);

				if (updated) {
					Core.getInstance().getLanguageManager().notifyStaff(Core.getInstance().getPluginName(),
							"maintenance.broadcast.off", Arrays.asList(cs.getName()), true);
				}

				return true;

			} else if (args[0].equalsIgnoreCase("toggle")) {

				boolean updated = this.getMaintenanceSystem().toggleMaintenance();
				boolean value = this.getMaintenanceSystem().isMaintenance();

				if (updated) {

					if (value) {

						// Kick all players that are not bypassing maintenance mode
						for (Player all : Bukkit.getOnlinePlayers()) {

							if (!all.hasPermission(CorePermission.BYPASS_MAINTENANCE.getPermission())) {

								User user = Core.getInstance().getUserSystem().getUser(all.getUniqueId());
								all.kickPlayer(Core.getInstance().getLanguageManager().getMessage(
										Core.getInstance().getPluginName(), user.getLanguage(), "maintenance.kick",
										Arrays.asList(Core.getInstance().getSettingsManager().getServerName(),
												Core.getInstance().getSettingsManager().getServerWebsite())));

							}

						}

						Core.getInstance().getLanguageManager().sendMessage(cs, Core.getInstance().getPluginName(),
								"command.maintenance.on." + (updated ? "success" : "error"), null, true);

						Core.getInstance().getLanguageManager().notifyStaff(Core.getInstance().getPluginName(),
								"maintenance.broadcast.on", Arrays.asList(cs.getName()), true);
						return true;

					} else {

						Core.getInstance().getLanguageManager().sendMessage(cs, Core.getInstance().getPluginName(),
								"command.maintenance.off." + (updated ? "success" : "error"), null, true);

						Core.getInstance().getLanguageManager().notifyStaff(Core.getInstance().getPluginName(),
								"maintenance.broadcast.off", Arrays.asList(cs.getName()), true);

					}

				}

				return true;

			} else {
				this.sendHelp(cs);
				return true;
			}

		} else if (args.length == 2) {

			String playerName = args[1];
			User user = Core.getInstance().getUserSystem().getByLastKnownName(playerName);

			if (user == null) {
				Core.getInstance().getLanguageManager().sendMessage(cs, Core.getInstance().getPluginName(),
						"command.maintenance.whitelist.error.player-not-registered", Arrays.asList(playerName), true);
				return true;
			}

			if (args[0].equalsIgnoreCase("add")) {

				boolean updated = this.getMaintenanceSystem().addToWhitelist(user.getUUID());
				Core.getInstance().getLanguageManager().sendMessage(cs, Core.getInstance().getPluginName(),
						"command.maintenance.add." + (updated ? "success" : "error"),
						Arrays.asList(user.getDisplayName()), true);
				return true;

			} else if (args[0].equalsIgnoreCase("remove")) {

				boolean updated = this.getMaintenanceSystem().removeFromWhitelist(user.getUUID());
				Core.getInstance().getLanguageManager().sendMessage(cs, Core.getInstance().getPluginName(),
						"command.maintenance.remove." + (updated ? "success" : "error"),
						Arrays.asList(user.getDisplayName()), true);
				return true;

			} else {
				this.sendHelp(cs);
				return true;
			}

		} else {
			this.sendHelp(cs);
			return true;
		}

		return true;

	}

	private final void sendHelp(@Nonnull CommandSender cs) {
		cs.sendMessage(Core.getInstance().getLanguageManager().getMessage(Core.getInstance().getPluginName(),
				Language.ENGLISH, "command.maintenance.syntax", null));
	}

	private final MaintenanceSystem getMaintenanceSystem() {
		return Core.getInstance().getMaintenanceSystem();
	}

}
