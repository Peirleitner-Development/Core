package at.peirleitner.core.command.local;

import java.math.BigDecimal;
import java.util.Arrays;

import javax.annotation.Nonnull;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import at.peirleitner.core.Core;
import at.peirleitner.core.SpigotMain;
import at.peirleitner.core.api.local.EconomyTransferEvent;
import at.peirleitner.core.manager.SettingsManager;
import at.peirleitner.core.system.EconomySystem;
import at.peirleitner.core.util.LogType;
import at.peirleitner.core.util.database.SaveType;
import at.peirleitner.core.util.user.CorePermission;
import at.peirleitner.core.util.user.PredefinedMessage;
import at.peirleitner.core.util.user.User;

/**
 * Allows sending money to another {@link User}. This uses the SaveType
 * specified in {@link SettingsManager#getSaveType()}.
 * 
 * @since 1.0.6
 * @author Markus Peirleitner (Rengobli)
 * @see CorePermission#COMMAND_PAY
 * @see EconomySystem#isMoneySendingAllowed()
 */
public class CommandPay implements CommandExecutor {

	public CommandPay() {
		SpigotMain.getInstance().getCommand("pay").setExecutor(this);
	}

	/**
	 * If an unexpected error between transactions occur, this will be set to
	 * <code>true</code>. While this is <code>true</code>, all transactions using
	 * this command will be cancelled.<br>
	 * The server is required to restart to enable payments again.
	 * 
	 * @since 1.0.6
	 * @author Markus Peirleitner (Rengobli)
	 */
	private boolean disallowPayments = false;

	@Override
	public boolean onCommand(CommandSender cs, Command cmd, String arg, String[] args) {

		if (!(cs instanceof Player)) {
			cs.sendMessage(
					Core.getInstance().getLanguageManager().getMessage(PredefinedMessage.ACTION_REQUIRES_PLAYER));
			return true;
		}

		final Player p = (Player) cs;
		final User user = Core.getInstance().getUserSystem().getUser(p.getUniqueId());
		final SaveType saveType = Core.getInstance().getSettingsManager().getSaveType();

		if (!Core.getInstance().getEconomySystem().isMoneySendingAllowed()) {
			user.sendMessage(Core.getInstance().getPluginName(), "command.pay.error.permanently-disabled", null, true);
			return true;
		}

		if (args.length != 2) {
			this.sendHelp(user);
			return true;
		}

		if (this.disallowPayments) {
			user.sendMessage(Core.getInstance().getPluginName(), "command.pay.error.temporarily-disabled", null, true);
			return true;
		}

		final User targetUser = Core.getInstance().getUserSystem().getByLastKnownName(args[0]);

		if (targetUser == null) {
			cs.sendMessage(Core.getInstance().getLanguageManager().getMessage(PredefinedMessage.NOT_REGISTERED));
			return true;
		}

		if (targetUser == user) {
			cs.sendMessage(Core.getInstance().getLanguageManager().getMessage(PredefinedMessage.CANT_TARGET_YOURSELF));
			return true;
		}

		try {

			String entered = args[1];
			entered = entered.replace(",", ".");
			final double value = Double.valueOf(entered); // Replace comma value for European countries
			final BigDecimal amount = new BigDecimal(value);

			if (amount.doubleValue() < 0.1 || amount.doubleValue() > Double.MAX_VALUE) {
				user.sendMessage(Core.getInstance().getPluginName(), "command.pay.error.negative-input",
						Arrays.asList("" + amount.doubleValue()), true);
				return true;
			}

			String[] s = entered.split("\\.");

			final int enteredValues = Integer.valueOf(s[0].length());
			final int commaValues = (s.length == 1 ? 0 : Integer.valueOf(s[1].length()));

			if (enteredValues > EconomySystem.MAX_DIGITS) {
				user.sendMessage(Core.getInstance().getPluginName(), "command.pay.error.too-many-digits",
						Arrays.asList("" + enteredValues, "" + EconomySystem.MAX_DIGITS), true);
				return true;
			}

			if (commaValues > EconomySystem.MAX_COMMA) {
				user.sendMessage(Core.getInstance().getPluginName(), "command.pay.error.too-many-commas",
						Arrays.asList("" + commaValues, "" + EconomySystem.MAX_COMMA), true);
				return true;
			}

			final double currentBalance = user.getEconomy(saveType);

			if (currentBalance < amount.doubleValue()) {
				user.sendMessage(Core.getInstance().getPluginName(), "command.pay.error.invalid-balance",
						Arrays.asList("" + currentBalance, "" + amount.doubleValue()), true);
				return true;
			}

			EconomyTransferEvent event = new EconomyTransferEvent(user, targetUser, currentBalance);
			SpigotMain.getInstance().getServer().getPluginManager().callEvent(event);

			if (event.isCancelled()) {
				user.sendMessage(Core.getInstance().getPluginName(), "command.pay.error.event-cancelled", null, true);
				return true;
			}

			boolean success1 = user.removeEconomy(saveType, amount.doubleValue());

			// Do not add economy if it can't be removed in the first place
			if (!success1) {
				Core.getInstance().log(getClass(), LogType.WARNING,
						"Did not proceed transaction because the money could not be removed from User '"
								+ user.getUUID().toString() + ".");
				return true;
			}

			boolean success2 = targetUser.addEconomy(saveType, amount.doubleValue());

			if (success1 && success2) {

				user.sendMessage(Core.getInstance().getPluginName(), "command.pay.success.sender",
						Arrays.asList(targetUser.getDisplayName(), "" + amount.doubleValue(),
								"" + Core.getInstance().getEconomySystem().getChar()),
						true);

				targetUser.sendMessage(Core.getInstance().getPluginName(), "command.pay.success.target",
						Arrays.asList(user.getDisplayName(), "" + amount.doubleValue(),
								"" + Core.getInstance().getEconomySystem().getChar()),
						true);
				
				return true;
			} else {
//				this.disallowPayments = true;
				user.sendMessage(Core.getInstance().getPluginName(),
						"command.pay.error.transaction-could-not-be-completed",
						Arrays.asList("" + success1, "" + success2), true);
				Core.getInstance().log(getClass(), LogType.ERROR, "Could not complete Transaction " + event.toString()
						+ ". Removed from sender: " + success1 + ", Added to target: " + success2);
				return true;
			}

		} catch (NumberFormatException ex) {
			user.sendMessage(Core.getInstance().getPluginName(), "command.pay.error.invalid-amount",
					Arrays.asList(args[1]), true);
			return true;
		}

	}

	private final void sendHelp(@Nonnull User user) {

		String prefix = Core.getInstance().getLanguageManager().getPrefix(Core.getInstance().getPluginName(),
				user.getLanguage());
		String message = Core.getInstance().getLanguageManager().getMessage(Core.getInstance().getPluginName(),
				user.getLanguage(), "command.pay.syntax", null);

		Player p = Bukkit.getPlayer(user.getUUID());

		if (p != null) {
			p.sendMessage(prefix + message);
		}

	}

}
