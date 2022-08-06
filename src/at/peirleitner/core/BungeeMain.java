package at.peirleitner.core;

import at.peirleitner.core.listener.network.LoginListener;
import at.peirleitner.core.util.RunMode;
import net.md_5.bungee.api.plugin.Plugin;

public class BungeeMain extends Plugin {

	private static BungeeMain instance;

	@Override
	public void onEnable() {

		if (!this.getDataFolder().exists()) {
			this.getDataFolder().mkdir();
		}

		instance = this;
		Core.instance = new Core(RunMode.NETWORK);

		// Commands

		// Listener
		this.getProxy().getPluginManager().registerListener(this, new LoginListener());

	}

	@Override
	public void onDisable() {

		if (Core.getInstance().getMySQL().isConnected()) {
			Core.getInstance().getMySQL().close();
		}

	}

	public static BungeeMain getInstance() {
		return instance;
	}

}
