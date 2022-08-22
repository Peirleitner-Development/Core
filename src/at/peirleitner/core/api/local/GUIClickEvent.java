package at.peirleitner.core.api.local;

import javax.annotation.Nonnull;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import at.peirleitner.core.util.local.GUI;

public class GUIClickEvent extends Event implements Cancellable {

	private static final HandlerList HANDLERS = new HandlerList();
	private boolean cancelled;
	private Player player;
	private GUI gui;
	private ItemStack is;
	private int slot;
	private ClickType clickType;
	private Inventory inventory;

	public static HandlerList getHandlerList() {
		return HANDLERS;
	}

	@Override
	public HandlerList getHandlers() {
		return HANDLERS;
	}

	public GUIClickEvent(@Nonnull Player player, @Nonnull GUI gui, @Nonnull ItemStack is, @Nonnull int slot,
			@Nonnull ClickType clickType, @Nonnull Inventory inventory) {
		this.cancelled = false;
		this.player = player;
		this.gui = gui;
		this.is = is;
		this.slot = slot;
		this.clickType = clickType;
		this.inventory = inventory;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	public Player getPlayer() {
		return player;
	}

	public GUI getGUI() {
		return gui;
	}

	public ItemStack getItemStack() {
		return is;
	}

	public int getSlot() {
		return slot;
	}

	public ClickType getClickType() {
		return clickType;
	}

	public Inventory getClickedInventory() {
		return inventory;
	}

}
