package at.peirleitner.core.api.exception;

import javax.annotation.Nonnull;

import at.peirleitner.core.system.EconomySystem;

/**
 * Thrown if a manipulating economy function inside the {@link EconomySystem}
 * returns <code>false</code>.
 * 
 * @since 1.0.6
 * @author Markus Peirleitner (Rengobli)
 *
 */
public class EconomyManipulateException extends RuntimeException {

	private static final long serialVersionUID = 5285362154073378051L;

	public EconomyManipulateException(@Nonnull String message) {
		super(message);
	}

}
