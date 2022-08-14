package at.peirleitner.core.manager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import at.peirleitner.core.BungeeMain;
import at.peirleitner.core.Core;
import at.peirleitner.core.SpigotMain;
import at.peirleitner.core.util.LogType;
import at.peirleitner.core.util.RunMode;
import at.peirleitner.core.util.user.Language;
import at.peirleitner.core.util.user.PredefinedMessage;
import at.peirleitner.core.util.user.User;

/**
 * Manager for message handling.<br>
 * Messages registered via {@link #registerNewMessage(String, String)} will be
 * placed in the default language file {@link #getFile(Language)} with the
 * default language being {@link #getDefaultLanguage()}.
 * 
 * @since 1.0.0
 * @author Markus Peirleitner (Rengobli)
 *
 */
public class LanguageManager {

	public LanguageManager() {

		// Create default data
		this.createDirectory();

	}

	private final File getDataFolder() {
		return Core.getInstance().getRunMode() == RunMode.NETWORK ? BungeeMain.getInstance().getDataFolder()
				: SpigotMain.getInstance().getDataFolder();
	}

	private final File getMainDirectory() {
		return new File(this.getDataFolder() + "/messages");
	}

	private final File getDirectory(@Nonnull Language language) {
		return new File(this.getMainDirectory().getPath() + "/" + language.toString());
	}

	private final void createDirectory() {

		// Main Directory
		if (!this.getMainDirectory().exists()) {
			this.getMainDirectory().mkdir();
		}

		// Loop through languages
		for (Language language : Language.values()) {

			if (!this.getDirectory(language).exists()) {
				this.getDirectory(language).mkdir();
			}

		}

	}

	private final File getFile(@Nonnull String pluginName, @Nonnull Language language) {
		return new File(this.getDirectory(language) + "/" + pluginName + ".properties");
	}
	
	private final Properties getProperties(@Nonnull String pluginName, @Nonnull Language language) {
		Properties p = new Properties();
		try {
			p.load(new FileInputStream(this.getFile(pluginName, language)));
		} catch (FileNotFoundException e) {
			Core.getInstance().log(this.getClass(), LogType.WARNING, "Could not get Properties file for language '" + language.toString() + "': File not found.");
			return null;
		} catch (IOException e) {
			Core.getInstance().log(this.getClass(), LogType.WARNING, "Could not get Properties file for language '" + language.toString() + "':" + e.getMessage());
			return null;
		}
		return p;
	}

	private final void createProperties(@Nonnull String pluginName) {

		File f = this.getFile(pluginName, this.getDefaultLanguage());

		if (!f.exists()) {

			Core.getInstance().log(this.getClass(), LogType.DEBUG,
					"Could not find file '" + f.getPath() + "', attempting to create..");

			try {

				f.createNewFile();
				Core.getInstance().log(this.getClass(), LogType.DEBUG, "Successfully created a new Messages file.");
				
				// This will only set the default values for the default language
				this.setDefaultValues(pluginName);

			} catch (IOException e) {
				Core.getInstance().log(this.getClass(), LogType.ERROR,
						"Could not create new Messages file: " + e.getMessage());
			}
		} else {
//			Core.getInstance().log(this.getClass(), LogType.DEBUG,
//					"Did not attempt to create a new Messages file because one does already exist.");
		}

	}

	private final HashMap<String, String> getDefaultValues() {

		final HashMap<String, String> map = new HashMap<>();

		map.put("prefix", "&9Core> &f");
		map.put("main.action-may-only-be-performed-by-a-player", "&7This action may only be performed by a player.");
		map.put("main.no-permission", "&cYou are not allowed to perform this action.");
		map.put("main.action-requires-item-in-main-hand", "&7This action requires you to hold an item in your main hand.");

		return map;
	}
	
	public final String getPrefix(@Nonnull String pluginName, @Nonnull Language language) {
		return this.getMessage(pluginName, language, PredefinedMessage.PREFIX.getPath(), null);
	}

	/**
	 * 
	 * @return Default language for this manager.
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 * @apiNote Not to be confused with {@link Core#getDefaultLanguage()} that is
	 *          used for the {@link User}
	 */
	private final Language getDefaultLanguage() {
		return Language.ENGLISH;
	}

	private final void setDefaultValues(@Nonnull String pluginName) {

		Language defaultLanguage = this.getDefaultLanguage();

		// Only set default values for the Core Instance
		if (!pluginName.equals(Core.getInstance().getPluginName()))
			return;

		if (!this.getFile(pluginName, defaultLanguage).exists()) {
			Core.getInstance().log(this.getClass(), LogType.DEBUG,
					"Did not attempt to set default values because Messages file does not exist.");
			return;
		}
		
		Properties p = this.getProperties(pluginName, defaultLanguage);

		for (Map.Entry<String, String> entry : this.getDefaultValues().entrySet()) {

			if (!this.isMessageRegistered(pluginName, defaultLanguage, entry.getKey())) {
				p.setProperty(entry.getKey(), entry.getValue());
				Core.getInstance().log(this.getClass(), LogType.DEBUG,
						"Messages: Added default key '" + entry.getKey() + "' with value '" + entry.getValue() + "' (Main Default Values).");
			}

		}

		this.save(pluginName);

	}

	private final boolean save(@Nonnull String pluginName) {

		Language language = this.getDefaultLanguage();

		try {
			this.getProperties(pluginName, language).store(new FileWriter(this.getFile(pluginName, language)),
					"Last update on " + new Date(System.currentTimeMillis()));
			return true;
		} catch (IOException e) {
			Core.getInstance().log(this.getClass(), LogType.ERROR, "Could not save Messages file: " + e.getMessage());
			return false;
		}

	}

	/**
	 * Register a new message for the {@link #getDefaultLanguage()}.
	 * @param pluginName - Name of the plugin
	 * @param key - Unique identifier
	 * @param value - Message
	 * @return If a new message has been registered
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 * @see #isMessageRegistered(String, Language, String)
	 * @apiNote This will fail if the message has already been registered
	 */
	public final boolean registerNewMessage(@Nonnull String pluginName, @Nonnull String key, @Nonnull String value) {

		if (this.isMessageRegistered(pluginName, this.getDefaultLanguage(), key))
			return false;

		this.getProperties(pluginName, this.getDefaultLanguage()).setProperty(key, value);
		return this.save(pluginName);
	}

	public final boolean isMessageRegistered(@Nonnull String pluginName, @Nonnull Language language, @Nonnull String key) {
		return this.getProperties(pluginName, language).getProperty(key) == null ? false : true;
	}

	public final String getMessage(@Nonnull String pluginName, @Nonnull Language language, @Nonnull String key, @Nullable List<String> replacements) {

		String message = this.getProperties(pluginName, language).getProperty(key);

		if (replacements != null && !replacements.isEmpty()) {

			for (int i = 0; i < replacements.size(); i++) {
				message = message.replace("{" + i + "}", replacements.get(i));
			}

		}

		return message;
	}

}
