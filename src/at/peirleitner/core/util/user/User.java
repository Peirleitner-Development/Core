package at.peirleitner.core.util.user;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import at.peirleitner.core.Core;
import at.peirleitner.core.system.ExperienceSystem;
import at.peirleitner.core.system.LicenseSystem;
import at.peirleitner.core.system.StatSystem;
import at.peirleitner.core.util.RunMode;
import at.peirleitner.core.util.database.SaveType;
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

			// TODO: Add message to cache, up to a maximum and display it on joining
			if (pp == null)
				return;

			pp.sendMessage(
					new net.md_5.bungee.api.chat.TextComponent(ChatColor.translateAlternateColorCodes('&', message)));

		} else {

			org.bukkit.entity.Player p = org.bukkit.Bukkit.getPlayer(this.getUUID());

			// TODO: Add message to cache, up to a maximum and display it on joining
			if (p == null)
				return;

			at.peirleitner.core.api.local.UserMessageSendEvent event = new at.peirleitner.core.api.local.UserMessageSendEvent(
					this, pluginName, key, replacements, prefix);
			at.peirleitner.core.SpigotMain.getInstance().getServer().getPluginManager().callEvent(event);

			if (event.isCancelled()) {
				return;
			}

			p.sendMessage(ChatColor.translateAlternateColorCodes('&', message));

		}

	}

	/**
	 * 
	 * @param pluginName
	 * @param key
	 * @param replacements
	 * @param prefix
	 * @since 1.0.14
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final void sendAsyncMessage(@Nonnull String pluginName, @Nonnull String key,
			@Nullable List<String> replacements, @Nonnull boolean prefix) {

		String message = Core.getInstance().getLanguageManager().getMessage(pluginName, this.getLanguage(), key,
				replacements);

		if (prefix) {
			message = Core.getInstance().getLanguageManager().getPrefix(pluginName, this.getLanguage()) + message;
		}

		if (Core.getInstance().getRunMode() == RunMode.NETWORK) {

			net.md_5.bungee.api.connection.ProxiedPlayer pp = net.md_5.bungee.api.ProxyServer.getInstance()
					.getPlayer(this.getUUID());

			// TODO: Add message to cache, up to a maximum and display it on joining
			if (pp == null)
				return;

			pp.sendMessage(
					new net.md_5.bungee.api.chat.TextComponent(ChatColor.translateAlternateColorCodes('&', message)));

		} else {

			org.bukkit.entity.Player p = org.bukkit.Bukkit.getPlayer(this.getUUID());

			// TODO: Add message to cache, up to a maximum and display it on joining
			if (p == null)
				return;

			at.peirleitner.core.api.local.AsyncUserMessageSendEvent event = new at.peirleitner.core.api.local.AsyncUserMessageSendEvent(
					this, pluginName, key, replacements, prefix);
			at.peirleitner.core.SpigotMain.getInstance().getServer().getPluginManager().callEvent(event);

			if (event.isCancelled()) {
				return;
			}

			p.sendMessage(ChatColor.translateAlternateColorCodes('&', message));

		}

	}

	/**
	 * 
	 * @return If this User should be treated as immune against restrictions (for
	 *         example bans)
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final boolean isImmune() {
		return immune;
	}

	/**
	 * 
	 * @return If this User should be treated with a freepass (for example should
	 *         always have all Kits available)
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final boolean hasFreepass() {
		return freepass;
	}

	public final Rank getRank() {

		org.bukkit.entity.Player p = org.bukkit.Bukkit.getPlayer(this.getUUID());

		if (p == null)
			return Core.getInstance().getDefaultRank();

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

	/**
	 * 
	 * @param id - ID of the {@link MasterLicense}
	 * @return If this User has an active {@link UserLicense} for the specified
	 *         {@link MasterLicense}
	 * @since 1.0.6
	 * @author Markus Peirleitner (Rengobli)
	 * @see LicenseSystem#hasActiveLicense(UUID, MasterLicense)
	 */
	public final boolean hasActiveLicense(@Nonnull int id) {
		return Core.getInstance().getLicenseSystem().hasActiveLicense(this.getUUID(),
				Core.getInstance().getLicenseSystem().getMasterLicense(id));
	}

	/**
	 * 
	 * @param saveType
	 * @return Current Economy for the given {@link SaveType}
	 * @since 1.0.6
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final double getEconomy(@Nonnull SaveType saveType) {
		return Core.getInstance().getEconomySystem().getEconomy(this.getUUID(), saveType);
	}

	/**
	 * 
	 * @return All Economy balance for this {@link User}
	 * @since 1.0.6
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final HashMap<SaveType, Double> getEconomy() {
		return Core.getInstance().getEconomySystem().getEconomy(this.getUUID());
	}

	/**
	 * 
	 * @param saveType - SaveType
	 * @param amount   - Amount
	 * @return If the Economy has been added successfully
	 * @since 1.0.6
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final boolean addEconomy(@Nonnull SaveType saveType, @Nonnull double amount) {
		return Core.getInstance().getEconomySystem().addEconomy(this.getUUID(), saveType, amount);
	}

	/**
	 * 
	 * @param saveType - SaveType
	 * @param amount   - Amount
	 * @return If the Economy has been removed successfully
	 * @since 1.0.6
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final boolean removeEconomy(@Nonnull SaveType saveType, @Nonnull double amount) {
		return Core.getInstance().getEconomySystem().removeEconomy(this.getUUID(), saveType, amount);
	}

	/**
	 * 
	 * @param saveType - SaveType
	 * @param amount   - Amount
	 * @return If the Economy has been set successfully
	 * @since 1.0.6
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final boolean setEconomy(@Nonnull SaveType saveType, @Nonnull double amount) {
		return Core.getInstance().getEconomySystem().setEconomy(this.getUUID(), saveType, amount);
	}

	/**
	 * 
	 * @param saveType - SaveType
	 * @param amount   - Amount
	 * @return If this {@link User} has at least as much money as provided in the
	 *         arguments
	 * @since 1.0.6
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final boolean hasEconomy(@Nonnull SaveType saveType, @Nonnull double amount) {
		return this.getEconomy(saveType) >= amount;
	}

	/**
	 * 
	 * @return All Statistics of this User
	 * @since 1.0.17
	 * @author Markus Peirleitner (Rengobli)
	 * @see StatSystem
	 */
	public final Collection<UserStatistic> getStatistics() {
		return Core.getInstance().getStatSystem().getStatistics(this.getUUID());
	}

	/**
	 * 
	 * @param saveType - SaveType to gain the Statistics for
	 * @return All Statistics of this User, as of the given SaveType
	 * @since 1.0.17
	 * @author Markus Peirleitner (Rengobli)
	 * @see StatSystem
	 */
	public final Collection<UserStatistic> getStatistics(@Nonnull SaveType saveType) {
		return Core.getInstance().getStatSystem().getStatistics(this.getUUID(), saveType);
	}
	
	/**
	 * 
	 * @return Experience across all {@link SaveType}s
	 * @since 1.0.18
	 * @author Markus Peirleitner (Rengobli)
	 * @see ExperienceSystem
	 */
	public final UserExperience[] getExperience() {
		return Core.getInstance().getExperienceSystem().getExperience(this.getUUID());
	}
	
	/**
	 * 
	 * @param saveType - SaveType to gain the Experience for
	 * @return Experience on the given {@link SaveType}
	 * @since 1.0.18
	 * @author Markus Peirleitner (Rengobli)
	 * @see ExperienceSystem
	 */
	public final UserExperience getExperience(@Nonnull SaveType saveType) {
		return Core.getInstance().getExperienceSystem().getExperience(this.getUUID(), saveType);
	}
	
	/**
	 * 
	 * @param saveType - SaveType 
	 * @param amount - Amount of Experience to add
	 * @param message - If a message should be displayed
	 * @return If the Experience has been added successfully.
	 * @since 1.0.18
	 * @author Markus Peirleitner (Rengobli)
	 * @see ExperienceSystem#addExperience(UUID, SaveType, int)
	 */
	public final boolean addExperience(@Nonnull SaveType saveType, @Nonnull int amount, @Nonnull boolean message) {
		return Core.getInstance().getExperienceSystem().addExperience(this.getUUID(), saveType, amount, message);
	}

	@Deprecated
	public final boolean isNicked() {
		return false;
	}

	@Deprecated
	public final String getNickName() {
		return null;
	}

	@Deprecated
	public final boolean nick() {
		return false;
	}

	@Deprecated
	public final boolean unNick() {
		return false;
	}

	@Override
	public String toString() {
		return "User[uuid=" + this.getUUID().toString() + ",lastKnownName=" + this.getLastKnownName() + ",registered="
				+ this.getRegistered() + ",lastLogin=" + this.getLastLogin() + ",lastLogout =" + this.getLastLogout()
				+ ",enabled=" + this.isEnabled() + ",language=" + this.getLanguage().toString() + ",immune="
				+ this.isImmune() + ",freepass=" + this.hasFreepass() + "]";
	}

}
