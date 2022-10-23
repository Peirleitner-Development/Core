package at.peirleitner.core.util;

import at.peirleitner.core.util.database.TableType;

/**
 * Simple interface to tag all Systems
 * 
 * @since 1.0.6
 * @author Markus Peirleitner (Rengobli)
 *
 */
public interface CoreSystem {

	public void createTable();

	public TableType getTableType();

}