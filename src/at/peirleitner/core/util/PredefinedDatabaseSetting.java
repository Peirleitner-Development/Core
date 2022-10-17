package at.peirleitner.core.util;

import javax.annotation.Nonnull;

/**
 * Settings that are stored inside the Database
 * 
 * @since 1.0.5
 * @author Markus Peirleitner (Rengobli)
 *
 */
public enum PredefinedDatabaseSetting {

	/**
	 * Current state of the maintenance mode (Unused as of v1.0.5)
	 * 
	 * @since 1.0.5
	 * @author Markus Peirleitner (Rengobli)
	 */
	MAINTENANCE("false"),

	/**
	 * Current Slots (Unused as of v1.0.5)
	 * 
	 * @since 1.0.5
	 * @author Markus Peirleitner (Rengobli)
	 * @apiNote Only applicable on {@link RunMode#NETWORK}
	 */
	SLOTS("100");

	private final String defaultValue;

	private PredefinedDatabaseSetting(@Nonnull String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public final String getDefaultValue() {
		return defaultValue;
	}

}
