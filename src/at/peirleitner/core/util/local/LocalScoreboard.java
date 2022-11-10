package at.peirleitner.core.util.local;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import at.peirleitner.core.Core;
import at.peirleitner.core.util.user.Rank;
import at.peirleitner.core.util.user.User;

public class LocalScoreboard {

	private boolean DEFAULT_TEAMS_ENABLED;

	public boolean isDEFAULT_TEAMS_ENABLED() {
		return DEFAULT_TEAMS_ENABLED;
	}

	public void enableDefaultTeams() {
		DEFAULT_TEAMS_ENABLED = true;
		for (Player all : Bukkit.getOnlinePlayers())
			this.resetDefaultTeams(all);
	}

	public void disableDefaultTeams() {
		DEFAULT_TEAMS_ENABLED = false;
		for (Player all : Bukkit.getOnlinePlayers())
			this.refreshDefaultTeams(all);
	}

	private Map<Player, Scoreboard> scoreboard;

	public Scoreboard getPlayerScoreboard(Player p) {
		if (!scoreboard.containsKey(p))
			scoreboard.put(p, Bukkit.getScoreboardManager().getNewScoreboard());
		return scoreboard.get(p);
	}

	public LocalScoreboard() {
		this.scoreboard = new HashMap<Player, Scoreboard>();
		DEFAULT_TEAMS_ENABLED = true;

	}

	public void resetDefaultTeams(Player p) {
		Scoreboard s = this.getPlayerScoreboard(p);
		try {
			s.getTeams().stream().forEach(team -> team.getEntries().clear());
		} catch (Exception ex) {

		}
		try {
			s.getTeams().stream().forEach(team -> team.getEntries().stream().forEach(entry -> team.removeEntry(entry)));
		} catch (Exception ex) {

		}
		s.getObjectives().stream().forEach(o -> {
			if (o != null && o.getDisplayName() != null && o.getDisplaySlot() == DisplaySlot.BELOW_NAME) {
				o.unregister();
			}
		});
		p.setScoreboard(s);
	}

	public void refreshDefaultTeams() {
		for (Player all : Bukkit.getOnlinePlayers())
			refreshDefaultTeams(all);
	}

	@SuppressWarnings("deprecation")
	public void refreshDefaultTeams(Player p) {
		if (!DEFAULT_TEAMS_ENABLED) {
			resetDefaultTeams(p);
			return;
		}

		Scoreboard s = this.getPlayerScoreboard(p);
		for (Rank rank : Core.getInstance().getRanks()) {
			Team t = s.getTeam("" + this.getTeamID(rank));
			if (t == null) {
				t = s.registerNewTeam("" + this.getTeamID(rank));
//				
			}

			t.setCanSeeFriendlyInvisibles(true);
			t.setPrefix((!rank.isDefault() ? rank.getColoredDisplayName() + " " : ""));

			t.setColor(ChatColor.GRAY);

		}

		for (Player all : Bukkit.getOnlinePlayers()) {

			User user = Core.getInstance().getUserSystem().getUser(all.getUniqueId());
			Objective o = getPlayerScoreboard(p).getObjective("lvl");
			if (o == null)
				o = getPlayerScoreboard(p).registerNewObjective("lvl", "dummy");
			o.setDisplaySlot(DisplaySlot.BELOW_NAME);
			o.setDisplayName(ChatColor.YELLOW + "Level");

			Rank rank = user.getRank();

			Team t = getPlayerScoreboard(p).getTeam("" + this.getTeamID(rank));
			t.addEntry(all.getName());

			// Set the tab prefix here
			all.setPlayerListName((!rank.isDefault()
					? rank.getChatColor() + "" + ChatColor.BOLD + rank.getDisplayName().toUpperCase() + " "
					: "") + ChatColor.GRAY + all.getName());

		}

		p.setScoreboard(s);
	}

	private int getTeamID(@Nonnull Rank rank) {

		List<Rank> ranks = Core.getInstance().getInRightOrder();

		int i = 0;

		for (Rank r : ranks) {

			if (r == rank) {
				return i;
			}

			i++;

		}

		return -1;

	}

}
