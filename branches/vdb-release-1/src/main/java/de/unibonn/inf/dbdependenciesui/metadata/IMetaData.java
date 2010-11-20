/**
 * $Id: Main.java 41 2009-03-01 16:21:20Z philipp $
 */
package de.unibonn.inf.dbdependenciesui.metadata;

import java.beans.PropertyChangeListener;
import java.util.Observer;

import javax.transaction.NotSupportedException;

import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseConnection;
import de.unibonn.inf.dbdependenciesui.metadata.MetaDataFactory.Vendor;

/**
 * An object that implements this interface retrives several meta information about a given database connection. Example
 * usage after setting the database connection via {@link #setDatabaseConnection(DatabaseConnection)}:
 * <ol>
 * <li>{@link #connect()}</li>
 * <li>{@link #initialize()}</li>
 * <li>{@link #analyze()}</li>
 * <li>{@link #parseViews()}</li>
 * <li>{@link #parseTriggers()}</li>
 * <li>{@link #close()}</li>
 * <li>Doing some stuff with the specified {@link DatabaseConnection} object.
 * </ol>
 * 
 * @author Andre Kasper
 * @author Jan Philipp
 */
public interface IMetaData extends Observer {
	/**
	 * Return the acutal implemented vendor.
	 * 
	 * @return
	 */
	Vendor getVendor();

	/**
	 * Set the connection model.
	 * 
	 * @param databaseConnection
	 */
	void setDatabaseConnection(DatabaseConnection connection);

	/**
	 * Check the set connection model. This method throws an exception if the login is not supported. It returns true if
	 * the login is valid.
	 * 
	 * @return
	 * @throws NotSupportedException
	 */
	boolean checkConnect() throws NotSupportedException;

	/**
	 * Set if the process should replace all existing tables, views and triggers. Otherwise they will only be added.
	 * Default is true.
	 * 
	 * @param replace
	 */
	void setReplaceMode(boolean replace);

	/**
	 * Return if the replace mode is enabled. See {@link #setReplaceMode(boolean)}
	 * 
	 * @return
	 */
	boolean isReplaceMode();

	IConnectionBuilder getConnectionBuilder();

	/**
	 * Connect to a database and open a new connection. Return true if established.
	 * 
	 * @return
	 */
	boolean connect();

	/**
	 * Close the current database connection.
	 */
	void close();

	/**
	 * Return if the database connection is connected.
	 * 
	 * @return
	 */
	boolean isConnected();

	/**
	 * Return the number of tables for the given database connection.
	 * 
	 * @return
	 */
	int getNumberOfTables();

	/**
	 * Return the number of views for the given database connection.
	 * 
	 * @return
	 */
	int getNumberOfViews();

	/**
	 * Return the number of triggers for the given database connection.
	 * 
	 * @return
	 */
	int getNumberOfTriggers();

	/**
	 * Return the number of procedures for the given database connection.
	 * 
	 * @return
	 */
	int getNumberOfProcedures();

	/**
	 * Return if an error occurred.
	 * 
	 * @return
	 */
	boolean isErrorOccured();

	/**
	 * Get the next error. Like a fifo, it will removed from the error list.
	 * 
	 * @return
	 */
	String getNextError();

	/**
	 * Get the latest error. No change will made on the error list.
	 * 
	 * @return
	 */
	String getLastError();

	/**
	 * Clear all error messages. isErrorOccured() will return false.
	 */
	void clearErrors();

	/**
	 * Preload all tables, views and triggers (internal).
	 */
	void initialize();

	/**
	 * Add a property change listener.
	 * 
	 * @param listener
	 */
	void addPropertyChangeListener(PropertyChangeListener listener);

	/**
	 * Remove the property change listener.
	 * 
	 * @param listener
	 */
	void removePropertyChangeListener(PropertyChangeListener listener);

	/**
	 * Perform the analyzing of all objects.
	 */
	void analyze();

	/**
	 * Perform the parsing of the views.
	 */
	void parseViews();

	/**
	 * Perform the parsing of the triggers.
	 */
	void parseTriggers();

	/**
	 * Perform the parsing of the procedures.
	 */
	void parseProcedures();

	/**
	 * Create and return a vendor specific url resource.
	 * 
	 * @param host
	 * @param port
	 * @param database
	 * @return
	 */
	String createUrl(String host, int port, String database);

	/**
	 * Return a individuell description text for the given metadata module.
	 * 
	 * @return
	 */
	String getDescription();
}
