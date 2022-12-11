package at.peirleitner.core.util.local;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.annotation.Nonnull;

import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import at.peirleitner.core.Core;
import at.peirleitner.core.SpigotMain;
import at.peirleitner.core.util.CustomLocation;
import at.peirleitner.core.util.database.SaveType.WorldType;
import at.peirleitner.core.util.user.User;

/**
 * Utils for local development
 * 
 * @since ?
 * @author Markus Peirleitner (Rengobli)
 *
 */
public class LocalUtils {

	public static CustomLocation getCustomLocationByLocation(@Nonnull Location location) {

		String worldName = location.getWorld().getName();
		double x = location.getX();
		double y = location.getY();
		double z = location.getZ();
		float yaw = location.getYaw();
		float pitch = location.getPitch();

		return new CustomLocation(worldName, x, y, z, yaw, pitch);
	}

	/**
	 * @param customLocation
	 * @return
	 * @since 1.0.3
	 */
	public static Location getLocation(@Nonnull CustomLocation customLocation) {
		return new Location(Bukkit.getWorld(customLocation.getWorldName()), customLocation.getX(),
				customLocation.getY(), customLocation.getZ(), customLocation.getYaw(), customLocation.getPitch());
	}

	public static String itemStackArrayToBase64(ItemStack[] items) throws IllegalStateException {
		try {
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

			// Write the size of the inventory
			dataOutput.writeInt(items.length);

			// Save every element in the list
			for (int i = 0; i < items.length; i++) {
				dataOutput.writeObject(items[i]);
			}

			// Serialize that array
			dataOutput.close();
			return Base64Coder.encodeLines(outputStream.toByteArray());
		} catch (Exception e) {
			throw new IllegalStateException("Unable to save item stacks.", e);
		}
	}

	public static ItemStack[] itemStackArrayFromBase64(String data) throws IOException {
		try {
			ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
			BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
			ItemStack[] items = new ItemStack[dataInput.readInt()];

			// Read the serialized inventory
			for (int i = 0; i < items.length; i++) {
				items[i] = (ItemStack) dataInput.readObject();
			}

			dataInput.close();
			return items;
		} catch (ClassNotFoundException e) {
			throw new IOException("Unable to decode class type.", e);
		}
	}

	/**
	 * 
	 * @return Server Version formated by 1.MAJOR.MINOR
	 * @since 1.0.5
	 * @author Markus Peirleitner (Rengobli)
	 */
	public static String getServerVersion() {

		String version = SpigotMain.getInstance().getServer().getBukkitVersion();
		String v = version.split("-")[0];

		return v;

	}

	/**
	 * @since 1.0.6
	 * @author Markus Peirleitner (Rengobli)
	 */
	public static boolean isInventoryFull(org.bukkit.entity.Player p) {

		boolean invFull = true;
		for (org.bukkit.inventory.ItemStack is : p.getInventory().getContents()) {
			if (is == null || is.getType() == org.bukkit.Material.AIR) {
				invFull = false;
				break;
			}
		}

		return invFull;

	}

	/**
	 * @since 1.0.6
	 * @author https://www.spigotmc.org/threads/remove-a-specific-amount-of-items-from-a-chest.388457/
	 */
	public static void removeItem(org.bukkit.inventory.Inventory inventory, org.bukkit.Material type, int toRemove) {
		for (int i = 0; i < inventory.getSize(); i++) {
			org.bukkit.inventory.ItemStack loopItem = inventory.getItem(i);
			if (loopItem == null || !new org.bukkit.inventory.ItemStack(type).isSimilar(loopItem)) {
				continue;
			}
			if (toRemove <= 0) {
				return;
			}
			if (toRemove < loopItem.getAmount()) {
				loopItem.setAmount(loopItem.getAmount() - toRemove);
				return;
			}
			inventory.clear(i);
			toRemove -= loopItem.getAmount();
		}
	}

	/**
	 * @since 1.0.6
	 * @author Markus Peirleitner (Rengobli)
	 */
	public static void removeItem(org.bukkit.inventory.Inventory inventory, ItemStack item, int toRemove) {
		for (int i = 0; i < inventory.getSize(); i++) {
			org.bukkit.inventory.ItemStack loopItem = inventory.getItem(i);
			if (loopItem == null || !item.isSimilar(loopItem)) {
				continue;
			}
			if (toRemove <= 0) {
				return;
			}
			if (toRemove < loopItem.getAmount()) {
				loopItem.setAmount(loopItem.getAmount() - toRemove);
				return;
			}
			inventory.clear(i);
			toRemove -= loopItem.getAmount();
		}
	}

	/**
	 * @since 1.0.6
	 * @author Markus Peirleitner (Rengobli)
	 */
	public static boolean hasItem(org.bukkit.inventory.Inventory inventory, org.bukkit.Material type, int amount) {

		int has = 0;

		for (org.bukkit.inventory.ItemStack is : inventory.getContents()) {

			if (is == null || is.getType() == org.bukkit.Material.AIR)
				continue;

			if (is.getType() == type) {
				has += is.getAmount();
			}

		}

		if (has >= amount) {
			return true;
		}

		return false;
	}

	/**
	 * @since 1.0.6
	 * @author Markus Peirleitner (Rengobli)
	 */
	public static boolean hasItem(org.bukkit.inventory.Inventory inventory, ItemStack item, int amount) {

		int has = 0;

		for (org.bukkit.inventory.ItemStack is : inventory.getContents()) {

			if (is == null || is.getType() == org.bukkit.Material.AIR)
				continue;

			if (is.isSimilar(item)) {
				has += is.getAmount();
			}

		}

		if (has >= amount) {
			return true;
		}

		return false;
	}

	/**
	 * 
	 * @param entityType
	 * @return Material to represent the EntityType or Barrier if none can be found
	 * @since 1.0.6
	 * @author Markus Peirleitner (Rengobli)
	 */
	public static Material getIcon(EntityType entityType) {

		// | Monster | \\
		// Overworld
		if (entityType == EntityType.ZOMBIE) {
			return Material.ROTTEN_FLESH;
		} else if (entityType == EntityType.SKELETON) {
			return Material.ARROW;
		} else if (entityType == EntityType.CREEPER) {
			return Material.GUNPOWDER;
		} else if (entityType == EntityType.SPIDER) {
			return Material.STRING;
		} else if (entityType == EntityType.ZOMBIE_VILLAGER) {
			return Material.ZOMBIE_HEAD;
		} else if (entityType == EntityType.WITCH) {
			return Material.GLASS_BOTTLE;
		} else if (entityType == EntityType.SLIME) {
			return Material.SLIME_BALL;
		} else if (entityType == EntityType.SILVERFISH) {
			return Material.SILVERFISH_SPAWN_EGG; // TODO:
		} else if (entityType == EntityType.CAVE_SPIDER) {
			return Material.SPIDER_EYE;
		} else if (entityType == EntityType.GUARDIAN) {
			return Material.PRISMARINE;
		} else if (entityType == EntityType.ELDER_GUARDIAN) {
			return Material.PRISMARINE_SHARD;
		} else if (entityType == EntityType.HUSK) {
			return Material.RED_SAND;
		} else if (entityType == EntityType.STRAY) {
			return Material.BOW;
		} else if (entityType == EntityType.VINDICATOR) {
			return Material.IRON_AXE;
		} else if (entityType == EntityType.EVOKER) {
			return Material.ENCHANTING_TABLE;
		} else if (entityType == EntityType.VEX) {
			return Material.IRON_SWORD;
		} else if (entityType == EntityType.PHANTOM) {
			return Material.PHANTOM_MEMBRANE;
		} else if (entityType == EntityType.DROWNED) {
			return Material.TRIDENT;
		} else if (entityType == EntityType.PILLAGER) {
			return Material.CROSSBOW;
		} else if (entityType == EntityType.RAVAGER) {
			return Material.IRON_BLOCK;

			// Nether
		} else if (entityType == EntityType.ZOMBIFIED_PIGLIN) {
			return Material.GOLD_NUGGET;
		} else if (entityType == EntityType.GHAST) {
			return Material.GHAST_TEAR;
		} else if (entityType == EntityType.MAGMA_CUBE) {
			return Material.MAGMA_CREAM;
		} else if (entityType == EntityType.HOGLIN) {
			return Material.HOGLIN_SPAWN_EGG; // TODO:
		} else if (entityType == EntityType.PIGLIN) {
			return Material.PIGLIN_SPAWN_EGG; // TODO:
		} else if (entityType == EntityType.PIGLIN_BRUTE) {
			return Material.PIGLIN_BRUTE_SPAWN_EGG; // TODO:
		} else if (entityType == EntityType.BLAZE) {
			return Material.BLAZE_ROD;
		} else if (entityType == EntityType.WITHER_SKELETON) {
			return Material.WITHER_SKELETON_SKULL;
		} else if (entityType == EntityType.WITHER) {
			return Material.NETHER_STAR;

			// End
		} else if (entityType == EntityType.ENDERMAN) {
			return Material.ENDER_PEARL;
		} else if (entityType == EntityType.SHULKER) {
			return Material.SHULKER_SHELL;
		} else if (entityType == EntityType.ENDERMITE) {
			return Material.ENDERMITE_SPAWN_EGG;
		} else if (entityType == EntityType.ENDER_DRAGON) {
			return Material.DRAGON_EGG;

			// | Animals | \\
		} else if (entityType == EntityType.PIG) {
			return Material.PORKCHOP;
		} else if (entityType == EntityType.SHEEP) {
			return Material.WHITE_WOOL;
		} else if (entityType == EntityType.COW) {
			return Material.LEATHER;
		} else if (entityType == EntityType.CHICKEN) {
			return Material.EGG;
		} else if (entityType == EntityType.SQUID) {
			return Material.INK_SAC;
		} else if (entityType == EntityType.BAT) {
			return Material.BAT_SPAWN_EGG; // TODO:
		} else if (entityType == EntityType.MUSHROOM_COW) {
			return Material.RED_MUSHROOM;
		} else if (entityType == EntityType.RABBIT) {
			return Material.RABBIT_FOOT;
		} else if (entityType == EntityType.POLAR_BEAR) {
			return Material.SNOWBALL;
		} else if (entityType == EntityType.TURTLE) {
			return Material.TURTLE_EGG;
		} else if (entityType == EntityType.COD) {
			return Material.COD;
		} else if (entityType == EntityType.SALMON) {
			return Material.SALMON;
		} else if (entityType == EntityType.PUFFERFISH) {
			return Material.PUFFERFISH;
		} else if (entityType == EntityType.TROPICAL_FISH) {
			return Material.TROPICAL_FISH;
		} else if (entityType == EntityType.DOLPHIN) {
			return Material.DOLPHIN_SPAWN_EGG; // TODO:
		} else if (entityType == EntityType.PANDA) {
			return Material.BAMBOO;
		} else if (entityType == EntityType.FOX) {
			return Material.FOX_SPAWN_EGG; // TODO:
		} else if (entityType == EntityType.BEE) {
			return Material.HONEY_BOTTLE;
		} else if (entityType == EntityType.AXOLOTL) {
			return Material.AXOLOTL_BUCKET;
		} else if (entityType == EntityType.GLOW_SQUID) {
			return Material.GLOW_INK_SAC;
		} else if (entityType == EntityType.GOAT) {
			return Material.GOAT_SPAWN_EGG; // TODO:
		} else if (entityType == EntityType.WOLF) {
			return Material.BONE;
		} else if (entityType == EntityType.OCELOT) {
			return Material.OCELOT_SPAWN_EGG; // TODO:
		} else if (entityType == EntityType.CAT) {
			return Material.CAT_SPAWN_EGG; // TOOD:
		} else if (entityType == EntityType.HORSE) {
			return Material.SADDLE;
		} else if (entityType == EntityType.DONKEY) {
			return Material.CHEST;
		} else if (entityType == EntityType.MULE) {
			return Material.MULE_SPAWN_EGG; // TODO:
		} else if (entityType == EntityType.SKELETON_HORSE) {
			return Material.SKELETON_HORSE_SPAWN_EGG; // TODO:
		} else if (entityType == EntityType.LLAMA) {
			return Material.LLAMA_SPAWN_EGG; // TODO:
		} else if (entityType == EntityType.PARROT) {
			return Material.COOKIE;
		} else if (entityType == EntityType.TRADER_LLAMA) {
			return Material.TRADER_LLAMA_SPAWN_EGG; // TODO:

			// | Others | \\

		} else if (entityType == EntityType.SNOWMAN) {
			return Material.CARVED_PUMPKIN;
		} else if (entityType == EntityType.IRON_GOLEM) {
			return Material.IRON_INGOT;
		} else if (entityType == EntityType.VILLAGER) {
			return Material.EMERALD;
		} else if (entityType == EntityType.WANDERING_TRADER) {
			return Material.WANDERING_TRADER_SPAWN_EGG; // TODO:
		} else if (entityType == EntityType.GIANT) {
			return Material.BARREL; // TODO:
		} else if (entityType == EntityType.ZOMBIE_HORSE) {
			return Material.ZOMBIE_HORSE_SPAWN_EGG; // TODO:
		} else if (entityType == EntityType.ILLUSIONER) {
			return Material.BARREL; // TODO:

		}

		return Material.BARRIER;
	}

	/**
	 * 
	 * @param name - Name of the world
	 * @return If a {@link World} with the given name exists in the WorldContainer
	 * @since 1.0.10
	 * @author Markus Peirleitner (Rengobli)
	 */
	public static final boolean isWorld(@Nonnull String name) {

		for (File f : Bukkit.getWorldContainer().listFiles()) {
			if (f.getName().equalsIgnoreCase(name)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * 
	 * @param user
	 * @param name
	 * @param worldType
	 * @return If the world has been created
	 * @since 1.0.10
	 * @author Markus Peirleitner (Rengobli)
	 */
	public static final boolean createWorld(@Nonnull User user, @Nonnull String name, @Nonnull WorldType worldType) {

		if (isDefaultWorld(name)) {
			user.sendMessage(Core.getInstance().getPluginName(),
					"command.world.main.error.cant-manipulate-default-world", null, true);
			return false;
		}

		if (isWorld(name)) {
			user.sendMessage(Core.getInstance().getPluginName(),
					"command.world.create.error.already-exists-in-world-container", Arrays.asList(name), true);
			return false;
		}

		World w = Bukkit.getWorld(name);

		if (w != null) {
			user.sendMessage(Core.getInstance().getPluginName(), "command.world.create.error.world-already-exists",
					Arrays.asList(w.getName()), true);
			return false;
		}

		WorldCreator wc = new WorldCreator(name);

		switch (worldType) {
		case NORMAL:
			wc.environment(Environment.NORMAL);
			break;
		case FLAT:
			wc.type(org.bukkit.WorldType.FLAT);
			break;
		case NETHER:
			wc.environment(Environment.NETHER);
			break;
		case END:
			wc.environment(Environment.THE_END);
			break;
		case VOID:
			wc.generator(new VoidChunkGenerator());
			break;
		default:
			wc.type(org.bukkit.WorldType.NORMAL);
			break;
		}

		w = Bukkit.createWorld(wc);

		if (w == null) {
			user.sendMessage(Core.getInstance().getPluginName(), "command.world.create.error.could-not-create",
					Arrays.asList(name), true);
			return false;
		}

		if (worldType == WorldType.VOID) {
			Location spawn = w.getSpawnLocation();
			w.getBlockAt(spawn.getBlockX(), spawn.getBlockY() - 1, spawn.getBlockZ()).setType(Material.BEDROCK);
		}

		user.sendMessage(Core.getInstance().getPluginName(), "command.world.create.success",
				Arrays.asList(name, worldType.toString()), true);
		return true;
	}

	/**
	 * 
	 * @param worldName
	 * @return If the world has been unloaded
	 * @since 1.0.10
	 * @author Markus Peirleitner (Rengobli)
	 */
	public static boolean unloadWorld(@Nonnull String worldName) {

		if (isDefaultWorld(worldName)) {
			return false;
		}

		World w = Bukkit.getWorld(worldName);

		if (w == null) {
			return false;
		}

		for (Player all : w.getPlayers()) {
			all.teleport(Bukkit.getWorld("world").getSpawnLocation());
			Core.getInstance().getLanguageManager().sendMessage(all, Core.getInstance().getPluginName(),
					"command.world.unload.player-info", Arrays.asList(worldName), true);
		}

		return Bukkit.unloadWorld(w, true);
	}

	/**
	 * 
	 * @param worldName
	 * @return If the world has been loaded
	 * @since 1.0.10
	 * @author Markus Peirleitner (Rengobli)
	 */
	public static boolean loadWorld(@Nonnull String worldName) {

		if (!isWorld(worldName)) {
			return false;
		}

		World w = Bukkit.getWorld(worldName);

		if (w != null) {
			// Already loaded
			return true;
		}

		Bukkit.createWorld(new WorldCreator(worldName));
		return true;
	}

	/**
	 * 
	 * @return Default world names as specified by Microsoft
	 * @since 1.0.10
	 * @author Markus Peirleitner (Rengobli)
	 */
	public static final String[] getDefaultWorldNames() {

		String[] names = new String[3];
		names[0] = "world";
		names[1] = "world_nether";
		names[2] = "world_the_end";

		return names;
	}

	/**
	 * 
	 * @param worldName
	 * @return If the given world is inside {@link #getDefaultWorldNames()}
	 * @since 1.0.10
	 * @author Markus Peirleitner (Rengobli)
	 */
	public static final boolean isDefaultWorld(@Nonnull String worldName) {

		for (String s : getDefaultWorldNames()) {
			if (s.equalsIgnoreCase(worldName)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * 
	 * @param user
	 * @param worldName
	 * @return If the world has been deleted
	 * @since 1.0.10
	 * @author Markus Peirleitner (Rengobli)
	 */
	public static boolean deleteWorld(@Nonnull User user, @Nonnull String worldName) {

		if (isDefaultWorld(worldName)) {
			user.sendMessage(Core.getInstance().getPluginName(),
					"command.world.main.error.cant-manipulate-default-world", null, true);
			return false;
		}

		if (!isWorld(worldName)) {
			user.sendMessage(Core.getInstance().getPluginName(), "command.world.main.error.not-in-world-container",
					Arrays.asList(worldName), true);
			return false;
		}

		World w = Bukkit.getWorld(worldName);

		if (w != null) {
			if (!unloadWorld(worldName)) {
				user.sendMessage(Core.getInstance().getPluginName(), "command.world.delete.error.cant-unload-world",
						Arrays.asList(w.getName()), true);
				return false;
			}
		}

		File f = new File(Bukkit.getWorldContainer() + "/" + worldName);

		try {
			FileUtils.deleteDirectory(f);
			user.sendMessage(Core.getInstance().getPluginName(), "command.world.delete.success",
					Arrays.asList(worldName), true);
			return true;
		} catch (IOException e) {
			user.sendMessage(Core.getInstance().getPluginName(), "command.world.delete.error.cant-delete-directory",
					Arrays.asList(worldName), true);
			return false;
		}

	}

	/**
	 * 
	 * @param user
	 * @param worldName
	 * @return If the given {@link User} has been teleported inside the world
	 * @since 1.0.10
	 * @author Markus Peirleitner (Rengobli)
	 */
	public static boolean teleportToWorld(@Nonnull User user, @Nonnull String worldName) {

		if (!isWorld(worldName)) {
			user.sendMessage(Core.getInstance().getPluginName(), "command.world.main.error.not-in-world-container",
					Arrays.asList(worldName), true);
			return false;
		}

		World w = Bukkit.getWorld(worldName);

		if (w == null) {

			if (!loadWorld(worldName)) {
				user.sendMessage(Core.getInstance().getPluginName(), "command.world.teleport.error.cant-load-world",
						Arrays.asList(worldName), true);
				return false;
			}

		}

		w = Bukkit.getWorld(worldName);
		boolean success = Bukkit.getPlayer(user.getUUID()).teleport(w.getSpawnLocation());

		user.sendMessage(Core.getInstance().getPluginName(),
				"command.world.teleport." + (success ? "success" : "error.cant-teleport"), Arrays.asList(worldName),
				true);
		return success;
	}

	public static boolean isHelmet(@Nonnull ItemStack is) {
		return Arrays.asList(new ItemStack(Material.LEATHER_HELMET), new ItemStack(Material.CHAINMAIL_HELMET),
				new ItemStack(Material.IRON_HELMET), new ItemStack(Material.DIAMOND_HELMET),
				new ItemStack(Material.GOLDEN_HELMET), new ItemStack(Material.NETHERITE_HELMET),
				new ItemStack(Material.TURTLE_HELMET)).contains(is);
	}
	
	public static boolean isChestplate(@Nonnull ItemStack is) {
		return Arrays.asList(new ItemStack(Material.LEATHER_CHESTPLATE), new ItemStack(Material.CHAINMAIL_CHESTPLATE),
				new ItemStack(Material.IRON_CHESTPLATE), new ItemStack(Material.DIAMOND_CHESTPLATE),
				new ItemStack(Material.GOLDEN_CHESTPLATE), new ItemStack(Material.NETHERITE_CHESTPLATE)).contains(is);
	}
	
	public static boolean isLeggings(@Nonnull ItemStack is) {
		return Arrays.asList(new ItemStack(Material.LEATHER_LEGGINGS), new ItemStack(Material.CHAINMAIL_LEGGINGS),
				new ItemStack(Material.IRON_LEGGINGS), new ItemStack(Material.DIAMOND_LEGGINGS),
				new ItemStack(Material.GOLDEN_LEGGINGS), new ItemStack(Material.NETHERITE_LEGGINGS)).contains(is);
	}
	
	public static boolean isBoots(@Nonnull ItemStack is) {
		return Arrays.asList(new ItemStack(Material.LEATHER_BOOTS), new ItemStack(Material.CHAINMAIL_BOOTS),
				new ItemStack(Material.IRON_BOOTS), new ItemStack(Material.DIAMOND_BOOTS),
				new ItemStack(Material.GOLDEN_BOOTS), new ItemStack(Material.NETHERITE_BOOTS)).contains(is);
	}

}
