package at.peirleitner.core.listener.local;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import at.peirleitner.core.Core;
import at.peirleitner.core.SpigotMain;
import at.peirleitner.core.command.local.CommandLog;
import at.peirleitner.core.util.LogType;
import at.peirleitner.core.util.local.OperatorJoinAction;
import at.peirleitner.core.util.user.CorePermission;
import at.peirleitner.core.util.user.User;
import net.md_5.bungee.api.ChatColor;

public class PlayerJoinListener implements Listener {

	public PlayerJoinListener() {
		SpigotMain.getInstance().getServer().getPluginManager().registerEvents(this, SpigotMain.getInstance());
	}

	private final String CONNECTION_DISALLOWED_USER_CANT_BE_VALIDATED = "Could not get User Object for your UUID: Connection disallowed (Local).";

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {

		Player p = e.getPlayer();

		// Register User and check for Maintenance if Network-Mode is disabled
		if (!Core.getInstance().isNetwork()) {

			// Create Player Object if none exists
			Core.getInstance().getUserSystem().register(p.getUniqueId(), p.getName());

		}

		// Define User
		User user = Core.getInstance().getUserSystem().getUser(p.getUniqueId());

		// Cancel connection if the User can't be validated
		if (user == null) {
			p.kickPlayer(ChatColor.RED + CONNECTION_DISALLOWED_USER_CANT_BE_VALIDATED);
			Core.getInstance().log(this.getClass(), LogType.WARNING,
					"Could not get User Object for UUID '" + p.getUniqueId().toString() + "': Connection disallowed.");
			return;
		}

		// Operator check, v1.0.5
		if (p.isOp()) {

			try {

				OperatorJoinAction action = OperatorJoinAction.valueOf(Core.getInstance().getSettingsManager()
						.getSetting(Core.getInstance().getPluginName(), "manager.settings.operator-join-action"));

				switch (action) {
				case ALLOW:
					break;
				case DISALLOW:
					p.kickPlayer(Core.getInstance().getLanguageManager().getMessage(Core.getInstance().getPluginName(),
							user.getLanguage(), "listener.player-join.operator-join-action.disallow", null));
					break;
				case REMOVE_STATUS:
					p.setOp(false);
					user.sendMessage(Core.getInstance().getPluginName(),
							"listener.player-join.operator-join-action.remove-status", null, true);
					break;
				default:
					p.kickPlayer(
							ChatColor.RED + "No OP-Join action could be selected, disallowing for security reasons.");
				}

			} catch (NullPointerException | IllegalArgumentException ex) {
				Core.getInstance().log(this.getClass(), LogType.CRITICAL,
						"Could not check for OperatorJoinAction - Player " + p.getName() + " is an Operator!");
			}

		}

		// Full Server Join, v1.0.6
		if (Bukkit.getOnlinePlayers().size() >= Core.getInstance().getSettingsManager().getSlots()) {

			if (!p.hasPermission(CorePermission.BYPASS_FULL_SERVER_JOIN.getPermission())) {
				p.kickPlayer(Core.getInstance().getLanguageManager().getMessage(Core.getInstance().getPluginName(),
						user.getLanguage(), "listener.player-join.server-full-not-bypassing",
						Arrays.asList(Core.getInstance().getSettingsManager().getServerName(),
								Core.getInstance().getSettingsManager().getServerStore())));
				return;
			}

		}

		// Log, v1.0.8
		if (Core.getInstance().getSettingsManager().isSetting(Core.getInstance().getPluginName(),
				"manager.settings.enable-log-on-join") && p.hasPermission(CorePermission.COMMAND_LOG.getPermission())
				&& !CommandLog.LOG_LIST.contains(p.getUniqueId())) {
			p.performCommand("log");
		}

		if (!Core.getInstance().isNetwork()) {
			Core.getInstance().getUserSystem().setLastLogin(user, System.currentTimeMillis());

			if (Core.getInstance().getUserSystem().isCachingEnabled()) {
				Core.getInstance().getUserSystem().getCachedUsers().add(user);
			}

		}

		Core.getInstance().log(this.getClass(), LogType.DEBUG,
				"Connection for User '" + user.getUUID().toString() + "' has been allowed.");

		new BukkitRunnable() {

			@Override
			public void run() {
				SpigotMain.getInstance().getLocalScoreboard().refreshDefaultTeams();
			}
		}.runTaskLater(SpigotMain.getInstance(), 20L);

	}

}
