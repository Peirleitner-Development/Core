package at.peirleitner.core.util.user;

import javax.annotation.Nonnull;

import at.peirleitner.core.command.local.CommandChatLog;
import at.peirleitner.core.command.local.CommandEconomy;
import at.peirleitner.core.command.local.CommandLicense;
import at.peirleitner.core.command.local.CommandLog;
import at.peirleitner.core.command.local.CommandMaintenance;
import at.peirleitner.core.command.local.CommandMod;
import at.peirleitner.core.command.local.CommandPay;
import at.peirleitner.core.command.local.CommandRedeem;
import at.peirleitner.core.command.local.CommandSlot;
import at.peirleitner.core.command.local.CommandVoucher;
import at.peirleitner.core.command.local.CommandWorld;
import at.peirleitner.core.manager.SettingsManager;

public enum CorePermission {

	COMMAND_CORE_ADMIN("Core.command.core.admin"),

	COMMAND_LANGUAGE("Core.command.language"),

	/**
	 * Access to the {@link #COMMAND_MOTD} command
	 * 
	 * @since 1.0.4
	 * @author Markus Peirleitner (Rengobli)
	 */
	COMMAND_MOTD("Core.command.motd"),

	/**
	 * Access to the {@link CommandMaintenance} command
	 * 
	 * @since 1.0.5
	 * @author Markus Peirleitner (Rengobli)
	 */
	COMMAND_MAINTENANCE("Core.command.maintenance"),

	/**
	 * Access to the License Management via {@link CommandLicense} command
	 * 
	 * @since 1.0.6
	 * @author Markus Peirleitner (Rengobli)
	 */
	COMMAND_LICENSE_ADMIN("Core.command.license.admin"),

	/**
	 * Access to the Economy Management via {@link CommandEconomy}
	 * 
	 * @since 1.0.6
	 * @author Markus Peirleitner (Rengobli)
	 */
	COMMAND_ECONOMY("Core.command.economy"),

	/**
	 * Display current Slots on {@link CommandSlot}
	 * 
	 * @since 1.0.6
	 * @author Markus Peirleitner (Rengobli)
	 */
	COMMAND_SLOT_DISPLAY("Core.command.slot.display"),

	/**
	 * Set new Slots with {@link CommandSlot}
	 * 
	 * @since 1.0.6
	 * @author Markus Peirleitner (Rengobli)
	 */
	COMMAND_SLOT_CHANGE("Core.command.slot.change"),

	/**
	 * Send money to other players using {@link CommandPay}
	 * 
	 * @since 1.0.6
	 * @author Markus Peirleitner (Rengobli)
	 */
	COMMAND_PAY("Core.command.pay"),

	/**
	 * Toggle display of log messages using {@link CommandLog}
	 * 
	 * @since 1.0.8
	 * @author Markus Peirleitner (Rengobli)
	 */
	COMMAND_LOG("Core.command.log"),

	/**
	 * Allows usage of the {@link CommandWorld} command
	 * 
	 * @since 1.0.10
	 * @author Markus Peirleitner (Rengobli)
	 */
	COMMAND_WORLD("Core.command.world"),

	/**
	 * Allows you to teleport towards another player
	 * 
	 * @since 1.0.11
	 * @author Markus Peirleitner (Rengobli)
	 */
	COMMAND_TELEPORT_SELF("Core.command.teleport.self"),

	/**
	 * Allows you to teleport others towards you
	 * 
	 * @since 1.0.11
	 * @author Markus Peirleitner (Rengobli)
	 */
	COMMAND_TELEPORT_OTHER("Core.command.teleport.other"),

	/**
	 * Grants access to the {@link CommandChatLog} command (check)
	 * 
	 * @since 1.0.14
	 * @author Markus Peirleitner (Rengobli)
	 */
	COMMAND_CHATLOG_CHECK("Core.command.chatlog.check"),

	/**
	 * Grants access to the {@link CommandChatLog} command (review)
	 * 
	 * @since 1.0.14
	 * @author Markus Peirleitner (Rengobli)
	 */
	COMMAND_CHATLOG_REVIEW("Core.command.chatlog.review"),

	/**
	 * Grants access to the {@link CommandMod} command
	 * 
	 * @since 1.0.14
	 * @author Markus Peirleitner (Rengobli)
	 */
	COMMAND_MOD("Core.command.mod"),

	/**
	 * Grants access to the {@link CommandRedeem} command
	 * 
	 * @since 1.0.19
	 * @author Markus Peirleitner (Rengobli)
	 */
	COMMAND_REDEEM("Core.command.redeem"),

	/**
	 * Grants access to the {@link CommandVoucher} command
	 * 
	 * @since 1.0.19
	 * @author Markus Peirleitner (Rengobli)
	 */
	COMMAND_VOUCHER("Core.command.voucher"),

	/**
	 * Allows to join the full server, even if {@link SettingsManager#getSlots()} is
	 * full
	 * 
	 * @since 1.0.6
	 * @author Markus Peirleitner (Rengobli)
	 */
	BYPASS_FULL_SERVER_JOIN("Core.bypass.fullServerJoin"),

	/**
	 * Bypass the chat cooldown
	 * 
	 * @since 1.0.14
	 * @author Markus Peirleitner (Rengobli)
	 */
	BYPASS_CHAT_COOLDOWN("Core.bypass.chatCooldown"),

	/**
	 * Use ColorCodes on Signs
	 * 
	 * @since 1.0.12
	 * @author Markus Peirleitner (Rengobli)
	 */
	EXTRA_SIGN_COLOR("Core.extra.signColor"),

	/**
	 * Receive staff notifications such as reports or bans
	 * 
	 * @since Undefined
	 * @author Markus Peirleitner (Rengobli)
	 */
	NOTIFY_STAFF("Core.notify");

	private String permission;

	private CorePermission(@Nonnull String permission) {
		this.permission = permission;
	}

	public final String getPermission() {
		return this.permission;
	}

}
