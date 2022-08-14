package at.peirleitner.core.command.local;

import java.util.Arrays;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import at.peirleitner.core.Core;
import at.peirleitner.core.SpigotMain;
import at.peirleitner.core.util.user.CorePermission;
import at.peirleitner.core.util.user.Language;
import at.peirleitner.core.util.user.PredefinedMessage;
import at.peirleitner.core.util.user.User;
import net.md_5.bungee.api.ChatColor;

public class CommandLanguage implements CommandExecutor {

	public CommandLanguage() {
		SpigotMain.getInstance().getCommand("language").setExecutor(this);
	}

	@Override
	public boolean onCommand(CommandSender cs, Command cmd, String arg, String[] args) {

		// Return if the command isn't executed by a player
		if (!(cs instanceof Player)) {
			cs.sendMessage(
					Core.getInstance().getLanguageManager().getMessage(PredefinedMessage.ACTION_REQUIRES_PLAYER));
			return true;
		}

		// Return if the player doesn't have the required permissions
		if (!cs.hasPermission(CorePermission.COMMAND_LANGUAGE.getPermission())) {
			cs.sendMessage(Core.getInstance().getLanguageManager().getMessage(PredefinedMessage.NO_PERMISSION));
			return true;
		}

		Player p = (Player) cs;
		User user = Core.getInstance().getUserSystem().getUser(p.getUniqueId());

		if (args.length != 1) {

			// Send current selected language
			user.sendMessage(Core.getInstance().getPluginName(), "command.language.current-language",
					Arrays.asList(user.getLanguage().getNativeName(), this.getAvailableLanguages()), true);

		} else {
			// Update language

			try {

				Language language = Language.valueOf(args[0].toUpperCase());
				Core.getInstance().getUserSystem().setLanguage(user, language);
				user.sendMessage(Core.getInstance().getPluginName(), "command.language.language-updated", Arrays.asList(language.getNativeName()), true);

			} catch (IllegalArgumentException ex) {
				user.sendMessage(Core.getInstance().getPluginName(), "command.language.language-not-found",
						Arrays.asList(args[0], this.getAvailableLanguages()), true);
			}

		}

		return true;
	}

	private final String getAvailableLanguages() {

		StringBuilder sb = new StringBuilder();
		int size = Language.values().length;
		int current = 0;
		
		for (Language language : Language.values()) {
			current++;
			sb.append(ChatColor.WHITE + language.getEnglishName() + (current < size ? ChatColor.GRAY + ", " : ""));
		}

		return sb.toString();
	}

}
