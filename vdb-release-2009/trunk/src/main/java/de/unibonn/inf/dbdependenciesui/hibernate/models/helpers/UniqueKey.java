/**
 * $Id: Main.java 41 2009-03-01 16:21:20Z philipp $
 */
package de.unibonn.inf.dbdependenciesui.hibernate.models.helpers;

/**
 * This is the object representation of a unique key. A column belongs to an object of the type {@link DdlSchema}.
 * 
 * @author Andre Kasper
 * @author Jan Philipp
 */
public class UniqueKey extends ColumnKey {
	private static final long serialVersionUID = 3037914727740851126L;

	public UniqueKey(final String name) {
		super(name);
	}

	@Override
	public String toString() {
		return String.format("%s (%s: %s)", getName(), "UNIQUE", getColumns().toString());
	}

}
