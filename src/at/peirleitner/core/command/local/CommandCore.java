package at.peirleitner.core.command.local;

import javax.annotation.Nonnull;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginDescriptionFile;

import at.peirleitner.core.Core;
import at.peirleitner.core.SpigotMain;
import at.peirleitner.core.util.user.CorePermission;
import at.peirleitner.core.util.user.Language;
import net.md_5.bungee.api.ChatColor;

public class CommandCore implements CommandExecutor {

	public CommandCore() {
		SpigotMain.getInstance().getCommand("core").setExecutor(this);
	}

	private final String VERSION_OUTPUT = ChatColor.translateAlternateColorCodes('&',
			"&9{0} &7version &9{1} &7by &9{2}&7. Website&8: &9{3}&7.");

	@Override
	public boolean onCommand(CommandSender cs, Command cmd, String arg, String[] args) {

		// Send version output if the user isn't an admin or the console itself
		if (args.length == 0 || !cs.hasPermission(CorePermission.COMMAND_CORE_ADMIN.getPermission())) {

			PluginDescriptionFile pdf = SpigotMain.getInstance().getDescription();

			cs.sendMessage(Core.getInstance().getLanguageManager().getPrefix(Core.getInstance().getPluginName(),
					Language.ENGLISH)
					+ this.VERSION_OUTPUT.replace("{0}", pdf.getName()).replace("{1}", pdf.getVersion())
							.replace("{2}", pdf.getAuthors().get(0)).replace("{3}", pdf.getWebsite()));
			return true;
		}

		// Admin
		if (args.length == 1) {

			if (args[0].equalsIgnoreCase("loadDefaultSaveTypes")) {
				Core.getInstance().createDefaultSaveTypes();
				cs.sendMessage(Core.getInstance().getLanguageManager().getMessage(Core.getInstance().getPluginName(),
						Language.ENGLISH, "command.core.loadDefaultSaveTypes.info", null));
				return true;
				
			} else if(args[0].equalsIgnoreCase("reload")) {
				
				Core.getInstance().getModerationSystem().reload();
				
				return true;
				
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
				Language.ENGLISH, "command.core.syntax", null));
	}

}
