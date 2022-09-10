package at.peirleitner.core.util.user;

import javax.annotation.Nonnull;

public enum PredefinedStatistic {

	KILLS("Kills", "kills"), 
	DEATHS("Deaths", "deaths"), 
	VICTORIES("Victories", "victories"),
	LOSSES("Losses", "losses"), 
	GAMES_PLAYED("Games played", "games_played"),
	
	/**
	 * @since 1.0.1
	 * @author Markus Peirleitner (Rengobli)
	 */
	ASSISTS("Assists", "assists")
	;

	private final String name;
	private final String dataName;

	private PredefinedStatistic(@Nonnull String name, @Nonnull String dataName) {
		this.name = name;
		this.dataName = dataName;
	}

	public final String getName() {
		return name;
	}

	public final String getDataName() {
		return dataName;
	}

}
