/**
 * $Id: Main.java 41 2009-03-01 16:21:20Z philipp $
 */
package de.unibonn.inf.dbdependenciesui.hibernate.models.helpers;

import java.io.Serializable;

import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseConnection;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseTable;

/**
 * This is the object representation of a relation. A relation belongs to an object of the type {@link DdlSchema}. A
 * relation is defined as from a source to a target. Because a relation can stored in both (source or target), the
 * property isFromSource shows the current state. Note: The isFromSource property is not stored. <code>isFromSource ==
 * getName().equals(getSourceName()</code>). Example: The view "VIEW1" has a relation to the table "TABLE1". You will
 * need two relation objects: The first belongs to the ddl schema of VIEW1, the second to the ddl schema of TABLE1.
 * Relation1 will constructed with <code>new Relation(view1, table1, true)</code>, but Relation with
 * <code>new Relation(view1, table1, false)</code>.
 * 
 * @author Andre Kasper
 * @author Jan Philipp
 */
public class Relation implements Serializable {
	private static final long serialVersionUID = -7679805502036694067L;

	private String name;

	private String targetName;

	private String sourceName;

	private boolean view;

	private String column;

	private String condition;

	private boolean positive;

	private boolean andCondition;

	private DatabaseConnection connection = null;

	public Relation(final String sourceName, final String targetName, final boolean isFromSource) {
		name = isFromSource ? sourceName : targetName;
		this.sourceName = sourceName;
		this.targetName = targetName;
	}

	public Relation(final DatabaseTable source, final DatabaseTable target, final boolean isFromSource) {
		this(source.getTitle(), target.getTitle(), isFromSource);
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
	 * @return the tableName
	 */
	public String getTargetName() {
		return targetName;
	}

	/**
	 * Find and return the table object for the given table name.
	 * 
	 * @param name
	 *            the name of the object
	 * @param useAlwaysViewLoader
	 *            specifies if the searched object is a table or a view
	 * @return
	 */
	private DatabaseTable getTableByName(final String name, final boolean useAlwaysViewLoader) {

		DatabaseTable entity = null;

		if (useAlwaysViewLoader || isView()) {
			entity = connection.getViewByTitle(name);
		}

		if (entity == null) {
			entity = connection.getTableByTitle(name);
		}

		return entity;
	}

	/**
	 * Get the source object entity.
	 * 
	 * @return
	 */
	public DatabaseTable getSourceTable() {
		return getTableByName(getSourceName(), true);
	}

	/**
	 * Get the target object entity.
	 * 
	 * @return
	 */
	public DatabaseTable getTargetTable() {
		return getTableByName(getTargetName(), false);
	}

	/**
	 * @param targetName
	 *            the tableName to set
	 */
	public void setTargetName(final String targetName) {
		this.targetName = targetName;
	}

	/**
	 * @return the tableName
	 */
	public String getSourceName() {
		return sourceName;
	}

	/**
	 * @param sourceName
	 *            the tableName to set
	 */
	public void setSourceName(final String sourceName) {
		this.sourceName = sourceName;
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
	 * @return the condition
	 */
	public String getCondition() {
		return condition;
	}

	/**
	 * @param condition
	 *            the condition to set
	 */
	public void setCondition(final String condition) {
		this.condition = condition;
	}

	/**
	 * @return the view
	 */
	public boolean isView() {
		return view;
	}

	/**
	 * @param view
	 *            the view to set
	 */
	public void setView(final boolean view) {
		this.view = view;
	}

	/**
	 * @return the positive
	 */
	public boolean isPositive() {
		return positive;
	}

	/**
	 * @param positive
	 *            the positive to set
	 */
	public void setPositive(final boolean positive) {
		this.positive = positive;
	}

	/**
	 * @return the andCondition
	 */
	public boolean isAndCondition() {
		return andCondition;
	}

	/**
	 * @param andCondition
	 *            the andCondition to set
	 */
	public void setAndCondition(final boolean andCondition) {
		this.andCondition = andCondition;
	}

	@Override
	public String toString() {
		return getName();
	}

	/**
	 * Return if the relation's point of view is from the source.
	 * 
	 * @return
	 */
	public boolean isFromSource() {
		if (name != null) {
			return name.equals(sourceName);
		} else {
			return false;
		}
	}

	/**
	 * Set the corresponding database connection id. This information is required for
	 * {@link #getTableByName(String, boolean)}
	 * 
	 * @param connection
	 */
	public void setConnection(final DatabaseConnection connection) {
		this.connection = connection;
	}
}
