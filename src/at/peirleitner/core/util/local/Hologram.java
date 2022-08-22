package at.peirleitner.core.util.local;

import javax.annotation.Nonnull;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;

public class Hologram {

	private Location loc;
	private String name;
	private ArmorStand entity;

	public Hologram(Location loc, String name) {
		this.loc = loc;
		this.name = name;
		this.entity = null;
	}

	public final Location getLocation() {
		return loc;
	}

	public final String getName() {
		return name;
	}

	public ArmorStand getEntity() {
		return entity;
	}
	
	public void setEntity(@Nonnull ArmorStand entity) {
		this.entity = entity;
	}

}
