package at.peirleitner.core.command.local;

import java.util.Arrays;
import java.util.Collection;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import at.peirleitner.core.Core;
import at.peirleitner.core.SpigotMain;
import at.peirleitner.core.util.moderation.ChatLog;
import at.peirleitner.core.util.user.CorePermission;
import at.peirleitner.core.util.user.PredefinedMessage;
import net.md_5.bungee.api.ChatColor;

public class CommandMod implements CommandExecutor {

	public CommandMod() {
		SpigotMain.getInstance().getCommand("mod").setExecutor(this);
	}

	@Override
	public boolean onCommand(CommandSender cs, Command cmd, String arg, String[] args) {

		if (!cs.hasPermission(CorePermission.COMMAND_MOD.getPermission())) {
			cs.sendMessage(Core.getInstance().getLanguageManager().getMessage(PredefinedMessage.NO_PERMISSION));
			return true;
		}

		final Collection<ChatLog> chatLogs = Core.getInstance().getModerationSystem().getUnreviewedChatLogs();
		final int reports = 0;
		final int supportRequests = 0;
		
		final int total = (chatLogs.size() + reports + supportRequests);
		
		StringBuilder sbChatLogs = new StringBuilder();
		
		for(ChatLog cl : chatLogs) {
			sbChatLogs.append(ChatColor.WHITE + "" + cl.getID() + ChatColor.GRAY + ", ");
		}
		
		Core.getInstance().getLanguageManager().sendMessage(cs, Core.getInstance().getPluginName(), "command.mod.active-tasks", Arrays.asList(
				"" + total,
				"" + chatLogs.size(),
				sbChatLogs.toString(),
				"" + reports,
				"-", //TODO: Report List
				"" + supportRequests,
				"-" //TODO: Support Request List
				), true);
		return true;

	}

}
