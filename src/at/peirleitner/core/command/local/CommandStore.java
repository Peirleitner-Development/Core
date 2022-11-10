package at.peirleitner.core.command.local;

import java.util.Arrays;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import at.peirleitner.core.Core;
import at.peirleitner.core.SpigotMain;
import at.peirleitner.core.manager.SettingsManager;

/**
 * Display the Store URL in the Chat
 * 
 * @since 1.0.11
 * @author Markus Peirleitner (Rengobli)
 * @see SettingsManager#getServerStore()
 *
 */
public class CommandStore implements CommandExecutor {

	public CommandStore() {
		SpigotMain.getInstance().getCommand("store").setExecutor(this);
	}

	@Override
	public boolean onCommand(CommandSender cs, Command cmd, String arg, String[] args) {

		Core.getInstance().getLanguageManager().sendMessage(cs, Core.getInstance().getPluginName(),
				"command.store.text", Arrays.asList(Core.getInstance().getSettingsManager().getServerStore()), true);
		return true;

	}

}
