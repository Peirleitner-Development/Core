package at.peirleitner.core.listener.local;

import java.util.Arrays;
import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.help.HelpTopic;

import at.peirleitner.core.Core;
import at.peirleitner.core.SpigotMain;
import at.peirleitner.core.util.user.User;

public class PlayerCommandPreProcessListener implements Listener {

	public PlayerCommandPreProcessListener() {
		SpigotMain.getInstance().getServer().getPluginManager().registerEvents(this, SpigotMain.getInstance());
	}

	@EventHandler
	public void onPlayerCommandPreProcess(PlayerCommandPreprocessEvent e) {

		Player p = e.getPlayer();
		User user = Core.getInstance().getUserSystem().getUser(p.getUniqueId());
		String cmd = e.getMessage().split(" ")[0];
		
		if(cmd.contains("/") && cmd.contains(":")) {
			user.sendAsyncMessage(Core.getInstance().getPluginName(), "listener.player-command-pre-process.error.plugin-enters-are-disabled", null, true);
			e.setCancelled(true);
			return;
		}

		// Check Restriction
		if (Core.getInstance().getModerationSystem().hasActiveChatLog(user.getUUID())
				&& this.getBlockedCommandsInChatRestriction().contains(cmd.toLowerCase())) {
			user.sendMessage(Core.getInstance().getPluginName(), "system.moderation.chat-log-restriction-active",
					Arrays.asList(
							"" + Core.getInstance().getModerationSystem().getActiveChatLog(user.getUUID()).getID()),
					true);
			e.setCancelled(true);
			return;
		}

		HelpTopic ht = Bukkit.getHelpMap().getHelpTopic(cmd);

		if (ht == null) {

			e.setCancelled(true);

			user.sendMessage(Core.getInstance().getPluginName(), "listener.player-command-pre-process.unknown-command",
					Arrays.asList(cmd), true);
			return;

		}

	}

	private Collection<String> getBlockedCommandsInChatRestriction() {
		return Arrays.asList("/msg", "/tell", "/whisper", "/r", "/reply", "/me");
	}

}
