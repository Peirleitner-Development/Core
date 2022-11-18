package at.peirleitner.core.command.local;

import java.util.Arrays;

import javax.annotation.Nonnull;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import at.peirleitner.core.Core;
import at.peirleitner.core.SpigotMain;
import at.peirleitner.core.util.DiscordWebHookType;
import at.peirleitner.core.util.GlobalUtils;
import at.peirleitner.core.util.moderation.ChatLog;
import at.peirleitner.core.util.moderation.ChatLogReviewResult;
import at.peirleitner.core.util.moderation.UserChatMessage;
import at.peirleitner.core.util.user.CorePermission;
import at.peirleitner.core.util.user.Language;
import at.peirleitner.core.util.user.LanguagePhrase;
import at.peirleitner.core.util.user.PredefinedMessage;
import at.peirleitner.core.util.user.User;

public class CommandChatLog implements CommandExecutor {

	public CommandChatLog() {
		SpigotMain.getInstance().getCommand("chatlog").setExecutor(this);
	}

	@Override
	public boolean onCommand(CommandSender cs, Command cmd, String arg, String[] args) {

		if (!(cs instanceof Player)) {
			cs.sendMessage(
					Core.getInstance().getLanguageManager().getMessage(PredefinedMessage.ACTION_REQUIRES_PLAYER));
			return true;
		}
		
		Player p = (Player) cs;
		User user = Core.getInstance().getUserSystem().getUser(p.getUniqueId());
		
		if(args.length == 1) {
			
			if (!cs.hasPermission(CorePermission.COMMAND_CHATLOG_CHECK.getPermission())) {
				cs.sendMessage(Core.getInstance().getLanguageManager().getMessage(PredefinedMessage.NO_PERMISSION));
				return true;
			}
			
			try {

				int id = Integer.valueOf(args[0]);
				ChatLog chatLog = Core.getInstance().getModerationSystem().getChatLog(id);

				if (chatLog == null) {
					Core.getInstance().getLanguageManager().sendMessage(cs, Core.getInstance().getPluginName(),
							"command.chatlog.error.none-found-with-given-id", Arrays.asList("" + id), true);
					return true;
				}

				UserChatMessage message = chatLog.getChatMessage();

//				Core.getInstance().log(getClass(), LogType.DEBUG, message.toString());
//				Core.getInstance().log(getClass(), LogType.DEBUG, chatLog.toString());

				Core.getInstance().getLanguageManager().sendMessage(cs, Core.getInstance().getPluginName(),
						"command.chatlog.info",
						Arrays.asList("" + chatLog.getID(), chatLog.getFlags().toString(),
								(chatLog.getStaff() == null ? LanguagePhrase.NOT_REVIEWED.getTranslatedText(p.getUniqueId())
										: Core.getInstance().getUserSystem().getUser(chatLog.getStaff()).getDisplayName()),
								(chatLog.getReviewed() == -1
										? LanguagePhrase.NOT_REVIEWED.getTranslatedText(p.getUniqueId())
										: GlobalUtils.getFormatedDate(chatLog.getReviewed())),
								(chatLog.getResult() == null
										? LanguagePhrase.NOT_REVIEWED.getTranslatedText(p.getUniqueId())
										: chatLog.getResult().toString()),
								"" + chatLog.getMessageID(),
								Core.getInstance().getUserSystem().getUser(message.getUUID()).getDisplayName(),
								message.getMessage(), GlobalUtils.getFormatedDate(message.getSent()),
								message.getSaveType().getName(), message.getType().toString(),
								(message.hasRecipient() ? message.getRecipientUser().getDisplayName()
										: LanguagePhrase.NONE.getTranslatedText(p.getUniqueId())),
								(message.hasMetaData() ? message.getMessage() : LanguagePhrase.NONE.getTranslatedText(p.getUniqueId()))
								),
						true);
				
				return true;

			} catch (NumberFormatException ex) {
				cs.sendMessage(Core.getInstance().getLanguageManager().getMessage(PredefinedMessage.INVALID_ID));
				return true;
			}
			
		} else if(args.length == 3) {
			
			if(args[0].equalsIgnoreCase("review")) {
				
				if (!cs.hasPermission(CorePermission.COMMAND_CHATLOG_REVIEW.getPermission())) {
					cs.sendMessage(Core.getInstance().getLanguageManager().getMessage(PredefinedMessage.NO_PERMISSION));
					return true;
				}
				
				int id = -1;
				ChatLogReviewResult result = null;
				ChatLog chatLog = null;
				
				try {
					
					id = Integer.valueOf(args[1]);
					chatLog = Core.getInstance().getModerationSystem().getChatLog(id);

					if (chatLog == null) {
						Core.getInstance().getLanguageManager().sendMessage(cs, Core.getInstance().getPluginName(),
								"command.chatlog.error.none-found-with-given-id", Arrays.asList("" + id), true);
						return true;
					}
					
				}catch(NumberFormatException ex) {
					cs.sendMessage(Core.getInstance().getLanguageManager().getMessage(PredefinedMessage.INVALID_ID));
					return true;
				}
				
				try {
					
					result = ChatLogReviewResult.valueOf(args[2].toUpperCase());
					
				}catch(IllegalArgumentException ex) {
					Core.getInstance().getLanguageManager().sendMessage(cs, Core.getInstance().getPluginName(),
							"command.chatlog.error.invalid-review-result", Arrays.asList("" + args[2], Arrays.asList(ChatLogReviewResult.values()).toString()), true);
					return true;
				}
				
				boolean success = Core.getInstance().getModerationSystem().reviewChatLog(user.getUUID(), id, result);
				
				if(success) {
					
					user.sendMessage(Core.getInstance().getPluginName(), "command.chatLog.review.success", Arrays.asList("" + id, result.toString()), true);
					Core.getInstance().getLanguageManager().notifyStaff(Core.getInstance().getPluginName(), "notify.chatLog.review.staff", Arrays.asList(user.getDisplayName(), "" + id, result.toString()), false);
					Core.getInstance().createWebhook(user.getLastKnownName() + " reviewed the ChatLog " + id + ". Result: " + result.toString(), DiscordWebHookType.STAFF_NOTIFICATION);
					
					User targetUser = Core.getInstance().getUserSystem().getUser(chatLog.getChatMessage().getUUID());
					targetUser.sendMessage(Core.getInstance().getPluginName(), "notify.chatLog.review.target", Arrays.asList(result.toString()), true);
					
					return true;
				
				} else {
					
					user.sendMessage(Core.getInstance().getPluginName(), "command.chatLog.review.error.sql", Arrays.asList("" + id), true);
					return true;
					
				}
				
				
				
			} else {
				this.sendHelp(cs);
				return true;
			}
			
		} else {
			this.sendHelp(cs);
			return true;
		}
	}

	private final void sendHelp(@Nonnull CommandSender cs) {
		cs.sendMessage(Core.getInstance().getLanguageManager().getMessage(Core.getInstance().getPluginName(),
				Language.ENGLISH, "command.chatlog.syntax", null));
	}

}
