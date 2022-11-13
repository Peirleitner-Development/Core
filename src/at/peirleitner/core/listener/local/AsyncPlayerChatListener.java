package at.peirleitner.core.listener.local;

import java.util.Arrays;
import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import at.peirleitner.core.Core;
import at.peirleitner.core.SpigotMain;
import at.peirleitner.core.util.DiscordWebHookType;
import at.peirleitner.core.util.LogType;
import at.peirleitner.core.util.moderation.ChatLog;
import at.peirleitner.core.util.moderation.UserChatMessage;
import at.peirleitner.core.util.moderation.UserChatMessageFlag;
import at.peirleitner.core.util.user.CorePermission;
import at.peirleitner.core.util.user.Rank;
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
		
		// Check for current Chat Restriction
		if(Core.getInstance().getModerationSystem().hasActiveChatLog(p.getUniqueId())) {
			user.sendAsyncMessage(Core.getInstance().getPluginName(),
					"system.moderation.chat-log-restriction-active", Arrays.asList("" + Core.getInstance().getModerationSystem().getActiveChatLog(user.getUUID()).getID()), true);
			e.setCancelled(true);
			return;
		}

		// Define Message
		UserChatMessage userChatMessage = new UserChatMessage(p.getUniqueId(), ChatColor.stripColor(e.getMessage()));

		// Checks
		Collection<UserChatMessageFlag> flags = Core.getInstance().getModerationSystem().checkMessage(user.getUUID(),
				userChatMessage.getMessage());

		Core.getInstance().log(getClass(), LogType.DEBUG, "Flags: " + flags.toString());

		if (!flags.isEmpty()) {

			userChatMessage.setFlags(flags);
			Core.getInstance().getModerationSystem().getCachedMessages().add(userChatMessage);

			for (UserChatMessageFlag flag : flags) {

				if (flag.isForceChatRestriction()) {

					ChatLog chatLog = Core.getInstance().getModerationSystem().createChatLog(null, user.getUUID(), "SYSTEM_MODERATION");

					if (chatLog == null) {
						Core.getInstance().log(getClass(), LogType.WARNING,
								"Could not create ChatLog! Message won't be sent, but the User won't be restricted either due to lack of verification.");
						break;
					}

					// Create Messages
					user.sendAsyncMessage(Core.getInstance().getPluginName(),
							"system.moderation.chat-log-restriction-active", Arrays.asList("" + chatLog.getID()), true);
					Core.getInstance().createWebhook(user.getLastKnownName()
							+ " has been flagged by the Chat Message Filter and automatically been restricted from the Chat. ChatLog-ID: "
							+ chatLog.getID() + ". Flags: " + flags.toString(), DiscordWebHookType.STAFF_NOTIFICATION);

					// Break out since it is no longer required to loop through
					break;

				}

				if (flag == UserChatMessageFlag.SPAM) {
					user.sendAsyncMessage(Core.getInstance().getPluginName(), "system.moderation.chat-spam", null, true);
					continue;
				}

				if (flag == UserChatMessageFlag.CAPS) {
					user.sendAsyncMessage(Core.getInstance().getPluginName(), "system.moderation.chat-caps", null, true);
					continue;
				}

				if (flag == UserChatMessageFlag.COOLDOWN
						&& !p.hasPermission(CorePermission.BYPASS_CHAT_COOLDOWN.getPermission())) {
					user.sendAsyncMessage(Core.getInstance().getPluginName(), "system.moderation.chat-cooldown",
							Arrays.asList("" + Core.getInstance().getModerationSystem().getChatCooldown()), true);
					continue;
				}

			}

			e.setCancelled(true);
			return;

		}

		// Chat Format
		if (Core.getInstance().getSettingsManager().isChatFormatEnabled()) {

			Rank rank = user.getRank();
			String message = e.getMessage();
			message = message.replace("%", "%%");

			// Chat Mention
			if (this.isChatMentionPingEnabled()) {

				for (Player all : Bukkit.getOnlinePlayers()) {

					if (e.getMessage().contains(all.getName())) {
						message = message.replace(all.getName(),
								(ChatColor.DARK_AQUA + "@" + all.getName()) + rank.getRankType().getTextColor());

						if (all == p) {
							all.playSound(all.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0F, 1.0F);
						}

					}

				}

			}

			e.setFormat(
					ChatColor.translateAlternateColorCodes('&', Core.getInstance().getSettingsManager().getChatFormat())
							.replace("{player}", rank.getChatColor() + p.getDisplayName())
							.replace("{message}", rank.getRankType().getTextColor() + message));

			Core.getInstance().getModerationSystem().getCachedMessages().add(userChatMessage);

			Core.getInstance().createWebhook("[" + rank.getName() + "] " + user.getLastKnownName() + ": " + message,
					DiscordWebHookType.USER_CHAT_MESSAGE);

		}
	}

	private final boolean isChatMentionPingEnabled() {
		return Core.getInstance().getSettingsManager().isSetting(Core.getInstance().getPluginName(),
				"manager.settings.chat.enable-mention-pings");
	}

}
