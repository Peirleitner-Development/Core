package at.peirleitner.core.util.database;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.annotation.Nonnull;

import at.peirleitner.core.BungeeMain;
import at.peirleitner.core.Core;
import at.peirleitner.core.SpigotMain;
import at.peirleitner.core.util.LogType;
import at.peirleitner.core.util.RunMode;

public class CredentialsFile {

	public final static File getCredentialsFile(@Nonnull String pluginName) {

		RunMode runMode = Core.getInstance().getRunMode();
		File path = runMode == RunMode.LOCAL ? SpigotMain.getInstance().getDataFolder()
				: BungeeMain.getInstance().getDataFolder();
		File f = new File(path + "/mysql.yml");

		if (!f.exists()) {
			Core.getInstance().log(pluginName, LogType.DEBUG,
					"Could not find MySQL Data file for Plugin " + pluginName + ", attempting to create a new one..");
			try {
				f.createNewFile();

				BufferedWriter bw = new BufferedWriter(new FileWriter(f));
				bw.write("\nhost: localhost");
				bw.write("\ndatabase: core");
				bw.write("\nport: 3306");
				bw.write("\nusername: root");
				bw.write("\npassword: passy");
				bw.write("\ntable-prefix: core_");
				bw.close();

				Core.getInstance().log(pluginName, LogType.DEBUG, "Successfully created a new MySQL Data file.");
			} catch (IOException e) {
				Core.getInstance().log(pluginName, LogType.ERROR,
						"Could not create new MySQL Data file: " + e.getMessage());
				return null;
			}
		} else {
			Core.getInstance().log(pluginName, LogType.DEBUG,
					"Did not attempt to create a new MySQL Data file because one does already exist.");
		}

		return f;
	}

}
