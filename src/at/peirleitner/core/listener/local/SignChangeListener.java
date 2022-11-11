package at.peirleitner.core.listener.local;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

import at.peirleitner.core.Core;
import at.peirleitner.core.SpigotMain;
import at.peirleitner.core.util.user.CorePermission;
import net.md_5.bungee.api.ChatColor;

/**
 * Allows Users to use ColorCodes on Signs
 * 
 * @since 1.0.12
 * @author Markus Peirleitner (Rengobli)
 *
 */
public class SignChangeListener implements Listener {

	public SignChangeListener() {
		SpigotMain.getInstance().getServer().getPluginManager().registerEvents(this, SpigotMain.getInstance());
	}

	@EventHandler
	public void onSignChange(SignChangeEvent e) {

		Player p = e.getPlayer();

		if (!p.hasPermission(CorePermission.EXTRA_SIGN_COLOR.getPermission())) {
			Core.getInstance().getLanguageManager().sendMessage(p, Core.getInstance().getPluginName(), "listener.sign-change.sign-color.no-permission", null, true);
			return;
		}

		for (int i = 0; i < e.getLines().length; i++) {
			e.setLine(i, ChatColor.translateAlternateColorCodes('&', e.getLine(i)));
		}

	}

}
