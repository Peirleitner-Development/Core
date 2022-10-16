package at.peirleitner.core.util.local;

/**
 * Defines how the plugin should behave on operator joins
 * @since 1.0.5
 * @author Markus Peirleitner (Rengobli)
 *
 */
public enum OperatorJoinAction {

	/**
	 * Allow joining, do nothing.
	 * @since 1.0.5
	 * @author Markus Peirleitner (Rengobli)
	 */
	ALLOW,
	
	/**
	 * Disallow joining.
	 * @since 1.0.5
	 * @author Markus Peirleitner (Rengobli)
	 */
	DISALLOW,
	
	/**
	 * Remove Operator Status.
	 * @since 1.0.5
	 * @author Markus Peirleitner (Rengobli)
	 */
	REMOVE_STATUS;

}
