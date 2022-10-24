package at.peirleitner.core.command.local;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import at.peirleitner.core.Core;
import at.peirleitner.core.SpigotMain;
import at.peirleitner.core.system.EconomySystem;
import at.peirleitner.core.util.database.SaveType;
import at.peirleitner.core.util.user.CorePermission;
import at.peirleitner.core.util.user.Language;
import at.peirleitner.core.util.user.PredefinedMessage;
import at.peirleitner.core.util.user.User;
import net.md_5.bungee.api.ChatColor;

public class CommandEconomy implements CommandExecutor {

	public CommandEconomy() {
		SpigotMain.getInstance().getCommand("economy").setExecutor(this);
	}

	@Override
	public boolean onCommand(CommandSender cs, Command cmd, String arg, String[] args) {

		if (!(cs.hasPermission(CorePermission.COMMAND_ECONOMY.getPermission()))) {
			cs.sendMessage(Core.getInstance().getLanguageManager().getMessage(PredefinedMessage.NO_PERMISSION));
			return true;
		}

		if (args.length < 2) {
			this.sendHelp(cs);
			return true;
		}

		User targetUser = Core.getInstance().getUserSystem().getByLastKnownName(args[1]);

		if (targetUser == null) {
			cs.sendMessage(Core.getInstance().getLanguageManager().getMessage(PredefinedMessage.NOT_REGISTERED));
			return true;
		}

		if (args.length == 2) {

			if (args[0].equalsIgnoreCase("get")) {

				HashMap<SaveType, Double> economy = this.getEconomySystem().getEconomy(targetUser.getUUID());

				if (economy.isEmpty()) {
					Core.getInstance().getLanguageManager().sendMessage(cs, Core.getInstance().getPluginName(),
							"command.economy.get.list.no-economy", Arrays.asList(targetUser.getDisplayName()), true);
					return true;
				}

				Core.getInstance().getLanguageManager().sendMessage(cs, Core.getInstance().getPluginName(),
						"command.economy.get.list.pre-text", Arrays.asList(targetUser.getDisplayName()), true);
				for (Map.Entry<SaveType, Double> entry : economy.entrySet()) {
					cs.sendMessage(ChatColor.DARK_GRAY + "- " + ChatColor.GRAY + entry.getKey().getName() + ": "
							+ entry.getValue() + this.getEconomySystem().getChar());
				}

			} else {
				this.sendHelp(cs);
				return true;
			}

		} else if (args.length == 3) {

			if (args[0].equalsIgnoreCase("get")) {

				try {

					int id = Integer.valueOf(args[2]);
					SaveType saveType = Core.getInstance().getSaveTypeByID(id);

					if (saveType == null) {
						cs.sendMessage(
								Core.getInstance().getLanguageManager().getMessage(PredefinedMessage.INVALID_SAVETYPE));
						return true;
					}

					double economy = this.getEconomySystem().getEconomy(targetUser.getUUID(), saveType);

					Core.getInstance().getLanguageManager().sendMessage(cs, Core.getInstance().getPluginName(),
							"command.economy.get.list-economy", Arrays.asList(targetUser.getDisplayName(), "" + economy,
									"" + this.getEconomySystem().getChar(), saveType.getName()),
							true);
					return true;

				} catch (NumberFormatException ex) {
					cs.sendMessage(Core.getInstance().getLanguageManager().getMessage(PredefinedMessage.INVALID_ID));
					return true;
				}

			} else {
				this.sendHelp(cs);
				return true;
			}

		} else if (args.length == 4) {

			SaveType saveType = null;
			double amount = -1;

			try {

				int id = Integer.valueOf(args[2]);
				amount = Double.valueOf(args[3]);
				saveType = Core.getInstance().getSaveTypeByID(id);

				if (saveType == null) {
					cs.sendMessage(
							Core.getInstance().getLanguageManager().getMessage(PredefinedMessage.INVALID_SAVETYPE));
					return true;
				}

			} catch (NumberFormatException ex) {
				Core.getInstance().getLanguageManager().sendMessage(cs, Core.getInstance().getPluginName(),
						"command.economy.main.error.invalid-id-or-amount", Arrays.asList(args[2], args[3]), true);
				return true;
			}

			if (args[0].equalsIgnoreCase("add")) {

				boolean success = this.getEconomySystem().addEconomy(targetUser.getUUID(), saveType, amount);

				if (success) {
					Core.getInstance().getLanguageManager().sendMessage(cs, Core.getInstance().getPluginName(),
							"command.economy.add.success.sender", Arrays.asList(targetUser.getDisplayName(),
									"" + amount, "" + this.getEconomySystem().getChar(), saveType.getName()),
							true);
					return true;
				} else {
					Core.getInstance().getLanguageManager().sendMessage(cs, Core.getInstance().getPluginName(),
							"command.economy.add.error.sql", Arrays.asList(targetUser.getDisplayName()), true);
					return true;
				}

			} else if (args[0].equalsIgnoreCase("remove")) {

				boolean success = this.getEconomySystem().removeEconomy(targetUser.getUUID(), saveType, amount);

				if (success) {
					Core.getInstance().getLanguageManager()
							.sendMessage(cs, Core.getInstance().getPluginName(),
									"command.economy.remove.success.sender", Arrays.asList(targetUser.getDisplayName(),
											"" + amount, "" + this.getEconomySystem().getChar(), saveType.getName()),
									true);
					return true;
				} else {
					Core.getInstance().getLanguageManager().sendMessage(cs, Core.getInstance().getPluginName(),
							"command.economy.remove.error.sql", Arrays.asList(targetUser.getDisplayName()), true);
					return true;
				}

			} else if (args[0].equalsIgnoreCase("set")) {

				boolean success = this.getEconomySystem().setEconomy(targetUser.getUUID(), saveType, amount);

				if (success) {
					Core.getInstance().getLanguageManager().sendMessage(cs, Core.getInstance().getPluginName(),
							"command.economy.set.success.sender", Arrays.asList(targetUser.getDisplayName(),
									"" + amount, "" + this.getEconomySystem().getChar(), saveType.getName()),
							true);
					return true;
				} else {
					Core.getInstance().getLanguageManager().sendMessage(cs, Core.getInstance().getPluginName(),
							"command.economy.set.error.sql", Arrays.asList(targetUser.getDisplayName()), true);
					return true;
				}

			} else {
				this.sendHelp(cs);
				return true;
			}

		}

		return true;
	}

	private final void sendHelp(@Nonnull CommandSender cs) {
		cs.sendMessage(Core.getInstance().getLanguageManager().getMessage(Core.getInstance().getPluginName(),
				Language.ENGLISH, "command.economy.syntax", null));
	}

	private final EconomySystem getEconomySystem() {
		return Core.getInstance().getEconomySystem();
	}

}
