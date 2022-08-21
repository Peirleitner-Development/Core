package at.peirleitner.core.util.user;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import at.peirleitner.core.Core;
import at.peirleitner.core.util.RunMode;
import at.peirleitner.core.util.local.Rank;
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
	private boolean immune;
	private boolean freepass;

	public User(UUID uuid, String lastKnownName, long registered, long lastLogin, long lastLogout, boolean enabled,
			Language language, boolean immune, boolean freepass) {
		this.uuid = uuid;
		this.lastKnownName = lastKnownName;
		this.registered = registered;
		this.lastLogin = lastLogin;
		this.lastLogout = lastLogout;
		this.enabled = enabled;
		this.language = language;
		this.immune = immune;
		this.freepass = freepass;
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

	public final void sendMessage(@Nonnull String pluginName, @Nonnull String key, @Nullable List<String> replacements,
			@Nonnull boolean prefix) {

		String message = Core.getInstance().getLanguageManager().getMessage(pluginName, this.getLanguage(), key,
				replacements);

		if (prefix) {
			message = Core.getInstance().getLanguageManager().getPrefix(pluginName, this.getLanguage()) + message;
		}

		if (Core.getInstance().getRunMode() == RunMode.NETWORK) {

			net.md_5.bungee.api.connection.ProxiedPlayer pp = net.md_5.bungee.api.ProxyServer.getInstance()
					.getPlayer(this.getUUID());
			
			//TODO: Add message to cache, up to a maximum and display it on joining
			if(pp == null) return;
			
			pp.sendMessage(
					new net.md_5.bungee.api.chat.TextComponent(ChatColor.translateAlternateColorCodes('&', message)));

		} else {

			org.bukkit.entity.Player p = org.bukkit.Bukkit.getPlayer(this.getUUID());
			
			//TODO: Add message to cache, up to a maximum and display it on joining
			if(p == null) return;
			
			p.sendMessage(ChatColor.translateAlternateColorCodes('&', message));

		}

	}

	/**
	 * 
	 * @return If this User should be treated as immune against restrictions (for example bans)
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final boolean isImmune() {
		return immune;
	}

	/**
	 * 
	 * @return If this User should be treated with a freepass (for example should always have all Kits available)
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final boolean hasFreepass() {
		return freepass;
	}

	public final Rank getRank() {

		org.bukkit.entity.Player p = org.bukkit.Bukkit.getPlayer(this.getUUID());

		for (Rank rank : Core.getInstance().getInRightOrder()) {

			if (p.hasPermission("Core.rank." + rank.getName().toLowerCase())) {
				return rank;
			}

		}

		return Core.getInstance().getDefaultRank();
	}

	public final String getDisplayName() {
		return this.getRank().getChatColor() + this.getLastKnownName();
	}

}
