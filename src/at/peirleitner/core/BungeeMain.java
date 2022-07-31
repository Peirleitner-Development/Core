package at.peirleitner.core;

import at.peirleitner.core.util.RunMode;
import net.md_5.bungee.api.plugin.Plugin;

public class BungeeMain extends Plugin {

	private static BungeeMain instance;

	public BungeeMain() {

		if (!this.getDataFolder().exists()) {
			this.getDataFolder().mkdir();
		}

		instance = this;
		Core.instance = new Core(RunMode.NETWORK);
	}

	public static BungeeMain getInstance() {
		return instance;
	}

}
