package at.peirleitner.core.system;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nonnull;

import at.peirleitner.core.Core;
import at.peirleitner.core.util.CoreSystem;
import at.peirleitner.core.util.DiscordWebHookType;
import at.peirleitner.core.util.LogType;
import at.peirleitner.core.util.RunMode;
import at.peirleitner.core.util.Voucher;
import at.peirleitner.core.util.database.SaveType;
import at.peirleitner.core.util.database.TableType;
import at.peirleitner.core.util.user.User;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.TextComponent;

/**
 * This System allows you to create and redeem {@link Voucher}s.
 * 
 * @since 1.0.19
 * @author Markus Peirleitner (Rengobli)
 *
 */
public final class VoucherSystem implements CoreSystem {

	public VoucherSystem() {

		Core.getInstance().getSettingsManager().registerSetting(Core.getInstance().getPluginName(),
				"system.voucher.code-length", "20");
		Core.getInstance().getSettingsManager().registerSetting(Core.getInstance().getPluginName(),
				"system.voucher.command.redeem.cooldown-in-seconds", "10");

		this.createTable();
	}

	@Override
	public void createTable() {

		try {

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection()
					.prepareStatement("CREATE TABLE IF NOT EXISTS " + TableType.VOUCHER_CODES.getTableName(true) + " ("
							+ "id INT AUTO_INCREMENT NOT NULL, " + "created BIGINT(255) NOT NULL, "
							+ "creator CHAR(36) NOT NULL, " + "code CHAR(" + this.getCodeLength() + ") NOT NULL,"
							+ "maxRedeems INT NOT NULL, " + "command VARCHAR(200) NOT NULL, "
							+ "expiration BIGINT(255) NOT NULL, " + "saveType INT NOT NULL, "
							+ "PRIMARY KEY (id, code), " + "FOREIGN KEY (saveType) REFERENCES "
							+ TableType.SAVE_TYPE.getTableName(true) + "(id));");

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

		if (maxRedeems > Integer.MAX_VALUE || maxRedeems < -1) {
			user.sendMessage(Core.getInstance().getPluginName(), "command.voucher.create.error.invalid-max-redeems",
					Arrays.asList("" + maxRedeems), true);
			return false;
		}

		if (expirationInDays > Integer.MAX_VALUE || expirationInDays < -1) {
			user.sendMessage(Core.getInstance().getPluginName(),
					"command.voucher.create.error.invalid-expiration-in-days", Arrays.asList("" + expirationInDays),
					true);
			return false;
		}

		try {

			String code = this.getRandomCode();

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection().prepareStatement("INSERT INTO "
					+ TableType.VOUCHER_CODES.getTableName(true)
					+ " (created, creator, code, maxRedeems, command, expiration, saveType) VALUES (?, ?, ?, ?, ?, ?, ?);",
					Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, System.currentTimeMillis());
			stmt.setString(2, user.getUUID().toString());
			stmt.setString(3, code);
			stmt.setInt(4, maxRedeems);
			stmt.setString(5, command.replace("#", " "));
			stmt.setInt(6, expirationInDays);
			stmt.setInt(7, Core.getInstance().getSettingsManager().getSaveType().getID());

			stmt.executeUpdate();

			ResultSet rs = stmt.getGeneratedKeys();
			rs.next();

			int id = rs.getInt(1);

			Core.getInstance().createWebhook("User '" + user.getUUID().toString() + "/" + user.getLastKnownName()
					+ "' created the Voucher #" + id, DiscordWebHookType.STAFF_NOTIFICATION);

			user.sendMessage(Core.getInstance().getPluginName(), "command.voucher.create.success",
					Arrays.asList("" + id, code), true);

			TextComponent component = new TextComponent("[CLICK TO COPY]");
			component.setClickEvent(new ClickEvent(Action.COPY_TO_CLIPBOARD, code));

			if (Core.getInstance().getRunMode() == RunMode.LOCAL) {
				org.bukkit.Bukkit.getPlayer(user.getUUID()).spigot().sendMessage(component);
			}

			return true;

		} catch (SQLException e) {
			Core.getInstance().log(getClass(), LogType.ERROR, "Could not create new Voucher/SQL: " + e.getMessage());
			user.sendMessage(Core.getInstance().getPluginName(), "command.voucher.create.error.sql", null, true);
			return false;
		}
	}

	public final String getRandomCode() {

		int leftLimit = 48; // numeral '0'
		int rightLimit = 122; // letter 'z'
		int targetStringLength = this.getCodeLength();
		Random random = new Random();

		String generatedString = random.ints(leftLimit, rightLimit + 1)
				.filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97)).limit(targetStringLength)
				.collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString();

		return generatedString;

	}

	public final boolean disable(@Nonnull User user, @Nonnull int id) {
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

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection().prepareStatement(
					"SELECT * FROM " + TableType.VOUCHER_CODES.getTableName(true) + " WHERE code = ?");
			stmt.setString(1, code);

			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {
				return this.getByResultSet(rs);
			} else {
				// None found
				return null;
			}

		} catch (SQLException e) {
			Core.getInstance().log(getClass(), LogType.ERROR, "Could not get Voucher by Code/SQL: " + e.getMessage());
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
		SaveType saveType = Core.getInstance().getSaveTypeByID(rs.getInt(8));

		return new Voucher(id, created, creator, code, maxRedeems, command, expiration, saveType);

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
