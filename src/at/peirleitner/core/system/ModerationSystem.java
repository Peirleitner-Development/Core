package at.peirleitner.core.system;

import java.sql.PreparedStatement;
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
		
	}

	@Override
	public void createTable() {
		
		try {
			
			PreparedStatement stmt = null;
			List<String> statements = new ArrayList<>();
			
			statements.add("CREATE TABLE IF NOT EXISTS " + TableType.MODERATION_BLOCKED_PHRASES.getTableName(true) + " ("
					+ "phrase VARCHAR(30) NOT NULL, "
					+ "added BIGINT(255) NOT NULL DEFAULT '" + System.currentTimeMillis() + "', "
					+ "staff CHAR(36), "
					+ "PRIMARY KEY(phrase));");
			
			statements.add("CREATE TABLE IF NOT EXISTS " + TableType.MODERATION_ALLOWED_DOMAINS.getTableName(true) + " ("
					+ "domain VARCHAR(80) NOT NULL, "
					+ "added BIGINT(255) NOT NULL DEFAULT '" + System.currentTimeMillis() + "', "
					+ "staff CHAR(36), "
					+ "PRIMARY KEY(domain));");
			
			for(String s : statements) {
				
				stmt = Core.getInstance().getMySQL().getConnection().prepareStatement(s);
				stmt.execute();
				
			}
			
		} catch (SQLException e) {
			Core.getInstance().log(getClass(), LogType.ERROR, "Could not create tables for ModerationSystem/SQL: " + e.getMessage());
			return;
		}
	}

	@Override
	public TableType getTableType() {
		return null;
	}

}
