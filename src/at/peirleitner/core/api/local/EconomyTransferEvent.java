package at.peirleitner.core.api.local;

import javax.annotation.Nonnull;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import at.peirleitner.core.command.local.CommandPay;
import at.peirleitner.core.util.user.User;

/**
 * Triggered when attempting to send money between players using
 * {@link CommandPay}.
 * 
 * @since 1.0.6
 * @author Markus Peirleitner (Rengobli)
 *
 */
public class EconomyTransferEvent extends Event implements Cancellable {

	private static final HandlerList HANDLERS = new HandlerList();
	private boolean cancelled;
	private final User sender;
	private final User target;
	private final double amount;

	public EconomyTransferEvent(@Nonnull User sender, @Nonnull User target, @Nonnull double amount) {
		this.cancelled = false;
		this.sender = sender;
		this.target = target;
		this.amount = amount;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS;
	}

	@Override
	public HandlerList getHandlers() {
		return HANDLERS;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	public final User getSender() {
		return sender;
	}

	public final User getTarget() {
		return target;
	}

	public final double getAmount() {
		return amount;
	}

	@Override
	public final String toString() {
		return "EconomyTransferEvent[cancelled=" + this.isCancelled() + ",sender="
				+ this.getSender().getUUID().toString() + ",target=" + this.getTarget().getUUID().toString()
				+ ",amount=" + this.getAmount() + "]";
	}

}
