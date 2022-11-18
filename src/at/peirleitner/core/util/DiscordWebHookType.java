package at.peirleitner.core.util;

import at.peirleitner.core.Core;

/**
 * Type of the {@link DiscordWebhook}
 * 
 * @since 1.0.14
 * @author Markus Peirleitner (Rengobli)
 *
 */
public enum DiscordWebHookType {

	LOG,
	USER_CHAT_MESSAGE,
	STAFF_NOTIFICATION,
	SERVER_NOTIFICIATION;
	
	public final boolean isEnabled() {
		return Core.getInstance().getSettingsManager().isSetting(Core.getInstance().getPluginName(), "manager.settings.discord-webhook." + this.toString().toLowerCase() + ".enabled");
	}
	
	public final String getURL() {
		return Core.getInstance().getSettingsManager().getSetting(Core.getInstance().getPluginName(), "manager.settings.discord-webhook." + this.toString().toLowerCase() + ".url");
	}

}
