/**
 * $Id: Main.java 41 2009-03-01 16:21:20Z philipp $
 */
package de.unibonn.inf.dbdependenciesui.hibernate.models.helpers;

/**
 * This is the <b>editable</b> object representation of a ddl schema.
 * 
 * @see {@link DdlSchema}
 * @author Andre Kasper
 * @author Jan Philipp
 */
final public class DdlSchemaEditable extends DdlSchema {
	private static final long serialVersionUID = 8904553631608565204L;

	public DdlSchemaEditable(final String xml) {
		super(xml);
	}

	public DdlSchemaEditable() {
		super();
	}

	/**
	 * Add a column.
	 * 
	 * @param column
	 */
	public void addColumn(final Column column) {
		columns.add(column);
	}

	/**
	 * Remove a column.
	 * 
	 * @param column
	 * @return
	 */
	public boolean removeColumn(final Column column) {
		return columns.remove(column);
	}

	/**
	 * Add a primary key.
	 * 
	 * @param primaryKey
	 */
	public void addPrimaryKey(final PrimaryKey primaryKey) {
		primaryKeys.add(primaryKey);
	}

	/**
	 * Remove a primary key.
	 * 
	 * @param primaryKey
	 * @return
	 */
	public boolean removePrimaryKey(final PrimaryKey primaryKey) {
		return primaryKeys.remove(primaryKey);
	}

	/**
	 * Add an unique key.
	 * 
	 * @param uniqueKey
	 */
	public void addUniqueKey(final UniqueKey uniqueKey) {
		uniqueKeys.add(uniqueKey);
	}

	/**
	 * Remove an unique key.
	 * 
	 * @param uniqueKey
	 * @return
	 */
	public boolean removeUniqueKey(final UniqueKey uniqueKey) {
		return uniqueKeys.remove(uniqueKey);
	}

	/**
	 * Add an index.
	 * 
	 * @param index
	 */
	public void addIndex(final Index index) {
		indices.add(index);
	}

	/**
	 * Remove an index.
	 * 
	 * @param index
	 * @return
	 */
	public boolean removeIndex(final Index index) {
		return indices.remove(index);
	}

	/**
	 * Add a foreign key.
	 * 
	 * @param foreignKey
	 */
	public void addForeignKey(final ForeignKey foreignKey) {
		foreignKeys.add(foreignKey);
	}

	/**
	 * Remove a foreign key.
	 * 
	 * @param foreignKey
	 * @return
	 */
	public boolean removeForeignKey(final ForeignKey foreignKey) {
		return foreignKeys.remove(foreignKey);
	}

	/**
	 * Add a relation.
	 * 
	 * @param relation
	 */
	public void addRelation(final Relation relation) {
		relations.add(relation);
	}

	/**
	 * Remove a relation.
	 * 
	 * @param relation
	 * @return
	 */
	public boolean removeRelation(final Relation relation) {
		return relations.remove(relation);
	}

	public void addTrigger(final String trigger) {
		triggers.add(trigger);
	}

	public void removeTrigger(final String trigger) {
		triggers.remove(trigger);
	}
}
