package at.peirleitner.core.manager;

import at.peirleitner.core.util.Setting;

/**
 * Class to manage settings
 * @since 1.0.0
 * @author Markus Peirleitner (Rengobli)
 */
public class SettingsManager {

	/**
	 * Create a new setting
	 * @param setting - Setting
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final void create(Setting<?> setting) {
		
	}
	
	/**
	 * Get a Setting by its name
	 * @param pluginName - Name of the Plugin
	 * @param name - {@link Setting#getName()}
	 * @return Setting or <code>null</code> if none can be found with the given name
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final Setting<?> getByName(String pluginName, String name) {
		return null;
	}

}
