package at.peirleitner.core.listener.local;

import javax.annotation.Nonnull;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import at.peirleitner.core.Core;
import at.peirleitner.core.SpigotMain;
import at.peirleitner.core.util.local.Rank;
import net.md_5.bungee.api.ChatColor;

public class AsyncPlayerChatListener implements Listener {

	public AsyncPlayerChatListener() {
		SpigotMain.getInstance().getServer().getPluginManager().registerEvents(this, SpigotMain.getInstance());
	}

	@EventHandler
	public void onAsyncPlayerChat(AsyncPlayerChatEvent e) {

		if (e.isCancelled())
			return;

		Player p = e.getPlayer();

		// Chat Format
		if (Core.getInstance().getSettingsManager().isChatFormatEnabled()) {

			Rank rank = this.getRank(p);

			e.setFormat(ChatColor.translateAlternateColorCodes('&', Core.getInstance().getSettingsManager().getChatFormat())
					.replace("{player}", rank.getChatColor() + p.getDisplayName())
					.replace("{message}", rank.getRankType().getTextColor() + e.getMessage()));
		}

	}

	private final Rank getRank(@Nonnull Player p) {

		for (Rank rank : SpigotMain.getInstance().getLocalScoreboard().getRanks()) {

			if (p.hasPermission("Core.rank." + rank.getName().toLowerCase())) {
				return rank;
			}

		}

		return SpigotMain.getInstance().getLocalScoreboard().getDefaultRank();
	}

}
