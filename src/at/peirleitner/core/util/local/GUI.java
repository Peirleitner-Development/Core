package at.peirleitner.core.util.local;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import at.peirleitner.core.Core;
import at.peirleitner.core.SpigotMain;
import at.peirleitner.core.util.user.User;
import net.md_5.bungee.api.ChatColor;

/**
 * This class represents a GUI that the User is able to interact with.<br>
 * If the filled in Items exceed the Size of the Inventory you will also be able
 * to navigate back and fourth between pages.
 * 
 * @since 1.0.0
 * @author Markus Peirleitner (Rengobli)
 */
public class GUI implements Listener {

	private int page;
	private String title;
	private List<ItemStack> items;
	private HashMap<Integer, ItemStack> replacements = new HashMap<>();
	private Player player;

	/**
	 * 
	 * @return ItemStack to be used as a filler
	 * @since 1.0.1
	 * @author Markus Peirleitner (Rengobli)
	 */
	private ItemStack getPaneItemStack() {
		return new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).name(" ").build();
	}

//	private ItemStack getBackItemStack() {
//		return new ItemBuilder(Material.RED_BED).name(ChatColor.DARK_AQUA + "Back").build();
//	}

	/**
	 * 
	 * @param user - NetworkUser to gain the Language for
	 * @return ItemStack to be used as 'Current Page' Information
	 * @since 1.0.1
	 * @author Markus Peirleitner (Rengobli)
	 * @see NetworkUser
	 */
	private ItemStack getCurrentPageItemStack(@Nonnull User user) {
		return new ItemBuilder(Material.BOOK)
				.name(Core.getInstance().getLanguageManager().getMessage(SpigotMain.getInstance().getDescription().getName(), user.getLanguage(),
						"gui.gui-builder.item.current-page", Arrays.asList("" + this.page, "" + this.getMaxPage())))
				.build();
	}

	/**
	 * 
	 * @param user - NetworkUser to gain the Language for
	 * @return ItemStack to be used as 'Next' Button
	 * @since 1.0.1
	 * @author Markus Peirleitner (Rengobli)
	 * @see NetworkUser
	 */
	private ItemStack getNextPageItemSack(@Nonnull User user) {
		return new ItemBuilder(Material.PLAYER_HEAD).skullowner("MHF_ArrowRight")
				.name(Core.getInstance().getLanguageManager().getMessage(SpigotMain.getInstance().getDescription().getName(), user.getLanguage(),
						"gui.gui-builder.item.next-page", null))
				.build();
	}

	/**
	 * 
	 * @param user - NetworkUser to gain the Language for
	 * @return ItemStack to be used as 'Back' Button
	 * @since 1.0.1
	 * @author Markus Peirleitner (Rengobli)
	 * @see NetworkUser
	 */
	private ItemStack getPreviousPageItemSack(@Nonnull User user) {
		return new ItemBuilder(Material.PLAYER_HEAD).skullowner("MHF_ArrowLeft")
				.name(Core.getInstance().getLanguageManager().getMessage(SpigotMain.getInstance().getDescription().getName(), user.getLanguage(),
						"gui.gui-builder.item.previous-page", null))
				.build();
	}

	/**
	 * Create a GUI with no Title or Items specified yet
	 * 
	 * @since 1.0.1
	 */
	public GUI() {
		this.setTitle(this.getDefaultTitle());
		this.items = new ArrayList<>();
		this.page = 1;

		SpigotMain.getInstance().getServer().getPluginManager().registerEvents(this, SpigotMain.getInstance());
	}

	/**
	 * Create a GUI with a Title but none Items specified
	 * 
	 * @param title - Title for this GUI
	 * @since 1.0.1
	 */
	public GUI(@Nullable String title) {
		this.setTitle(title);
		this.items = new ArrayList<>();
		this.page = 1;

		SpigotMain.getInstance().getServer().getPluginManager().registerEvents(this, SpigotMain.getInstance());
	}

	/**
	 * Create a GUI with the Title and Items being specified
	 * 
	 * @param title - Title for this GUI
	 * @param items - Items for this GUI
	 * @since 1.0.1
	 */
	public GUI(@Nullable String title, @Nonnull List<ItemStack> items) {
		this.setTitle(title);
		this.setItems(items);
		this.page = 1;

		SpigotMain.getInstance().getServer().getPluginManager().registerEvents(this, SpigotMain.getInstance());
	}

	/**
	 * Create a GUI with the Title and Items being specified
	 * 
	 * @param title - Title for this GUI
	 * @param items - Items for this GUI
	 * @since 1.0.1
	 */
	@Deprecated
	public GUI(@Nullable String title, @Nonnull Collection<ItemStack> items) {
		this.setTitle(title);
		this.setItems(items);
		this.page = 1;

		SpigotMain.getInstance().getServer().getPluginManager().registerEvents(this, SpigotMain.getInstance());
	}

	/**
	 * 
	 * @return Default Title to be used on GUIs without a specified Title
	 * @since 1.0.1
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final String getDefaultTitle() {
		return "GUI";
	}

	/**
	 * 
	 * @return Title of this GUI
	 * @since 1.0.2
	 * @author Markus Peirleitner (Rengobli)
	 */
	public String getTitle() {
		return this.title;
	}

	/**
	 * 
	 * @return If this GUI has a Title
	 * @since 1.0.2
	 * @author Markus Peirleitner (Rengobli)
	 */
	public boolean hasTitle() {
		return this.getTitle() == null || this.getTitle().equals("") ? false : true;
	}

	/**
	 * Set the Title for this GUI
	 * 
	 * @param title - New Title
	 * @since 1.0.1
	 * @author Markus Peirleitner (Rengobli)
	 * @apiNote If the given Title is <code>null</code> or <code>empty</code>, the
	 *          <code>default</code> title will be used.
	 */
	public void setTitle(@Nullable String title) {
		this.title = (title == null || title.equals("") ? this.getDefaultTitle() : title);
	}

	/**
	 * Add an Item to this GUI
	 * 
	 * @param is - Item to add
	 * @since 1.0.1
	 * @author Markus Peirleitner (Rengobli)
	 */
	public void addItem(@Nonnull ItemStack is) {
		this.items.add(is);
	}

	/**
	 * Remove an Item from this GUI
	 * 
	 * @param is - Item to remove
	 * @since 1.0.1
	 * @author Markus Peirleitner (Rengobli)
	 */
	public void removeItem(@Nonnull ItemStack is) {
		this.items.remove(is);
	}

	/**
	 * Set an item to a specific location on this GUI
	 * 
	 * @param slot - Slot to set
	 * @param is   - Item to set
	 * @since 1.0.1
	 * @author Markus Peirleitner (Rengobli)
	 */
	public void setItem(@Nonnull int slot, @Nonnull ItemStack is) {
		replacements.put(slot, is);
	}

	/**
	 * 
	 * @return Items that are replaced in the Inventory
	 * @since 1.0.2
	 * @author Markus Peirleitner (Rengobli)
	 * @apiNote The Integer refers to the Slot of the virtual Inventory
	 * @apiNote The ItemStack is the Replacement itself
	 */
	public HashMap<Integer, ItemStack> getReplacements() {
		return this.replacements;
	}

	/**
	 * 
	 * @return If this GUI has Replacements
	 * @since 1.0.2
	 * @author Markus Peirleitner (Rengobli)
	 */
	public boolean hasReplacements() {
		return this.getReplacements().isEmpty();
	}

	/**
	 * 
	 * @return Player that has this GUI opened
	 * @since 1.0.2
	 * @author Markus Peirleitner (Rengobli)
	 */
	public Player getPlayer() {
		return this.player;
	}

	/**
	 * Specify the Items the GUI is filled with
	 * 
	 * @param items - Items to set
	 * @since 1.0.1
	 * @author Markus Peirleitner (Rengobli)
	 */
	public void setItems(@Nonnull List<ItemStack> items) {
		this.items = items;
	}

	/**
	 * Specify the Items the GUI is filled with
	 * 
	 * @param items - Items to set
	 * @since 1.0.1
	 * @author Markus Peirleitner (Rengobli)
	 */
	@Deprecated
	public void setItems(@Nonnull Collection<ItemStack> items) {
		List<ItemStack> list = new ArrayList<>();
		list.addAll(items);

		this.items = list;
	}

	/**
	 * Open the GUI on a specified Page for the Player
	 * 
	 * @param p    - Player to open the GUI for
	 * @param page - Page
	 * @since 1.0.1
	 * @author Markus Peirleitner (Rengobli)
	 */
	private void openInventory(@Nonnull Player p, @Nonnull int page) {
		this.page = page;
		this.player = p;
		User user = Core.getInstance().getUserSystem().getUser(p.getUniqueId());
		Inventory inv = this.getInventory(user, page);

		if (!this.replacements.isEmpty()) {

			for (Map.Entry<Integer, ItemStack> entry : this.replacements.entrySet()) {
				inv.setItem(entry.getKey(), entry.getValue());
			}

		}

		p.openInventory(inv);
		SpigotMain.getInstance().getGUIManager().getGUIS().put(p.getUniqueId(), this);
	}

	/**
	 * Open the GUI on the Main Page
	 * 
	 * @param p - Player to open the GUI for
	 * @since 1.0.1
	 * @author Markus Peirleitner (Rengobli)
	 */
	public void open(@Nonnull Player p) {
		this.openInventory(p, 1);
	}

	/**
	 * Open the previous Page for a Player
	 * 
	 * @param p - Player
	 * @since 1.0.1
	 * @author Markus Peirleitner (Rengobli)
	 * @apiNote If no previous Page is available, this will do nothing
	 */
	public void openPreviousPage(@Nonnull Player p) {

		if (!this.hasPreviousPage())
			return;

		this.openInventory(p, this.getPreviousPage());

	}

	/**
	 * Open the next Page for a Player
	 * 
	 * @param p - Player
	 * @since 1.0.1
	 * @author Markus Peirleitner (Rengobli)
	 * @apiNote If no next Page is available, this will do nothing
	 */
	public void openNextPage(@Nonnull Player p) {

		if (!this.hasNextPage())
			return;

		this.openInventory(p, this.getNextPage());

	}

	/**
	 * Get the virtual Inventory for this GUI on a specified Page
	 * 
	 * @param user - NetworkUser
	 * @param page - Page
	 * @return Virtual Inventory
	 * @since 1.0.1
	 * @author Markus Peirleitner (Rengobli)
	 * @see NetworkUser
	 */
	private Inventory getInventory(@Nonnull User user, @Nonnull int page) {

		Inventory inv = this.getDefaultInventory(user);
		List<ItemStack> items = this.getItems(page);

		items.forEach(item -> inv.addItem(item));

		return inv;
	}

	/**
	 * Get the default virtual Inventory Layout
	 * 
	 * @param user - NetworkUser
	 * @return Default Inventory
	 * @since 1.0.1
	 * @author Markus Peirleitner (Rengobli)
	 * @see NetworkUser
	 */
	private Inventory getDefaultInventory(@Nonnull User user) {

		// Define Inventory
		Inventory inv = Bukkit.createInventory(null, 54, ChatColor.DARK_AQUA + ChatColor.stripColor(title));

		// Fillers
		for (int i = 0; i < 9; i++) {
			inv.setItem(i, this.getPaneItemStack());
		}

		for (int i = 45; i < 54; i++) {
			inv.setItem(i, this.getPaneItemStack());
		}

		// Page Navigation

//		if (this.getMaxPage() > 1) {
//			inv.setItem(46, this.getPreviousPageItemSack());
//			inv.setItem(49, this.getCurrentPageItemStack());
//			inv.setItem(52, this.getNextPageItemSack());
//		}

		inv.setItem(46, this.getPreviousPageItemSack(user));
		inv.setItem(49, this.getCurrentPageItemStack(user));
		inv.setItem(52, this.getNextPageItemSack(user));

		return inv;
	}

	/**
	 * Get the Items to be displayed on a specific Page
	 * 
	 * @param page - Page
	 * @return Items to be displayed on the given Page
	 * @since 1.0.1
	 * @author Markus Peirleitner (Rengobli)
	 */
	private List<ItemStack> getItems(@Nonnull int page) {

		List<ItemStack> items = new ArrayList<>();
		items.addAll(this.items);

		int start = (page == 1 ? 0 : (page - 1) * 36);
		int removed = 0;

		Iterator<ItemStack> it = items.iterator();

		while (removed < start && it.hasNext()) {

			it.next();
			it.remove();
			removed++;

		}

		return items;
	}

	/**
	 * 
	 * @return Maximum Amount of Pages that this GUI offers based on the Items given
	 * @since 1.0.1
	 * @author Markus Peirleitner (Rengobli)
	 */
	public int getMaxPage() {

		int max = 1;
		int size = items.size();

		while (size > 36) {
			max++;
			size -= 36;
		}

		return max;
	}

	/**
	 * 
	 * @return Previous Page Number
	 * @since 1.0.1
	 * @author Markus Peirleitner (Rengobli)
	 */
	public int getPreviousPage() {
		return this.page - 1;
	}

	/**
	 * 
	 * @return If the GUI has a previous Page on the current virtual Inventory
	 * @since 1.0.1
	 * @author Markus Peirleitner (Rengobli)
	 */
	public boolean hasPreviousPage() {
		return this.page > 1;
	}

	/**
	 * 
	 * @return Next Page Number
	 * @since 1.0.1
	 * @author Markus Peirleitner (Rengobli)
	 */
	public int getNextPage() {
		return this.page + 1;
	}

	/**
	 * 
	 * @return If the GUI has a next Page on the current virtual Inventory
	 * @since 1.0.1
	 * @author Markus Peirleitner (Rengobli)
	 */
	public boolean hasNextPage() {
		return this.page < this.getMaxPage();
	}

}
