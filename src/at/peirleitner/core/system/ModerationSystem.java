package at.peirleitner.core.system;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import at.peirleitner.core.Core;
import at.peirleitner.core.util.CoreSystem;
import at.peirleitner.core.util.LogType;
import at.peirleitner.core.util.database.TableType;

public class ModerationSystem implements CoreSystem {

	private Collection<String> cachedBlockedPhrases;
	private Collection<String> cachedAllowedDomains;

	public ModerationSystem() {

		// Initialize
		this.createTable();
		this.cachedBlockedPhrases = new ArrayList<>();
		this.cachedAllowedDomains = new ArrayList<>();

		// Load
		this.reload();
		
	}

	private final void reload() {

		if(Core.getInstance().getMySQL() == null || Core.getInstance().getMySQL().isConnected()) {
			Core.getInstance().log(getClass(), LogType.DEBUG, "Not loading ModerationSystem cache data since MySQL Connection has not yet been established.");
			return;
		}
		
		this.getCachedBlockedPhrases().clear();
		this.getCachedBlockedPhrases().addAll(this.getBlockedPhrasesFromDatabase());
		Core.getInstance().log(getClass(), LogType.INFO,
				"Loaded " + this.getCachedBlockedPhrases().size() + " blocked Phrases from Database.");

		this.getCachedAllowedDomains().clear();
		this.getCachedAllowedDomains().addAll(this.getAllowedDomainsFromDatabase());
		Core.getInstance().log(getClass(), LogType.INFO,
				"Loaded " + this.getCachedAllowedDomains().size() + " allowed Domains from Database.");

	}

	public final Collection<String> getBlockedPhrases() {
		return this.getCachedBlockedPhrases().isEmpty() ? this.getBlockedPhrasesFromDatabase()
				: this.getCachedBlockedPhrases();
	}

	private final Collection<String> getCachedBlockedPhrases() {
		return this.cachedBlockedPhrases;
	}

	private final Collection<String> getBlockedPhrasesFromDatabase() {

		Collection<String> phrases = new ArrayList<>();

		try {

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection()
					.prepareStatement("SELECT * FROM " + TableType.MODERATION_BLOCKED_PHRASES.getTableName(true));
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {

				String phrase = rs.getString(1);
				phrases.add(phrase);

			}

		} catch (SQLException e) {
			Core.getInstance().log(getClass(), LogType.ERROR,
					"Could not get blocked phrases from Database/SQL: " + e.getMessage());

		}

		return phrases;

	}

	public final Collection<String> getAllowedDomains() {
		return this.getCachedAllowedDomains().isEmpty() ? this.getAllowedDomainsFromDatabase()
				: this.getCachedAllowedDomains();
	}

	private final Collection<String> getCachedAllowedDomains() {
		return this.cachedAllowedDomains;
	}

	private final Collection<String> getAllowedDomainsFromDatabase() {

		Collection<String> domains = new ArrayList<>();

		try {

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection()
					.prepareStatement("SELECT * FROM " + TableType.MODERATION_ALLOWED_DOMAINS.getTableName(true));
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {

				String domain = rs.getString(1);
				domains.add(domain);

			}

		} catch (SQLException e) {
			Core.getInstance().log(getClass(), LogType.ERROR,
					"Could not get allowed domains from Database/SQL: " + e.getMessage());

		}

		return domains;

	}

	@Override
	public void createTable() {

		try {

			PreparedStatement stmt = null;
			List<String> statements = new ArrayList<>();

			statements.add("CREATE TABLE IF NOT EXISTS " + TableType.MODERATION_BLOCKED_PHRASES.getTableName(true)
					+ " (" + "phrase VARCHAR(30) NOT NULL, " + "added BIGINT(255) NOT NULL DEFAULT '"
					+ System.currentTimeMillis() + "', " + "staff CHAR(36), " + "PRIMARY KEY(phrase));");

			statements.add("CREATE TABLE IF NOT EXISTS " + TableType.MODERATION_ALLOWED_DOMAINS.getTableName(true)
					+ " (" + "domain VARCHAR(80) NOT NULL, " + "added BIGINT(255) NOT NULL DEFAULT '"
					+ System.currentTimeMillis() + "', " + "staff CHAR(36), " + "PRIMARY KEY(domain));");

			for (String s : statements) {

				stmt = Core.getInstance().getMySQL().getConnection().prepareStatement(s);
				stmt.execute();

			}

		} catch (SQLException e) {
			Core.getInstance().log(getClass(), LogType.ERROR,
					"Could not create tables for ModerationSystem/SQL: " + e.getMessage());
			return;
		}
	}

	@Override
	public TableType getTableType() {
		return null;
	}

}
