package at.peirleitner.core.util.moderation;

import javax.annotation.Nonnull;

import at.peirleitner.core.system.ModerationSystem;
import at.peirleitner.core.util.user.User;

/**
 * Flags that can be assigned to a {@link UserChatMessage}
 * 
 * @since 1.0.14
 * @author Markus Peirleitner (Rengobli)
 *
 */
public enum UserChatMessageFlag {

	/**
	 * User is typing the same (or similar) message
	 * 
	 * @since 1.0.14
	 * @author Markus Peirleitner (Rengobli)
	 */
	SPAM(false),

	/**
	 * User is typing in upper-case letters
	 * 
	 * @since 1.0.14
	 * @author Markus Peirleitner (Rengobli)
	 */
	CAPS(false),

	/**
	 * User is typing a blocked phrase
	 * 
	 * @since 1.0.14
	 * @author Markus Peirleitner (Rengobli)
	 * @see ModerationSystem#getBlockedPhrases()
	 */
	BLOCKED_PHRASE(true),

	/**
	 * User is typing a URL that has not been allowed
	 * 
	 * @since 1.0.14
	 * @author Markus Peirleitner (Rengobli)
	 * @see ModerationSystem#getAllowedDomains()
	 */
	ADVERTISING(true),

	/**
	 * User is typing a message whilst still having to wait
	 * 
	 * @since 1.0.14
	 * @author Markus Peirleitner (Rengobli)
	 */
	COOLDOWN(false);

	private final boolean forceChatRestriction;

	private UserChatMessageFlag(@Nonnull boolean forceChatRestriction) {
		this.forceChatRestriction = forceChatRestriction;
	}

	/**
	 * 
	 * @return If the {@link User} should be restricted from Chat on trigger
	 * @since 1.0.14
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final boolean isForceChatRestriction() {
		return forceChatRestriction;
	}

}
