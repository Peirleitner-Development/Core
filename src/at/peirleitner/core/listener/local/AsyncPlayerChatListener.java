package at.peirleitner.core.listener.local;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import at.peirleitner.core.Core;
import at.peirleitner.core.SpigotMain;
import at.peirleitner.core.util.local.Rank;
import at.peirleitner.core.util.user.User;
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
		User user = Core.getInstance().getUserSystem().getUser(p.getUniqueId());

		// Chat Format
		if (Core.getInstance().getSettingsManager().isChatFormatEnabled()) {

			Rank rank = user.getRank();
			String message = e.getMessage();
			message = message.replace("%", "%%");

			e.setFormat(
					ChatColor.translateAlternateColorCodes('&', Core.getInstance().getSettingsManager().getChatFormat())
							.replace("{player}", rank.getChatColor() + p.getDisplayName())
							.replace("{message}", rank.getRankType().getTextColor() + message));
			
			// Chat Mention
			if(this.isChatMentionPingEnabled()) {
				
				for(Player all : Bukkit.getOnlinePlayers()) {
					
					if(e.getMessage().contains(all.getName())) {
						String s = e.getMessage();
						s = s.replace(all.getName(), ChatColor.DARK_AQUA + "@" + all.getName() + rank.getRankType().getTextColor());
						e.setMessage(s);
						all.playSound(all.getLocation(), Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1.0F, 1.0F);
					}
					
				}
				
			}
			
		}
	}
	
	private final boolean isChatMentionPingEnabled() {
		return Core.getInstance().getSettingsManager().isSetting(Core.getInstance().getPluginName(), "manager.settings.chat.enable-mention-pings");
	}

}
