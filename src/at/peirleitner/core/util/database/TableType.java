package at.peirleitner.core.util.database;

import javax.annotation.Nonnull;

import at.peirleitner.core.Core;
import at.peirleitner.core.system.EconomySystem;
import at.peirleitner.core.system.GameMapSystem;
import at.peirleitner.core.system.LicenseSystem;
import at.peirleitner.core.system.MotdSystem;
import at.peirleitner.core.system.StatSystem;
import at.peirleitner.core.system.UserSystem;
import at.peirleitner.core.util.MOTD;
import at.peirleitner.core.util.local.GameMap;
import at.peirleitner.core.util.moderation.ChatLog;
import at.peirleitner.core.util.user.MasterLicense;
import at.peirleitner.core.util.user.User;

/**
 * Enum to get the name of a table on the database
 * 
 * @since 1.0.6
 * @author Markus Peirleitner (Rengobli)
 *
 */
public enum TableType {

	/**
	 * Table name for {@link SaveType}
	 * 
	 * @since 1.0.6
	 * @author Markus Peirleitner (Rengobli)
	 * @see Core#getSaveTypes()
	 */
	SAVE_TYPE("saveType"),

	/**
	 * Table name for {@link User}s
	 * 
	 * @since 1.0.6
	 * @author Markus Peirleitner (Rengobli)
	 * @see UserSystem
	 */
	USERS("users"),

	/**
	 * Table name for shops (Unused as of v1.0.6)
	 * 
	 * @since 1.0.6
	 * @author Markus Peirleitner (Rengobli)
	 */
	SHOP("shop"),

	/**
	 * Table name for {@link GameMap}s
	 * 
	 * @since 1.0.6
	 * @author Markus Peirleitner (Rengobli)
	 * @see GameMapSystem
	 */
	MAPS("maps"),

	/**
	 * Table name for {@link MOTD}s
	 * 
	 * @since 1.0.6
	 * @author Markus Peirleitner (Rengobli)
	 * @see MotdSystem
	 */
	MOTD("motd"),

	/**
	 * Table name for settings saved inside the database (Unused as of v1.0.6)
	 * 
	 * @since 1.0.6
	 * @author Markus Peirleitner (Rengobli)
	 */
	SETTINGS("settings"),

	/**
	 * Table name for maintenance list
	 * 
	 * @since 1.0.6
	 * @author Markus Peirleitner (Rengobli)
	 */
	MAINTENANCE("maintenance"),

	/**
	 * Table name for all {@link MasterLicense}s
	 * 
	 * @since 1.0.6
	 * @author Markus Peirleitner (Rengobli)
	 * @see LicenseSystem
	 */
	LICENSES_MASTER("licenses_master"),

	/**
	 * Table name for {@link MasterLicense}s that can be obtained by a {@link User}
	 * 
	 * @since 1.0.6
	 * @author Markus Peirleitner (Rengobli)
	 */
	LICENSES_USER("licenses_user"),

	/**
	 * Table name for {@link EconomySystem}
	 * 
	 * @since 1.0.6
	 * @author Markus Peirleitner (Rengobli)
	 */
	ECONOMY("economy"),

	/**
	 * Table name for blocked phrases
	 * 
	 * @since 1.0.14
	 * @author Markus Peirleitner (Rengobli)
	 */
	MODERATION_BLOCKED_PHRASES("moderation_blocked_phrases"),

	/**
	 * Table name for allowed domains
	 * 
	 * @since 1.0.14
	 * @author Markus Peirleitner (Rengobli)
	 * @apiNote These will not be flagged by the chat filter
	 */
	MODERATION_ALLOWED_DOMAINS("moderation_allowed_domains"),

	/**
	 * Table name for logged user messages
	 * 
	 * @since 1.0.14
	 * @author Markus Peirleitner (Rengobli)
	 */
	MODERATION_LOG_USER_MESSAGES("moderation_log_user_messages"),

	/**
	 * Table name for {@link ChatLog}s
	 * 
	 * @since 1.0.14
	 * @author Markus Peirleitner (Rengobli)
	 */
	MODERATION_CHATLOGS("moderation_chatLogs"),
	
	/**
	 * @since 1.0.15
	 * @author Markus Peirleitner (Rengobli)
	 */
	COOLDOWNS("cooldowns"),
	
	/**
	 * Table name for statistics
	 * 
	 * @since 1.0.17
	 * @author Markus Peirleitner (Rengobli)
	 * @see StatSystem
	 */
	STATISTICS_AVAILABLE("statistics_available"),
	
	/**
	 * Table name for statistics
	 * 
	 * @since 1.0.17
	 * @author Markus Peirleitner (Rengobli)
	 * @see StatSystem
	 */
	STATISTICS_USER("statistics_user"),
	
	EXPERIENCE("experience"),
	
	;

	private final String tableName;

	private TableType(@Nonnull String tableName) {
		this.tableName = tableName;
	}

	/**
	 * 
	 * @return Table name
	 * @since 1.0.6
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final String getTableName(@Nonnull boolean prefix) {
		return (prefix ? this.getTablePrefix() : "") + this.tableName;
	}

	/**
	 * 
	 * @return Table prefix
	 * @since 1.0.6
	 * @author Markus Peirleitner (Rengobli)
	 * @apiNote Before v1.0.6 this method has been <code>private</code>.
	 */
	public final String getTablePrefix() {
		return Core.getInstance().getMySQL().isConnected() ? Core.getInstance().getMySQL().getTablePrefix()
				: "NOT_CONNECTED_";
	}

}
