package at.peirleitner.core.command.local;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.sql.PreparedStatement;
import java.util.UUID;

import javax.annotation.Nonnull;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import at.peirleitner.core.Core;
import at.peirleitner.core.SpigotMain;
import at.peirleitner.core.util.LogType;
import at.peirleitner.core.util.UUIDFetcher;
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
			} else if (args[0].equalsIgnoreCase("importOldJsonData")) {

				
				new BukkitRunnable() {
					
					@Override
					public void run() {
						
						String c = "";
						
						try {
							JsonParser parser = new JsonParser();
							JsonArray a = (JsonArray) parser.parse(new FileReader(Core.getInstance().getDataFolder() + "/players.json"));

							File f = new File(Core.getInstance().getDataFolder() + "/sql.txt");
							if(!f.exists()) {
								f.createNewFile();
							}
							
							FileWriter fw = new FileWriter(f);
							
							int i=1;
							Core.getInstance().log(getClass(), LogType.INFO, "--- START IMPORT JSON DATA ---");
							for (Object o : a) {
								JsonObject person = (JsonObject) o;

								String uuid = person.get("uuid").getAsString();
								String rank = person.get("rank").getAsString();
								long firstJoinTimestamp = person.get("firstJoinTimestamp").getAsLong();
								long lastQuitTimestamp = person.get("lastQuitTimestamp").getAsLong();
								UUID realUUID = UUID.fromString(uuid);
								String name = UUIDFetcher.getName(realUUID);
								fw.write("INSERT IGNORE INTO players (uuid, lastKnownName, registered, lastLogin, lastLogout, rank) VALUES ('" + uuid + "', '" + name + "', '" + firstJoinTimestamp + "', '" + firstJoinTimestamp + "', '" + lastQuitTimestamp + "', '" + rank + "');\n");
								Core.getInstance().log(getClass(), LogType.INFO, "Imported old JSON-Data (#" + i + ") for UUID '" + uuid + "'.");
								i++;
//								Core.getInstance().log(getClass(), LogType.INFO, "Import #" + i + ": [uuid=" + uuid + ",rank=" + rank + ",firstJoinTimestamp=" + firstJoinTimestamp + ",lastQuitTimestamp=" + lastQuitTimestamp + "]");
//								i++;
								
							}
							
							fw.close();
							Core.getInstance().log(getClass(), LogType.INFO, "--- END IMPORT JSON DATA ---");
							
						}catch(Exception ex) {
							Core.getInstance().log(this.getClass(), LogType.ERROR, "Error on import: " + ex.getMessage() + ",uuid='" + c + "'");
							ex.printStackTrace();
						}
						
					}
				}.runTaskAsynchronously(SpigotMain.getInstance());

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
