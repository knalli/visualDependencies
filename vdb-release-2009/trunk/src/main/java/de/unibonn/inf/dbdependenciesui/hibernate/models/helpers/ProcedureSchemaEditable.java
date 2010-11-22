/**
 * $Id: Main.java 41 2009-03-01 16:21:20Z philipp $
 */
package de.unibonn.inf.dbdependenciesui.hibernate.models.helpers;

import java.util.List;

/**
 * This is the <b>editable</b> object representation of a trigger schema.
 * 
 * @see {@link ProcedureSchema}
 * @author Marc Kastleiner
 */
final public class ProcedureSchemaEditable extends ProcedureSchema {
	private static final long serialVersionUID = -4394790303310731653L;

	public ProcedureSchemaEditable(final String xml) {
		super(xml);
	}

	public ProcedureSchemaEditable() {
		super();
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(final String name) {
		this.name = name;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(final String type) {
		this.type = type;
	}

	/**
	 * @param body
	 *            the body to set
	 */
	public void setBody(final String body) {
		this.body = body;
	}

	/**
	 * @param state
	 *            the state to set
	 */
	public void setState(final String state) {
		this.state = state;
	}

	/**
	 * @param creation
	 *            date the creation date to set
	 */
	public void setCreationDate(final String creationDate) {
		this.creationDate = creationDate;
	}

	/**
	 * @param affectedTables
	 *            the affectedTables to set
	 */
	public void setAffectedDeletedTables(final List<String> affectedTables) {
		affectedDeletedTables = affectedTables;
	}

	public void addAffectedDeletedTable(final String table) {
		affectedDeletedTables.add(table);
	}

	public void removeAffectedDeletedTable(final String table) {
		affectedDeletedTables.remove(table);
	}

	/**
	 * @param affectedTables
	 *            the affectedTables to set
	 */
	public void setAffectedInsertedTables(final List<String> affectedTables) {
		affectedInsertedTables = affectedTables;
	}

	public void addAffectedInsertedTable(final String table) {
		affectedInsertedTables.add(table);
	}

	public void removeAffectedInsertedTable(final String table) {
		affectedInsertedTables.remove(table);
	}

	/**
	 * @param affectedTables
	 *            the affectedTables to set
	 */
	public void setAffectedUpdatedTables(final List<String> affectedTables) {
		affectedUpdatedTables = affectedTables;
	}

	public void addAffectedUpdatedTable(final String table) {
		affectedUpdatedTables.add(table);
	}

	public void removeAffectedUpdatedTable(final String table) {
		affectedUpdatedTables.remove(table);
	}

	/**
	 * @param usedTables
	 *            the usedTables to set
	 */
	public void setUsedTables(final List<String> usedTables) {
		this.usedTables = usedTables;
	}

	public void addUsedTable(final String table) {
		usedTables.add(table);
	}

	public void removeUsedTable(final String table) {
		usedTables.remove(table);
	}
}
