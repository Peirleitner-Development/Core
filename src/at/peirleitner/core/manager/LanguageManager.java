package at.peirleitner.core.manager;

import java.io.File;
import java.io.FileInputStream;
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

	private boolean initialized = false;
	private final String pluginName;
	private Properties messages;

	public LanguageManager(@Nonnull String pluginName) {

		// Initialize
		this.pluginName = pluginName;

		// Create default data
		this.createDirectory();
		this.createProperties();
		this.loadProperties(this.getDefaultLanguage());

	}

	public final String getPluginName() {
		return this.pluginName;
	}
	
	public final void setPrefix(@Nonnull String prefix) {
		this.registerNewMessage(PredefinedMessage.PREFIX.getPath(), prefix);
	}
	
	public final String getPrefix() {
		return this.getMessage(PredefinedMessage.PREFIX.getPath(), null);
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

	private final File getFile(@Nonnull Language language) {
		return new File(this.getDirectory(language) + "/" + this.getPluginName() + ".properties");
	}

	private final void createProperties() {

		File f = this.getFile(this.getDefaultLanguage());

		if (!f.exists()) {

			Core.getInstance().log(this.getClass(), LogType.DEBUG,
					"Could not find file '" + f.getPath() + "', attempting to create..");

			try {

				f.createNewFile();
				Core.getInstance().log(this.getClass(), LogType.DEBUG, "Successfully created a new Messages file.");

				this.loadProperties(this.getDefaultLanguage());

			} catch (IOException e) {
				Core.getInstance().log(this.getClass(), LogType.ERROR,
						"Could not create new Messages file: " + e.getMessage());
			}
		} else {
//			Core.getInstance().log(this.getClass(), LogType.DEBUG,
//					"Did not attempt to create a new Messages file because one does already exist.");
		}

	}

	private final void loadProperties(@Nonnull Language language) {

		if (this.getFile(language) == null || !this.getFile(language).exists()) {
			Core.getInstance().log(this.getClass(), LogType.DEBUG,
					"Did not attempt to load messages file because none does exist.");
			return;
		}

		if (this.initialized) {
			Core.getInstance().log(this.getClass(), LogType.DEBUG,
					"Did not attempt to load messages file because it has already been initialized.");
			return;
		}

		try {

			this.messages = new Properties();
			this.messages.load(new FileInputStream(this.getFile(language)));
			this.setDefaultValues();

			this.initialized = true;

		} catch (IOException e) {
			Core.getInstance().log(this.getClass(), LogType.ERROR,
					"Error while attempting to load Messages file: " + e.getMessage());
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

	private final Properties getMessages() {
		return this.messages;
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

	private final void setDefaultValues() {

		Language defaultLanguage = this.getDefaultLanguage();

		// Only set default values for the Core Instance
		if (!pluginName.equals(Core.getInstance().getPluginName()))
			return;

		if (this.getFile(defaultLanguage) == null || !this.getFile(defaultLanguage).exists()) {
			Core.getInstance().log(this.getClass(), LogType.DEBUG,
					"Did not attempt to set default values because Messages file does not exist.");
			return;
		}

		for (Map.Entry<String, String> entry : this.getDefaultValues().entrySet()) {

			if (!this.isMessageRegistered(entry.getKey())) {
				this.getMessages().setProperty(entry.getKey(), entry.getValue());
				Core.getInstance().log(this.getClass(), LogType.DEBUG,
						"Messages: Added default key '" + entry.getKey() + "' with value '" + entry.getValue() + "' (Main Default Values).");
			}

		}

		this.save(pluginName);

	}

	private final boolean save(@Nonnull String pluginName) {

		Language language = this.getDefaultLanguage();

		try {
			this.getMessages().store(new FileWriter(this.getFile(language)),
					"Last update on " + new Date(System.currentTimeMillis()));
			return true;
		} catch (IOException e) {
			Core.getInstance().log(this.getClass(), LogType.ERROR, "Could not save Messages file: " + e.getMessage());
			return false;
		}

	}

	public final boolean registerNewMessage(@Nonnull String key, @Nonnull String value) {

		if (this.isMessageRegistered(key))
			return false;

		this.getMessages().setProperty(key, value);
		return this.save(pluginName);
	}

	public final boolean isMessageRegistered(@Nonnull String key) {
		return this.getMessages().getProperty(key) == null ? false : true;
	}

	public final String getMessage(@Nonnull String key, @Nullable List<String> replacements) {

		String message = this.getMessages().getProperty(key);

		if (replacements != null && !replacements.isEmpty()) {

			for (int i = 0; i < replacements.size(); i++) {
				message = message.replace("{" + i + "}", replacements.get(i));
			}

		}

		return message;
	}

}
