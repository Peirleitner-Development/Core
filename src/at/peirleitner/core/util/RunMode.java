package at.peirleitner.core.util;

import at.peirleitner.core.Core;

/**
 * This enum defines the current RunMode set in {@link Core#getRunMode()}.<br>
 * This will be set by the {@link Core} class on plugin initialization and can
 * not be changed manually.
 * 
 * @author Markus Peirleitner (Rengobli)
 *
 */
public enum RunMode {

	/**
	 * Local instance (Bukkit, Spigot, Paper)
	 * 
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 */
	LOCAL,

	/**
	 * Proxy instance (BungeeCord, Waterfall)
	 * 
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 */
	NETWORK;

}
