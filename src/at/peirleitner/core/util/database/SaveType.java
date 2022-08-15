package at.peirleitner.core.util.database;

import javax.annotation.Nonnull;

import org.bukkit.Material;

public class SaveType {

	private String name;
	private Material icon;

	public SaveType(@Nonnull String name, @Nonnull Material icon) {
		this.name = name;
		this.icon = icon;
	}

	public final String getName() {
		return name;
	}

	public final Material getIcon() {
		return icon;
	}

}
