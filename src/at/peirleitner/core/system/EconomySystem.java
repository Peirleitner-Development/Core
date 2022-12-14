package at.peirleitner.core.system;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

import javax.annotation.Nonnull;

import at.peirleitner.core.Core;
import at.peirleitner.core.command.local.CommandPay;
import at.peirleitner.core.manager.SettingsManager;
import at.peirleitner.core.util.CoreSystem;
import at.peirleitner.core.util.LogType;
import at.peirleitner.core.util.RunMode;
import at.peirleitner.core.util.database.SaveType;
import at.peirleitner.core.util.database.TableType;
import at.peirleitner.core.util.user.User;

/**
 * System to interact and manipulate economy of a {@link User}
 * 
 * @since 1.0.6
 * @author Markus Peirleitner (Rengobli)
 *
 */
public class EconomySystem implements CoreSystem {

	/**
	 * Maximum amount of digits that the balance is able to have (Numbers <b>before</b> comma)
	 * @since 1.0.6
	 * @author Markus Peirleitner (Rengobli)
	 */
	public static final int MAX_DIGITS = 20;
	
	/**
	 * Maximum amount of digits that the balance is able to have (Numbers <b>after</b> comma)
	 * @since 1.0.6
	 * @author Markus Peirleitner (Rengobli)
	 */
	public static final int MAX_COMMA = 5;
	
	public EconomySystem() {
		this.createTable();
		Core.getInstance().getSettingsManager().registerSetting(Core.getInstance().getPluginName(),
				"system.economy.char", "$");
	}

	public final char getChar() {
		return Core.getInstance().getSettingsManager()
				.getSetting(Core.getInstance().getPluginName(), "system.economy.char").charAt(0);
	}

	/**
	 * 
	 * @return If sending money between players is allowed
	 * @since 1.0.6
	 * @author Markus Peirleitner (Rengobli)
	 * @apiNote Required to use {@link CommandPay}. This should be ignored if the
	 *          current {@link RunMode} is set to {@link RunMode#NETWORK}, as
	 *          {@link CommandPay} uses {@link SettingsManager#getSaveType()}.
	 */
	public final boolean isMoneySendingAllowed() {
		return Core.getInstance().getSettingsManager().isSetting(Core.getInstance().getPluginName(),
				"manager.settings.allow-money-sending-between-players");
	}

	public final double getEconomy(@Nonnull UUID uuid, @Nonnull SaveType saveType) {

		try {

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection()
					.prepareStatement("SELECT balance FROM " + this.getTableType().getTableName(true)
							+ " WHERE uuid = ? AND saveType = ?");
			stmt.setString(1, uuid.toString());
			stmt.setInt(2, saveType.getID());

			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {

				BigDecimal bd = rs.getBigDecimal(1);
				return bd.doubleValue();

			} else {
				// No Economy
				return 0.0;
			}

		} catch (SQLException e) {
			Core.getInstance().log(getClass(), LogType.ERROR, "Could not get Economy for SaveType '"
					+ saveType.toString() + "' for User '" + uuid.toString() + "'/SQL: " + e.getMessage());
			return -1;
		}

	}

	/**
	 * 
	 * @param uuid - UUID of the {@link User}
	 * @return HashMap containing the balance for every {@link SaveType} that this
	 *         {@link User} has
	 * @since 1.0.6
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final HashMap<SaveType, Double> getEconomy(@Nonnull UUID uuid) {

		HashMap<SaveType, Double> map = new HashMap<>();

		for (SaveType st : Core.getInstance().getSaveTypes()) {

			double economy = this.getEconomy(uuid, st);

			if (economy > 0) {
				map.put(st, economy);
			}

		}

		return map;
	}

	public final boolean addEconomy(@Nonnull UUID uuid, @Nonnull SaveType saveType, @Nonnull double amount) {

		double current = this.getEconomy(uuid, saveType);
		final boolean has = (current > 0 ? true : false);

		if (current < 0) {
			Core.getInstance().log(getClass(), LogType.ERROR, "Could not add Economy for User '" + uuid.toString()
					+ "' on SaveType '" + saveType.getID() + "': Current Economy is negative.");
			return false;
		}

		if (amount < 0.1 || amount >= Double.MAX_VALUE) {
			Core.getInstance().log(getClass(), LogType.ERROR, "Could not add Economy for User '" + uuid.toString()
					+ "' on SaveType '" + saveType.getID() + "': Amount (" + amount + ") is negative.");
			return false;
		}

		double newEconomy = new BigDecimal(current).add(new BigDecimal(amount)).doubleValue();

		if (newEconomy >= Double.MAX_VALUE) {
			Core.getInstance().log(getClass(), LogType.ERROR,
					"Could not add Economy for User '" + uuid.toString() + "' on SaveType '" + saveType.getID()
							+ "': New Economy (" + newEconomy + ") would be negative (too big).");
			return false;
		}

		try {

			PreparedStatement stmt = null;
//			Core.getInstance().log(getClass(), LogType.DEBUG, "Has: " + has + ", Current: " + current + ", New: " + newEconomy);

			if (has) {
				stmt = Core.getInstance().getMySQL().getConnection().prepareStatement("UPDATE "
						+ this.getTableType().getTableName(true) + " SET balance = ? WHERE uuid = ? AND saveType = ?");
				stmt.setBigDecimal(1, new BigDecimal(newEconomy));
				stmt.setString(2, uuid.toString());
				stmt.setInt(3, saveType.getID());
			} else {
				stmt = Core.getInstance().getMySQL().getConnection().prepareStatement("INSERT INTO "
						+ this.getTableType().getTableName(true) + " (uuid, saveType, balance) VALUES (?, ?, ?);");
				stmt.setString(1, uuid.toString());
				stmt.setInt(2, saveType.getID());
				stmt.setBigDecimal(3, new BigDecimal(newEconomy));
			}

			stmt.executeUpdate();

			Core.getInstance().log(getClass(), LogType.DEBUG, "Added '" + amount + "' Economy on SaveType '"
					+ saveType.getID() + "' to User '" + uuid.toString() + "'. New Value: '" + newEconomy + "'.");
			
			Core.getInstance().getUserSystem().getUser(uuid).sendMessage(Core.getInstance().getPluginName(), "command.economy.add.success.target",
					Arrays.asList("" + amount, "" + this.getChar()),
					true);
			return true;

		} catch (SQLException e) {
			Core.getInstance().log(getClass(), LogType.ERROR, "Could not add Economy for User '" + uuid.toString()
					+ "' on SaveType '" + saveType.getID() + "'/SQL: " + e.getMessage());
			return false;
		}

	}

	public final boolean removeEconomy(@Nonnull UUID uuid, @Nonnull SaveType saveType, @Nonnull double amount) {

		double current = this.getEconomy(uuid, saveType);
		final boolean has = (current > 0 ? true : false);

		if (current < 0) {
			Core.getInstance().log(getClass(), LogType.ERROR, "Could not remove Economy for User '" + uuid.toString()
					+ "' on SaveType '" + saveType.getID() + "': Current Economy is negative.");
			return false;
		}

		if (amount < 0.1 || amount >= Double.MAX_VALUE) {
			Core.getInstance().log(getClass(), LogType.ERROR, "Could not remove Economy for User '" + uuid.toString()
					+ "' on SaveType '" + saveType.getID() + "': Amount (" + amount + ") is negative.");
			return false;
		}

		double newEconomy = new BigDecimal(current).subtract(new BigDecimal(amount)).doubleValue();

		if (newEconomy <= 0.0 || newEconomy >= Double.MAX_VALUE) {
			Core.getInstance().log(getClass(), LogType.ERROR, "Could not remove Economy for User '" + uuid.toString()
					+ "' on SaveType '" + saveType.getID() + "': New Economy (" + newEconomy + ") would be negative.");
			return false;
		}

		try {

			PreparedStatement stmt = null;

			if (has) {
				stmt = Core.getInstance().getMySQL().getConnection().prepareStatement("UPDATE "
						+ this.getTableType().getTableName(true) + " SET balance = ? WHERE uuid = ? AND saveType = ?");
				stmt.setBigDecimal(1, new BigDecimal(newEconomy));
				stmt.setString(2, uuid.toString());
				stmt.setInt(3, saveType.getID());
			} else {
				stmt = Core.getInstance().getMySQL().getConnection().prepareStatement("INSERT INTO "
						+ this.getTableType().getTableName(true) + " (uuid, saveType, balance) VALUES (?, ?, ?);");
				stmt.setString(1, uuid.toString());
				stmt.setInt(2, saveType.getID());
				stmt.setBigDecimal(3, new BigDecimal(newEconomy));
			}

			stmt.execute();

			Core.getInstance().log(getClass(), LogType.DEBUG, "Removed '" + amount + "' Economy on SaveType '"
					+ saveType.getID() + "' for User '" + uuid.toString() + "'. New Value: '" + newEconomy + "'.");
			
			Core.getInstance().getUserSystem().getUser(uuid).sendMessage(Core.getInstance().getPluginName(), "command.economy.remove.success.target",
					Arrays.asList("" + amount, "" + this.getChar()),
					true);
			return true;

		} catch (SQLException e) {
			Core.getInstance().log(getClass(), LogType.ERROR, "Could not remove Economy for User '" + uuid.toString()
					+ "' on SaveType '" + saveType.getID() + "'/SQL: " + e.getMessage());
			return false;
		}

	}

	public final boolean setEconomy(@Nonnull UUID uuid, @Nonnull SaveType saveType, @Nonnull double amount) {

		double current = this.getEconomy(uuid, saveType);
		final boolean has = (current > 0 ? true : false);

		if (current < 0) {
			Core.getInstance().log(getClass(), LogType.ERROR, "Could not set Economy for User '" + uuid.toString()
					+ "' on SaveType '" + saveType.getID() + "': Current Economy is negative.");
			return false;
		}

		if (amount < 0.1 || amount >= Double.MAX_VALUE) {
			Core.getInstance().log(getClass(), LogType.ERROR, "Could not set Economy for User '" + uuid.toString()
					+ "' on SaveType '" + saveType.getID() + "': Amount (" + amount + ") is negative.");
			return false;
		}

		double newEconomy = new BigDecimal(amount).doubleValue();

		if (amount <= 0.0 || amount >= Double.MAX_VALUE) {
			Core.getInstance().log(getClass(), LogType.ERROR, "Could not set Economy for User '" + uuid.toString()
					+ "' on SaveType '" + saveType.getID() + "': New Economy (" + newEconomy + ") would be negative.");
			return false;
		}

		try {

			PreparedStatement stmt = null;

			if (has) {
				stmt = Core.getInstance().getMySQL().getConnection().prepareStatement("UPDATE "
						+ this.getTableType().getTableName(true) + " SET balance = ? WHERE uuid = ? AND saveType = ?");
				stmt.setBigDecimal(1, new BigDecimal(newEconomy));
				stmt.setString(2, uuid.toString());
				stmt.setInt(3, saveType.getID());
			} else {
				stmt = Core.getInstance().getMySQL().getConnection().prepareStatement("INSERT INTO "
						+ this.getTableType().getTableName(true) + " (uuid, saveType, balance) VALUES (?, ?, ?);");
				stmt.setString(1, uuid.toString());
				stmt.setInt(2, saveType.getID());
				stmt.setBigDecimal(3, new BigDecimal(newEconomy));
			}

			stmt.execute();

			Core.getInstance().log(getClass(), LogType.DEBUG, "The Economy of User '" + uuid.toString()
					+ "' on SaveType '" + saveType.getID() + "' has been set to '" + amount + "'.");
			
			Core.getInstance().getUserSystem().getUser(uuid).sendMessage(Core.getInstance().getPluginName(), "command.economy.set.success.target",
					Arrays.asList("" + amount, "" + this.getChar()),
					true);
			return true;

		} catch (SQLException e) {
			Core.getInstance().log(getClass(), LogType.ERROR, "Could not set Economy for User '" + uuid.toString()
					+ "' on SaveType '" + saveType.getID() + "'/SQL: " + e.getMessage());
			return false;
		}

	}

	@Override
	public void createTable() {

		try {

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection()
					.prepareStatement("CREATE TABLE IF NOT EXISTS " + Core.getInstance().getMySQL().getTablePrefix()
							+ this.getTableType().getTableName(false) + " (" + "uuid CHAR (36) NOT NULL, "
							+ "saveType INT NOT NULL, " + "balance DECIMAL(" + MAX_DIGITS + "," + MAX_COMMA + ") NOT NULL, "
							+ "PRIMARY KEY (uuid, saveType), " + "FOREIGN KEY (saveType) REFERENCES "
							+ Core.getInstance().getMySQL().getTablePrefix() + TableType.SAVE_TYPE.getTableName(false)
							+ "(id));");
			stmt.execute();
			return;

		} catch (SQLException e) {
			Core.getInstance().log(getClass(), LogType.ERROR,
					"Could not create Table for EconomySystem/SQL: " + e.getMessage());
			return;
		}

	}

	@Override
	public TableType getTableType() {
		return TableType.ECONOMY;
	}
	
	

}
