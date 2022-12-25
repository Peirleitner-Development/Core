package at.peirleitner.core.listener.network;

import at.peirleitner.core.Core;
import at.peirleitner.core.util.LogType;
import at.peirleitner.core.util.user.User;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class PlayerDisconnectListener implements Listener {

	@EventHandler
	public void onPlayerDisconnect(PlayerDisconnectEvent e) {

		// Tasks will only be performed if network-mode is enabled
		if (!Core.getInstance().isNetwork())
			return;

		ProxiedPlayer pp = e.getPlayer();
		User user = Core.getInstance().getUserSystem().getUser(pp.getUniqueId());

		// Return if the User can't be validated
		if (user == null) {
			Core.getInstance().log(this.getClass(), LogType.WARNING,
					"Did not attempt to change anyting on disconnect for User '" + pp.getUniqueId().toString() + "/"
							+ pp.getName() + " because the User Object doesn't exist.");
			return;
		}

		// Update data if the server is running in network-mode.
		Core.getInstance().getUserSystem().setLastLogout(user, System.currentTimeMillis());

		if (Core.getInstance().getUserSystem().isCachingEnabled()) {
			Core.getInstance().getUserSystem().getCachedUsers().remove(user);
		}

		// v1.0.18
		if (Core.getInstance().getExperienceSystem().isCachingEnabled()
				&& Core.getInstance().getExperienceSystem().isCached(user.getUUID())) {
			Core.getInstance().getExperienceSystem().updateCacheToDatabase(user.getUUID());
		}

	}

}
