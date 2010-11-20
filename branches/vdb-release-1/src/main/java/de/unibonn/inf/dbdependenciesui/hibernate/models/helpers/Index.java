package de.unibonn.inf.dbdependenciesui.hibernate.models.helpers;

/**
 * This is the object representation of an index. A column belongs to an object of the type {@link DdlSchema}.
 * 
 * @author Andre Kasper
 * @author Jan Philipp
 */
public class Index extends ColumnKey {
	private static final long serialVersionUID = -1468871856003042554L;

	public Index(final String name) {
		super(name);
	}

}
