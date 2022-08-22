package at.peirleitner.core.util.local;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.material.MaterialData;

public class ItemBuilder {

	private final ItemStack is;

	public ItemBuilder(final Material mat) {
		is = new ItemStack(mat);
	}

	public ItemBuilder(final ItemStack is) {
		this.is = is;
	}

	public ItemBuilder amount(final int amount) {
		is.setAmount(amount);
		return this;
	}

	public ItemBuilder name(final String name) {
		final ItemMeta meta = is.getItemMeta();
		meta.setDisplayName(name);
		is.setItemMeta(meta);
		return this;
	}

	public ItemBuilder unbreakable() {
		final ItemMeta meta = is.getItemMeta();
		meta.setUnbreakable(true);
		is.setItemMeta(meta);
		return this;
	}

	public ItemBuilder lore(final List<String> name) {
		final ItemMeta meta = is.getItemMeta();
		List<String> lore = meta.getLore();
		if (lore == null) {
			meta.setLore(name);
		} else {
			lore.addAll(name);
			meta.setLore(lore);
		}
		is.setItemMeta(meta);
		return this;
	}

	public ItemBuilder lore(final String... name) {
		final ItemMeta meta = is.getItemMeta();
		List<String> lore = meta.getLore();
		if (lore == null) {
			lore = new ArrayList<String>();
		}
		lore.addAll(Arrays.asList(name));
		meta.setLore(lore);
		is.setItemMeta(meta);
		return this;
	}

	public ItemBuilder skullowner(final String name) {
		final SkullMeta meta = (SkullMeta) is.getItemMeta();
		meta.setOwner(name);
		is.setItemMeta(meta);
		return this;
	}

	public ItemBuilder durability(final int durability) {
		is.setDurability((short) durability);
		return this;
	}

	@SuppressWarnings("deprecation")
	public ItemBuilder data(final int data) {
		is.setData(new MaterialData(is.getType(), (byte) data));
		return this;
	}

	public ItemBuilder enchantment(final Enchantment enchantment, final int level) {
		is.addUnsafeEnchantment(enchantment, level);
		return this;
	}

	public ItemBuilder enchantment(final Enchantment enchantment) {
		is.addUnsafeEnchantment(enchantment, 1);
		return this;
	}

	public ItemBuilder type(final Material material) {
		is.setType(material);
		return this;
	}

	public ItemBuilder clearLore() {
		final ItemMeta meta = is.getItemMeta();
		meta.setLore(new ArrayList<String>());
		is.setItemMeta(meta);
		return this;
	}

	public ItemBuilder addItemFlag(ItemFlag... itemFlags) {
		final ItemMeta meta = is.getItemMeta();
		for (ItemFlag flag : itemFlags)
			meta.addItemFlags(flag);
		is.setItemMeta(meta);
		return this;
	}

	public ItemBuilder addItemFlags() {
		final ItemMeta meta = is.getItemMeta();
		for (ItemFlag flag : ItemFlag.values())
			meta.addItemFlags(flag);
		is.setItemMeta(meta);
		return this;
	}

	public ItemBuilder clearEnchantments() {
		for (final Enchantment e : is.getEnchantments().keySet()) {
			is.removeEnchantment(e);
		}
		return this;
	}

	public ItemBuilder color(Color color) {
		if (is.getType() == Material.LEATHER_BOOTS || is.getType() == Material.LEATHER_CHESTPLATE
				|| is.getType() == Material.LEATHER_HELMET || is.getType() == Material.LEATHER_LEGGINGS) {
			LeatherArmorMeta meta = (LeatherArmorMeta) is.getItemMeta();
			meta.setColor(color);
			is.setItemMeta(meta);
			return this;
		} else {
			throw new IllegalArgumentException("color() only applicable for leather armor!");
		}
	}

	public ItemStack build() {
		return is;
	}

}
