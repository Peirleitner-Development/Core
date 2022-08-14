package at.peirleitner.core;

import org.bukkit.plugin.java.JavaPlugin;

import at.peirleitner.core.command.local.CommandLanguage;
import at.peirleitner.core.listener.local.AsyncPlayerChatListener;
import at.peirleitner.core.listener.local.AsyncPlayerPreLoginListener;
import at.peirleitner.core.listener.local.PlayerCommandPreProcessListener;
import at.peirleitner.core.listener.local.PlayerJoinListener;
import at.peirleitner.core.listener.local.PlayerQuitListener;
import at.peirleitner.core.manager.LanguageManager;
import at.peirleitner.core.util.RunMode;
import at.peirleitner.core.util.local.LocalScoreboard;

public class SpigotMain extends JavaPlugin {

	private static SpigotMain instance;
	private LocalScoreboard localScoreboard;

	@Override
	public void onEnable() {

		if (!this.getDataFolder().exists()) {
			this.getDataFolder().mkdir();
		}

		// Initialize
		instance = this;
		Core.instance = new Core(RunMode.LOCAL);
		this.localScoreboard = new LocalScoreboard();
		this.registerMessages();
		
		// Commands
		new CommandLanguage();

		// Listener
		new PlayerJoinListener();
		new AsyncPlayerPreLoginListener();
		new PlayerQuitListener();
		new PlayerCommandPreProcessListener();
		new AsyncPlayerChatListener();

	}
	
	@Override
	public void onDisable() {
		
		if(Core.getInstance().getMySQL().isConnected()) {
			Core.getInstance().getMySQL().close();
		}
		
	}

	public static SpigotMain getInstance() {
		return instance;
	}
	
	public final LocalScoreboard getLocalScoreboard() {
		return this.localScoreboard;
	}
	
	private final void registerMessages() {
		
		LanguageManager languageManager = Core.getInstance().getLanguageManager();
		String pluginName = Core.getInstance().getPluginName();
		
		// Command
		languageManager.registerNewMessage(pluginName, "command.language.current-language", "&7Current language&8: &f{0}&7. Use &f/language <New Language> &7to change it. Available&8: &f{1}&7.");
		languageManager.registerNewMessage(pluginName, "command.language.language-updated", "&7Your language has been updated to &f{0}&7.");
		languageManager.registerNewMessage(pluginName, "command.language.language-not-found", "&cCould not validate language &e{0}&c. Available&8: &e{1}&c.");
		
	}

}
