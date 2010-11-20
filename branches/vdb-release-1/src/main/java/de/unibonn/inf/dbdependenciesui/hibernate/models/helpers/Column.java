/**
 * $Id: Main.java 41 2009-03-01 16:21:20Z philipp $
 */
package de.unibonn.inf.dbdependenciesui.hibernate.models.helpers;

import java.io.Serializable;

/**
 * This is the object representation of a column. A column belongs to an object of the type {@link DdlSchema}. It
 * contains the information about a table column: name, type, size, nullable.
 * 
 * @author Andre Kasper
 * @author Jan Philipp
 */
public class Column implements Serializable {
	private static final long serialVersionUID = 9086463803971830082L;

	private String name;

	private String type;

	private int size;

	private int fractionalDigits;

	private boolean nullable;

	public Column(final String name) {
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
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(final String type) {
		this.type = type;
	}

	/**
	 * @return the size
	 */
	public int getSize() {
		return size;
	}

	/**
	 * @param size
	 *            the size to set
	 */
	public void setSize(final int size) {
		this.size = size;
	}

	/**
	 * @return the fractionalDigits
	 */
	public int getFractionalDigits() {
		return fractionalDigits;
	}

	/**
	 * @param fractionalDigits
	 *            the fractionalDigits to set
	 */
	public void setFractionalDigits(final int fractionalDigits) {
		this.fractionalDigits = fractionalDigits;
	}

	/**
	 * @return the size
	 */
	public boolean isNullable() {
		return nullable;
	}

	/**
	 * @param size
	 *            the size to set
	 */
	public void setNullable(final boolean isNullable) {
		nullable = isNullable;
	}

	@Override
	public boolean equals(final Object other) {
		if (other instanceof Column) {
			if (name != null) { return name.equals(((Column) other).getName()); }
		}
		return super.equals(other);
	}

	@Override
	public String toString() {
		return getName();
	}
}
