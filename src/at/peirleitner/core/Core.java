package at.peirleitner.core;

import at.peirleitner.core.manager.SettingsManager;
import at.peirleitner.core.system.UserSystem;
import at.peirleitner.core.util.RunMode;

/**
 * This class represents the Core Instance
 * 
 * @since 1.0.0
 * @author Markus Peirleitner (Rengobli)
 */
public final class Core {

	private final RunMode runMode;
	public static Core instance;

	// Manager
	private SettingsManager settingsManager;

	// System
	private UserSystem userSystem;

	/**
	 * Create a new Instance
	 * 
	 * @param runMode - RunMode
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 */
	public Core(RunMode runMode) {

		// Initialize
		instance = this;
		this.runMode = runMode;

		// Manager
		this.settingsManager = new SettingsManager();

		// System
		this.userSystem = new UserSystem();

	}

	/**
	 * 
	 * @return Core instance
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final static Core getInstance() {
		return instance;
	}

	/**
	 * 
	 * @return Current RunMode
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final RunMode getRunMode() {
		return this.runMode;
	}

	/**
	 * 
	 * @return Name of the current main plugin
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 * @see SpigotMain
	 * @see BungeeMain
	 */
	public final String getPluginName() {
		return this.getRunMode() == RunMode.LOCAL ? SpigotMain.getInstance().getDescription().getName().toLowerCase()
				: BungeeMain.getInstance().getDescription().getName().toLowerCase();
	}
	
	/**
	 * 
	 * @return If the admins of this server are performing on a network/proxy.<br>
	 * This setting is critical as certain data (ex. lastLogin timestamp of a user connection) will be updated on every local (ex. spigot) connection if this is set to <code>false</code>.
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 * @apiNote If you are running a network, set this to <b>true</b>. If you are only running one server instance, set this to <b>false</b>.
	 */
	public final boolean isNetwork() {
		return (boolean) this.getSettingsManager().getByName(this.getPluginName(), "core.is-network").getValue();
	}

	// | Manager | \\

	/**
	 * 
	 * @return Manager for Settings
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final SettingsManager getSettingsManager() {
		return this.settingsManager;
	}

	// | System | \\

	/**
	 * 
	 * @return System for User interactions
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final UserSystem getUserSystem() {
		return this.userSystem;
	}

}
