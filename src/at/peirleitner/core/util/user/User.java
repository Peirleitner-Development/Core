package at.peirleitner.core.util.user;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import at.peirleitner.core.Core;
import at.peirleitner.core.manager.LanguageManager;
import at.peirleitner.core.util.RunMode;
import net.md_5.bungee.api.ChatColor;

/**
 * This class represents a User on the local server instance/network.
 * 
 * @since 1.0.0
 * @author Markus Peirleitner (Rengobli)
 */
public final class User {

	private final UUID uuid;
	private String lastKnownName;
	private final long registered;
	private long lastLogin;
	private long lastLogout;
	private boolean enabled;
	private Language language;

	public User(UUID uuid, String lastKnownName, long registered, long lastLogin, long lastLogout, boolean enabled,
			Language language) {
		this.uuid = uuid;
		this.lastKnownName = lastKnownName;
		this.registered = registered;
		this.lastLogin = lastLogin;
		this.lastLogout = lastLogout;
		this.enabled = enabled;
		this.language = language;
	}

	/**
	 * 
	 * @return UUID of the User
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final UUID getUUID() {
		return uuid;
	}

	/**
	 * 
	 * @return Last known name of the user
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final String getLastKnownName() {
		return lastKnownName;
	}

	public final void setLastKnownName(@Nonnull String lastKnownName) {
		this.lastKnownName = lastKnownName;
	}

	/**
	 * 
	 * @return TimeStamp of the first connection
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final long getRegistered() {
		return registered;
	}

	/**
	 * 
	 * @return TimeStamp of the latest connection login
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final long getLastLogin() {
		return lastLogin;
	}

	public final void setLastLogin(@Nonnull long lastLogin) {
		this.lastLogin = lastLogin;
	}

	/**
	 * 
	 * @return TimeStamp of the latest connection logout
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final long getLastLogout() {
		return lastLogout;
	}

	public final void setLastLogout(@Nonnull long lastLogout) {
		this.lastLogout = lastLogout;
	}

	/**
	 * 
	 * @return If this account is enabled
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final boolean isEnabled() {
		return enabled;
	}

	public final void setEnabled(@Nonnull boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * 
	 * @return Current language that has been selected
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final Language getLanguage() {
		return language;
	}

	/**
	 * Update the language of this user
	 * 
	 * @param language - New language
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final void setLanguage(Language language) {
		this.language = language;
	}

	public final void sendMessage(@Nonnull LanguageManager languageManager, @Nonnull String key, @Nullable List<String> replacements, @Nonnull boolean prefix) {

		String message = languageManager.getMessage(key, replacements);

		if (prefix) {
			message = languageManager.getPrefix() + message;
		}

		if (Core.getInstance().getRunMode() == RunMode.NETWORK) {

			net.md_5.bungee.api.connection.ProxiedPlayer pp = net.md_5.bungee.api.ProxyServer.getInstance()
					.getPlayer(this.getUUID());
			pp.sendMessage(
					new net.md_5.bungee.api.chat.TextComponent(ChatColor.translateAlternateColorCodes('&', message)));

		} else {

			org.bukkit.entity.Player p = org.bukkit.Bukkit.getPlayer(this.getUUID());
			p.sendMessage(ChatColor.translateAlternateColorCodes('&', message));

		}

	}

}
