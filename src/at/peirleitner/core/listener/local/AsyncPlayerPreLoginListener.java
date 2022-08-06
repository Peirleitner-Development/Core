package at.peirleitner.core.listener.local;

import java.util.UUID;

import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;

import at.peirleitner.core.Core;
import at.peirleitner.core.SpigotMain;
import at.peirleitner.core.util.LogType;
import at.peirleitner.core.util.user.User;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.event.EventHandler;

public class AsyncPlayerPreLoginListener implements Listener {

	public AsyncPlayerPreLoginListener() {
		SpigotMain.getInstance().getServer().getPluginManager().registerEvents(this, SpigotMain.getInstance());
	}

	private final String CONNECTION_DISALLOWED_USER_CANT_BE_VALIDATED = "Could not get User Object for your UUID: Connection disallowed (Local).";

	@EventHandler
	public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent e) {

		// Tasks will only be performed if network-mode is disabled
		if (Core.getInstance().isNetwork())
			return;

		UUID uuid = e.getUniqueId();
		User user = Core.getInstance().getUserSystem().getUser(uuid);

		// Cancel connection if the User can't be validated
		if (user == null) {
			e.setLoginResult(Result.KICK_OTHER);
			e.setKickMessage(ChatColor.RED + CONNECTION_DISALLOWED_USER_CANT_BE_VALIDATED);
			Core.getInstance().log(this.getClass(), LogType.WARNING,
					"Could not get User Object for UUID '" + uuid.toString() + "': Connection disallowed.");
			return;
		}
		
		// Cancel connection if the User's Account has been disabled
		if(!user.isEnabled()) {
			
			e.setLoginResult(Result.KICK_OTHER);
			e.setKickMessage("Account disabled"); //TODO:
			Core.getInstance().log(this.getClass(), LogType.DEBUG, "Disallowed connection for User '" + user.getUUID().toString() + "/" + user.getLastKnownName() + "': Account is disabled.");
			return;
			
		}

	}

}
