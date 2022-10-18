package at.peirleitner.core.listener.local;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.LeavesDecayEvent;

import at.peirleitner.core.Core;
import at.peirleitner.core.SpigotMain;

public class LeavesDecayListener implements Listener {

	public LeavesDecayListener() {
		SpigotMain.getInstance().getServer().getPluginManager().registerEvents(this, SpigotMain.getInstance());
	}

	@EventHandler
	public void onLeavesDecay(LeavesDecayEvent e) {

		if (Core.getInstance().getSettingsManager().isSetting(Core.getInstance().getPluginName(),
				"manager.settings.disable-leaves-decay")) {
			e.setCancelled(true);
		}

	}

}
