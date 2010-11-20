/**
 * $Id: Main.java 41 2009-03-01 16:21:20Z philipp $
 */
package de.unibonn.inf.dbdependenciesui.hibernate.models.helpers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This is the object representation of a column key. A key belongs to an object of the type {@link DdlSchema}. It
 * contains the name and a list of assigned object of the type {@link Column} .
 * 
 * @author Andre Kasper
 * @author Jan Philipp
 */
public class ColumnKey implements Serializable {
	private static final long serialVersionUID = -5243330561452393700L;

	private String name;

	private final List<String> columns = new ArrayList<String>(1);

	public ColumnKey(final String name) {
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
	 * @return the columns
	 */
	public List<String> getColumns() {
		return columns;
	}

	/**
	 * @param column
	 *            the column to add
	 */
	public void addColumn(final String column) {
		columns.add(column);
	}

	@Override
	public boolean equals(final Object other) {
		if (other instanceof ColumnKey) {
			if (name != null) { return name.equals(((ColumnKey) other).getName()); }
		}
		return false;
	}
}
