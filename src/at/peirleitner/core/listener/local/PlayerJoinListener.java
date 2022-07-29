package at.peirleitner.core.listener.local;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import at.peirleitner.core.Core;
import at.peirleitner.core.SpigotMain;

public class PlayerJoinListener implements Listener {

	public PlayerJoinListener() {
		SpigotMain.getInstance().getServer().getPluginManager().registerEvents(this, SpigotMain.getInstance());
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		
		// Don't update data if this is a network
		if(Core.getInstance().isNetwork()) return;
		
		
		
	}
	
}
