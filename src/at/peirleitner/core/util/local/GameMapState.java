package at.peirleitner.core.util.local;

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
	AWAITING_APPROVAL,

	/**
	 * Approved by Build Lead
	 * 
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 */
	APPROVED,

	/**
	 * Marked as done by the Creator, waiting for check by Build Lead
	 * 
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 */
	DONE,

	/**
	 * Build Lead approved this Map for productive use
	 * 
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 */
	FINISHED,

	/**
	 * Map has been soft-deleted (File still exists)
	 * 
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 */
	DELETED,

	/**
	 * Map isn't available for selective use by the GameCore
	 * 
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 */
	DAMAGED;

}
