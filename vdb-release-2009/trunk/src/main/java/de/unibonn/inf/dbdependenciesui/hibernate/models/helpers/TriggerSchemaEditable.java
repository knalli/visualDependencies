/**
 * $Id: Main.java 41 2009-03-01 16:21:20Z philipp $
 */
package de.unibonn.inf.dbdependenciesui.hibernate.models.helpers;

import java.util.List;

/**
 * This is the <b>editable</b> object representation of a trigger schema.
 * 
 * @see {@link TriggerSchema}
 * @author Andre Kasper
 * @author Jan Philipp
 */
final public class TriggerSchemaEditable extends TriggerSchema {
	private static final long serialVersionUID = -4394790303310731653L;

	public TriggerSchemaEditable(final String xml) {
		super(xml);
	}

	public TriggerSchemaEditable() {
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
	 * @param events
	 *            the events to set
	 */
	public void setEvents(final List<String> events) {
		this.events = events;
	}

	/**
	 * @param tableOwner
	 *            the tableOwner to set
	 */
	public void setTableOwner(final String tableOwner) {
		this.tableOwner = tableOwner;
	}

	/**
	 * @param baseObjectType
	 *            the baseObjectType to set
	 */
	public void setBaseObjectType(final String baseObjectType) {
		this.baseObjectType = baseObjectType;
	}

	/**
	 * @param tableName
	 *            the tableName to set
	 */
	public void setTableName(final String tableName) {
		this.tableName = tableName;
	}

	/**
	 * @param columnName
	 *            the columnName to set
	 */
	public void setColumnName(final String columnName) {
		this.columnName = columnName;
	}

	/**
	 * @param referencingNames
	 *            the referencingNames to set
	 */
	public void setReferencingNames(final String referencingNames) {
		this.referencingNames = referencingNames;
	}

	/**
	 * @param whenClause
	 *            the whenClause to set
	 */
	public void setWhenClause(final String whenClause) {
		this.whenClause = whenClause;
	}

	/**
	 * @param enabled
	 *            the enabled to set
	 */
	public void setEnabled(final boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public void setDescription(final String description) {
		this.description = description;
	}

	/**
	 * @param actionType
	 *            the actionType to set
	 */
	public void setActionType(final String actionType) {
		this.actionType = actionType;
	}

	/**
	 * @param body
	 *            the body to set
	 */
	public void setBody(final String body) {
		this.body = body;
	}

	/**
	 * @param crossEdition
	 *            the crossEdition to set
	 */
	public void setCrossEdition(final boolean crossEdition) {
		this.crossEdition = crossEdition;
	}

	/**
	 * @param beforeStatement
	 *            the beforeStatement to set
	 */
	public void setBeforeStatement(final boolean beforeStatement) {
		this.beforeStatement = beforeStatement;
	}

	/**
	 * @param beforeRow
	 *            the beforeRow to set
	 */
	public void setBeforeRow(final boolean beforeRow) {
		this.beforeRow = beforeRow;
	}

	/**
	 * @param afterStatement
	 *            the afterStatement to set
	 */
	public void setAfterStatement(final boolean afterStatement) {
		this.afterStatement = afterStatement;
	}

	/**
	 * @param afterRow
	 *            the afterRow to set
	 */
	public void setAfterRow(final boolean afterRow) {
		this.afterRow = afterRow;
	}

	/**
	 * @param insteadOfRow
	 *            the insteadOfRow to set
	 */
	public void setInsteadOfRow(final boolean insteadOfRow) {
		this.insteadOfRow = insteadOfRow;
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
