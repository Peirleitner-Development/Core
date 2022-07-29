package at.peirleitner.core;

import org.bukkit.plugin.java.JavaPlugin;

import at.peirleitner.core.util.RunMode;

public class SpigotMain extends JavaPlugin {

	private static SpigotMain instance;

	public SpigotMain() {
		instance = this;
		Core.instance = new Core(RunMode.LOCAL);
	}

	public static SpigotMain getInstance() {
		return instance;
	}

}
