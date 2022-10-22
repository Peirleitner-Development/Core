package at.peirleitner.core.util.user;

import javax.annotation.Nonnull;

import at.peirleitner.core.command.local.CommandLicense;
import at.peirleitner.core.command.local.CommandMaintenance;

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
	 * Bypass active maintenance mode
	 * @since 1.0.5
	 * @author Markus Peirleitner (Rengobli)
	 */
	BYPASS_MAINTENANCE("core.bypass.maintenance"),
	
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
