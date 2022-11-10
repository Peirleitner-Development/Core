package at.peirleitner.core.command.local;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import at.peirleitner.core.Core;
import at.peirleitner.core.SpigotMain;
import at.peirleitner.core.util.LogType;
import at.peirleitner.core.util.user.Language;
import at.peirleitner.core.util.user.User;
import net.md_5.bungee.api.ChatColor;

/**
 * Display help in the Chat
 * 
 * @since 1.0.11
 * @author Markus Peirleitner (Rengobli)
 */
public class CommandHelp implements CommandExecutor {

	public CommandHelp() {
		SpigotMain.getInstance().getCommand("help").setExecutor(this);
	}

	@Override
	public boolean onCommand(CommandSender cs, Command cmd, String arg, String[] args) {

		if (!getFile().exists()) {

			try {

				boolean success = getFile().createNewFile();

				if (success) {
					Core.getInstance().log(getClass(), LogType.DEBUG, "Created default Help-File (/help)");

					YamlConfiguration cfg = YamlConfiguration.loadConfiguration(getFile());
					List<String> list = new ArrayList<>();
					list.add("&7{serverName} Server Help");
					list.add("&6/language &f- &7Change current Language");
//					list.add("&6/license &f- &7Your Licenses");
//					list.add("&6/money &f- &7Display current Economy");
//					list.add("&6/pay &f- &7Send money to another Player");
//					list.add("&6/msg &f- &7Send private messages");
//					list.add("&6/support &f- &7Request Support from the Staff");
//					list.add("&6/report &f- &7Report a Player to the Staff");

					cfg.set("help." + Language.ENGLISH.toString(), list);
					cfg.save(getFile());

				}

			} catch (IOException e) {
				Core.getInstance().log(getClass(), LogType.ERROR,
						"Could not create default Help-File (/help): " + e.getMessage());
				return true;
			}

		}

		Language language = Language.ENGLISH;

		if (cs instanceof Player) {
			Player p = (Player) cs;
			User user = Core.getInstance().getUserSystem().getUser(p.getUniqueId());
			language = user.getLanguage();
		}

		YamlConfiguration cfg = YamlConfiguration.loadConfiguration(getFile());
		List<String> help = cfg.getStringList("help." + language.toString());

		if (help == null || help.isEmpty()) {
			help = cfg.getStringList("help." + Language.ENGLISH.toString());
		}

		for (String s : help) {
			s = s.replace("{serverName}", Core.getInstance().getSettingsManager().getServerName());
			cs.sendMessage(ChatColor.translateAlternateColorCodes('&', s));
		}

		return true;

	}

	/**
	 * 
	 * @return
	 */
	public static final File getFile() {
		return new File(SpigotMain.getInstance().getDataFolder() + "/help.yml");
	}

}
