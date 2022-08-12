package at.peirleitner.core.listener.local;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.help.HelpTopic;

import at.peirleitner.core.Core;
import at.peirleitner.core.SpigotMain;
import at.peirleitner.core.util.user.User;

public class PlayerCommandPreProcessListener implements Listener {

	public PlayerCommandPreProcessListener() {
		SpigotMain.getInstance().getServer().getPluginManager().registerEvents(this, SpigotMain.getInstance());
	}
	
	@EventHandler
	public void onPlayerCommandPreProcess(PlayerCommandPreprocessEvent e) {
		
		Player p = e.getPlayer();
		User user = Core.getInstance().getUserSystem().getUser(p.getUniqueId());
		String cmd = e.getMessage().split(" ")[0];
		
		HelpTopic ht = Bukkit.getHelpMap().getHelpTopic(cmd);
		
		if(ht == null) {
			
			e.setCancelled(true);
			
			user.sendMessage(
					Core.getInstance().getLanguageManager(),
					"listener.player-command-pre-process.unknown-command", Arrays.asList(cmd), true);
			return;
			
		}
		
	}
	
}
