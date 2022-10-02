package at.peirleitner.core.util;

public class CustomLocation {

	private final String worldName;
	private final double x;
	private final double y;
	private final double z;
	private final float yaw;
	private final float pitch;

	public CustomLocation(String worldName, double x, double y, double z, float yaw, float pitch) {
		this.worldName = worldName;
		this.x = x;
		this.y = y;
		this.z = z;
		this.yaw = yaw;
		this.pitch = pitch;
	}

	public final String getWorldName() {
		return worldName;
	}

	public final double getX() {
		return x;
	}

	public final double getY() {
		return y;
	}

	public final double getZ() {
		return z;
	}

	public final float getYaw() {
		return yaw;
	}

	public final float getPitch() {
		return pitch;
	}

	@Override
	public String toString() {
		return worldName + ";" + x + ";" + y + ";" + z + ";" + yaw + ";" + pitch;
	}

}
