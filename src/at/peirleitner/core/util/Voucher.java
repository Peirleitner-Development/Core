package at.peirleitner.core.util;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.UUID;

import javax.annotation.Nonnull;

import at.peirleitner.core.Core;
import at.peirleitner.core.command.local.CommandRedeem;
import at.peirleitner.core.util.database.SaveType;
import at.peirleitner.core.util.database.TableType;
import at.peirleitner.core.util.user.User;

/**
 * This class represents a code that is redeemable by a {@link User} using the
 * {@link CommandRedeem} command.
 * 
 * @since 1.0.19
 * @author Markus Peirleitner (Rengobli)
 *
 */
public final class Voucher {

	private final int id;
	private final long created;
	private final UUID creator;
	private final String code;
	private final int maxRedeems;
	private final String command;
	private final long expiration;
	private final SaveType saveType;

	public Voucher(int id, long created, UUID creator, String code, int maxRedeems, String command, long expiration, SaveType saveType) {
		this.id = id;
		this.created = created;
		this.creator = creator;
		this.code = code;
		this.maxRedeems = maxRedeems;
		this.command = command;
		this.expiration = expiration;
		this.saveType = saveType;
	}

	public final int getID() {
		return id;
	}

	public final long getCreated() {
		return created;
	}

	public final UUID getCreator() {
		return creator;
	}

	public final String getCode() {
		return code;
	}

	public final int getMaxRedeems() {
		return maxRedeems;
	}
	
	/**
	 * 
	 * @return If the maximum amount of redeems has yet been reached
	 * @since 1.0.19
	 * @author Markus Peirleitner (Rengobli)
	 * @apiNote Not to be confused with {@link #isValid()}
	 */
	public final boolean isRedeemable() {
		
		if(this.getMaxRedeems() == -1) {
			return true;
		}
		
		try {
			
			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection().prepareStatement("SELECT COUNT(*) FROM " + TableType.VOUCHER_USERS.getTableName(true) + " WHERE voucher = ?");
			stmt.setInt(1, this.getID());
			
			ResultSet rs = stmt.executeQuery();
			rs.next();
			
			int redeems = rs.getInt(1);
			
			return redeems < this.getMaxRedeems();
			
		} catch (SQLException e) {
			Core.getInstance().log(getClass(), LogType.ERROR, "Could not check if the Voucher '" + this.getID() + "' is redeemable/SQL: " + e.getMessage());
			return false;
		}
		
	}
	
	public final boolean hasRedeemed(@Nonnull User user) {
		
		try {
			
			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection().prepareStatement("SELECT * FROM " + TableType.VOUCHER_USERS.getTableName(true) + " WHERE uuid = ? AND voucher = ?");
			stmt.setString(1, user.getUUID().toString());
			stmt.setInt(2, this.getID());
			
			ResultSet rs = stmt.executeQuery();
			
			if(rs.next()) {
//				Core.getInstance().log(getClass(), LogType.DEBUG, "User '" + user.getUUID().toString() + "' has already redeemed the Voucher '" + this.getID() + "'.");
				return true;
			} else {
//				Core.getInstance().log(getClass(), LogType.DEBUG, "User '" + user.getUUID().toString() + "' has not redeemed the Voucher '" + this.getID() + "'.");
				return false;
			}
			
		} catch (SQLException e) {
			Core.getInstance().log(getClass(), LogType.ERROR, "Could not check if the User '" + user.getUUID().toString() + "' has redeemed the entered Voucher '" + this.getID() + "', returning true to avoid exploits.");
			return true;
		}
		
	}
	
	public final boolean redeem(@Nonnull User user) {
		
		if(this.getSaveType().getID() != Core.getInstance().getSettingsManager().getSaveType().getID()) {
			user.sendMessage(Core.getInstance().getPluginName(), "command.redeem.error.invalid-save-type",
					Arrays.asList("" + this.getSaveType().getName()), true);
			return false;
		}
		
		if(!(this.isRedeemable())) {
			user.sendMessage(Core.getInstance().getPluginName(), "command.redeem.error.max-redeems-reached",
					Arrays.asList("" + this.getMaxRedeems()), true);
			return false;
		}
		
		if(this.isExpired()) {
			user.sendMessage(Core.getInstance().getPluginName(), "command.redeem.error.expired",
					Arrays.asList(GlobalUtils.getFormatedDate(this.getExpiration())), true);
			return false;
		}
		
		if(this.hasRedeemed(user)) {
			user.sendMessage(Core.getInstance().getPluginName(), "command.redeem.error.already-redeemed",
					null, true);
			return false;
		}
		
		try {
			
			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection().prepareStatement("INSERT INTO " + TableType.VOUCHER_USERS.getTableName(true) + " (uuid, voucher, redeemed) VALUES (?, ?, ?);");
			stmt.setString(1, user.getUUID().toString());
			stmt.setInt(2, this.getID());
			stmt.setLong(3, System.currentTimeMillis());
			
			int updated = stmt.executeUpdate();
			
			if(updated == 1) {
				
				String commandToExecute = this.getCommand();
				commandToExecute = commandToExecute.replace("{uuid}", user.getUUID().toString());
				commandToExecute = commandToExecute.replace("{player}", user.getLastKnownName());
				
				if(Core.getInstance().getRunMode() == RunMode.LOCAL) {
					org.bukkit.Bukkit.dispatchCommand(org.bukkit.Bukkit.getConsoleSender(),commandToExecute);
				} else {
					net.md_5.bungee.api.ProxyServer.getInstance().getPluginManager().dispatchCommand(net.md_5.bungee.api.ProxyServer.getInstance().getConsole(), commandToExecute);
				}
				
				Core.getInstance().log(getClass(), LogType.DEBUG, "User '" + user.getUUID().toString() + "' redeemed the Voucher '" + this.getID() + "'.");
				Core.getInstance().createWebhook("User '" + user.getUUID().toString() + "' redeemed the Voucher '" + this.getID() + "'.", DiscordWebHookType.STAFF_NOTIFICATION);
				
				user.sendMessage(Core.getInstance().getPluginName(), "command.redeem.success", null, true);
				
				return true;
			} else {
				Core.getInstance().log(getClass(), LogType.WARNING, "Could not redeem Voucher '" + this.getID() + "' for User '" + user.getUUID().toString() + "': No rows updated.");
				return false;
			}
			
		} catch (SQLException e) {
			Core.getInstance().log(getClass(), LogType.ERROR, "Could not redeem Voucher for User '" + user.getUUID().toString() + "'/SQL: " + e.getMessage());
			return false;
		}
		
	}
	
	/**
	 * 
	 * @return If this Voucher is redeemable as well as not expired
	 * @since 1.0.19
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final boolean isValid() {
		return this.isRedeemable() && !this.isExpired() ? true : false;
	}

	public final String getCommand() {
		return command;
	}

	public final long getExpiration() {
		return expiration;
	}

	public final boolean isExpired() {
		return this.getExpiration() == -1 ? false : System.currentTimeMillis() >= this.getExpiration();
	}
	
	public final SaveType getSaveType() {
		return saveType;
	}

}
