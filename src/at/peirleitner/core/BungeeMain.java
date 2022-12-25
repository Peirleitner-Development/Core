package at.peirleitner.core;

import at.peirleitner.core.listener.network.LoginListener;
import at.peirleitner.core.listener.network.PlayerDisconnectListener;
import at.peirleitner.core.util.RunMode;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
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
		this.getProxy().getPluginManager().registerListener(this, new PlayerDisconnectListener());

	}

	@Override
	public void onDisable() {

		for (ProxiedPlayer pp : ProxyServer.getInstance().getPlayers()) {

			// v1.0.18
			if (Core.getInstance().getExperienceSystem().isCachingEnabled()
					&& Core.getInstance().getExperienceSystem().isCached(pp.getUniqueId())) {
				Core.getInstance().getExperienceSystem().updateCacheToDatabase(pp.getUniqueId());
			}

		}

		if (Core.getInstance().getMySQL().isConnected()) {
			Core.getInstance().getMySQL().close();
		}

	}

	public static BungeeMain getInstance() {
		return instance;
	}

}
