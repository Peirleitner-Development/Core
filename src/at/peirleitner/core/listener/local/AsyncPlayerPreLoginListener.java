package at.peirleitner.core.listener.local;

import java.util.Arrays;
import java.util.UUID;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;

import at.peirleitner.core.Core;
import at.peirleitner.core.SpigotMain;
import at.peirleitner.core.util.LogType;
import at.peirleitner.core.util.user.User;

public class AsyncPlayerPreLoginListener implements Listener {

	public AsyncPlayerPreLoginListener() {
		SpigotMain.getInstance().getServer().getPluginManager().registerEvents(this, SpigotMain.getInstance());
	}

	@EventHandler
	public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent e) {

		// Tasks will only be performed if network-mode is disabled
		if (Core.getInstance().isNetwork())
			return;

		UUID uuid = e.getUniqueId();
		User user = Core.getInstance().getUserSystem().getUser(uuid);

		// Maintenance
		if (Core.getInstance().getMaintenanceSystem().isMaintenance()
				&& !Core.getInstance().getMaintenanceSystem().isWhitelisted(uuid)) {
			e.setLoginResult(Result.KICK_OTHER);

			String message = Core.getInstance().getLanguageManager().getMessage(Core.getInstance().getPluginName(),
					user == null ? Core.getInstance().getDefaultLanguage() : user.getLanguage(),
					"listener.async-player-pre-login.maintenance",
					Arrays.asList(Core.getInstance().getSettingsManager().getServerName(),
							Core.getInstance().getSettingsManager().getServerWebsite()));

			e.setKickMessage(message);
			Core.getInstance().log(this.getClass(), LogType.DEBUG,
					"Disallowed connection for UUID '" + uuid.toString() + "': Not whitelisted on maintenance list.");
			return;
		}

		// Don't check for disabled accounts if the account doesn't even exist.
		if (user == null) {
			Core.getInstance().log(this.getClass(), LogType.WARNING, "Could not get User Object for UUID '"
					+ uuid.toString() + "': Not attempting to cancel disabled account login.");
			return;
		}

		// Cancel connection if the User's Account has been disabled
		if (!user.isEnabled()) {

			e.setLoginResult(Result.KICK_OTHER);
			e.setKickMessage("Account disabled"); // TODO:
			Core.getInstance().log(this.getClass(), LogType.DEBUG, "Disallowed connection for User '"
					+ user.getUUID().toString() + "/" + user.getLastKnownName() + "': Account is disabled.");
			return;

		}

	}

}
