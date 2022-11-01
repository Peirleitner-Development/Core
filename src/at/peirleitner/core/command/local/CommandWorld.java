package at.peirleitner.core.command.local;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import at.peirleitner.core.Core;
import at.peirleitner.core.SpigotMain;
import at.peirleitner.core.util.database.SaveType.WorldType;
import at.peirleitner.core.util.local.LocalUtils;
import at.peirleitner.core.util.user.CorePermission;
import at.peirleitner.core.util.user.Language;
import at.peirleitner.core.util.user.LanguagePhrase;
import at.peirleitner.core.util.user.PredefinedMessage;
import at.peirleitner.core.util.user.User;

/**
 * Allows management of Bukkit Worlds (Create, Delete, Un-/Load, Import,
 * Teleport, List)
 * 
 * @since 1.0.10
 * @author Markus Peirleitner (Rengobli)
 *
 */
public class CommandWorld implements CommandExecutor {

	public CommandWorld() {
		SpigotMain.getInstance().getCommand("world").setExecutor(this);
	}

	@Override
	public boolean onCommand(CommandSender cs, Command cmd, String arg, String[] args) {

		if (!(cs instanceof Player)) {
			cs.sendMessage(
					Core.getInstance().getLanguageManager().getMessage(PredefinedMessage.ACTION_REQUIRES_PLAYER));
			return true;
		}

		if (!cs.hasPermission(CorePermission.COMMAND_WORLD.getPermission())) {
			cs.sendMessage(Core.getInstance().getLanguageManager().getMessage(PredefinedMessage.NO_PERMISSION));
			return true;
		}

		Player p = (Player) cs;
		User user = Core.getInstance().getUserSystem().getUser(p.getUniqueId());

		if (args.length == 0) {

			user.sendMessage(Core.getInstance().getPluginName(), "command.world.current-world",
					Arrays.asList(p.getWorld().getName()), true);
			return true;

		} else if (args.length == 1) {

			if (args[0].equalsIgnoreCase("list")) {

				List<String> worlds = new ArrayList<>();

				for (File f : Bukkit.getWorldContainer().listFiles()) {

					if (!f.isDirectory()) {
						continue;
					}

					worlds.add(f.getName());

				}

				user.sendMessage(Core.getInstance().getPluginName(), "command.world.list.pre-text",
						Arrays.asList("" + worlds.size()), true);

				for (String world : worlds) {
					user.sendMessage(Core.getInstance().getPluginName(), "command.world.list.world-text",
							Arrays.asList(world,
									Bukkit.getWorld(world) == null
											? LanguagePhrase.NOT_LOADED.getTranslatedText(user.getUUID())
											: LanguagePhrase.LOADED.getTranslatedText(user.getUUID())),
							true);
				}

				return true;
			} else {
				this.sendHelp(cs);
				return true;
			}

		} else if (args.length == 2) {

			final String worldName = args[1];

			if (args[0].equalsIgnoreCase("delete")) {

				LocalUtils.deleteWorld(user, worldName);
				return true;

			} else if (args[0].equalsIgnoreCase("load")) {

				boolean success = LocalUtils.loadWorld(worldName);

				user.sendMessage(Core.getInstance().getPluginName(),
						"command.world.load." + (success ? "success" : "error"), Arrays.asList(worldName), true);
				return true;

			} else if (args[0].equalsIgnoreCase("unload")) {

				boolean success = LocalUtils.unloadWorld(worldName);

				user.sendMessage(Core.getInstance().getPluginName(),
						"command.world.unload." + (success ? "success" : "error"), Arrays.asList(worldName), true);
				return true;

			} else if (args[0].equalsIgnoreCase("tp")) {

				LocalUtils.teleportToWorld(user, worldName);
				return true;

			} else {
				this.sendHelp(cs);
				return true;
			}

		} else if (args.length == 3) {

			if (args[0].equalsIgnoreCase("create")) {

				try {

					String worldName = args[1];
					WorldType worldType = WorldType.valueOf(args[2].toUpperCase());

					LocalUtils.createWorld(user, worldName, worldType);
					return true;

				} catch (IllegalArgumentException ex) {

					StringBuilder sb = new StringBuilder();
					int current = 0;

					for (WorldType wt : WorldType.values()) {
						current++;
						sb.append(wt.toString() + (current >= WorldType.values().length ? "" : ", "));
					}

					user.sendMessage(Core.getInstance().getPluginName(), "command.world.create.error.invalid-worldType",
							Arrays.asList(args[2], sb.toString()), true);
					return true;
				}

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
				Language.ENGLISH, "command.world.syntax", null));
	}

}
