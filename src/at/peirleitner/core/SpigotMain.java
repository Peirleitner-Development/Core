package at.peirleitner.core;

import org.bukkit.plugin.java.JavaPlugin;

import at.peirleitner.core.listener.local.PlayerJoinListener;
import at.peirleitner.core.util.RunMode;

public class SpigotMain extends JavaPlugin {

	private static SpigotMain instance;

	public SpigotMain() {
		
		// Initialize
		instance = this;
		Core.instance = new Core(RunMode.LOCAL);
		
		// Commands
		
		// Listener
		new PlayerJoinListener();
		
	}

	public static SpigotMain getInstance() {
		return instance;
	}

}
