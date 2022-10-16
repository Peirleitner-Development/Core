package at.peirleitner.core.util.user;

/**
 * Message that can be send towards a {@link User}
 * 
 * @since 1.0.5
 * @author Markus Peirleitner (Rengobli)
 */
public class LanguageMessage {

	private String pluginName;
	private final Language language;
	private final String key;
	private final String value;

	public LanguageMessage(String pluginName, Language language, String key, String value) {
		this.pluginName = pluginName;
		this.language = language;
		this.key = key;
		this.value = value;
	}

	public final String getPluginName() {
		return pluginName;
	}

	public final Language getLanguage() {
		return language;
	}

	public final String getKey() {
		return key;
	}

	public final String getValue() {
		return value;
	}
	
	@Override
	public String toString() {
		return "LanguageMessage[pluginName=" + pluginName + ",language=" + language + ",key=" + key + ",value=" + value + "]";
	}

}
