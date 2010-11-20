/**
 * $Id: Main.java 41 2009-03-01 16:21:20Z philipp $
 */
package de.unibonn.inf.dbdependenciesui.hibernate.models.helpers;

import java.io.Serializable;

/**
 * This is the object representation of a foreign key. A key belongs to an object of the type {@link DdlSchema}.
 * 
 * @author Andre Kasper
 * @author Jan Philipp
 */
public class ForeignKey implements Serializable {
	private static final long serialVersionUID = -1582658748480557008L;

	private String name;

	private String column;

	private String referToTable;

	private String referToColumn;

	public ForeignKey(final String name) {
		this.name = name;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(final String name) {
		this.name = name;
	}

	/**
	 * @return the column
	 */
	public String getColumn() {
		return column;
	}

	/**
	 * @param column
	 *            the column to set
	 */
	public void setColumn(final String column) {
		this.column = column;
	}

	/**
	 * @return the referToTable
	 */
	public String getReferToTable() {
		return referToTable;
	}

	/**
	 * @param referToTable
	 *            the referToTable to set
	 */
	public void setReferToTable(final String referToTable) {
		this.referToTable = referToTable;
	}

	/**
	 * @return the referToColumn
	 */
	public String getReferToColumn() {
		return referToColumn;
	}

	/**
	 * @param referToColumn
	 *            the referToColumn to set
	 */
	public void setReferToColumn(final String referToColumn) {
		this.referToColumn = referToColumn;
	}

	@Override
	public String toString() {
		return String.format("%s (%s.%s)", getColumn(), getReferToTable(), getReferToColumn());
	}
}
