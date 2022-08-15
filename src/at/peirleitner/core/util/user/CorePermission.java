package at.peirleitner.core.util.user;

import javax.annotation.Nonnull;

public enum CorePermission {

	MAIN_LOGIN_BYPASS_MAINTENANCE("core.login.bypass.maintenance"),
	COMMAND_CORE_ADMIN("core.command.core.admin"),
	COMMAND_LANGUAGE("core.command.language")
	;
	
	private String permission;
	
	private CorePermission(@Nonnull String permission) {
		this.permission = permission;
	}
	
	public final String getPermission() {
		return this.permission;
	}

}
