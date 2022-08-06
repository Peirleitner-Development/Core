package at.peirleitner.core.listener.local;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import at.peirleitner.core.Core;
import at.peirleitner.core.SpigotMain;
import at.peirleitner.core.util.LogType;
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

			// Check for network maintenance
			if (this.isMaintenance()) {

				if (!p.hasPermission(CorePermission.MAIN_LOGIN_BYPASS_MAINTENANCE.getPermission())) {
					p.kickPlayer("Network Maintenance"); // TODO: Replace message
					Core.getInstance().log(this.getClass(), LogType.DEBUG,
							"Disallowed Login for User '" + p.getUniqueId().toString() + "': Maintenance active.");
				} else {
					Core.getInstance().log(this.getClass(), LogType.DEBUG,
							"Allowed Login for User '" + p.getUniqueId().toString()
									+ "' whilst maintenance is active due to permission node '"
									+ CorePermission.MAIN_LOGIN_BYPASS_MAINTENANCE.getPermission() + "'.");
				}

				return;
			}

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
		
		if(!Core.getInstance().isNetwork()) {
			Core.getInstance().getUserSystem().setLastLogin(user, System.currentTimeMillis());
		}

		Core.getInstance().log(this.getClass(), LogType.DEBUG,
				"Connection for User '" + user.getUUID().toString() + "' has been allowed.");

	}

	private final boolean isMaintenance() {
		return Boolean.valueOf(Core.getInstance().getSettingsManager().getSetting("manager.settings.maintenance"));
	}

}
