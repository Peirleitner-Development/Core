package at.peirleitner.core.manager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import at.peirleitner.core.BungeeMain;
import at.peirleitner.core.Core;
import at.peirleitner.core.util.LogType;
import at.peirleitner.core.util.RunMode;
import at.peirleitner.core.util.user.CorePermission;
import at.peirleitner.core.util.user.Language;
import at.peirleitner.core.util.user.LanguageMessage;
import at.peirleitner.core.util.user.PredefinedMessage;
import at.peirleitner.core.util.user.User;
import net.md_5.bungee.api.ChatColor;

/**
 * Manager for message handling.<br>
 * Messages registered via {@link #registerNewMessage(String, String)} will be
 * placed in the default language file {@link #getFile(Language)} with the
 * default language being {@link #getDefaultLanguage()}.<br>
 * <br>
 * 
 * Drastic changes have been made in v1.0.5, not all methods may be available
 * before that.
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
	 * Hosting that powers this server. Message can be replaced inside the language
	 * file.
	 * 
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 */
	private final String POWERED_BY_NAME = "Nayola.net";
	private Collection<LanguageMessage> messages;

	public LanguageManager() {

		// Create default data
		this.createDirectory();
		this.messages = new ArrayList<>();

		// Load Messages
		this.loadMessages();

	}

	private final File getDataFolder() {
		return Core.getInstance().getRunMode() == RunMode.NETWORK ? BungeeMain.getInstance().getDataFolder()
				: at.peirleitner.core.SpigotMain.getInstance().getDataFolder();
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

	}

	/**
	 * Load messages into cache
	 * 
	 * @return If all messages have been loaded
	 * @since 1.0.5
	 * @author Markus Peirleitner (Rengobli)
	 */
	private final boolean loadMessages() {

		// Loop through available languages
		for (Language language : Language.values()) {

			File directory = this.getDirectory(language);

			if (!directory.exists()) {
				directory.mkdir();
			}

			// Loop through files for that language
			for (File f : this.getFiles(language)) {

				Properties p = new Properties();
				String pluginName = f.getName().split(".properties")[0];

				try {

					p.load(new FileInputStream(f));

					for (Map.Entry<Object, Object> entry : p.entrySet()) {

						String key = entry.getKey().toString();
						String value = entry.getValue().toString();

						LanguageMessage message = new LanguageMessage(pluginName, language, key, value);
						this.getMessages().add(message);

//						Core.getInstance().log(this.getClass(), LogType.DEBUG, "Cached message " + message.toString());

					}

				} catch (FileNotFoundException e) {
					Core.getInstance().log(this.getClass(), LogType.ERROR, "Could not load messages for language "
							+ language.toString() + " because the file does not exist. Error: " + e.getMessage());
					return false;
				} catch (IOException e) {
					Core.getInstance().log(this.getClass(), LogType.WARNING,
							"Could not get Properties file for language '" + language.toString() + "':"
									+ e.getMessage());
					return false;
				}

			}

		}

		Collection<String> plugins = new ArrayList<>();
		Collection<Language> languages = new ArrayList<>(Language.values().length);
		int messages = 0;

		for (LanguageMessage lm : this.getMessages()) {

			if (!plugins.contains(lm.getPluginName())) {
				plugins.add(lm.getPluginName());
			}

			if (!languages.contains(lm.getLanguage())) {
				languages.add(lm.getLanguage());
			}

			messages++;

		}

		Core.getInstance().log(this.getClass(), LogType.DEBUG,
				"Loaded " + messages + " Messages for " + plugins.size() + " different Plugins on " + languages.size()
						+ "/" + Language.values().length + " different languages.");
		return true;
	}

	/**
	 * 
	 * @param language - Language
	 * @return All message files for the specified language
	 * @since 1.0.5
	 * @author Markus Peirleitner (Rengobli)
	 */
	private final File[] getFiles(@Nonnull Language language) {
		File f = new File(this.getMainDirectory() + "/" + language);
		return f.listFiles();
	}

	/**
	 * @return All messages that have been loaded into the cache by
	 *         {@link #loadMessages()}
	 * @since 1.0.5
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final Collection<LanguageMessage> getMessages() {
		return this.messages;
	}

	private final File getFile(@Nonnull String pluginName, @Nonnull Language language) {
		return new File(this.getDirectory(language) + "/" + pluginName + ".properties");
	}

	private final Properties getProperties(@Nonnull String pluginName, @Nonnull Language language) {
		Properties p = new Properties();
		try {
			p.load(new FileInputStream(this.getFile(pluginName, language)));
		} catch (FileNotFoundException e) {

			if (language != this.getDefaultLanguage()) {
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
		map.put("main.player-not-registered", "&cThe specified player does not have a Core-Account.");
		map.put("main.cant-target-yourself", "&cYou can't target yourself for this action.");
		map.put("main.invalid-id", "&cPlease enter a valid id.");
		map.put("main.invalid-saveType", "&cCould not get the specified SaveType.");
		map.put("main.not-enough-economy", "&cYour Account does not provide the required amount of balance.");
		map.put("main.inventory-full", "&cYour Inventory is full.");
		map.put("tab.header", "&8&m---------------------------------------\n" + "&9{0}\n\n"
				+ "&7Global Players online&8: &f{1}&7/&f{2}\n" + "&7Currently connected to&8: &f{3}\n");
		map.put("tab.footer",
				"\n" + "&7Server Help&8: &f/help\n" + "&7Online-Store&8: &f/store\n"
						+ "&7Request Support&8: &f/support\n\n" + "&7Powered by &9" + this.POWERED_BY_NAME
						+ "\n&8&m---------------------------------------");

		return map;
	}

	public final String getPrefix(@Nonnull String pluginName, @Nonnull Language language) {
		return this.getMessage(pluginName, language, PredefinedMessage.PREFIX.getPath(), null);
	}
	
	/**
	 * 
	 * @return Prefix for system notifications
	 * @since 1.0.8
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final String getNotifyPrefix() {
		return ChatColor.GRAY + "[" + ChatColor.RED + "✦" + ChatColor.GRAY + "]" + ChatColor.RESET + " ";
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
//				Core.getInstance().log(this.getClass(), LogType.DEBUG, "Messages: Added default key '" + entry.getKey()
//						+ "' with value '" + entry.getValue() + "' (Main Default Values).");
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

		// Cache message if not registered
		if (this.getLanguageMessage(pluginName, getDefaultLanguage(), key, null) == null) {
			LanguageMessage message = new LanguageMessage(pluginName, getDefaultLanguage(), key, value);
			this.getMessages().add(message);
		}

		return this.save(pluginName, p);
	}

	public final boolean isMessageRegistered(@Nonnull String pluginName, @Nonnull Language language,
			@Nonnull String key) {
		return this.getProperties(pluginName, language).getProperty(key) == null ? false : true;
	}

	public final String getMessage(@Nonnull PredefinedMessage predefinedMessage) {
		
		if(Core.getInstance().getRunMode() == RunMode.LOCAL) {
			
			at.peirleitner.core.api.local.UserMessageGetEvent event = new at.peirleitner.core.api.local.UserMessageGetEvent(predefinedMessage);
			at.peirleitner.core.SpigotMain.getInstance().getServer().getPluginManager().callEvent(event);
			
			return event.getMessage();
			
		} else {
			
			return this.getMessage(Core.getInstance().getPluginName(), this.getDefaultLanguage(),
					predefinedMessage.getPath(), null);
			
		}
		
	}

	public final LanguageMessage getLanguageMessage(@Nonnull String pluginName, @Nonnull Language language,
			@Nonnull String key, @Nullable List<String> replacements) {

		LanguageMessage message = null;

		// Try to get it for main language
		for (LanguageMessage lm : this.getMessages()) {

			if (lm.getPluginName().equalsIgnoreCase(pluginName) && lm.getLanguage() == language
					&& lm.getKey().equalsIgnoreCase(key)) {
				message = lm;
				break;
			}

		}

		// Set to default system language if message is null here
		if (message == null) {

			for (LanguageMessage lm : this.getMessages()) {

				if (lm.getPluginName().equalsIgnoreCase(pluginName) && lm.getLanguage() == this.getDefaultLanguage()
						&& lm.getKey().equalsIgnoreCase(key)) {
					message = lm;
					break;
				}

			}
		}

		return message;

	}

	public final String getMessage(@Nonnull String pluginName, @Nonnull Language language, @Nonnull String key,
			@Nullable List<String> replacements) {

		LanguageMessage message = this.getLanguageMessage(pluginName, language, key, replacements);

		// Attempt to load from default file if not existing in here
		if (message == null) {

			Properties p = this.getProperties(pluginName, this.getDefaultLanguage());
			String m = p.getProperty(key);

			if (m != null) {
				message = new LanguageMessage(pluginName, this.getDefaultLanguage(), key, m);
				this.getMessages().add(message);
			}

		}

		// Message does not exist at all
		if (message == null) {
			return this.getMessageNotFoundString(pluginName, key);
		}

		return ChatColor.translateAlternateColorCodes('&', message.getMessage(replacements));

	}

	public final void broadcastMessage(@Nonnull String pluginName, @Nonnull String key,
			@Nullable List<String> replacements, @Nonnull boolean prefix) {

		if (Core.getInstance().getRunMode() == RunMode.LOCAL) {

			for (org.bukkit.entity.Player all : org.bukkit.Bukkit.getOnlinePlayers()) {

				User user = Core.getInstance().getUserSystem().getUser(all.getUniqueId());
				user.sendMessage(pluginName, key, replacements, prefix);

			}

		} else {
			// TODO: Network-Mode
		}

	}

	public final void sendMessage(@Nonnull org.bukkit.command.CommandSender cs, @Nonnull String pluginName,
			@Nonnull String key, @Nullable List<String> replacements, @Nonnull boolean prefix) {

		if (cs instanceof org.bukkit.entity.Player) {

			org.bukkit.entity.Player p = (org.bukkit.entity.Player) cs;
			User user = Core.getInstance().getUserSystem().getUser(p.getUniqueId());
			user.sendMessage(pluginName, key, replacements, prefix);
			return;

		}

		String message = this.getMessage(pluginName, getDefaultLanguage(), key, replacements);
		cs.sendMessage((prefix ? getPrefix(pluginName, getDefaultLanguage()) : "") + message);

	}

	public final void notifyStaff(@Nonnull String pluginName, @Nonnull String key, @Nullable List<String> replacements,
			@Nonnull boolean prefix) {

		if (Core.getInstance().getRunMode() == RunMode.LOCAL) {

			for (org.bukkit.entity.Player all : org.bukkit.Bukkit.getOnlinePlayers()) {

				if (!all.hasPermission(CorePermission.NOTIFY_STAFF.getPermission()))
					continue;

				User user = Core.getInstance().getUserSystem().getUser(all.getUniqueId());
				user.sendMessage(pluginName, key, replacements, prefix);

			}

		} else {
			// TODO: Network-Mode
		}

	}

	/**
	 * 
	 * @param pluginName
	 * @param key
	 * @param replacements
	 * @param prefix
	 * @since 1.0.14
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final void notifyStaffAsync(@Nonnull String pluginName, @Nonnull String key, @Nullable List<String> replacements,
			@Nonnull boolean prefix) {

		if (Core.getInstance().getRunMode() == RunMode.LOCAL) {

			for (org.bukkit.entity.Player all : org.bukkit.Bukkit.getOnlinePlayers()) {

				if (!all.hasPermission(CorePermission.NOTIFY_STAFF.getPermission()))
					continue;

				User user = Core.getInstance().getUserSystem().getUser(all.getUniqueId());
				user.sendAsyncMessage(pluginName, key, replacements, prefix);

			}

		} else {
			// TODO: Network-Mode
		}

	}

}
