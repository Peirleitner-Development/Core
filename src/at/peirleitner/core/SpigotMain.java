package at.peirleitner.core;

import java.util.Arrays;

import javax.annotation.Nonnull;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import at.peirleitner.core.command.local.CommandCore;
import at.peirleitner.core.command.local.CommandLanguage;
import at.peirleitner.core.command.local.CommandMaintenance;
import at.peirleitner.core.command.local.CommandMotd;
import at.peirleitner.core.listener.local.AsyncPlayerChatListener;
import at.peirleitner.core.listener.local.AsyncPlayerPreLoginListener;
import at.peirleitner.core.listener.local.LeavesDecayListener;
import at.peirleitner.core.listener.local.PlayerCommandPreProcessListener;
import at.peirleitner.core.listener.local.PlayerJoinListener;
import at.peirleitner.core.listener.local.PlayerQuitListener;
import at.peirleitner.core.listener.local.ServerListPingListener;
import at.peirleitner.core.manager.GUIManager;
import at.peirleitner.core.manager.LanguageManager;
import at.peirleitner.core.util.RunMode;
import at.peirleitner.core.util.local.LocalScoreboard;
import at.peirleitner.core.util.user.PredefinedMessage;
import at.peirleitner.core.util.user.User;

public class SpigotMain extends JavaPlugin {

	private static SpigotMain instance;
	private LocalScoreboard localScoreboard;
	
	private GUIManager guiManager;

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
		
		// Manager
		this.guiManager = new GUIManager();
		
		// Commands
		new CommandLanguage();
		new CommandCore();
		new CommandMotd();
		new CommandMaintenance();

		// Listener
		new PlayerJoinListener();
		new AsyncPlayerPreLoginListener();
		new PlayerQuitListener();
		new PlayerCommandPreProcessListener();
		new AsyncPlayerChatListener();
		new ServerListPingListener();
		new LeavesDecayListener();
		
		// Run
		this.startTabHeaderRunnable();

	}
	
	@Override
	public void onDisable() {
		
		if(Core.getInstance().getMySQL() != null && Core.getInstance().getMySQL().isConnected()) {
			Core.getInstance().getMySQL().close();
		}
		
	}

	public static SpigotMain getInstance() {
		return instance;
	}
	
	public final GUIManager getGUIManager() {
		return this.guiManager;
	}
	
	public final LocalScoreboard getLocalScoreboard() {
		return this.localScoreboard;
	}
	
	private final void registerMessages() {
		
		LanguageManager languageManager = Core.getInstance().getLanguageManager();
		String pluginName = Core.getInstance().getPluginName();
		
		// Notify
		languageManager.registerNewMessage(pluginName, "notify.motd.update", "&7[&9+&7] &9{0} &7updated the MOTD&8:\n"
				+ "&7[&9+&7] &9{1}\n"
				+ "&7[&9+&7] &9{2}");
		
		// Other
		languageManager.registerNewMessage(pluginName, "maintenance.kick", "&f&l{0}\n"
				+ "&c&lSERVER MAINTENANCE\n\n"
				+ "&7Maintenance mode has been enabled.\n"
				+ "&7Please visit our website for further details about this incident.\n\n"
				+ "&7Web&8: &e{1}");
		languageManager.registerNewMessage(pluginName, "maintenance.broadcast.on", "&9{0} &7enabled maintenance mode");
		languageManager.registerNewMessage(pluginName, "maintenance.broadcast.off", "&9{0} &7disabled maintenance mode");
		
		// GUI
		languageManager.registerNewMessage(pluginName, "gui.gui-builder.item.current-page", "&7Current Page&7: &9{0}&7/&9{1}");
		languageManager.registerNewMessage(pluginName, "gui.gui-builder.item.next-page", "&7Next Page");
		languageManager.registerNewMessage(pluginName, "gui.gui-builder.item.previous-page", "&7Previous Page");
		
		// Command
		languageManager.registerNewMessage(pluginName, "command.core.syntax", "&fCore Management\n"
				+ "&f/core\n"
				+ "  &9loadDefaultSaveTypes &7- &fLoad default SaveTypes");
		languageManager.registerNewMessage(pluginName, "command.core.loadDefaultSaveTypes.info", "&7Loading of default SaveTypes has been finished, see console for further details.");
		
		languageManager.registerNewMessage(pluginName, "command.language.current-language", "&7Current language&8: &9{0}&7. Use &9/language <New Language> &7to change it. Available&8: &9{1}&7.");
		languageManager.registerNewMessage(pluginName, "command.language.language-updated", "&7Your language has been updated to &9{0}&7.");
		languageManager.registerNewMessage(pluginName, "command.language.language-not-found", "&cCould not validate language &e{0}&c. Available&8: &e{1}&c.");
		
		languageManager.registerNewMessage(pluginName, "command.motd.syntax", "&7Syntax&8: &9/motd [set/update] [New MOTD]");
		languageManager.registerNewMessage(pluginName, "command.motd.info.no-motd-set", "&7No MOTD has been set.");
		languageManager.registerNewMessage(pluginName, "command.motd.info.success", "&7Current MOTD with last change by &9{2} &7on &9{3}&8:\n"
				+ "&9{0}\n"
				+ "&9{1}");
		languageManager.registerNewMessage(pluginName, "command.motd.update.error.caching-disabled", "&7Could not force-update MOTD because caching has been disabled.");
		languageManager.registerNewMessage(pluginName, "command.motd.update.error.cant-get-motd", "&cCould not get updated MOTD, please see console for details.");
		languageManager.registerNewMessage(pluginName, "command.motd.update.success", "&7Successfully force-updated the MOTD.");
		
		languageManager.registerNewMessage(pluginName, "command.motd.set.success", "&7Successfully updated the MOTD&8:\n"
				+ "{0}\n"
				+ "{1}");
		languageManager.registerNewMessage(pluginName, "command.motd.set.error.sql", "&cCould not update MOTD, please see console for details.");
		
		languageManager.registerNewMessage(pluginName, "command.maintenance.syntax", "&7Syntax&8: &9/maintenance <list/add/remove/on/off/toggle> [Player]");
		languageManager.registerNewMessage(pluginName, "command.maintenance.list.success.empty", "&7No players are currently placed on the maintenance list.");
		languageManager.registerNewMessage(pluginName, "command.maintenance.list.success.listing-in-chat", "&7The following players are on the maintenance list&8:");
		languageManager.registerNewMessage(pluginName, "command.maintenance.on.success", "&7Maintenance mode has been enabled.");
		languageManager.registerNewMessage(pluginName, "command.maintenance.on.error", "&cMaintenance mode could not be enabled, see console for details.");
		languageManager.registerNewMessage(pluginName, "command.maintenance.off.success", "&7Maintenance mode has been disabled.");
		languageManager.registerNewMessage(pluginName, "command.maintenance.off.error", "&cMaintenance mode could not be disabled, see console for details.");
		languageManager.registerNewMessage(pluginName, "command.maintenance.whitelist.error.player-not-registered", "&cThe player &e{0} &cis not registered.");
		languageManager.registerNewMessage(pluginName, "command.maintenance.add.success", "&7Successfully added &9{0} &7to the maintenance list.");
		languageManager.registerNewMessage(pluginName, "command.maintenance.add.error", "&cCould not add &e{0} &cto the maintenance list, see console for more details.");
		languageManager.registerNewMessage(pluginName, "command.maintenance.remove.success", "&7Successfully removed &9{0} &7from the maintenance list.");
		languageManager.registerNewMessage(pluginName, "command.maintenance.remove.error", "&cCould not remove &e{0} &cfrom the maintenance list, see console for more details.");
		
		// Listener
		languageManager.registerNewMessage(pluginName, "listener.player-command-pre-process.unknown-command", "&7The command &9{0} &7could not be validated.");
		languageManager.registerNewMessage(pluginName, "listener.player-join.operator-join-action.disallow", "&cOperators are not allowed to join this server.");
		languageManager.registerNewMessage(pluginName, "listener.player-join.operator-join-action.remove-status", "&7Your operator status has automatically been removed for security reasons.");
		languageManager.registerNewMessage(pluginName, "listener.async-player-pre-login.maintenance", "&f&l{0}\n"
				+ "&c&lSERVER MAINTENANCE\n\n"
				+ "&7Only staff members are currently allowed to join the server.\n"
				+ "&7Please visit our website for further details about this incident.\n\n"
				+ "&7Web&8: &e{1}");
		
	}
	
	private final void startTabHeaderRunnable() {
		
		if(!Core.getInstance().isNetwork() && Core.getInstance().getSettingsManager().isUseTabHeader()) {
			
			new BukkitRunnable() {
				
				@Override
				public void run() {
					
					Bukkit.getOnlinePlayers().forEach(player -> {
						
						User user = Core.getInstance().getUserSystem().getUser(player.getUniqueId());
						player.setPlayerListHeaderFooter(getTabHeader(user), getTabFooter(user));
						
					});
					
				}
			}.runTaskTimerAsynchronously(this, 20L * 2, 20L * 2);
			
		}
		
	}
	
	private final String getTabHeader(@Nonnull User user) {
		return Core.getInstance().getLanguageManager().getMessage(Core.getInstance().getPluginName(), user.getLanguage(), PredefinedMessage.TAB_HEADER.getPath(), Arrays.asList(
				Core.getInstance().getSettingsManager().getServerName(),
				"" + Bukkit.getOnlinePlayers().size(),
				"" + Bukkit.getMaxPlayers(),
				Bukkit.getServer().getName()
				));
	}
	
	private final String getTabFooter(@Nonnull User user) {
		return Core.getInstance().getLanguageManager().getMessage(Core.getInstance().getPluginName(), user.getLanguage(), PredefinedMessage.TAB_FOOTER.getPath(), null);
	}

}
