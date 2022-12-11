package at.peirleitner.core.util.user;

import at.peirleitner.core.system.CooldownSystem;
import at.peirleitner.core.util.database.SaveType;

/**
 * This Object is used to represent an active Cooldown of a {@link User} inside
 * the {@link CooldownSystem}
 * 
 * @since 1.0.15
 * @author Markus Peirleitner (Rengobli)
 *
 */
public final class CooldownInfo {

	private final String metadata;
	private final long lastUsage;
	private final long nextUsage;
	private final SaveType saveType;

	public CooldownInfo(String metadata, long lastUsage, long nextUsage, SaveType saveType) {
		this.metadata = metadata;
		this.lastUsage = lastUsage;
		this.nextUsage = nextUsage;
		this.saveType = saveType;
	}

	public final String getMetadata() {
		return metadata;
	}

	public final long getLastUsage() {
		return lastUsage;
	}

	public final long getNextUsage() {
		return nextUsage;
	}

	public final SaveType getSaveType() {
		return saveType;
	}

}
