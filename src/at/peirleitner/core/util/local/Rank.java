package at.peirleitner.core.util.local;

import javax.annotation.Nonnull;

import net.md_5.bungee.api.ChatColor;

/**
 * This class represents a rank from the ranks.json file
 * @since 1.0.0
 * @author Markus Peirleitner (Rengobli)
 *
 */
public class Rank {

	private int priority;
	private String name;
	private String displayName;
	private String color;
	private RankType rankType;
	private boolean isDefault;

	public Rank(@Nonnull int priority, @Nonnull String name, @Nonnull String displayName, @Nonnull String color, @Nonnull RankType rankType, @Nonnull boolean isDefault) {
		this.priority = priority;
		this.name = name;
		this.displayName = displayName;
		this.color = color;
		this.rankType = rankType;
		this.isDefault = isDefault;
	}

	public int getPriority() {
		return priority;
	}

	public String getName() {
		return name;
	}

	public String getDisplayName() {
		return displayName;
	}
	
	public String getColor() {
		return color;
	}
	
	public ChatColor getChatColor() {
		return ChatColor.of(this.getColor());
	}
	
	public String getColoredDisplayName() {
		return ChatColor.of(this.getColor()) + this.getDisplayName();
	}
	
	public RankType getRankType() {
		return this.rankType;
	}
	
	public boolean isDefault() {
		return this.isDefault;
	}

}
