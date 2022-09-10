package at.peirleitner.core.system;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import javax.annotation.Nonnull;

import at.peirleitner.core.Core;
import at.peirleitner.core.util.LogType;
import at.peirleitner.core.util.database.SaveType;

public class StatSystem {

	private final String table = Core.getInstance().table_stats;
	private final SaveType saveType = Core.getInstance().getSettingsManager().getSaveType();

	/**
	 * 
	 * @param uuid
	 * @param statistic
	 * @return Statistic amount <b>OR</b><br>
	 *         -1 - If the User doesn't have a Statistic <b>OR</b><br>
	 *         -2 - If an error occurs
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final int getStatistic(@Nonnull UUID uuid, @Nonnull String statistic) {

		try {

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection()
					.prepareStatement("SELECT amount FROM " + table + " WHERE uuid = ? AND saveType = ?");
			stmt.setString(1, uuid.toString());
			stmt.setInt(2, saveType.getID());

			ResultSet rs = stmt.executeQuery();

			if (!rs.next()) {
				return -1;
			} else {
				return rs.getInt(1);
			}

		} catch (SQLException e) {
			Core.getInstance().log(this.getClass(), LogType.ERROR,
					"Could not get Statistic '" + statistic + "' on SaveType '" + saveType.getName() + "' for UUID '"
							+ uuid.toString() + "'/SQL: " + e.getMessage());
			return -2;
		}

	}

	public final boolean hasStatistic(@Nonnull UUID uuid, @Nonnull String statistic) {
		return this.getStatistic(uuid, statistic) < 0 ? false : true;
	}

	public final boolean addStatistic(@Nonnull UUID uuid, @Nonnull String statistic, @Nonnull int amount) {

		// Create Statistic if none exists
		if (!this.hasStatistic(uuid, statistic)) {
			return this.createStatistic(uuid, statistic, amount);
		}

		try {

			int current = this.getStatistic(uuid, statistic);

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection().prepareStatement(
					"UPDATE " + table + " SET amount = ? WHERE uuid = ? AND saveType = ? AND statistic = ?");
			stmt.setInt(1, current + amount);
			stmt.setString(2, uuid.toString());
			stmt.setInt(3, this.saveType.getID());
			stmt.setString(4, statistic);

			stmt.execute();

			Core.getInstance().log(this.getClass(), LogType.ERROR, "Added Statistic '" + statistic + "' on SaveType '"
					+ saveType.getName() + "' for UUID '" + uuid.toString() + "' with value '" + amount + "'.");
			return true;

		} catch (SQLException e) {
			Core.getInstance().log(this.getClass(), LogType.ERROR,
					"Could not add Statistic '" + statistic + "' on SaveType '" + saveType.getName() + "' for UUID '"
							+ uuid.toString() + "'/SQL: " + e.getMessage());
			return false;
		}

	}

	private final boolean createStatistic(@Nonnull UUID uuid, @Nonnull String statistic, @Nonnull int amount) {

		try {

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection().prepareStatement(
					"INSERT INTO " + table + " (uuid, saveType, statistic, amount) VALUES (?, ?, ?, ?);");
			stmt.setString(1, uuid.toString());
			stmt.setInt(2, saveType.getID());
			stmt.setString(3, statistic);
			stmt.setInt(4, amount);

			stmt.execute();

			Core.getInstance().log(this.getClass(), LogType.ERROR, "Created Statistic '" + statistic + "' on SaveType '"
					+ saveType.getName() + "' for UUID '" + uuid.toString() + "' with value '" + amount + "'.");
			return true;

		} catch (SQLException e) {
			Core.getInstance().log(this.getClass(), LogType.ERROR,
					"Could not create Statistic '" + statistic + "' on SaveType '" + saveType.getName() + "' for UUID '"
							+ uuid.toString() + "'/SQL: " + e.getMessage());
			return false;
		}

	}

	/**
	 * Same as {@link #addStatistic(UUID, String, int)}, but increases it with 1
	 * 
	 * @param uuid
	 * @param statistic
	 * @return
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final boolean incrementStatistic(@Nonnull UUID uuid, @Nonnull String statistic) {
		return this.addStatistic(uuid, statistic, 1);
	}

}
