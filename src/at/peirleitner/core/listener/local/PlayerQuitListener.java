package at.peirleitner.core.listener.local;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import at.peirleitner.core.Core;
import at.peirleitner.core.SpigotMain;
import at.peirleitner.core.util.LogType;
import at.peirleitner.core.util.user.User;
import net.md_5.bungee.event.EventHandler;

public class PlayerQuitListener implements Listener {

	public PlayerQuitListener() {
		SpigotMain.getInstance().getServer().getPluginManager().registerEvents(this, SpigotMain.getInstance());
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {

		Player p = e.getPlayer();
		User user = Core.getInstance().getUserSystem().getUser(p.getUniqueId());

		// Return if the User can't be validated
		if (user == null) {
			Core.getInstance().log(this.getClass(), LogType.WARNING,
					"Did not attempt to change anyting on disconnect for User '" + p.getUniqueId().toString() + "/"
							+ p.getName() + " because the User Object doesn't exist.");
			return;
		}

		// Update data if the server isn't running in network-mode.
		if (!Core.getInstance().isNetwork()) {
			Core.getInstance().getUserSystem().setLastLogout(user, System.currentTimeMillis());

			if (Core.getInstance().getUserSystem().isCachingEnabled()) {
				Core.getInstance().getUserSystem().getCachedUsers().remove(user);
			}

		}

	}

}
