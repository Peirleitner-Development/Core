package at.peirleitner.core.util;

import java.util.ArrayList;
import java.util.Collection;
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
	
	/**
	 * 
	 * @param string - String to translate
	 * @param separator - Should always be :
	 * @return Collection of CustomLocations
	 * @since 1.0.3
	 */
	public static Collection<CustomLocation> getCustomLocationListFromString(@Nonnull String string, @Nonnull String separator) {
		
		String[] s = string.split(separator);
		Collection<CustomLocation> locs = new ArrayList<>(s.length);
		
		for(String str : s) {
			locs.add(getCustomLocationFromString(str));
		}
		
		return locs;
	}

}
