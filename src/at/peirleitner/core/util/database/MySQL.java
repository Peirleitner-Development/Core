package at.peirleitner.core.util.database;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Iterator;

import javax.annotation.Nonnull;

import at.peirleitner.core.Core;
import at.peirleitner.core.util.LogType;

/**
 * Utility class to create a new MySQL Database connection
 * 
 * @since 1.0.0
 * @author Markus Peirleitner (Rengobli)
 *
 */
public final class MySQL {

	private String pluginName, host, database, username, password, tablePrefix;
	private int port;

	private Connection connection;

	public MySQL(@Nonnull String pluginName, @Nonnull String host, @Nonnull String database, @Nonnull int port,
			@Nonnull String username, @Nonnull String password, @Nonnull String tablePrefix) {

		this.pluginName = pluginName;
		this.host = host;
		this.database = database;
		this.port = port;
		this.username = username;
		this.password = password;
		this.tablePrefix = tablePrefix;

		this.connect();

	}

	public MySQL(@Nonnull String pluginName, @Nonnull File f) {

		this.pluginName = pluginName;
		
		if(!f.exists()) {
			Core.getInstance().log(pluginName, this.getClass(), LogType.ERROR, "Could not create MySQL Database Instance: Provided file doesn't exist.");
			return;
		}

		try {

			BufferedReader br = new BufferedReader(new FileReader(f));

			for (Iterator<String> it = br.lines().iterator(); it.hasNext();) {

				String s = it.next();

				if (s.startsWith("host: ")) {
					this.host = s.replace("host: ", "");

				} else if (s.startsWith("database: ")) {
					this.database = s.replace("database: ", "");

				} else if (s.startsWith("port: ")) {
					this.port = Integer.valueOf(s.replace("port: ", ""));

				} else if (s.startsWith("username: ")) {
					this.username = s.replace("username: ", "");

				} else if (s.startsWith("password: ")) {
					this.password = s.replace("password: ", "");

				} else if (s.startsWith("table-prefix: ")) {
					this.tablePrefix = s.replace("table-prefix: ", "");
				}

			}

			br.close();

		} catch (IOException e) {
			Core.getInstance().log(pluginName, this.getClass(), LogType.ERROR,
					"Could not create MySQL Instance: Error on closing reader: "
							+ e.getMessage());
		}

		this.connect();

	}

	/**
	 * 
	 * @return Prefix that should be attached to a table name
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final String getTablePrefix() {
		return this.tablePrefix;
	}
	
	public final void setTablePrefix(@Nonnull String prefix) {
		this.tablePrefix = prefix;
	}

	/**
	 * 
	 * @return Connection
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final Connection getConnection() {
		return this.connection;
	}

	/**
	 * 
	 * @return If this connection is established
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final boolean isConnected() {
		try {
			return this.connection != null && !connection.isClosed() ? true : false;
		} catch (SQLException e) {
			Core.getInstance().log(pluginName, this.getClass(), LogType.ERROR,
					"Could not check for database connection: " + e.getMessage());
			return false;
		}
	}

	/**
	 * Attempt to connect towards the database
	 * 
	 * @return If the connection could be established
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 */
	private final boolean connect() {

		try {

			this.connection = DriverManager.getConnection(
					"jdbc:mysql://" + host + ":" + port + "/" + database + "?autoReconnect=true", username, password);
			Core.getInstance().log(pluginName, this.getClass(), LogType.DEBUG,
					"Successfully connected to the MySQL Database " + database + ".");
			return true;
		} catch (SQLException e) {
			Core.getInstance().log(pluginName, this.getClass(), LogType.ERROR,
					"Could not connect to the MySQL Database: " + e.getMessage());
			return false;
		}

	}

	/**
	 * Close the connection
	 * 
	 * @return If the connection could be closed successfully
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final boolean close() {

		try {
			if (connection != null && !connection.isClosed()) {
				connection.close();
				Core.getInstance().log(pluginName, this.getClass(), LogType.INFO, "Successfully closed MySQL Database connection.");
				return true;
			} else {
				Core.getInstance().log(pluginName, this.getClass(), LogType.INFO, "Could not close database connection: Not connected.");
				return false;
			}
		} catch (SQLException e) {
			Core.getInstance().log(pluginName, this.getClass(), LogType.ERROR,
					"Error while attempting to close connection/ST: " + e.getMessage());
			return false;
		}

	}

}
