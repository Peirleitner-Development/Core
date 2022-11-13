package at.peirleitner.core.util.moderation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import javax.annotation.Nonnull;

import at.peirleitner.core.util.user.User;

/**
 * Chat Message sent by a {@link User}
 * 
 * @since 1.0.14
 * @author Markus Peirleitner (Rengobli)
 *
 */
public class UserChatMessage {

	private int id;
	private UUID uuid;
	private String message;
	private long created;
	private Collection<UserChatMessageFlag> flags;

	public UserChatMessage() {
		this.id = -1;
		this.uuid = null;
		this.message = null;
		this.created = -1;
		this.flags = new ArrayList<>();
	}

	public UserChatMessage(UUID uuid, String message) {
		this.id = -1;
		this.uuid = uuid;
		this.message = message;
		this.created = System.currentTimeMillis();
		this.flags = new ArrayList<>();
	}

	public final int getID() {
		return id;
	}

	public void setID(@Nonnull int id) {
		this.id = id;
	}

	public final UUID getUUID() {
		return uuid;
	}

	public final void setUUID(@Nonnull UUID uuid) {
		this.uuid = uuid;
	}

	public final String getMessage() {
		return message;
	}

	public final void setMessage(String message) {
		this.message = message;
	}

	public final long getCreated() {
		return created;
	}

	public final void setCreated(long created) {
		this.created = created;
	}

	public final Collection<UserChatMessageFlag> getFlags() {
		return flags;
	}

	public final void setFlags(Collection<UserChatMessageFlag> flags) {
		this.flags = flags;
	}

	public final boolean hasFlags() {
		return this.getFlags().isEmpty() ? false : true;
	}

}
