package at.peirleitner.core.command.local;

import java.util.Arrays;

import javax.annotation.Nonnull;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import at.peirleitner.core.Core;
import at.peirleitner.core.SpigotMain;
import at.peirleitner.core.util.GlobalUtils;
import at.peirleitner.core.util.moderation.ChatLog;
import at.peirleitner.core.util.moderation.UserChatMessage;
import at.peirleitner.core.util.user.CorePermission;
import at.peirleitner.core.util.user.Language;
import at.peirleitner.core.util.user.LanguagePhrase;
import at.peirleitner.core.util.user.PredefinedMessage;

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

		if (!cs.hasPermission(CorePermission.COMMAND_CHATLOG.getPermission())) {
			cs.sendMessage(Core.getInstance().getLanguageManager().getMessage(PredefinedMessage.NO_PERMISSION));
			return true;
		}

		if (args.length != 1) {
			this.sendHelp(cs);
			return true;
		}

		Player p = (Player) cs;

		try {

			int id = Integer.valueOf(args[0]);
			ChatLog chatLog = Core.getInstance().getModerationSystem().getChatLog(id);

			if (chatLog == null) {
				Core.getInstance().getLanguageManager().sendMessage(cs, Core.getInstance().getPluginName(),
						"command.chatlog.error.none-found-with-given-id", Arrays.asList("" + id), true);
				return true;
			}

			UserChatMessage message = chatLog.getChatMessage();

//			Core.getInstance().log(getClass(), LogType.DEBUG, message.toString());
//			Core.getInstance().log(getClass(), LogType.DEBUG, chatLog.toString());

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
									: LanguagePhrase.NONE.getTranslatedText(p.getUniqueId()))),
					true);

		} catch (NumberFormatException ex) {
			cs.sendMessage(Core.getInstance().getLanguageManager().getMessage(PredefinedMessage.INVALID_ID));
			return true;
		}

		return true;
	}

	private final void sendHelp(@Nonnull CommandSender cs) {
		cs.sendMessage(Core.getInstance().getLanguageManager().getMessage(Core.getInstance().getPluginName(),
				Language.ENGLISH, "command.chatlog.syntax", null));
	}

}
