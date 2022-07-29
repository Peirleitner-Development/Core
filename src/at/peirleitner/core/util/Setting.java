package at.peirleitner.core.util;

/**
 * Setting that a plugin can provide
 * 
 * @since 1.0.0
 * @author Markus Peirleitner (Rengobli)
 */
public class Setting<T> {

	private final String pluginName;
	private final String name;
	private final T defaultValue;

	public Setting(String pluginName, String name, T defaultValue) {
		this.pluginName = pluginName;
		this.name = name;
		this.defaultValue = defaultValue;
		
		this.register();
	}

	/**
	 * 
	 * @return Name of the Plugin
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 */
	public String getPluginName() {
		return pluginName;
	}

	/**
	 * 
	 * @return Name of this setting
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 */
	public String getName() {
		return name;
	}

	/**
	 * 
	 * @return Default value provided
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 */
	public T getDefaultValue() {
		return defaultValue;
	}
	
	/**
	 * 
	 * @return Current value
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 */
	public T getValue() {
		//TODO: Return real value
		return null;
	}
	
	private final void register() {
		//TODO: Register into created file: core -> settings.json -> name.setting.value
	}

}
