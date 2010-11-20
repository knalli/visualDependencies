/**
 * $Id$
 */
package de.unibonn.inf.dbdependenciesui.metadata.impl;

import java.beans.PropertyChangeEvent;
import java.util.Observable;

import de.unibonn.inf.dbdependenciesui.metadata.IViewSqlParser;
import de.unibonn.inf.dbdependenciesui.metadata.MetaDataFactory.Vendor;

/**
 * Abstract class for a view sql parser.
 * 
 * @author Andre Kasper
 * @author Jan Philipp
 */
abstract public class AbstractViewSqlParser extends Observable implements IViewSqlParser {
	/**
	 * implemented database vendor
	 */
	protected final Vendor vendor;

	public AbstractViewSqlParser(final Vendor vendor) {
		this.vendor = vendor;
	}

	@Override
	public Vendor getVendor() {
		return vendor;
	}

	/**
	 * Notofiy all observers with an PropertyChangeEvent.
	 * 
	 * @param propertyName
	 * @param newValue
	 */
	protected void firePropertyChange(final String propertyName, final Object newValue) {
		setChanged();
		this.notifyObservers(new PropertyChangeEvent(this, propertyName, null, newValue));
	}

	@Override
	public String getDescription() {
		return null;
	}
}
