package at.peirleitner.core.util;

import java.util.UUID;

import at.peirleitner.core.Core;
import at.peirleitner.core.system.MotdSystem;

/**
 * Message of the Day (MOTD)
 * 
 * @since 1.0.4
 * @author Markus Peirleitner (Rengobli)
 * @see MotdSystem
 */
public class MOTD {

	private final String line1;
	private final String line2;
	private final UUID staff;
	private final long changed;

	public MOTD(String line1, String line2, UUID staff, long changed) {
		this.line1 = line1;
		this.line2 = line2;
		this.staff = staff;
		this.changed = changed;
	}

	/**
	 * @deprecated See {@link #getFirstLine()}
	 * @return First line
	 * @since 1.0.4
	 * @author Markus Peirleitner (Rengobli)
	 */
	@Deprecated(since = "1.0.4", forRemoval = true)
	public final String getLine1() {
		return line1;
	}
	
	/**
	 * 
	 * @return First line
	 * @since 1.0.4
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final String getFirstLine() {
		return line1;
	}

	/**
	 * @deprecated See {@link #getSecondLine()}
	 * @return Second line
	 * @since 1.0.4
	 * @author Markus Peirleitner (Rengobli)
	 */
	@Deprecated(since = "1.0.4", forRemoval = true)
	public final String getLine2() {
		return line2;
	}
	
	/**
	 * 
	 * @return Second line
	 * @since 1.0.4
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final String getSecondLine() {
		return line2;
	}

	/**
	 * 
	 * @return UUID of the Staff that made the latest change towards the MOTD
	 * @since 1.0.4
	 * @author Markus Peirleitner (Rengobli)
	 * @apiNote May be <code>null</code> if changed by CONSOLE
	 */
	public final UUID getStaff() {
		return staff;
	}

	/**
	 * 
	 * @return Name of the Staff that made the latest change
	 * @since 1.0.4
	 * @author Markus Peirleitner (Rengobli)
	 * @apiNote This will return 'CONSOLE' if {@link #getStaff()} returns
	 *          <code>null</code>.
	 */
	public final String getStaffName() {
		return this.getStaff() == null ? "CONSOLE"
				: Core.getInstance().getUserSystem().getUser(this.getStaff()).getDisplayName();
	}

	/**
	 * 
	 * @return TimeStamp of the latest change
	 * @since 1.0.4
	 * @author Markus Peirleitner (Rengobli)
	 * @apiNote Default value if the MOTD has never been changed: -1
	 */
	public final long getChanged() {
		return changed;
	}

	@Override
	public final String toString() {
		return "MOTD[line1=" + line1 + ",line2=" + line2 + ",staff=" + staff + ",changed=" + changed + "]";
	}

}
