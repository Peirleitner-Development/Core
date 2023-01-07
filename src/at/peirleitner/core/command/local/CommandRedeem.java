package at.peirleitner.core.command.local;

import java.util.Arrays;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import at.peirleitner.core.Core;
import at.peirleitner.core.SpigotMain;
import at.peirleitner.core.system.VoucherSystem;
import at.peirleitner.core.util.user.PredefinedMessage;
import at.peirleitner.core.util.user.User;

public class CommandRedeem implements CommandExecutor {

	public CommandRedeem() {
		SpigotMain.getInstance().getCommand("redeem").setExecutor(this);
	}
	
	private static final String COOLDOWN_NAME = "command_redeem";

	@Override
	public boolean onCommand(CommandSender cs, Command cmd, String arg, String[] args) {

		if (!(cs instanceof Player)) {
			cs.sendMessage(
					Core.getInstance().getLanguageManager().getMessage(PredefinedMessage.ACTION_REQUIRES_PLAYER));
			return true;
		}

		Player p = (Player) cs;
		User user = Core.getInstance().getUserSystem().getUser(p.getUniqueId());

		if(Core.getInstance().getCooldownSystem().hasCooldown(user.getUUID(), COOLDOWN_NAME, Core.getInstance().getSettingsManager().getSaveType().getID())) {
			
		}
		
		if (args.length != 1) {
			user.sendMessage(Core.getInstance().getPluginName(), "command.redeem.syntax", null, true);
			return true;
		}

		if (args[0].length() != VoucherSystem.getInstance().getCodeLength()) {
			user.sendMessage(Core.getInstance().getPluginName(), "command.redeem.invalid-code-length",
					Arrays.asList("" + args[0].length(), "" + VoucherSystem.getInstance().getCodeLength()), true);
			return false;
		}

		VoucherSystem.getInstance().redeem(user, args[0]);

		Core.getInstance().getCooldownSystem().addCooldown(user.getUUID(), COOLDOWN_NAME,
				Core.getInstance().getSettingsManager().getSaveType().getID(),
				VoucherSystem.getInstance().getCommandRedeemCooldown());

		return true;

	}

}
