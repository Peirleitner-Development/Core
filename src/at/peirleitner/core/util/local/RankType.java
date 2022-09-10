package at.peirleitner.core.util.local;

import javax.annotation.Nonnull;

import net.md_5.bungee.api.ChatColor;

public enum RankType {

	STAFF(ChatColor.YELLOW), VIP(ChatColor.GREEN), DONATOR(ChatColor.AQUA), USER(ChatColor.WHITE);

	private ChatColor textColor;

	private RankType(@Nonnull ChatColor textColor) {
		this.textColor = textColor;
	}

	public final ChatColor getTextColor() {
		return this.textColor;
	}

}
