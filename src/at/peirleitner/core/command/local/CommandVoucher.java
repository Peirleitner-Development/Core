package at.peirleitner.core.command.local;

import java.util.Arrays;

import javax.annotation.Nonnull;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import at.peirleitner.core.Core;
import at.peirleitner.core.SpigotMain;
import at.peirleitner.core.util.GlobalUtils;
import at.peirleitner.core.util.Voucher;
import at.peirleitner.core.util.user.CorePermission;
import at.peirleitner.core.util.user.Language;
import at.peirleitner.core.util.user.PredefinedMessage;
import at.peirleitner.core.util.user.User;

/**
 * Manage {@link Voucher}s
 * 
 * @since 1.0.19
 * @author Markus Peirleitner (Rengobli)
 *
 */
public class CommandVoucher implements CommandExecutor {

	public CommandVoucher() {
		SpigotMain.getInstance().getCommand("voucher").setExecutor(this);
	}

	@Override
	public boolean onCommand(CommandSender cs, Command cmd, String arg, String[] args) {

		if (!cs.hasPermission(CorePermission.COMMAND_VOUCHER.getPermission())) {
			Core.getInstance().getLanguageManager().getMessage(PredefinedMessage.NO_PERMISSION);
			return true;
		}
		
		if(!(cs instanceof Player)) {
			cs.sendMessage(Core.getInstance().getLanguageManager().getMessage(PredefinedMessage.ACTION_REQUIRES_PLAYER));
			return true;
		}
		
		Player p = (Player) cs;
		User user = Core.getInstance().getUserSystem().getUser(p.getUniqueId());

		if (args.length == 1 && args[0].equalsIgnoreCase("list")) {

			Voucher[] vouchers = Core.getInstance().getVoucherSystem().getVouchers();

			if (vouchers.length == 0) {
				Core.getInstance().getLanguageManager().sendMessage(cs, Core.getInstance().getPluginName(),
						"command.voucher.list.none-exist", null, true);
				return true;
			}

			Core.getInstance().getLanguageManager().sendMessage(cs, Core.getInstance().getPluginName(),
					"command.voucher.list.pre-text", Arrays.asList("" + vouchers.length), true);

			for (Voucher v : vouchers) {
				Core.getInstance().getLanguageManager().sendMessage(cs, Core.getInstance().getPluginName(),
						"command.voucher.list.per-voucher",
						Arrays.asList("" + v.getID(), 
								GlobalUtils.getFormatedDate(v.getCreated()),
								Core.getInstance().getUserSystem().getUser(v.getCreator()).getDisplayName(),
								v.getCode(), "" + v.getMaxRedeems(), v.getCommand(),
								v.getExpiration() == -1 ? "Never" : GlobalUtils.getFormatedDate(v.getExpiration()),
								v.getSaveType().getName()),
						false);
			}

		} else if (args.length == 2 && args[0].equalsIgnoreCase("disable")) {

			try {
				
				int id = Integer.valueOf(args[1]);
				Core.getInstance().getVoucherSystem().disable(user, id);
				
			}catch(NumberFormatException ex) {
				this.sendHelp(cs);
				return true;
			}
			
		} else if (args.length == 4 && args[0].equalsIgnoreCase("create")) {

			try {
				
				String command = args[1].replace("#", " ");
				int maxRedeems = Integer.valueOf(args[2]);
				int expirationInDays = Integer.valueOf(args[3]);
				
				Core.getInstance().getVoucherSystem().create(user, command, maxRedeems, expirationInDays);
				return true;
				
			}catch(NumberFormatException ex) {
				this.sendHelp(cs);
				return true;
			}
			
		} else {
			this.sendHelp(cs);
			return true;
		}

		return true;

	}

	private final void sendHelp(@Nonnull CommandSender cs) {
		cs.sendMessage(Core.getInstance().getLanguageManager().getMessage(Core.getInstance().getPluginName(),
				Language.ENGLISH, "command.voucher.syntax", null));
	}

}
