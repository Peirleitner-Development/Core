package at.peirleitner.core.system;

import java.util.ArrayList;
import java.util.Collection;

import at.peirleitner.core.Core;
import at.peirleitner.core.util.Setting;
import at.peirleitner.core.util.user.User;

/**
 * This class handles all (database) interactions of a {@link User}
 * @since 1.0.0
 * @author Markus Peirleitner (Rengobli)
 *
 */
public final class UserSystem {

	private Collection<User> cachedUsers;
	
	public UserSystem() {
		
		// Initialize
		this.cachedUsers = new ArrayList<>();
		
		// Set default settings
		Core.getInstance().getSettingsManager().create(new Setting<Boolean>(Core.getInstance().getPluginName(), "system.user-system.enable-caching", true));
		
	}
	
	public final Collection<User> getCachedUsers() {
		return this.cachedUsers;
	}
	
	public final boolean isCachingEnabled() {
		return (boolean) Core.getInstance().getSettingsManager().getByName(Core.getInstance().getPluginName(), "system.user-system.enable-caching").getValue();
	}
	
}
