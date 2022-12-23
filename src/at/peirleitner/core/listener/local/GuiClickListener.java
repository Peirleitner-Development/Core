package at.peirleitner.core.listener.local;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import at.peirleitner.core.Core;
import at.peirleitner.core.SpigotMain;
import at.peirleitner.core.api.local.GUIClickEvent;
import at.peirleitner.core.manager.local.LocalGuiManager;
import at.peirleitner.core.util.database.SaveType;
import at.peirleitner.core.util.local.GUI;
import at.peirleitner.core.util.user.User;
import net.md_5.bungee.api.ChatColor;

public class GuiClickListener implements Listener {

	public GuiClickListener() {
		SpigotMain.getInstance().getServer().getPluginManager().registerEvents(this, SpigotMain.getInstance());
	}

	@EventHandler
	public void onGuiClick(GUIClickEvent e) {

		GUI gui = e.getGUI();
		Player p = e.getPlayer();
		User user = Core.getInstance().getUserSystem().getUser(p.getUniqueId());

		String name = "";

		if (e.getItemStack() != null && e.getItemStack().getType() != Material.AIR && e.getItemStack().hasItemMeta()
				&& e.getItemStack().getItemMeta().hasDisplayName()) {
			name = ChatColor.stripColor(e.getItemStack().getItemMeta().getDisplayName());
		}

		if (gui.getTitle().equals(LocalGuiManager.GUI_STATISTICS)) {
			
			if(e.getSlot() == 1 && e.getItemStack().getType() == Material.RED_BED) {
				SpigotMain.getInstance().getLocalGuiManager().getStatisticMainGUI(user).open(p);
				return;
			}

			SaveType saveType = Core.getInstance().getSaveTypeByName(name);

			if (saveType != null) {
				SpigotMain.getInstance().getLocalGuiManager().getStatisticsGUI(user, saveType).open(p);
				return;
			}
			
			return;

		}

	}

}
