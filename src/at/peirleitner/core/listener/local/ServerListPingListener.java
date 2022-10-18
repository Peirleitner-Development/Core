package at.peirleitner.core.listener.local;

import java.util.Arrays;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;

import at.peirleitner.core.Core;
import at.peirleitner.core.SpigotMain;
import at.peirleitner.core.util.MOTD;
import at.peirleitner.core.util.RunMode;
import at.peirleitner.core.util.local.LocalUtils;
import net.md_5.bungee.api.ChatColor;

public class ServerListPingListener implements Listener {

	public ServerListPingListener() {
		SpigotMain.getInstance().getServer().getPluginManager().registerEvents(this, SpigotMain.getInstance());
	}
	
	private final String CANT_GET_MOTD = ChatColor.RED + "Could not get MOTD (Local)";
	
	@EventHandler
	public void onServerListPing(ServerListPingEvent e) {
		
		if(Core.getInstance().getSettingsManager().isSetting(Core.getInstance().getPluginName(), "manager.settings.disable-motd-server-list-ping")) {
			return;
		}
		
		if(Core.getInstance().getRunMode() == RunMode.LOCAL && Core.getInstance().getMaintenanceSystem().isMaintenance()) {
			e.setMotd(Core.getInstance().getLanguageManager().getMessage(Core.getInstance().getPluginName(), Core.getInstance().getDefaultLanguage(), "listener.server-list-ping.maintenance", Arrays.asList(
					Core.getInstance().getSettingsManager().getServerName(),
					LocalUtils.getServerVersion(),
					Core.getInstance().getSettingsManager().getServerWebsite()
					)));
			return;
		}
		
		MOTD motd = Core.getInstance().getMotdSystem().getMOTD();
		
		if(motd == null) {
			e.setMotd(CANT_GET_MOTD);
			return;
		}
		
		String line1 = ChatColor.translateAlternateColorCodes('&', motd.getFirstLine());
		String line2 = ChatColor.translateAlternateColorCodes('&', motd.getSecondLine());
		
		e.setMotd(line1 + "\n" + line2);
		
	}
	
}
