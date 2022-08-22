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
import net.md_5.bungee.api.ChatColor;

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

	private final String getMessageNotFoundString(@Nonnull String pluginName, @Nonnull String key) {
		return "&cCould not get message key '&e" + key + "&c' for plugin '&e" + pluginName
				+ "&c', please contact the administrator.";
	}
	
	/**
	 * Hosting that powers this server. Message can be replaced inside the language file.
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 */
	private final String POWERED_BY_NAME = "Nayola.net";

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
			
			if(language != this.getDefaultLanguage()) {
//				Core.getInstance().log(this.getClass(), LogType.DEBUG, "Plugin '" + pluginName + "' does not offer a translation for the language '" + language.toString() + "'.");
				return this.getProperties(pluginName, this.getDefaultLanguage());
			}
			
			this.createProperties(pluginName);
			return this.getProperties(pluginName, language);
		} catch (IOException e) {
			Core.getInstance().log(this.getClass(), LogType.WARNING,
					"Could not get Properties file for language '" + language.toString() + "':" + e.getMessage());
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
		map.put("main.action-requires-item-in-main-hand",
				"&7This action requires you to hold an item in your main hand.");
		map.put("main.world-does-not-exist", "&cThe specified world doesn't exist.");
		map.put("main.target-player-not-found", "&cThe specified player could not be found.");
		map.put("main.cant-target-yourself", "&cYou can't target yourself for this action.");
		map.put("tab.header", "&8&m---------------------------------------\n" + "&9{0}\n\n"
				+ "&7Global Players online&8: &f{1}&7/&f{2}\n" + "&7Currently connected to&8: &f{3}\n");
		map.put("tab.footer", "\n" + "&7Server Help&8: &f/help\n" + "&7Online-Store&8: &f/store\n"
				+ "&7Request Support&8: &f/support\n\n"
				+ "&7Powered by &9" + this.POWERED_BY_NAME
				+ "\n&8&m---------------------------------------");

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
				Core.getInstance().log(this.getClass(), LogType.DEBUG, "Messages: Added default key '" + entry.getKey()
						+ "' with value '" + entry.getValue() + "' (Main Default Values).");
			}

		}

		this.save(pluginName, p);

	}

	private final boolean save(@Nonnull String pluginName, @Nonnull Properties properties) {

		Language language = this.getDefaultLanguage();

		try {
			properties.store(new FileWriter(this.getFile(pluginName, language)),
					"Last update on " + new Date(System.currentTimeMillis()));
			return true;
		} catch (IOException e) {
			Core.getInstance().log(this.getClass(), LogType.ERROR, "Could not save Messages file: " + e.getMessage());
			return false;
		}

	}

	/**
	 * Register a new message for the {@link #getDefaultLanguage()}.
	 * 
	 * @param pluginName - Name of the plugin
	 * @param key        - Unique identifier
	 * @param value      - Message
	 * @return If a new message has been registered
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 * @see #isMessageRegistered(String, Language, String)
	 * @apiNote This will fail if the message has already been registered
	 */
	public final boolean registerNewMessage(@Nonnull String pluginName, @Nonnull String key, @Nonnull String value) {

		if (this.isMessageRegistered(pluginName, this.getDefaultLanguage(), key))
			return false;

		Properties p = this.getProperties(pluginName, this.getDefaultLanguage());
		p.setProperty(key, value);
		return this.save(pluginName, p);
	}

	public final boolean isMessageRegistered(@Nonnull String pluginName, @Nonnull Language language,
			@Nonnull String key) {
		return this.getProperties(pluginName, language).getProperty(key) == null ? false : true;
	}
	
	public final String getMessage(@Nonnull PredefinedMessage predefinedMessage) {
		return this.getMessage(Core.getInstance().getPluginName(), this.getDefaultLanguage(), predefinedMessage.getPath(), null);
	}

	public final String getMessage(@Nonnull String pluginName, @Nonnull Language language, @Nonnull String key,
			@Nullable List<String> replacements) {

		// Replace language to default one if the specified translation isn't available.
		if (this.getFile(pluginName, language) == null) {
			language = this.getDefaultLanguage();
			Core.getInstance().log(this.getClass(), LogType.DEBUG,
					"Could not get message key '" + key + "' of plugin '" + pluginName + "' for language '"
							+ language.toString() + "', replacing it with default language '"
							+ this.getDefaultLanguage().toString() + "'.");
		}

		String message = this.getProperties(pluginName, language).getProperty(key);

		// Load default messages if the plugin is the core itself
		if (message == null) {

			if (pluginName.equals(Core.getInstance().getPluginName())) {
				this.setDefaultValues(pluginName);
				message = this.getProperties(pluginName, language).getProperty(key);
			} else {
				message = this.getMessageNotFoundString(pluginName, key);
				Core.getInstance().log(this.getClass(), LogType.WARNING,
						"Could not get message key '" + key + "' for plugin '" + pluginName + "': Non existent.");
			}

		}

		// Replace if not empty
		if (replacements != null && !replacements.isEmpty()) {

			for (int i = 0; i < replacements.size(); i++) {
				message = message.replace("{" + i + "}", replacements.get(i));
			}

		}

		return ChatColor.translateAlternateColorCodes('&', message);
	}
	
	public final void broadcastMessage(@Nonnull String pluginName, @Nonnull String key, @Nullable List<String> replacements, @Nonnull boolean prefix) {
		
		if(Core.getInstance().getRunMode() == RunMode.LOCAL) {
			
			for(org.bukkit.entity.Player all : org.bukkit.Bukkit.getOnlinePlayers()) {
				
				User user = Core.getInstance().getUserSystem().getUser(all.getUniqueId());
				user.sendMessage(pluginName, key, replacements, prefix);
				
			}
			
		}
		
	}
	
	public final void sendMessage(@Nonnull org.bukkit.command.CommandSender cs, @Nonnull String pluginName, @Nonnull String key, @Nullable List<String> replacements, @Nonnull boolean prefix) {
		
		if(cs instanceof org.bukkit.entity.Player) {
			
			org.bukkit.entity.Player p = (org.bukkit.entity.Player) cs;
			User user = Core.getInstance().getUserSystem().getUser(p.getUniqueId());
			user.sendMessage(pluginName, key, replacements, prefix);
			return;
			
		}
		
		String message = this.getMessage(pluginName, getDefaultLanguage(), key, replacements);
		cs.sendMessage((prefix ? getPrefix(pluginName, getDefaultLanguage()) : "") + message);
		
	}
	
	public final void notifyStaff(@Nonnull String pluginName, @Nonnull String key, @Nullable List<String> replacements) {
		//TODO: To all that have a special permission
	}

}
