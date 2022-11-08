package at.peirleitner.core;

import java.util.Arrays;

import javax.annotation.Nonnull;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import at.peirleitner.core.command.local.CommandCore;
import at.peirleitner.core.command.local.CommandEconomy;
import at.peirleitner.core.command.local.CommandLanguage;
import at.peirleitner.core.command.local.CommandLicense;
import at.peirleitner.core.command.local.CommandLog;
import at.peirleitner.core.command.local.CommandMaintenance;
import at.peirleitner.core.command.local.CommandMoney;
import at.peirleitner.core.command.local.CommandMotd;
import at.peirleitner.core.command.local.CommandPay;
import at.peirleitner.core.command.local.CommandSlot;
import at.peirleitner.core.command.local.CommandTeleport;
import at.peirleitner.core.command.local.CommandWorld;
import at.peirleitner.core.listener.local.AsyncPlayerChatListener;
import at.peirleitner.core.listener.local.AsyncPlayerPreLoginListener;
import at.peirleitner.core.listener.local.LeavesDecayListener;
import at.peirleitner.core.listener.local.LogMessageCreateListener;
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
		new CommandLicense();
		new CommandMoney();
		new CommandEconomy();
		new CommandSlot();
		new CommandPay();
		new CommandLog();
		new CommandWorld();
		new CommandTeleport();
		
		// Listener
		new PlayerJoinListener();
		new AsyncPlayerPreLoginListener();
		new PlayerQuitListener();
		new PlayerCommandPreProcessListener();
		new AsyncPlayerChatListener();
		new ServerListPingListener();
		new LeavesDecayListener();
		new LogMessageCreateListener();
		
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
		
		languageManager.registerNewMessage(pluginName, "command.license.syntax", "&7Syntax&8: &9/license\n"
				+ "  &9No Argument &f- &7View your own Licenses\n"
				+ "  &9list &f- &7List all available Master Licenses\n"
				+ "  &9create <SaveType ID> <Name> &f- &7Create a new Master License\n"
				+ "  &9expire <ID> &f- &7Expire a Master License\n"
				+ "  &9permanent <ID> &f- &7Set a Master License's duration to permanent\n"
				+ "  &9grant <User> <ID> [Hours] &f- &7Grant a User a new License\n"
				+ "  &9has <User> <ID> &f- &7Check if a User has a specific License\n"
				+ "  &9revert <User> <ID> &f- &7Expire a User License");
		languageManager.registerNewMessage(pluginName, "command.license.your-licenses.pretext", "&7Your Licenses&8:");
		languageManager.registerNewMessage(pluginName, "command.license.your-licenses.license", "&8- &9{0} &7| Received&8: &9{1}&8, &7Valid until&8: &9{2}");
		languageManager.registerNewMessage(pluginName, "command.license.main.no-master-license-found-with-given-id", "&7Could not find a MasterLicense with the ID of &9{0}&7.");
		languageManager.registerNewMessage(pluginName, "command.license.main.id-has-to-be-an-integer", "&cThe argument &e{0} &cis not a valid ID.");
		languageManager.registerNewMessage(pluginName, "command.license.list.no-licenses", "&7No Master Licenses have yet been created.");
		languageManager.registerNewMessage(pluginName, "command.license.list.list-of-licenses", "&7The following Master Licenses exist&8:");
		languageManager.registerNewMessage(pluginName, "command.license.expire.already-expired", "&7The Master License &9{0} &7has already expired.");
		languageManager.registerNewMessage(pluginName, "command.license.expire.success", "&7Successfully expired the Master License &9{0}&7.");
		languageManager.registerNewMessage(pluginName, "command.license.expire.error.sql", "&cCould not expire the Master License &e{0}&c, see console for details.");
		languageManager.registerNewMessage(pluginName, "command.license.permanent.already-permanent", "&7The Master License &9{0} &7has already been set to permanent.");
		languageManager.registerNewMessage(pluginName, "command.license.permanent.success", "&7Successfully set the Master License &9{0} &7to permanent.");
		languageManager.registerNewMessage(pluginName, "command.license.permanent.error.sql", "&cCould not set the Master License &e{0} &cto permanent, see console for details.");
		languageManager.registerNewMessage(pluginName, "command.license.create.error.invalid-saveType", "&cCould not create new Master License&8: &eCould not validate SaveType with ID {0}&c.");
		languageManager.registerNewMessage(pluginName, "command.license.create.error.already-exists", "&cCould not create new Master License&8: &eA Master License with the SaveType {0} and Name {1} does already exist&c.");
		languageManager.registerNewMessage(pluginName, "command.license.create.success", "&7Successfully created a new Master License named &9{1} &7for the SaveType &9{0}&7.");
		languageManager.registerNewMessage(pluginName, "command.license.create.error.sql", "&cCould not create new Master License, see console for details.");
		languageManager.registerNewMessage(pluginName, "command.license.grant.error.master-license-expired", "&7The Master License &9{0} &7has expired and can't be granted.");
		languageManager.registerNewMessage(pluginName, "command.license.grant.error.already-has-license", "&7The User &9{0} &7does already have an active &9{1} &7license.");
		languageManager.registerNewMessage(pluginName, "command.license.grant.success.temporary", "&7Successfully granted the User &9{1} &7a temporary license of the type &9{0} &7that expires on &9{2}.");
		languageManager.registerNewMessage(pluginName, "command.license.grant.success.permanent", "&7Successfully granted the User &9{1} &7a permanent license of the type &9{0}&7.");
		languageManager.registerNewMessage(pluginName, "command.license.grant.error.sql", "&cCould not grant &e{1} &cthe license &e{0}&c, see console for details.");
		languageManager.registerNewMessage(pluginName, "command.license.has.yes", "&7The User &9{0} &7has an active license of the type &9{1}&7. Expiration&8: &9{2}");
		languageManager.registerNewMessage(pluginName, "command.license.has.no", "&7The User &9{0} &7does not have an active license of the type &9{1}&7.");
		languageManager.registerNewMessage(pluginName, "command.license.revert.error.no-license", "&7The User &9{0} &7does not have an active license of the type &9{1}&7.");
		languageManager.registerNewMessage(pluginName, "command.license.revert.success.sender", "&7Successfully reverted the license &9{1} &7of User &9{0}&7.");
		languageManager.registerNewMessage(pluginName, "command.license.revert.success.target", "&7Your &9{0} &7license has been reverted by &9{1}&7.");
		languageManager.registerNewMessage(pluginName, "command.license.revert.error.sql", "&cCould not revert license &e{1} &cfor player &e{0}&c, see console for details.");
		
		languageManager.registerNewMessage(pluginName, "command.money.own-balance", "&7Your Balance&8: &9{0}{1}");
		
		languageManager.registerNewMessage(pluginName, "command.economy.syntax", "&7Syntax&8: &9/economy\n"
				+ "  &9get <Player> [SaveType] &f- &7Get Economy\n"
				+ "  &9add <Player> <SaveType> <Amount> &f- &7Add Economy\n"
				+ "  &9remove <Player> <SaveType> <Amount> &f- &7Remove Economy\n"
				+ "  &9set <Player> <SaveType> <Amount> &f- &7Set Economy");
		languageManager.registerNewMessage(pluginName, "command.economy.main.error.invalid-id-or-amount", "&cInvalid ID (&e{0}&c) or Amount (&e{1}&c).");
		languageManager.registerNewMessage(pluginName, "command.economy.get.list.no-economy", "&7The User &9{0} &7does not have any economy balance at all.");
		languageManager.registerNewMessage(pluginName, "command.economy.get.list.pre-text", "&7The User &9{0} &7does have the following balance accounts&8:");
		languageManager.registerNewMessage(pluginName, "command.economy.get.list-economy", "&7The User &9{0} &7has a balance of &9{1}{2} &7on the SaveType &9{3}&7.");
		languageManager.registerNewMessage(pluginName, "command.economy.add.success.sender", "&7Successfully added &9{1}{2} &7to &9{0} &7on SaveType &9{3}&7.");
		languageManager.registerNewMessage(pluginName, "command.economy.add.success.target", "&a+&7{0}{1}");
		languageManager.registerNewMessage(pluginName, "command.economy.add.error.sql", "&cCould not add Economy for User &e{0}&c, see console for details.");
		languageManager.registerNewMessage(pluginName, "command.economy.remove.success.sender", "&7Successfully removed &9{1}{2} &7from &9{0} &7on SaveType &9{3}&7.");
		languageManager.registerNewMessage(pluginName, "command.economy.remove.success.target", "&c-&7{0}{1}");
		languageManager.registerNewMessage(pluginName, "command.economy.remove.error.sql", "&cCould not remove Economy for User &e{0}&c, see console for details.");
		languageManager.registerNewMessage(pluginName, "command.economy.set.success.sender", "&7Successfully set the Economy of &9{0} &7on SaveType &9{3} &7to &9{1}{2}&7.");
		languageManager.registerNewMessage(pluginName, "command.economy.set.success.target", "&e=&7{0}{1}");
		languageManager.registerNewMessage(pluginName, "command.economy.set.error.sql", "&cCould not set Economy for User &e{0}&c, see console for details.");
		
		languageManager.registerNewMessage(pluginName, "command.slot.display", "&7This sever's slots are currently set to &9{0}&7.");
		languageManager.registerNewMessage(pluginName, "command.slot.set.error.negative", "&7Server Slots can't be negative.");
		languageManager.registerNewMessage(pluginName, "command.slot.set.success", "&7Successfully set this server's slots to &9{0}&7.");
		languageManager.registerNewMessage(pluginName, "command.slot.set.notify", "&7[&9+&7] &9{0} &7set this server's slots to &9{1}&7.");
		languageManager.registerNewMessage(pluginName, "command.slot.set.error.invalid-amount", "&cThe provided amount of slots (&e{0}&c) is invalid.");
		languageManager.registerNewMessage(pluginName, "command.slot.set.error.could-not-update-settings", "&cCould not update slots to settings manager, see console for details.");
		
		languageManager.registerNewMessage(pluginName, "command.pay.syntax", "&7Syntax&8: &9/pay <Player> <Amount>");
		languageManager.registerNewMessage(pluginName, "command.pay.error.permanently-disabled", "&7Money transfer isn't available on this server.");
		languageManager.registerNewMessage(pluginName, "command.pay.error.temporarily-disabled", "&7Money transfer has temporarily been disabled.");
		languageManager.registerNewMessage(pluginName, "command.pay.error.negative-input", "&cYou can't send negative amounts of balance (Input: &e{0}&c).");
		languageManager.registerNewMessage(pluginName, "command.pay.error.too-many-digits", "&cYour entered amount of digits (&e{0}&c, numbers before the comma) is greater than the maximum allowed amount of &e{1}&c.");
		languageManager.registerNewMessage(pluginName, "command.pay.error.too-many-commas", "&cYour entered amount of digits (&e{0}&c, numbers after the comma) is greater than the maximum allowed amount of &e{1}&c.");
		languageManager.registerNewMessage(pluginName, "command.pay.error.invalid-amount", "&cThe amount of money to send entered (&e{0}&c) is invalid. Please enter a valid number.");
		languageManager.registerNewMessage(pluginName, "command.pay.error.invalid-balance", "&cYou don't provide the specified amount of balance (&e{0} < {1}&c).");
		languageManager.registerNewMessage(pluginName, "command.pay.error.event-cancelled", "&cYour transaction has been cancelled by the server.");
		languageManager.registerNewMessage(pluginName, "command.pay.success.sender", "&7Successfully sent &9{1}{2} &7towards &9{0}&7. The transaction has been completed without any errors.");
		languageManager.registerNewMessage(pluginName, "command.pay.success.target", "&7You received &9{1}{2} &7from &9{0}&7. The transaction has been completed without any errors.");
		languageManager.registerNewMessage(pluginName, "command.pay.error.transaction-could-not-be-completed", "&cYour transaction could not be completed. Please contact the staff - This incident has been logged.");
		
		languageManager.registerNewMessage(pluginName, "command.log.success.on", "&7Log messages will now be displayed in the Chat.");
		languageManager.registerNewMessage(pluginName, "command.log.success.off", "&7Log messages will no longer be displayed in the Chat.");
		
		languageManager.registerNewMessage(pluginName, "command.world.syntax", "&7World Management\n"
				+ "&6/world\n"
				+ "  &ecreate <Name> <WorldType>\n"
				+ "  &edelete <Name>\n"
				+ "  &eload <Name>\n"
				+ "  &eunload <Name>\n"
				+ "  &etp <Name>\n"
				+ "  &elist");
		languageManager.registerNewMessage(pluginName, "command.world.current-world", "&7You are currently inside the world &9{0}&7.");
		languageManager.registerNewMessage(pluginName, "command.world.main.error.cant-manipulate-default-world", "&7Default worlds can not be manipulated.");
		languageManager.registerNewMessage(pluginName, "command.world.main.error.not-in-world-container", "&7Could not find a world named &9{0} &7in the WorldContainer.");
		languageManager.registerNewMessage(pluginName, "command.world.create.error.invalid-worldType", "&7The WorldType &9{0} &7is invalid. Valid Types&8: &9{1}&7.");
		languageManager.registerNewMessage(pluginName, "command.world.create.error.already-exists-in-world-container", "&7A file with the name of &9{0} &7does already exist in the WorldContainer.");
		languageManager.registerNewMessage(pluginName, "command.world.create.error.world-already-exists", "&7A world with the name of &9{0} &7does already exist.");
		languageManager.registerNewMessage(pluginName, "command.world.create.error.could-not-create", "&cCould not create world &e{0}&c.");
		languageManager.registerNewMessage(pluginName, "command.world.create.success", "&7Successfully created the world &9{0} &7with type &9{1}&7.");
		languageManager.registerNewMessage(pluginName, "command.world.load.success", "&7Successfully loaded the world &9{0}&7.");
		languageManager.registerNewMessage(pluginName, "command.world.load.error", "&7Could not load the world &9{0}&7. Does it exist inside the WorldContainer?");
		languageManager.registerNewMessage(pluginName, "command.world.unload.success", "&7Successfully unloaded the world &9{0}&7.");
		languageManager.registerNewMessage(pluginName, "command.world.unload.error", "&7Could not unload the world &9{0}&7. Does it exist inside the WorldContainer?");
		languageManager.registerNewMessage(pluginName, "command.world.unload.player-info", "&7The world you were in has been unloaded. Moving you to the default world's spawn location..");
		languageManager.registerNewMessage(pluginName, "command.world.delete.error.cant-unload-world", "&cCould not unload world &e{0}&c, can't proceed to delete it.");
		languageManager.registerNewMessage(pluginName, "command.world.delete.success", "&7Successfully deleted the world &9{0}&7.");
		languageManager.registerNewMessage(pluginName, "command.world.delete.error.cant-delete-directory", "&cCould not delete the directory of world &e{0}&c.");
		languageManager.registerNewMessage(pluginName, "command.world.teleport.error.cant-load-world", "&cCould not load world &e{0}&c, can't proceed to teleport towards it.");
		languageManager.registerNewMessage(pluginName, "command.world.teleport.error.cant-teleport", "&cCould not teleport you to inside the world &e{0}&c.");
		languageManager.registerNewMessage(pluginName, "command.world.teleport.success", "&7Successfully teleported you inside the world &9{0}&7.");
		languageManager.registerNewMessage(pluginName, "command.world.list.pre-text", "&7The following worlds exist in the WorldContainer (&9{0}&7)&8:");
		languageManager.registerNewMessage(pluginName, "command.world.list.world-text", "&7- &9{0} &7({1})");
		
		languageManager.registerNewMessage(pluginName, "command.teleport.syntax", "&7Syntax&8: &9/teleport <Player> [Player]");
		languageManager.registerNewMessage(pluginName, "command.teleport.self.success", "&7Successfully teleported you towards &9{0}&7.");
		languageManager.registerNewMessage(pluginName, "command.teleport.other.sender.success", "&7Successfully teleported &9{0} &7towards &9{1}&7.");
		languageManager.registerNewMessage(pluginName, "command.teleport.other.target1.success", "&9{0} &7teleported you towards &9{1}&7.");
		languageManager.registerNewMessage(pluginName, "command.teleport.other.target2.success", "&9{0} &7teleported &9{1} &7towards you.");
		languageManager.registerNewMessage(pluginName, "command.teleport.error.target1-cant-be-target2", "&7You can't teleport the player towards itself.");
		
		// Listener
		languageManager.registerNewMessage(pluginName, "listener.player-command-pre-process.unknown-command", "&7The command &9{0} &7could not be validated.");
		languageManager.registerNewMessage(pluginName, "listener.player-join.operator-join-action.disallow", "&cOperators are not allowed to join this server.");
		languageManager.registerNewMessage(pluginName, "listener.player-join.operator-join-action.remove-status", "&7Your operator status has automatically been removed for security reasons.");
		languageManager.registerNewMessage(pluginName, "listener.player-join.server-full-not-bypassing", "&f&l{0}\n"
				+ "&c&lSERVER FULL\n\n"
				+ "&7You need an active Premium Membership to join the full server.\n\n"
				+ "&7Store&8: &e{1}");
		languageManager.registerNewMessage(pluginName, "listener.async-player-pre-login.maintenance", "&f&l{0}\n"
				+ "&c&lSERVER MAINTENANCE\n\n"
				+ "&7Only staff members are currently allowed to join the server.\n"
				+ "&7Please visit our website for further details about this incident.\n\n"
				+ "&7Web&8: &e{1}");
		languageManager.registerNewMessage(pluginName, "listener.server-list-ping.maintenance", "&e{0} &7[&d{1}&7]\n"
				+ "&8> &cServer Maintenance &7| &e{2}");
		
		// GUI
		languageManager.registerNewMessage(pluginName, "gui.license.title", "&3My Licenses");
		languageManager.registerNewMessage(pluginName, "gui.license.item.no-licenses.name", "&7No Licenses");
		languageManager.registerNewMessage(pluginName, "gui.license.item.license.description", "&7Received&8: &9{0}\n"
				+ "&7Expiration&8: &9{1}");
		
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
				"" + Core.getInstance().getSettingsManager().getSlots(),
				Core.getInstance().getSettingsManager().getServerNameProperties()
				));
	}
	
	private final String getTabFooter(@Nonnull User user) {
		return Core.getInstance().getLanguageManager().getMessage(Core.getInstance().getPluginName(), user.getLanguage(), PredefinedMessage.TAB_FOOTER.getPath(), null);
	}

}
