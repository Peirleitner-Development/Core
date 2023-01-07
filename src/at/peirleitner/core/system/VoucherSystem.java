package at.peirleitner.core.system;

import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nonnull;

import at.peirleitner.core.Core;
import at.peirleitner.core.util.CoreSystem;
import at.peirleitner.core.util.GlobalUtils;
import at.peirleitner.core.util.LogType;
import at.peirleitner.core.util.Voucher;
import at.peirleitner.core.util.database.TableType;
import at.peirleitner.core.util.user.User;

/**
 * This System allows you to create and redeem {@link Voucher}s.
 * 
 * @since 1.0.19
 * @author Markus Peirleitner (Rengobli)
 *
 */
public class VoucherSystem implements CoreSystem {

	private static VoucherSystem instance;

	private VoucherSystem() {
		instance = this;

		Core.getInstance().getSettingsManager().registerSetting(Core.getInstance().getPluginName(),
				"system.voucher.code-length", "20");
		Core.getInstance().getSettingsManager().registerSetting(Core.getInstance().getPluginName(),
				"system.voucher.command.redeem.cooldown-in-seconds", "10");

		this.createTable();
	}

	public static VoucherSystem getInstance() {
		return instance;
	}

	@Override
	public void createTable() {

		try {

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection()
					.prepareStatement("CREATE TABLE IF NOT EXISTS " + TableType.VOUCHER_CODES.getTableName(true) + " ("
							+ "id AUTO_INCREMENT NOT NULL, " + "created BIGINT(255) NOT NULL, "
							+ "creator CHAR(36) NOT NULL, " + "code CHAR(" + this.getCodeLength() + ") NOT NULL,"
							+ "maxRedeems INT NOT NULL, " + "command VARCHAR(200) NOT NULL, "
							+ "expiration BIGINT(255) NOT NULL, " + "PRIMARY KEY (id, code));");

			stmt.execute();

			stmt = Core.getInstance().getMySQL().getConnection()
					.prepareStatement("CREATE TABLE IF NOT EXISTS " + TableType.VOUCHER_USERS.getTableName(true) + " ("
							+ "uuid CHAR(36) NOT NULL, " + "voucher INT NOT NULL, " + "redeemed BIGINT(255) NOT NULL, "
							+ "PRIMARY KEY (uuid, voucher), " + "FOREIGN KEY (voucher) REFERENCES "
							+ TableType.VOUCHER_CODES.getTableName(true) + "(id));");

			stmt.execute();

		} catch (SQLException e) {
			Core.getInstance().log(getClass(), LogType.ERROR, "Could not create Tables/SQL: " + e.getMessage());
		}

	}

	@Override
	public TableType getTableType() {
		return null;
	}

	public final boolean create(@Nonnull User user, @Nonnull String command, @Nonnull int maxRedeems,
			@Nonnull int expirationInDays) {
		return false;
	}

	public final boolean delete(@Nonnull User user, @Nonnull int id) {
		return false;
	}

	public final boolean redeem(@Nonnull User user, @Nonnull String code) {

		Voucher voucher = this.getByCode(code);

		if (voucher == null) {
			user.sendMessage(Core.getInstance().getPluginName(), "command.redeem.error.invalid-voucher", null, true);
			return false;
		}

		return voucher.redeem(user);

	}

	/**
	 * Get a Voucher by its PlainText Code. The entered <code>code</code> argument
	 * will be hashed, afterwards the database is queried to find a matching hash.
	 * 
	 * @param code - PlainText Code
	 * @return {@link Voucher} or <code>null</code> if (1) no code could be found
	 *         with the given hash or (2) an {@link SQLException} occured
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final Voucher getByCode(@Nonnull String code) {

		try {

			char[] hashcode = GlobalUtils.getShaHashCode(code);

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection().prepareStatement(
					"SELECT * FROM " + TableType.VOUCHER_CODES.getTableName(true) + " WHERE code = ?");
			stmt.setString(1, hashcode.toString());

			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {
				return this.getByResultSet(rs);
			} else {
				Core.getInstance().log(getClass(), LogType.DEBUG,
						"No Voucher found with code '" + code + "' (Hash: " + hashcode.toString() + "').");
				return null;
			}

		} catch (SQLException e) {
			Core.getInstance().log(getClass(), LogType.ERROR, "Could not get Voucher by Code/SQL: " + e.getMessage());
			return null;
		} catch (NoSuchAlgorithmException e) {
			Core.getInstance().log(getClass(), LogType.ERROR,
					"The entered alogirthm could not be found: " + e.getMessage());
			return null;
		}

	}

	public final Voucher[] getVouchers() {

		try {

			Set<Voucher> vouchers = new HashSet<>();

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection()
					.prepareStatement("SELECT * FROM " + TableType.VOUCHER_CODES.getTableName(true));
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {

				Voucher voucher = this.getByResultSet(rs);
				vouchers.add(voucher);

			}

			Voucher[] array = new Voucher[vouchers.size()];
			return vouchers.toArray(array);

		} catch (SQLException e) {
			Core.getInstance().log(getClass(), LogType.ERROR,
					"Could not get Vouchers from Database/SQL: " + e.getMessage());
			return null;
		}

	}

	public final Voucher getByResultSet(@Nonnull ResultSet rs) throws SQLException {

		int id = rs.getInt(1);
		long created = rs.getLong(2);
		UUID creator = UUID.fromString(rs.getString(3));
		String code = rs.getString(4);
		int maxRedeems = rs.getInt(5);
		String command = rs.getString(6);
		long expiration = rs.getLong(7);

		return new Voucher(id, created, creator, code, maxRedeems, command, expiration);

	}

	public final int getCodeLength() {
		return Integer.valueOf(Core.getInstance().getSettingsManager().getSetting(Core.getInstance().getPluginName(),
				"system.voucher.code-length"));
	}

	public final int getCommandRedeemCooldown() {
		return Integer.valueOf(Core.getInstance().getSettingsManager().getSetting(Core.getInstance().getPluginName(),
				"system.voucher.command.redeem.cooldown-in-seconds"));
	}

}
