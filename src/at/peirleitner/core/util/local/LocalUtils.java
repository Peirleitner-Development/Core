package at.peirleitner.core.util.local;

import java.util.UUID;

import javax.annotation.Nonnull;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public class LocalUtils {

	public static Collection<UUID> getUuidListByString(@Nonnull String string, @Nonnull String separator) {
		
		Collection<UUID> list = new ArrayList<>(); 
		
		String[] split = string.split(separator);
		
		for(String s : split) {
			list.add(UUID.fromString(s));
		}
		
		return list;
	}
	
	public static String getUuidString(@Nonnull Collection<UUID> list, @Nonnull String separator) {
		
		StringBuilder sb = new StringBuilder();
		
		for(UUID uuid : list) {
			sb.append(uuid.toString() + separator);
		}
		
		return sb.toString();
	}
	
	public static CustomLocation getCustomLocationFromString(@Nonnull String string) {
		
		String[] s = string.split(";");
		
		String worldName = s[0];
		double x = Double.valueOf(s[1]);
		double y = Double.valueOf(s[2]);
		double z = Double.valueOf(s[3]);
		float yaw = Float.valueOf(s[4]);
		float pitch = Float.valueOf(s[5]);
		
		return new CustomLocation(worldName, x, y, z, yaw, pitch);
	}
	
	public static CustomLocation getCustomLocationByLocation(@Nonnull Location location) {
		
		String worldName = location.getWorld().getName();
		double x = location.getX();
		double y = location.getY();
		double z = location.getZ();
		float yaw = location.getYaw();
		float pitch = location.getPitch();
		
		return new CustomLocation(worldName, x, y, z, yaw, pitch);
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
	
}
