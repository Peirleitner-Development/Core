package at.peirleitner.core.util.user;

import java.util.UUID;

import javax.annotation.Nonnull;

import at.peirleitner.core.Core;
import at.peirleitner.core.system.UserSystem;

/**
 * This class represents a User on the local server instance/network.
 * 
 * @since 1.0.0
 * @author Markus Peirleitner (Rengobli)
 */
public final class User {

	private final UUID uuid;
	private final String lastKnownName;
	private final long registered;
	private final long lastLogin;
	private final long lastLogout;
	private final boolean enabled;
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
	
	/**
	 * 
	 * @param name - New name
	 * @return If the name has been updated
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 * @see UserSystem#setLastKnownName(User, String)
	 */
	public final boolean setLastKnownName(@Nonnull String name) {
		return Core.getInstance().getUserSystem().setLastKnownName(this, name);
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
	
	/**
	 * 
	 * @param lastLogin - Last login
	 * @return If the TimeStamp has been updated
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 * @see UserSystem#setLastLogin(User, long)
	 */
	public final boolean setLastLogin(@Nonnull long lastLogin) {
		return Core.getInstance().getUserSystem().setLastLogin(this, lastLogin);
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
	
	/**
	 * 
	 * @param lastLogout - Last logout
	 * @return If the TimeStamp has been updated
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 * @see UserSystem#setLastLogout(User, long)
	 */
	public final boolean setLastLogout(@Nonnull long lastLogout) {
		return Core.getInstance().getUserSystem().setLastLogout(this, lastLogout);
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
	
	/**
	 * 
	 * @param enabled - New state
	 * @return If the state has been updated
	 */
	public final boolean setEnabled(@Nonnull boolean enabled) {
		return Core.getInstance().getUserSystem().setEnabled(this, enabled);
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
	 * @param language - New language
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final void setLanguage(Language language) {
		this.language = language;
	}

}
