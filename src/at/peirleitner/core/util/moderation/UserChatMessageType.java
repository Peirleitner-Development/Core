package at.peirleitner.core.util.moderation;

/**
 * Type of a {@link UserChatMessage}
 * 
 * @since 1.0.14
 * @author Markus Peirleitner (Rengobli)
 *
 */
public enum UserChatMessageType {

	/**
	 * Message has been sent to everyone online
	 * 
	 * @since 1.0.14
	 * @author Markus Peirleitner (Rengobli)
	 */
	PUBLIC,

	/**
	 * Message has been sent to a specific (group of) recipient(s), such as private
	 * messaging or a guild member-only chat
	 * 
	 * @since 1.0.14
	 * @author Markus Peirleitner (Rengobli)
	 */
	PRIVATE;

}
