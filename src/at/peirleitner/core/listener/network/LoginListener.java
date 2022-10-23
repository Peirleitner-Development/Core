package at.peirleitner.core.listener.network;

import java.util.UUID;

import at.peirleitner.core.Core;
import at.peirleitner.core.util.LogType;
import at.peirleitner.core.util.user.CorePermission;
import at.peirleitner.core.util.user.User;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class LoginListener implements Listener {

	private final String CONNECTION_DISALLOWED_UUID_CANT_BE_VALIDATED = "Could not get UUID for your login request: Connection disallowed (Network).";
	private final String CONNECTION_DISALLOWED_USER_CANT_BE_VALIDATED = "Could not get User Object for your UUID: Connection disallowed (Network).";

	@EventHandler
	public void onLogin(LoginEvent e) {

		// Tasks will only be performed if network-mode is enabled
		if (!Core.getInstance().isNetwork())
			return;

		UUID uuid = e.getConnection().getUniqueId();

		// Cancel connection if the UUID can't be validated
		if (uuid == null) {
			e.setCancelled(true);
			e.setCancelReason(new TextComponent(ChatColor.RED + CONNECTION_DISALLOWED_UUID_CANT_BE_VALIDATED));
			Core.getInstance().log(this.getClass(), LogType.WARNING,
					"Could not perform checks on login due to UUID being null: Connection disallowed.");
			return;
		}

		// Create Player Object if none exists
		Core.getInstance().getUserSystem().register(uuid, e.getConnection().getName());

		// Define User
		User user = Core.getInstance().getUserSystem().getUser(uuid);

		// Cancel connection if the User can't be validated
		if (user == null) {
			e.setCancelled(true);
			e.setCancelReason(new TextComponent(ChatColor.RED + CONNECTION_DISALLOWED_USER_CANT_BE_VALIDATED));
			Core.getInstance().log(this.getClass(), LogType.WARNING,
					"Could not get User Object for UUID '" + uuid.toString() + "': Connection disallowed.");
			return;
		}

		// Check for User Account State
		if (!user.isEnabled()) {
			e.setCancelled(true);
			e.setCancelReason(new TextComponent("Account disabled")); // TODO:
			Core.getInstance().log(this.getClass(), LogType.DEBUG, "Disallowed connection for User '"
					+ user.getUUID().toString() + "/" + user.getLastKnownName() + "': Account is disabled.");
			return;
		}

		//TODO: Check for network maintenance

		if (!e.isCancelled()) {
			Core.getInstance().getUserSystem().setLastLogin(user, System.currentTimeMillis());

			if (Core.getInstance().getUserSystem().isCachingEnabled()) {
				Core.getInstance().getUserSystem().getCachedUsers().add(user);
			}

		}

		Core.getInstance().log(this.getClass(), LogType.DEBUG,
				"Connection for User '" + user.getUUID().toString() + "' has been allowed.");

	}

	private final boolean isMaintenance() {
		return Boolean.valueOf(Core.getInstance().getSettingsManager().getSetting(Core.getInstance().getPluginName(), "manager.settings.maintenance"));
	}

}
