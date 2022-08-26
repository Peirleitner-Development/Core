package at.peirleitner.core.util.local;

import java.util.UUID;

import javax.annotation.Nonnull;

import org.bukkit.Location;

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
		float yaw = Long.valueOf(s[4]);
		float pitch = Long.valueOf(s[5]);
		
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
	
}
