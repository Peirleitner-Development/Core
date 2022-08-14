package at.peirleitner.core.util.local;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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

import com.google.common.reflect.TypeToken;

import at.peirleitner.core.BungeeMain;
import at.peirleitner.core.Core;
import at.peirleitner.core.SpigotMain;
import at.peirleitner.core.util.LogType;
import at.peirleitner.core.util.RunMode;
import at.peirleitner.core.util.user.User;

public class LocalScoreboard {

	private boolean DEFAULT_TEAMS_ENABLED;

	public boolean isDEFAULT_TEAMS_ENABLED() {
		return DEFAULT_TEAMS_ENABLED;
	}

	// private boolean ENABLE_COLLISION;
	//
	// public void enableCollision() {
	// ENABLE_COLLISION = true;
	// for (Player all : Bukkit.getOnlinePlayers())
	// this.resetDefaultTeams(all);
	// }
	//
	// public void disableCollision() {
	// ENABLE_COLLISION = false;
	// for (Player all : Bukkit.getOnlinePlayers())
	// this.refreshDefaultTeams(all);
	// }

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
	private List<Rank> ranks;
	private File ranksFile = new File(
			(Core.getInstance().getRunMode() == RunMode.NETWORK ? BungeeMain.getInstance().getDataFolder().getPath()
					: SpigotMain.getInstance().getDataFolder().getPath()) + "/ranks.json");

	public Scoreboard getPlayerScoreboard(Player p) {
		if (!scoreboard.containsKey(p))
			scoreboard.put(p, Bukkit.getScoreboardManager().getNewScoreboard());
		return scoreboard.get(p);
	}

	public LocalScoreboard() {
		this.scoreboard = new HashMap<Player, Scoreboard>();
		DEFAULT_TEAMS_ENABLED = true;
		this.ranks = new ArrayList<>();
		
		if(!this.ranksFile.exists()) {
			try {
				
				// Create File
				this.ranksFile.createNewFile();
				
				// Fill with default values
				List<Rank> defaultValues = new ArrayList<>();
				defaultValues.add(new Rank(200, "Administrator", "Admin", "#7a0d05", RankType.STAFF, false));
				defaultValues.add(new Rank(100, "Player", "Player", "#8c8484", RankType.USER, true));
				
				String s = Core.getInstance().getGson().toJson(defaultValues);
				BufferedWriter bw = new BufferedWriter(new FileWriter(ranksFile));
				bw.write(s);
				bw.close();
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		this.loadRanks();
		
	}
	
	public final List<Rank> getRanks() {
		return this.ranks;
	}

	@SuppressWarnings("serial")
	private final void loadRanks() {
		
		try {
			
			FileReader fr = new FileReader(this.ranksFile);
			List<Rank> loaded = Core.getInstance().getGson().fromJson(fr, new TypeToken<ArrayList<Rank>>() {
			}.getType());
			
			if(loaded != null) {
				this.ranks.addAll(loaded);
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Core.getInstance().log(this.getClass(), LogType.INFO, "Loaded " + this.ranks.size() + " Ranks");
		
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

		// Scoreboard main = Bukkit.getScoreboardManager().getMainScoreboard();
		//
		// Team col = main.getTeam("collision");
		// if (col == null)
		// col = main.registerNewTeam("collision");
		// if (ENABLE_COLLISION)
		// col.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.ALWAYS);
		// else
		// col.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
		// for (Player all : Bukkit.getOnlinePlayers()) {
		// col.addEntry(all.getName());
		// }

		Scoreboard s = this.getPlayerScoreboard(p);
		for (Rank rank : this.ranks) {
			Team t = s.getTeam("" + rank.getPriority());
			if (t == null) {
				t = s.registerNewTeam("" + rank.getPriority());
//				Bukkit.getConsoleSender().sendMessage("Registered team §a" + rank.getRightOrdinal() + " for rank " + rank.getName());
			}

			t.setCanSeeFriendlyInvisibles(true);
			// t.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);

			// Tab prefix is set down below to be able to display guilds since 1.0.1
			t.setPrefix((!rank.isDefault()
					? rank.getColoredDisplayName() + " "
					: ""));
//			t.setSuffix(g == null ? "" : " " + ChatColor.GRAY + "[" + g.getColor() + g.getTag() + ChatColor.GRAY + "]");

			t.setColor(ChatColor.GRAY);

		}

		for (Player all : Bukkit.getOnlinePlayers()) {

			User user = Core.getInstance().getUserSystem().getUser(all.getUniqueId());
			Objective o = getPlayerScoreboard(p).getObjective("lvl");
			if (o == null)
				o = getPlayerScoreboard(p).registerNewObjective("lvl", "dummy");
			o.setDisplaySlot(DisplaySlot.BELOW_NAME);
			o.setDisplayName(ChatColor.YELLOW + "Level");
//			o.getScore(all.getName()).setScore(user.getLevel()); // TODO: Update to real level

			Team t = getPlayerScoreboard(p).getTeam("" + this.getRank(all).getPriority());
			t.addEntry(all.getName());

			Rank rank = this.getRank(all);

			// Set the tab prefix here
			all.setPlayerListName((!rank.isDefault()
					? rank.getChatColor() + "" + ChatColor.BOLD + rank.getDisplayName().toUpperCase() + " "
					: "") + ChatColor.GRAY + all.getName());

			// t.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
//			all.setPlayerListName("§7" + pRank.getColor() + pRank.getName() + " §7| §7" + pRank.getColor()
//					+ all.getName() 
//					+ (Survival.getInstance().getAPI().getVanishedPlayers().contains(cp.getUUID()) ? " §4*VANISH*" : "")
//					+ (cp.getDonationAmount() > 0 ? " §c❤ " : "") 
//					+ (cp.isAFK() ? " §c*AFK*" : "")
//					+ (Survival.getInstance().getAPI().isInTutorial(all.getUniqueId()) ? " §b*TUTORIAL*" : "")
//					);

		}

//		Team t = getPlayerScoreboard(p).getTeam("" + NayolaCore.getInstance().getUserSystem().getUser(p.getUniqueId()).getRank().getRightOrdinal());
//		t.addEntry(p.getName());

		p.setScoreboard(s);
	}
	
	private final Rank getRankByPriority(@Nonnull int id) {
		return this.ranks.stream().filter(rank -> rank.getPriority() == id).findAny().orElse(null);
	}
	
	private final List<Rank> getInRightOrder() {
		
		List<Integer> list = new ArrayList<>();
		
		for(Rank rank : this.ranks) {
			list.add(rank.getPriority());
		}
		
		Collections.sort(list, Collections.reverseOrder());
		
		List<Rank> ranks = new ArrayList<>();
		
		for(int i : list) {
			Rank rank = this.getRankByPriority(i);
			ranks.add(rank);
		}
		
		
		return ranks;
	}

	private final Rank getRank(@Nonnull Player p) {
		
		for(Rank rank : this.getInRightOrder()) {
			
			if(p.hasPermission("Core.rank." + rank.getName().toLowerCase())) {
				return rank;
			}
			
		}
		
		return this.getDefaultRank();
	}
	
	public final Rank getDefaultRank() {
		
		for(Rank rank : this.ranks) {
			if(rank.isDefault()) return rank;
		}
		
		return null;
	}

}
