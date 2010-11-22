/**
 * $Id: Main.java 41 2009-03-01 16:21:20Z philipp $
 */
package de.unibonn.inf.dbdependenciesui.hibernate.models;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * This is the hibernate object representation of a view. A view is only a special table with additional attributes.
 * 
 * @author Andre Kasper
 * @author Jan Philipp
 */
@SuppressWarnings("serial")
@Entity
@Table(name = "db_views")
public class DatabaseView extends DatabaseTable {

	private boolean materialized = false;

	private String selectStatement;

	/**
	 * @return the materialized
	 */
	public boolean isMaterialized() {
		return materialized;
	}

	/**
	 * @return selectStatement
	 */
	public String getSelectStatement() {
		return selectStatement;
	}

	/**
	 * @param materialized
	 *            the materialized to set
	 */
	public void setMaterialized(final boolean materialized) {
		this.materialized = materialized;
	}

	/**
	 * @param selectStatement
	 *            the SelectStatement to set
	 */
	public void setSelectStatement(final String selectStatement) {
		this.selectStatement = selectStatement;
	}
}
