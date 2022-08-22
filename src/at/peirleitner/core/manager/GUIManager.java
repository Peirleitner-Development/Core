package at.peirleitner.core.manager;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;

import at.peirleitner.core.SpigotMain;
import at.peirleitner.core.api.local.GUIClickEvent;
import at.peirleitner.core.util.local.GUI;

public class GUIManager implements Listener {

	private HashMap<UUID, GUI> guis;

	public GUIManager() {
		this.guis = new HashMap<UUID, GUI>();
		SpigotMain.getInstance().getServer().getPluginManager().registerEvents(this, SpigotMain.getInstance());
	}

	public HashMap<UUID, GUI> getGUIS() {
		return guis;
	}

	@EventHandler
	private void onInventoryClick(InventoryClickEvent e) {

		// Define Player
		Player p = (Player) e.getWhoClicked();

		// Return if clicked Item is null
		if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR
				|| e.getSlotType() == SlotType.OUTSIDE) {
			return;
		}

		// Return if the User is not in the List
		if (!this.getGUIS().containsKey(p.getUniqueId()))
			return;

		GUI gui = this.getGUIS().get(p.getUniqueId());

		// Return if the Inventory would be null somehow
		if (gui == null)
			return;

		// Always cancel at this point
		e.setCancelled(true);

		// Open previous Page
		if (e.getSlot() == 46) {
			gui.openPreviousPage(p);
			return;
		}

		// Open next Page
		if (e.getSlot() == 52) {
			gui.openNextPage(p);
			return;
		}

		// Return if the clicked Slot is the current Page Information
		if (e.getSlot() == 49) {
			return;
		}

		GUIClickEvent event = new GUIClickEvent(p, gui, e.getCurrentItem(), e.getSlot(), e.getClick(),
				e.getClickedInventory());
		Bukkit.getPluginManager().callEvent(event);

	}

	@EventHandler
	private void onInventoryDrag(final InventoryDragEvent e) {
		if (this.getGUIS().containsKey(e.getWhoClicked().getUniqueId())) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	private void onInventoryClose(final InventoryCloseEvent e) {

		Player p = (Player) e.getPlayer();

		if (p != null && this.getGUIS().containsKey(p.getUniqueId())) {
			this.getGUIS().remove(p.getUniqueId());
		}

	}

}
