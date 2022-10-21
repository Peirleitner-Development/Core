package at.peirleitner.core.util.user;

import at.peirleitner.core.system.LicenseSystem;
import at.peirleitner.core.util.TableType;
import at.peirleitner.core.util.database.SaveType;
import at.peirleitner.core.util.local.GUI;

/**
 * A License provides a {@link User} with a limited or permanent accessibility
 * towards a certain action.
 * 
 * @since 1.0.6
 * @author Markus Peirleitner (Rengobli)
 * @see LicenseSystem
 */
public final class MasterLicense {

	private final int id;
	private final int saveTypeID;
	private final String name;
	private final long created;
	private final long expire;
	private final String iconName;

	public MasterLicense(int id, int saveTypeID, String name, long created, long expire, String iconName) {
		this.id = id;
		this.saveTypeID = saveTypeID;
		this.name = name;
		this.created = created;
		this.expire = expire;
		this.iconName = iconName;
	}

	/**
	 * 
	 * @return Unique ID of this License
	 * @since 1.0.6
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final int getID() {
		return id;
	}

	/**
	 * 
	 * @return Unique ID of the {@link SaveType} that this License is suitable for
	 * @since 1.0.6
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final int getSaveTypeID() {
		return saveTypeID;
	}

	/**
	 * 
	 * @return Name of this License
	 * @since 1.0.6
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final String getName() {
		return name;
	}

	/**
	 * 
	 * @return TimeStamp of License Creation
	 * @since 1.0.6
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final long getCreated() {
		return created;
	}

	/**
	 * 
	 * @return TimeStamp of License Expiration
	 * @since 1.0.6
	 * @author Markus Peirleitner (Rengobli)
	 * @apiNote If the Master License (Saved in {@link TableType#LICENSES} expires,
	 *          all licenses issued to the {@link User} will expire.
	 */
	public final long getExpire() {
		return expire;
	}
	
	public final boolean isPermanent() {
		return this.getExpire() == -1;
	}
	
	public final boolean isExpired() {
		return !this.isPermanent() && System.currentTimeMillis() >= this.getExpire();
	}
	
	public final boolean isValid() {
		return !this.isExpired();
	}

	/**
	 * 
	 * @return Name of the Icon for {@link GUI}s
	 * @since 1.0.6
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final String getIconName() {
		return iconName;
	}

}
