package at.peirleitner.core.command.local;

import java.util.Arrays;

import javax.annotation.Nonnull;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import at.peirleitner.core.Core;
import at.peirleitner.core.SpigotMain;
import at.peirleitner.core.util.user.CorePermission;
import at.peirleitner.core.util.user.Language;
import at.peirleitner.core.util.user.PredefinedMessage;

public class CommandSlot implements CommandExecutor {

	public CommandSlot() {
		SpigotMain.getInstance().getCommand("slot").setExecutor(this);
	}

	@Override
	public boolean onCommand(CommandSender cs, Command cmd, String arg, String[] args) {

		if (!cs.hasPermission(CorePermission.COMMAND_SLOT_DISPLAY.getPermission())
				&& !cs.hasPermission(CorePermission.COMMAND_SLOT_CHANGE.getPermission())) {
			cs.sendMessage(Core.getInstance().getLanguageManager().getMessage(PredefinedMessage.NO_PERMISSION));
			return true;
		}

		Player p = null;

		if (cs instanceof Player) {
			p = (Player) cs;
		}

		if (args.length == 0) {

			int slots = Core.getInstance().getSettingsManager().getSlots();

			Core.getInstance().getLanguageManager().sendMessage(cs, Core.getInstance().getPluginName(),
					"command.slot.display", Arrays.asList("" + slots), true);
			return true;

		} else if (args.length == 1) {

			try {

				int slots = Integer.valueOf(args[0]);

				if (Core.getInstance().getSettingsManager().setSlots(slots)) {

					Core.getInstance().getLanguageManager().sendMessage(cs, Core.getInstance().getPluginName(),
							"command.slot.set.success", Arrays.asList("" + slots), true);
					Core.getInstance().getLanguageManager().notifyStaff(Core.getInstance().getPluginName(),
							"command.slot.set.notify",
							Arrays.asList(p == null ? cs.getName()
									: Core.getInstance().getUserSystem().getUser(p.getUniqueId()).getDisplayName(),
									"" + slots),
							false);
					return true;

				} else {
					Core.getInstance().getLanguageManager().sendMessage(cs, Core.getInstance().getPluginName(),
							"command.slot.set.error.could-not-update-settings", null, true);
					return true;
				}

			} catch (NumberFormatException ex) {
				Core.getInstance().getLanguageManager().sendMessage(cs, Core.getInstance().getPluginName(),
						"command.slot.set.error.invalid-amount", Arrays.asList(args[0]), true);
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
