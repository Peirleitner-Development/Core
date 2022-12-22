package at.peirleitner.core.util.user;

import javax.annotation.Nonnull;

public enum PredefinedMessage {

	PREFIX("prefix"),
	ACTION_REQUIRES_PLAYER("main.action-may-only-be-performed-by-a-player"),
	NO_PERMISSION("main.no-permission"),
	REQUIRES_ITEM_IN_MAIN_HAND("main.action-requires-item-in-main-hand"),
	SERVER_NAME("manager.settings.server-name"),
	
	/**
	 * Website of the server
	 * @since 1.0.5
	 * @author Markus Peirleitner (Rengobli)
	 */
	SERVER_WEBSITE("manager.settings.server-website"),
	
	TAB_HEADER("tab.header"),
	TAB_FOOTER("tab.footer"),
	WORLD_DOES_NOT_EXIST("main.world-does-not-exist"),
	TARGET_PLAYER_NOT_FOUND("main.target-player-not-found"),
	CANT_TARGET_YOURSELF("main.cant-target-yourself"),
	
	/**
	 * No Core-Account ({@link User})
	 * @since 1.0.6
	 * @author Markus Peirleitner (Rengobli)
	 */
	NOT_REGISTERED("main.player-not-registered"),
	
	/**
	 * Could not get ID (Integer) from parsed String
	 * @since 1.0.6
	 * @author Markus Peirleitner (Rengobli)
	 */
	INVALID_ID("main.invalid-id"),
	
	/**
	 * Could not get SaveType
	 * @since 1.0.6
	 * @author Markus Peirleitner (Rengobli)
	 */
	INVALID_SAVETYPE("main.invalid-saveType"),
	
	/**
	 * Website of the server store
	 * @since 1.0.6
	 * @author Markus Peirleitner (Rengobli)
	 */
	SERVER_STORE("manager.settings.server-store"),
	
	/**
	 * Server Name as of Server#getName() of previous spigot/paper versions
	 * @since 1.0.7
	 * @author Markus Peirleitner (Rengobli)
	 * @apiNote Used in the Tab-Header of the SpigotMain
	 */
	SERVER_NAME_PROPERTIES("manager.settings.server-name-properties"),
	
	/**
	 * {@link User} does not have enough economy
	 * @since 1.0.9
	 * @author Markus Peirleitner (Rengobli)
	 */
	NOT_ENOUGH_ECONOMY("main.not-enough-economy"),
	
	/**
	 * Inventory full
	 * @since 1.0.16
	 * @author Markus Peirleitner (Rengobli)
	 */
	INVENTORY_FULL("main.inventory-full");
	
	private final String path;

	private PredefinedMessage(@Nonnull String path) {
		this.path = path;
	}
	
	public final String getPath() {
		return this.path;
	}
	
}
