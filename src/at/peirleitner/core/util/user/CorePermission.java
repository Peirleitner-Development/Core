package at.peirleitner.core.util.user;

import javax.annotation.Nonnull;

import at.peirleitner.core.command.local.CommandEconomy;
import at.peirleitner.core.command.local.CommandLicense;
import at.peirleitner.core.command.local.CommandLog;
import at.peirleitner.core.command.local.CommandMaintenance;
import at.peirleitner.core.command.local.CommandPay;
import at.peirleitner.core.command.local.CommandSlot;
import at.peirleitner.core.manager.SettingsManager;

public enum CorePermission {

	COMMAND_CORE_ADMIN("core.command.core.admin"),
	COMMAND_LANGUAGE("core.command.language"),
	
	/**
	 * Access to the {@link #COMMAND_MOTD} command
	 * @since 1.0.4
	 * @author Markus Peirleitner (Rengobli)
	 */
	COMMAND_MOTD("core.command.motd"),
	
	/**
	 * Access to the {@link CommandMaintenance} command
	 * @since 1.0.5
	 * @author Markus Peirleitner (Rengobli)
 	 */
	COMMAND_MAINTENANCE("core.command.maintenance"),
	
	/**
	 * Access to the License Management via {@link CommandLicense} command
	 * @since 1.0.6
	 * @author Markus Peirleitner (Rengobli)
	 */
	COMMAND_LICENSE_ADMIN("core.command.license.admin"),
	
	/**
	 * Access to the Economy Management via {@link CommandEconomy}
	 * @since 1.0.6
	 * @author Markus Peirleitner (Rengobli)
	 */
	COMMAND_ECONOMY("core.command.economy"),
	
	/**
	 * Display current Slots on {@link CommandSlot}
	 * @since 1.0.6
	 * @author Markus Peirleitner (Rengobli)
	 */
	COMMAND_SLOT_DISPLAY("core.command.slot.display"),
	
	/**
	 * Set new Slots with {@link CommandSlot}
	 * @since 1.0.6
	 * @author Markus Peirleitner (Rengobli)
	 */
	COMMAND_SLOT_CHANGE("core.command.slot.change"),
	
	/**
	 * Send money to other players using {@link CommandPay}
	 * @since 1.0.6
	 * @author Markus Peirleitner (Rengobli)
	 */
	COMMAND_PAY("core.command.pay"),
	
	/**
	 * Toggle display of log messages using {@link CommandLog}
	 * @since 1.0.8
	 * @author Markus Peirleitner (Rengobli)
	 */
	COMMAND_LOG("core.command.log"),
	
	/**
	 * Allows to join the full server, even if {@link SettingsManager#getSlots()} is full
	 * @since 1.0.6
	 * @author Markus Peirleitner (Rengobli)
	 */
	BYPASS_FULL_SERVER_JOIN("Core.bypass.fullServerJoin"),
	
	NOTIFY_STAFF("Core.notify")
	;
	
	private String permission;
	
	private CorePermission(@Nonnull String permission) {
		this.permission = permission;
	}
	
	public final String getPermission() {
		return this.permission;
	}

}
