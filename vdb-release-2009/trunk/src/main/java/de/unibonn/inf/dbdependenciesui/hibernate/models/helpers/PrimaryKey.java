/**
 * $Id: Main.java 41 2009-03-01 16:21:20Z philipp $
 */
package de.unibonn.inf.dbdependenciesui.hibernate.models.helpers;

/**
 *This is the object representation of a primary key. A column belongs to an object of the type {@link DdlSchema}.
 * 
 * @author Andre Kasper
 * @author Jan Philipp
 */
public class PrimaryKey extends ColumnKey {
	private static final long serialVersionUID = -6872970578726175460L;

	public PrimaryKey(final String name) {
		super(name);
	}

	@Override
	public String toString() {
		return String.format("%s (%s: %s)", getName(), "PRIMARY", getColumns().toString());
	}
}
