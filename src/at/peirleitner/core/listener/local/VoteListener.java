package at.peirleitner.core.listener.local;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;

import at.peirleitner.core.Core;
import at.peirleitner.core.SpigotMain;
import at.peirleitner.core.util.LogType;
import at.peirleitner.core.util.user.User;

/**
 * Listener for handling incoming Votes
 * 
 * @since 1.0.14
 * @author Markus Peirleitner (Rengobli)
 *
 */
public class VoteListener implements Listener {

	public VoteListener() {
		SpigotMain.getInstance().getServer().getPluginManager().registerEvents(this, SpigotMain.getInstance());
	}

	@EventHandler
	public void onVote(VotifierEvent e) {

		Vote v = e.getVote();
		String playerName = v.getUsername();

		User user = Core.getInstance().getUserSystem().getByLastKnownName(playerName);
		Core.getInstance().log(getClass(), LogType.DEBUG,
				"Received Vote for User '" + user.getUUID().toString() + "': " + v.toString());

	}

}
