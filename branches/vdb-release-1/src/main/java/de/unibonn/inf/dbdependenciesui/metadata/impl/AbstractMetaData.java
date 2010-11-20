/**
 * $Id$
 */
package de.unibonn.inf.dbdependenciesui.metadata.impl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Properties;
import java.util.logging.Logger;

import de.unibonn.inf.dbdependenciesui.Configuration;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseConnection;
import de.unibonn.inf.dbdependenciesui.metadata.IAnalyzer;
import de.unibonn.inf.dbdependenciesui.metadata.IConnectionBuilder;
import de.unibonn.inf.dbdependenciesui.metadata.IMetaData;
import de.unibonn.inf.dbdependenciesui.metadata.IProcedureSqlParser;
import de.unibonn.inf.dbdependenciesui.metadata.ITriggerSqlParser;
import de.unibonn.inf.dbdependenciesui.metadata.IViewSqlParser;
import de.unibonn.inf.dbdependenciesui.metadata.MetaDataFactory;
import de.unibonn.inf.dbdependenciesui.metadata.MetaDataFactory.Vendor;

/**
 * Abstract class for a metadata class. This abstract class delegates the analyzing and parsing jobs to the
 * corresponding subclasses of {@link AbstractAnalyzer}, {@link AbstractViewSqlParser} and
 * {@link AbstractTriggerSqlParser}. The metadata component observes both sub components analyzer and parser (observer
 * pattern).
 * 
 * @author Andre Kasper
 * @author Jan Philipp
 * @author Marc Kastleiner
 */
abstract public class AbstractMetaData implements IMetaData {

	/**
	 * database connection
	 */
	protected Connection connection = null;

	protected DatabaseConnection databaseConnection = null;

	protected static Logger log = Logger.getLogger(Configuration.LOGGER);

	/**
	 * list of occurred errors
	 */
	protected List<String> errors = new ArrayList<String>();

	protected DatabaseMetaData metaData;

	protected String connectionUrl = null;

	protected Properties properties;

	protected ConnectionType connectionType = null;

	protected List<PropertyChangeListener> changeListeners = new ArrayList<PropertyChangeListener>();

	protected boolean initialized = false;

	protected IAnalyzer analyzer = null;

	protected IViewSqlParser viewParser = null;

	protected ITriggerSqlParser triggerParser = null;

	protected IProcedureSqlParser procedureParser = null;

	protected boolean replaceMode = true;

	/**
	 * implemented database vendor
	 */
	protected final Vendor vendor;

	public AbstractMetaData(final Vendor vendor) {
		this.vendor = vendor;
		setReplaceMode(true);
	}

	@Override
	public Vendor getVendor() {
		return vendor;
	}

	@Override
	public void close() {
		if (connection != null) {
			try {
				connection.close();
			} catch (final SQLException e) {
				handleException(e);
			}
		}
	}

	@Override
	public void setDatabaseConnection(final DatabaseConnection connection) {
		databaseConnection = connection;
	}

	public boolean checkConnect() {

		final boolean success = connect();
		if (success) {
			close();
		}

		connectionType = null;
		connectionUrl = null;
		properties = null;

		return success;
	}

	@Override
	public void setReplaceMode(final boolean replace) {
		replaceMode = replace;
	}

	@Override
	public boolean isReplaceMode() {
		return replaceMode;
	}

	@Override
	public boolean connect() {
		boolean success = false;
		final IConnectionBuilder builder = getConnectionBuilder();

		if (builder != null) {
			try {
				connection = builder.createConnection(databaseConnection);
				success = true;
			} catch (final SQLException e) {
				handleException(e);
			}
		}

		if (success) {
			firePropertyChange("connection", null, connection.toString());
			createMetadata();
		}

		return success;
	}

	@Override
	public boolean isConnected() {
		if (connection != null) {
			try {
				return !connection.isClosed();
			} catch (final SQLException e) {
				handleException(e);
			}
		}
		return false;
	}

	@Override
	public void clearErrors() {
		errors.clear();
	}

	@Override
	public String getLastError() {
		if (isErrorOccured()) { return errors.get(errors.size() - 1); }
		return null;
	}

	@Override
	public String getNextError() {
		if (isErrorOccured()) { return errors.remove(0); }
		return null;
	}

	@Override
	public boolean isErrorOccured() {
		return !errors.isEmpty();
	}

	/**
	 * Handle the given exception.
	 * 
	 * @param e
	 */
	protected void handleException(final Exception e) {
		errors.add(e.getLocalizedMessage());
	}

	/**
	 * Create a metadata object.
	 */
	protected void createMetadata() {
		try {
			metaData = connection.getMetaData();
			firePropertyChange("metadata", null, metaData.toString());
		} catch (final SQLException e) {
			handleException(e);
		}
	}

	/**
	 * Return if the metadata is initialized.
	 * 
	 * @return
	 */
	protected boolean initializedMetaData() {
		return metaData != null;
	}

	@Override
	public void initialize() {
		initializeIfRequired();
	}

	protected void initializeIfRequired() {
		if (!initialized) {
			initialized = true;
			analyzer = MetaDataFactory.createAnalyzer(vendor);
			viewParser = MetaDataFactory.createViewSqlParser(vendor);
			triggerParser = MetaDataFactory.createTriggerSqlParser(vendor);
			procedureParser = MetaDataFactory.createProcedureSqlParser(vendor);

			analyzer.addObserver(this);
			viewParser.addObserver(this);
			triggerParser.addObserver(this);
			procedureParser.addObserver(this);
		}
	}

	public void addPropertyChangeListener(final PropertyChangeListener listener) {
		if (listener != null) {
			changeListeners.add(listener);
		}
	}

	public void removePropertyChangeListener(final PropertyChangeListener listener) {
		if (listener != null) {
			changeListeners.remove(listener);
		}
	}

	protected void firePropertyChange(final PropertyChangeEvent evt) {
		for (final PropertyChangeListener changeListener : changeListeners) {
			changeListener.propertyChange(evt);
		}
	}

	protected void firePropertyChange(final String propertyName, final Object oldValue, final Object newValue) {
		final PropertyChangeEvent evt = new PropertyChangeEvent(this, propertyName, oldValue, newValue);
		this.firePropertyChange(evt);
	}

	@Override
	public void update(final Observable o, final Object arg) {
		if (arg instanceof PropertyChangeEvent) {
			this.firePropertyChange((PropertyChangeEvent) arg);
		}
	}

	@Override
	public String getDescription() {
		return null;
	}

	protected static enum ConnectionType {
		ONLY_URL, URL_WITH_NAME_AND_PASSWORD, URL_WITH_PROPERTIES
	}

}
