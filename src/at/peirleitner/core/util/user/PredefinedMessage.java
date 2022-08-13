package at.peirleitner.core.util.user;

import javax.annotation.Nonnull;

public enum PredefinedMessage {

	PREFIX("prefix"),
	ACTION_REQUIRES_PLAYER("main.action-may-only-be-performed-by-a-player"),
	NO_PERMISSION("main.no-permission"),
	REQUIRES_ITEM_IN_MAIN_HAND("main.action-requires-item-in-main-hand");
	
	private final String path;

	private PredefinedMessage(@Nonnull String path) {
		this.path = path;
	}
	
	public final String getPath() {
		return this.path;
	}
	
}