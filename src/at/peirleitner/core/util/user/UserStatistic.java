package at.peirleitner.core.util.user;

import java.util.UUID;

import javax.annotation.Nonnull;

/**
 * This class defines an user statistic
 * 
 * @since 1.0.17
 * @author Markus Peirleitner (Rengobli)
 * @see AvailableUserStatistic
 */
public class UserStatistic {

	private final UUID uuid;
	private AvailableUserStatistic statistic;
	private int amount;
	private long firstAdded;
	private long lastAdded;

	public UserStatistic(@Nonnull UUID uuid) {
		this.uuid = uuid;
	}

	public final UUID getUUID() {
		return uuid;
	}

	public final AvailableUserStatistic getStatistic() {
		return statistic;
	}

	public final void setStatistic(AvailableUserStatistic statistic) {
		this.statistic = statistic;
	}

	public final int getAmount() {
		return amount;
	}

	public final void setAmount(int amount) {
		this.amount = amount;
	}

	public final long getFirstAdded() {
		return firstAdded;
	}

	public final void setFirstAdded(long firstAdded) {
		this.firstAdded = firstAdded;
	}

	public final long getLastAdded() {
		return lastAdded;
	}

	public final void setLastAdded(long lastAdded) {
		this.lastAdded = lastAdded;
	}

	@Override
	public String toString() {
		return "UserStatistic [uuid=" + uuid + ", statistic=" + statistic + ", amount=" + amount + ", firstAdded="
				+ firstAdded + ", lastAdded=" + lastAdded + "]";
	}

}
