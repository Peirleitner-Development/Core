package at.peirleitner.core.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import javax.annotation.Nonnull;

/**
 * @since 1.0.3
 * @author Markus Peirleitner (Rengobli)
 *
 */
public class GlobalUtils {

	/**
	 * @param string
	 * @param separator
	 * @return
	 * @since 1.0.3
	 */
	public static Collection<UUID> getUuidListByString(@Nonnull String string, @Nonnull String separator) {

		Collection<UUID> list = new ArrayList<>();

		String[] split = string.split(separator);

		for (String s : split) {
			list.add(UUID.fromString(s));
		}

		return list;
	}

	/**
	 * 
	 * @param list
	 * @param separator
	 * @return
	 * @since 1.0.3
	 */
	public static String getUuidString(@Nonnull Collection<UUID> list, @Nonnull String separator) {

		StringBuilder sb = new StringBuilder();

		for (UUID uuid : list) {
			sb.append(uuid.toString() + separator);
		}

		return sb.toString();
	}
	
	/**
	 * 
	 * @param string
	 * @return
	 * @since 1.0.3
	 */
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
	
	public static String getCustomLocationStringFromList(@Nonnull Collection<CustomLocation> locations) {
		
		StringBuilder sb = new StringBuilder();
		
		for(CustomLocation cl : locations) {
			sb.append(cl.toString() + ":");
		}
		
		return sb.toString();
	}
	
	/**
	 * 
	 * @param string - String to translate
	 * @param separator - Should always be :
	 * @return Collection of CustomLocations
	 * @since 1.0.3
	 */
	public static Collection<CustomLocation> getCustomLocationListFromString(@Nonnull String string) {
		
		String[] s = string.split(":");
		Collection<CustomLocation> locs = new ArrayList<>(s.length);
		
		for(String str : s) {
			locs.add(getCustomLocationFromString(str));
		}
		
		return locs;
	}
	
	/**
	 * 
	 * @param timestamp - TimeStamp to convert into a String
	 * @return Formated TimeStamp
	 * @since 1.0.4
	 * @author Markus Peirleitner (Rengobli)
	 * @apiNote dd.MM.YYYY HH:mm:ss
	 */
	public static String getFormatedDate(@Nonnull long timestamp) {
		
		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.YYYY HH:mm:ss");
		return sdf.format(new Date(timestamp));
		
	}
	
	/**
	 * 
	 * @param percent - Trigger Chance
	 * @return If it should trigger
	 * @since 1.0.10
	 * @author Markus Peirleitner (Rengobli)
	 */
	public static boolean shouldTrigger(double percent) {
		Random r = new Random();
		float temp = r.nextFloat();

		if (temp < (percent / 100))
			return true;
		else
			return false;
	}

	/**
	 * 
	 * @param s
	 * @return List of entered String, separated by line break
	 * @since 1.0.15
	 * @author Markus Peirleitner (Rengobli)
	 */
	public static List<String> getLore(@Nonnull String s) {
		
		List<String> lore = new ArrayList<>();
		String[] split = s.split("\n");
		
		for(String msg : split) {
			lore.add(msg);
		}
		
		return lore;
	}
	
}
