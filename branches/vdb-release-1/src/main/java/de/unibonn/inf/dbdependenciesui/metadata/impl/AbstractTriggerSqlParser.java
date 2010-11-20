/**
 * $Id$
 */
package de.unibonn.inf.dbdependenciesui.metadata.impl;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;

import de.unibonn.inf.dbdependenciesui.metadata.ITriggerSqlParser;
import de.unibonn.inf.dbdependenciesui.metadata.MetaDataFactory.Vendor;

/**
 * Abstract class for a trigger sql parser.
 * 
 * @author Andre Kasper
 * @author Jan Philipp
 */
abstract public class AbstractTriggerSqlParser extends Observable implements ITriggerSqlParser {
	/**
	 * implemented database vendor
	 */
	protected final Vendor vendor;

	public AbstractTriggerSqlParser(final Vendor vendor) {
		this.vendor = vendor;
	}

	@Override
	public Vendor getVendor() {
		return vendor;
	}

	/**
	 * Notify all observers with an PropertyChangeEvent.
	 * 
	 * @param propertyName
	 * @param newValue
	 */
	protected void firePropertyChange(final String propertyName, final Object newValue) {
		setChanged();
		this.notifyObservers(new PropertyChangeEvent(this, propertyName, null, newValue));
	}

	/**
	 * Return a list of all tables which are used in select statements (used by this trigger).
	 * 
	 * @return
	 */
	public List<String> getUsedTables() {
		return Collections.<String> emptyList();
	}

	/**
	 * Return a list of all tables which are modified in dml statements (modified by this trigger).
	 * 
	 * @see #getTablesOnAlter()
	 * @see #getTablesOnCreate()
	 * @see #getTablesOnDelete()
	 * @see #getTablesOnDrop()
	 * @see #getTablesOnInsert()
	 * @see #getTablesOnUpdate()
	 * @return
	 */
	public List<String> getModifiedTables() {
		final List<String> result = new ArrayList<String>();

		result.addAll(getTablesOnDelete());
		result.addAll(getTablesOnInsert());
		result.addAll(getTablesOnUpdate());

		return result;
	}

	/**
	 * Return a list of all tables which are modified in delete statements (deleted data by this trigger).
	 * 
	 * @return
	 */
	public List<String> getTablesOnDelete() {
		return Collections.<String> emptyList();
	}

	/**
	 * Return a list of all tables which are modified in insert statements (inserted data by this trigger).
	 * 
	 * @return
	 */
	public List<String> getTablesOnInsert() {
		return Collections.<String> emptyList();
	}

	/**
	 * Return a list of all tables which are modified in update statements (updated data by this trigger).
	 * 
	 * @return
	 */
	public List<String> getTablesOnUpdate() {
		return Collections.<String> emptyList();
	}

	@Override
	public String getDescription() {
		return null;
	}
}
