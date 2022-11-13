package at.peirleitner.core.util.moderation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import javax.annotation.Nonnull;

public class UserChatMessage {

	private final UUID uuid;
	private final String message;
	private Collection<UserChatMessageFlag> flags;
	private long created;

	public UserChatMessage(UUID uuid, String message) {
		this.uuid = uuid;
		this.message = message;
		this.flags = new ArrayList<>();
		this.created = System.currentTimeMillis();
	}

	public final UUID getUUID() {
		return uuid;
	}

	public final String getMessage() {
		return message;
	}

	public final Collection<UserChatMessageFlag> getFlags() {
		return flags;
	}

	public final void addFlag(@Nonnull UserChatMessageFlag flag) {
		this.flags.add(flag);
	}

	public final void setFlags(@Nonnull Collection<UserChatMessageFlag> flags) {
		this.flags = flags;
	}

	public final boolean isFlagged() {
		return this.getFlags().isEmpty() ? false : true;
	}

	public final long getCreated() {
		return created;
	}

	public final void setCreated(long created) {
		this.created = created;
	}

}
