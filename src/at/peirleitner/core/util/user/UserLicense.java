package at.peirleitner.core.util.user;

import java.util.UUID;

import at.peirleitner.core.Core;

/**
 * {@link MasterLicense} that has been issued towards a {@link User}
 * 
 * @since 1.0.6
 * @author Markus Peirleitner (Rengobli)
 *
 */
public class UserLicense {

	private UUID owner;
	private int licenseID;
	private long issued;
	private long expire;

	public UserLicense(UUID owner, int licenseID, long issued, long expire) {
		this.owner = owner;
		this.licenseID = licenseID;
		this.issued = issued;
		this.expire = expire;
	}

	public final UUID getOwner() {
		return owner;
	}

	/**
	 * 
	 * @return Unique ID of the {@link MasterLicense}
	 * @since 1.0.6
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final int getLicenseID() {
		return licenseID;
	}

	public final MasterLicense getMasterLicense() {
		return Core.getInstance().getLicenseSystem().getMasterLicense(this.getLicenseID());
	}

	public final long getIssued() {
		return issued;
	}

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

}
