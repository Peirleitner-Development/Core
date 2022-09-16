package at.peirleitner.core.util.local;

import javax.annotation.Nonnull;

/**
 * This class defines the current state saved inside {@link GameMapData}
 * 
 * @since 1.0.0
 * @author Markus Peirleitner (Rengobli)
 *
 */
public enum GameMapState {

	/**
	 * Map has been created
	 * 
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 */
	AWAITING_APPROVAL("WOODEN_PICKAXE"),

	/**
	 * Approved by Build Lead
	 * 
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 */
	APPROVED("STONE_PICKAXE"),

	/**
	 * Marked as done by the Creator, waiting for check by Build Lead
	 * 
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 */
	DONE("IRON_PICKAXE"),

	/**
	 * Build Lead approved this Map for productive use
	 * 
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 */
	FINISHED("DIAMOND_PICKAXE"),

	/**
	 * Map has been soft-deleted (File still exists)
	 * 
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 */
	DELETED("BARRIER"),

	/**
	 * Map isn't available for selective use by the GameCore
	 * 
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 */
	DAMAGED("ANVIL");
	
	private final String iconName;
	
	private GameMapState(@Nonnull String iconName) {
		this.iconName = iconName;
	}
	
	/**
	 * 
	 * @return Icon Name for {@link GUI}s
	 * @since 1.0.3
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final String getIconName() {
		return this.iconName;
	}

}
