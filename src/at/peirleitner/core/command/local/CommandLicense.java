package at.peirleitner.core.command.local;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import at.peirleitner.core.Core;
import at.peirleitner.core.SpigotMain;
import at.peirleitner.core.system.LicenseSystem;
import at.peirleitner.core.util.GlobalUtils;
import at.peirleitner.core.util.database.SaveType;
import at.peirleitner.core.util.local.GUI;
import at.peirleitner.core.util.local.ItemBuilder;
import at.peirleitner.core.util.user.CorePermission;
import at.peirleitner.core.util.user.Language;
import at.peirleitner.core.util.user.MasterLicense;
import at.peirleitner.core.util.user.PredefinedMessage;
import at.peirleitner.core.util.user.User;
import at.peirleitner.core.util.user.UserLicense;
import net.md_5.bungee.api.ChatColor;

/**
 * Command to interact with Licenses
 * 
 * @since 1.0.6
 * @author Markus Peirleitner (Rengobli)
 * @see UserLicense
 * @see MasterLicense
 * @see LicenseSystem
 */
public class CommandLicense implements CommandExecutor {

	public CommandLicense() {
		SpigotMain.getInstance().getCommand("license").setExecutor(this);
	}

	@Override
	public boolean onCommand(CommandSender cs, Command cmd, String arg, String[] args) {

		if (!(cs instanceof Player)) {
			cs.sendMessage(
					Core.getInstance().getLanguageManager().getMessage(PredefinedMessage.ACTION_REQUIRES_PLAYER));
			return true;
		}

		Player p = (Player) cs;
		User user = Core.getInstance().getUserSystem().getUser(p.getUniqueId());

		if (!p.hasPermission(CorePermission.COMMAND_LICENSE_ADMIN.getPermission()) || args.length == 0) {

			Collection<UserLicense> licenses = this.getLicenseSystem().getLicenses(user.getUUID());
			
			if(this.getLicenseSystem().isDisplayInGUI()) {
				
				GUI gui = new GUI(Core.getInstance().getLanguageManager().getMessage(Core.getInstance().getPluginName(),
						user.getLanguage(), "gui.license.title", null));
				

				if (licenses.isEmpty()) {
					gui.setItem(22,
							new ItemBuilder(Material.BARRIER).name(
									Core.getInstance().getLanguageManager().getMessage(Core.getInstance().getPluginName(),
											user.getLanguage(), "gui.license.item.no-licenses.name", null))
									.build());
				} else {

					List<String> description = new ArrayList<>();

					for (UserLicense ul : licenses) {
						
						description.clear();
						String[] s = Core.getInstance().getLanguageManager().getMessage(Core.getInstance().getPluginName(), user.getLanguage(), "gui.license.item.license.description", Arrays.asList(
								GlobalUtils.getFormatedDate(ul.getIssued()),
								ul.isPermanent() ? "Never" : GlobalUtils.getFormatedDate(ul.getExpire())
								)).split("\n");
						
						for(String desc : s) {
							description.add(desc);
						}
						
						gui.addItem(new ItemBuilder(Material.PAPER)
								.name(ChatColor.DARK_AQUA + ul.getMasterLicense().getName()).lore(description).build());
					}

				}
				
				gui.open(p);
				
			} else {
				
				user.sendMessage(Core.getInstance().getPluginName(), "command.license.your-licenses.pretext", null, true);
				
				for(UserLicense ul : licenses) {
					user.sendMessage(Core.getInstance().getPluginName(), "command,license.your-licenses.licencse", Arrays.asList(
							ul.getMasterLicense().getName(),
							GlobalUtils.getFormatedDate(ul.getIssued()),
							ul.isPermanent() ? "Never" : GlobalUtils.getFormatedDate(ul.getExpire())
							), true);
				}
				
			}

			return true;
		}

		if (args.length == 1) {

			if (args[0].equalsIgnoreCase("list")) {

				Collection<MasterLicense> licenses = this.getLicenseSystem().getMasterLicenses();

				if (licenses.isEmpty()) {
					user.sendMessage(Core.getInstance().getPluginName(), "command.license.list.no-licenses", null,
							true);
					return true;
				} else {

					user.sendMessage(Core.getInstance().getPluginName(), "command.license.list.list-of-licenses", null,
							true);
					for (MasterLicense ml : licenses) {
						cs.sendMessage(ChatColor.DARK_GRAY + "- " + ChatColor.GRAY + "#" + ml.getID() + ": "
								+ ml.getName() + " (Active: " + (ml.isValid() ? "Yes" : "No") + ")");
					}

					return true;
				}

			} else {
				this.sendHelp(cs);
				return true;
			}

		} else if (args.length == 2) {

			MasterLicense ml = null;

			try {

				int id = Integer.valueOf(args[1]);

				MasterLicense license = this.getLicenseSystem().getMasterLicense(id);

				if (license == null) {
					user.sendMessage(Core.getInstance().getPluginName(),
							"command.license.main.no-master-license-found-with-given-id", Arrays.asList("" + id), true);
					return true;
				}

				ml = license;

			} catch (NumberFormatException ex) {
				user.sendMessage(Core.getInstance().getPluginName(), "command.license.main.id-has-to-be-an-integer",
						Arrays.asList(args[0]), true);
				return true;
			}

			if (args[0].equalsIgnoreCase("expire")) {

				if (!ml.isValid()) {
					user.sendMessage(Core.getInstance().getPluginName(), "command.license.expire.already-expired",
							Arrays.asList("" + ml.getID()), true);
					return true;
				}

				boolean success = this.getLicenseSystem().setMasterLicenseToExpire(ml.getID());

				user.sendMessage(Core.getInstance().getPluginName(),
						"command.license.expire." + (success ? "success" : "error.sql"), Arrays.asList("" + ml.getID()),
						true);
				return true;

			} else if (args[0].equalsIgnoreCase("permanent")) {

				if (ml.isPermanent()) {
					user.sendMessage(Core.getInstance().getPluginName(), "command.license.permanent.already-permanent",
							Arrays.asList("" + ml.getID()), true);
					return true;
				}

				boolean success = this.getLicenseSystem().setMasterLicenseToPermanent(ml.getID());

				user.sendMessage(Core.getInstance().getPluginName(),
						"command.license.permanent." + (success ? "success" : "error.sql"),
						Arrays.asList("" + ml.getID()), true);
				return true;

			} else {
				this.sendHelp(cs);
				return true;
			}

		} else if (args.length == 3) {

			User targetUser = null;
			MasterLicense masterLicense = null;

			if (!args[0].equalsIgnoreCase("create")) {

				String target = args[1];
				User u = Core.getInstance().getUserSystem().getByLastKnownName(target);

				if (u == null) {
					cs.sendMessage(
							Core.getInstance().getLanguageManager().getMessage(PredefinedMessage.NOT_REGISTERED));
					return true;
				} else {
					targetUser = u;
				}

				try {

					int id = Integer.valueOf(args[2]);
					MasterLicense ml = this.getLicenseSystem().getMasterLicense(id);

					if (ml == null) {
						user.sendMessage(Core.getInstance().getPluginName(),
								"command.license.main.no-master-license-found-with-given-id", Arrays.asList("" + id),
								true);
						return true;
					}

					masterLicense = ml;

				} catch (NumberFormatException ex) {
					user.sendMessage(Core.getInstance().getPluginName(), "command.license.main.id-has-to-be-an-integer",
							Arrays.asList(args[2]), true);
					return true;
				}

			}

			if (args[0].equalsIgnoreCase("create")) {

				SaveType saveType = null;

				try {

					int id = Integer.valueOf(args[1]);

					SaveType st = Core.getInstance().getSaveTypeByID(id);

					if (st == null) {
						user.sendMessage(Core.getInstance().getPluginName(),
								"command.license.create.error.invalid-saveType", Arrays.asList(args[1]), true);
						return true;
					}

					saveType = st;

				} catch (NumberFormatException ex) {
					user.sendMessage(Core.getInstance().getPluginName(), "command.license.main.id-has-to-be-an-integer",
							Arrays.asList(args[1]), true);
					return true;
				}

				String name = args[2];

				if (this.getLicenseSystem().isLicense(saveType, name)) {
					user.sendMessage(Core.getInstance().getPluginName(), "command.license.create.error.already-exists",
							Arrays.asList(saveType.getName(), name), true);
					return true;
				}

				ItemStack is = p.getInventory().getItemInMainHand();
				String iconName = null;

				if (is == null || is.getType() == Material.AIR) {
					iconName = this.getLicenseSystem().getDefaultIconName();
				} else {
					iconName = is.getType().toString();
				}

				boolean created = this.getLicenseSystem().createMasterLicense(saveType, name, -1, iconName);

				user.sendMessage(Core.getInstance().getPluginName(),
						"command.license.create." + (created ? "success" : "error.sql"),
						Arrays.asList(saveType.getName(), name), true);
				return true;

			} else if (args[0].equalsIgnoreCase("grant")) {

				if (!masterLicense.isValid()) {
					user.sendMessage(Core.getInstance().getPluginName(),
							"command.license.grant.error.master-license-expired",
							Arrays.asList(masterLicense.getName()), true);
					return true;
				}

				if (this.getLicenseSystem().hasActiveLicense(targetUser.getUUID(), masterLicense)) {
					user.sendMessage(Core.getInstance().getPluginName(),
							"command.license.grant.error.already-has-license",
							Arrays.asList(targetUser.getDisplayName(), masterLicense.getName()), true);
					return true;
				}

				boolean granted = this.getLicenseSystem().grantLicense(targetUser.getUUID(), masterLicense, -1);

				user.sendMessage(Core.getInstance().getPluginName(),
						"command.license.grant." + (granted ? "success.permanent" : "error.sql"),
						Arrays.asList(masterLicense.getName(), targetUser.getDisplayName()), true);
				return true;

			} else if (args[0].equalsIgnoreCase("has")) {

				boolean has = this.getLicenseSystem().hasLicense(targetUser.getUUID(), masterLicense);

				if (has) {
					UserLicense license = this.getLicenseSystem().getLicense(targetUser.getUUID(), masterLicense);
					user.sendMessage(Core.getInstance().getPluginName(), "command.license.has.yes",
							Arrays.asList(targetUser.getDisplayName(), masterLicense.getName(),
									license.isPermanent() ? "Never" : GlobalUtils.getFormatedDate(license.getExpire())),
							true);
				} else {
					user.sendMessage(Core.getInstance().getPluginName(), "command.license.has.no",
							Arrays.asList(targetUser.getDisplayName(), masterLicense.getName()), true);
				}

				return true;

			} else if (args[0].equalsIgnoreCase("revert")) {

				if (!this.getLicenseSystem().hasLicense(targetUser.getUUID(), masterLicense)) {
					user.sendMessage(Core.getInstance().getPluginName(), "command.license.revert.error.no-license",
							Arrays.asList(targetUser.getDisplayName(), masterLicense.getName()), true);
					return true;
				}

				boolean reverted = this.getLicenseSystem().setUserLicenseToExpire(targetUser.getUUID(), masterLicense);

				user.sendMessage(Core.getInstance().getPluginName(),
						"command.license.revert." + (reverted ? "success.sender" : "error.sql"),
						Arrays.asList(targetUser.getDisplayName(), masterLicense.getName()), true);
				
				if(reverted) {
					targetUser.sendMessage(Core.getInstance().getPluginName(), "command.license.revert.success.target",
							Arrays.asList(masterLicense.getName(), user.getDisplayName()), true);
				}

			} else {
				this.sendHelp(cs);
				return true;
			}

		} else if (args.length == 4) {

			User targetUser = null;
			MasterLicense masterLicense = null;
			int hours = -1;

			if (args[0].equalsIgnoreCase("grant")) {

				String target = args[1];
				User u = Core.getInstance().getUserSystem().getByLastKnownName(target);

				if (u == null) {
					cs.sendMessage(
							Core.getInstance().getLanguageManager().getMessage(PredefinedMessage.NOT_REGISTERED));
					return true;
				} else {
					targetUser = u;
				}

				try {

					int id = Integer.valueOf(args[2]);
					int h = Integer.valueOf(args[3]);
					MasterLicense ml = this.getLicenseSystem().getMasterLicense(id);

					if (ml == null) {
						user.sendMessage(Core.getInstance().getPluginName(),
								"command.license.main.no-master-license-found-with-given-id", Arrays.asList("" + id),
								true);
						return true;
					}

					masterLicense = ml;
					hours = h;

				} catch (NumberFormatException ex) {
					user.sendMessage(Core.getInstance().getPluginName(), "command.license.main.id-has-to-be-an-integer",
							Arrays.asList(args[2]), true);
					return true;
				}

				if (!masterLicense.isValid()) {
					user.sendMessage(Core.getInstance().getPluginName(),
							"command.license.grant.error.master-license-expired",
							Arrays.asList(masterLicense.getName()), true);
					return true;
				}

				if (this.getLicenseSystem().hasActiveLicense(targetUser.getUUID(), masterLicense)) {
					user.sendMessage(Core.getInstance().getPluginName(),
							"command.license.grant.error.already-has-license",
							Arrays.asList(targetUser.getDisplayName(), masterLicense.getName()), true);
					return true;
				}

				boolean granted = this.getLicenseSystem().grantLicense(targetUser.getUUID(), masterLicense, hours);
				long expire = System.currentTimeMillis() + (1000L * 60 * 60 * hours);

				user.sendMessage(Core.getInstance().getPluginName(),
						"command.license.grant." + (granted ? "success.temporary" : "error.sql"),
						Arrays.asList(masterLicense.getName(), targetUser.getDisplayName(), GlobalUtils.getFormatedDate(expire)), true);
				return true;

			} else {
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
				Language.ENGLISH, "command.license.syntax", null));
	}

	private final LicenseSystem getLicenseSystem() {
		return Core.getInstance().getLicenseSystem();
	}

}
