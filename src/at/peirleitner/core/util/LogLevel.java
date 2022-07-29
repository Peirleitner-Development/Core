package at.peirleitner.core.util;

/**
 * This enum provides different log levels.
 * 
 * @since 1.0.0
 * @author Markus Peirleitner (Rengobli)
 *
 */
public enum LogLevel {

	/**
	 * Debug message, only necessary for development or debugging purposes.<br>
	 * These levels will only be sent towards the log if debugging is enabled.<br>
	 * <b>Example:</b> Cached data in the background
	 * 
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 */
	DEBUG,

	/**
	 * Informational content, nothing to worry about but still worth mentioning.<br>
	 * <b>Example:</b> Database data has been loaded
	 * 
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 */
	INFO,

	/**
	 * Something unexpected occurred, the system still runs and works as
	 * expected.<br>
	 * <b>Example:</b> Database update error
	 * 
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 */
	WARNING,

	/**
	 * Something unexpected occurred, at least some functionality of the system has
	 * been lost.<br>
	 * <b>Example:</b> Connection towards the database can no longer be established
	 * 
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 */
	ERROR,

	/**
	 * Something unexpected occurred, the system is required to shut down in order
	 * to prevent data loss.<br>
	 * <b>Example:</b> Connection towards the database is lost, can't create a
	 * cached file due to missing write permissions. Data loss can't be prevented.
	 * 
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 * @apiNote This should <b>not</b> be used by third-party developers, as
	 *          throwing this <u>may</u> result in a server crash.
	 */
	CRITICAL;

}
