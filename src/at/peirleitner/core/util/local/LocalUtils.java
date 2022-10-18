package at.peirleitner.core.util.local;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.annotation.Nonnull;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import at.peirleitner.core.SpigotMain;
import at.peirleitner.core.util.CustomLocation;

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
		return new Location(Bukkit.getWorld(customLocation.getWorldName()), customLocation.getX(), customLocation.getY(), customLocation.getZ(), customLocation.getYaw(), customLocation.getPitch());
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

}
