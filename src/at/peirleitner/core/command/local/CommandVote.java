package at.peirleitner.core.command.local;

import java.util.Arrays;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import at.peirleitner.core.Core;
import at.peirleitner.core.SpigotMain;
import at.peirleitner.core.manager.SettingsManager;

/**
 * Display URL to vote on towards the player
 * 
 * @since 1.0.14
 * @author Markus Peirleitner (Rengobli)
 * @see SettingsManager#getVoteURL()
 *
 */
public class CommandVote implements CommandExecutor {

	public CommandVote() {
		SpigotMain.getInstance().getCommand("vote").setExecutor(this);
	}

	@Override
	public boolean onCommand(CommandSender cs, Command cmd, String arg, String[] args) {

		Core.getInstance().getLanguageManager().sendMessage(cs, Core.getInstance().getPluginName(), "command.vote.text",
				Arrays.asList(Core.getInstance().getSettingsManager().getVoteURL()), true);
		return true;

	}

}
