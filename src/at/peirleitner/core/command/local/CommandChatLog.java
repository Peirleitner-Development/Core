package at.peirleitner.core.command.local;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import at.peirleitner.core.Core;
import at.peirleitner.core.SpigotMain;
import at.peirleitner.core.util.GlobalUtils;
import at.peirleitner.core.util.moderation.ChatLog;
import at.peirleitner.core.util.moderation.UserChatMessage;
import at.peirleitner.core.util.user.CorePermission;
import at.peirleitner.core.util.user.Language;
import at.peirleitner.core.util.user.PredefinedMessage;
import at.peirleitner.core.util.user.User;
import net.md_5.bungee.api.ChatColor;

public class CommandChatLog implements CommandExecutor {

	public CommandChatLog() {
		SpigotMain.getInstance().getCommand("chatlog").setExecutor(this);
	}

	@Override
	public boolean onCommand(CommandSender cs, Command cmd, String arg, String[] args) {

		if (!cs.hasPermission(CorePermission.COMMAND_CHATLOG.getPermission())) {
			cs.sendMessage(Core.getInstance().getLanguageManager().getMessage(PredefinedMessage.NO_PERMISSION));
			return true;
		}

		if (args.length != 1) {
			this.sendHelp(cs);
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

			List<UserChatMessage> messages = new ArrayList<>();
			messages.addAll(chatLog.getMessages());

			Core.getInstance().getLanguageManager().sendMessage(cs, Core.getInstance().getPluginName(),
					"command.chatlog.info",
					Arrays.asList("" + chatLog.getID(), GlobalUtils.getFormatedDate(chatLog.getCreated()),
							chatLog.getCreatorName(), chatLog.hasComment() ? "N/A" : chatLog.getComment(),
							"" + messages.size()),
					true);

			User user = Core.getInstance().getUserSystem().getUser(messages.get(0).getUUID());

			for (UserChatMessage ucm : messages) {

				cs.sendMessage((ucm.hasFlags() ? ChatColor.RED + "" + ChatColor.BOLD + ucm.getFlags().toString() : "")
						+ ChatColor.GRAY + "[" + GlobalUtils.getFormatedDate(ucm.getCreated()) + "] "
						+ user.getDisplayName() + ChatColor.DARK_GRAY + ": " + ChatColor.GRAY + ucm.getMessage());

			}

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
