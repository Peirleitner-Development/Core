package at.peirleitner.core.util.database;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.annotation.Nonnull;

import net.nayola.core.NayolaCore;

public class MySQL {
	
	private String pluginName;
	private String host;
	private String database;
	private int port;
	private String username;
	private String password;
	
	private Connection connection;

	public MySQL(@Nonnull String pluginName, @Nonnull String host, @Nonnull String database, @Nonnull int port, @Nonnull String username, @Nonnull String password) {
		
		this.pluginName = pluginName;
		this.host = host;
		this.database = database;
		this.port = port;
		this.username = username;
		this.password = password;

		

		this.connect();

	}

	public Connection getConnection() {
		return this.connection;
	}

	public boolean prepareStatement(@Nonnull String statement) {

		try {
			PreparedStatement stmt = this.getConnection().prepareStatement(statement);
			stmt.execute();
			return true;
		} catch (SQLException e) {
			NayolaCore.getInstance().log(LogType.ERROR,
					"Error while attempting to perform preparedStatement inside MySQL: " + e.getMessage()
							+ " (Statement: " + statement + ").");
			return false;
		}

	}

	public boolean isConnected() {
		try {
			return this.connection != null && !connection.isClosed() ? true : false;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	private void connect() {

		// MySQL Database Connection
		try {

			this.connection = DriverManager.getConnection(
					"jdbc:mysql://" + host + ":" + port + "/" + database + "?autoReconnect=true", username, password);
			NayolaCore.getInstance().log(LogType.INFO,
					"Successfully connected to the MySQL Database " + database + ".");
		} catch (SQLException e) {
			NayolaCore.getInstance().log(LogType.ERROR, "Could not connect to the MySQL Database: " + e.getMessage());
			return;
		}

	}

	public void close() {

		try {
			if (connection != null && !connection.isClosed()) {
				connection.close();
				NayolaCore.getInstance().log(LogType.INFO, "Successfully closed MySQL Database connection.");
			}
		} catch (SQLException e) {
			NayolaCore.getInstance().log(LogType.ERROR,
					"Error while attempting to close connection: " + e.getMessage());
			return;
		}

	}

}
