package at.peirleitner.core.util.user;

import java.util.UUID;

import javax.annotation.Nonnull;

import at.peirleitner.core.Core;

/**
 * Common phrases used in plugins
 * @since 1.0.6
 * @author Markus Peirleitner (Rengobli)
 *
 */
public enum LanguagePhrase {

	YES("Yes"),
	NO("No"),
	SELECT("Select"),
	SELECTED("Selected"),
	LEFT_CLICK("Left-Click"),
	RIGHT_CLICK("Right-Click"),
	MIDDLE_CLICK("Middle-Click"),
	DROP_CLICK("Drop-Click"),
	PURCHASE("Purchased"),
	EQUIP("Equip"),
	UNQEUIP("Un-Equip");
	
	private final String defaultValue;
	
	private LanguagePhrase(@Nonnull String defaultValue) {
		this.defaultValue = defaultValue;
	}
	
	/**
	 * 
	 * @return Default value specified in {@link Language#ENGLISH}
	 * @since 1.0.6
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final String getDefaultValue() {
		return defaultValue;
	}

	public final String getTranslatedText(@Nonnull UUID uuid) {
		User user = Core.getInstance().getUserSystem().getUser(uuid);
		return Core.getInstance().getLanguageManager().getMessage(Core.getInstance().getPluginName(), user.getLanguage(), "phrase." + this.name().toLowerCase(), null);
	}
	
}
