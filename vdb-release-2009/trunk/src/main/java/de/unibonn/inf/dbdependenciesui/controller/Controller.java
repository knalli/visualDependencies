/**
 *
 */
package de.unibonn.inf.dbdependenciesui.controller;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Logger;

import de.unibonn.inf.dbdependenciesui.Configuration;
import de.unibonn.inf.dbdependenciesui.graph.common.AbstractDatabaseGraph;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseConnection;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseTable;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseTrigger;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseView;
import de.unibonn.inf.dbdependenciesui.metadata.MetaDataFactory.Vendor;

/**
 * This is the global controller object. This object contains the functional requirements of this application. This
 * object is an observable one distributing event information. The acutal implementation is stored in
 * {@link ControllerImpl}.
 * 
 * @author Andre Kasper
 * @author Jan Philipp
 */
public class Controller extends Observable {

	protected static final Logger log = Logger.getLogger(Configuration.LOGGER);

	/**
	 * Controller implementations.
	 */
	private static ControllerImpl instance;

	private synchronized static ControllerImpl getImplInstance() {
		if (instance == null) {
			instance = new ControllerImpl();
		}
		return instance;
	}

	/**
	 * Private constructor (no construction outside of here).
	 */
	private Controller() {

	}

	/**
	 * Create a new database connection entity and stores it. A notification will sent to all observers.
	 * 
	 * @param vendor
	 * @param title
	 * @param host
	 * @param port
	 * @param username
	 * @param password
	 * @param database
	 * @param schema
	 */
	public static DatabaseConnection createNewConnection(final Vendor vendor, final String title, final String host,
			final String port, final String username, final String password, final String database,
			final String schema, final boolean isSysdba) {

		return getImplInstance().createNewConnection(vendor, title, host, port, username, password, database, schema,
				isSysdba);
	}

	/**
	 * Get a list of all connection titles.
	 * 
	 * @return
	 */
	public static List<String> getConnections() {
		return getImplInstance().getConnections();
	}

	/**
	 * Get a list of all table/view names for the given connection.
	 * 
	 * @param connection
	 * @return
	 */
	public static List<DatabaseTable> getTablesAndViews(final DatabaseConnection connection) {
		return getImplInstance().getTablesAndViews(connection);
	}

	/**
	 * Get a list of all table names for the given connection.
	 * 
	 * @param connection
	 * @return
	 */
	public static List<DatabaseTable> getTables(final DatabaseConnection connection) {
		return getImplInstance().getTables(connection);
	}

	/**
	 * Get a list of all trigger names for the given connection.
	 * 
	 * @param connection
	 * @return
	 */
	public static List<DatabaseTrigger> getTrigger(final DatabaseConnection connection) {
		return getImplInstance().getTrigger(connection);
	}

	/**
	 * Display a error message.
	 * 
	 * @param message
	 */
	public static void showErrorMessage(final String message) {
		getImplInstance().showErrorMessage(message);
	}

	/**
	 * Get the connection object by the given title.
	 * 
	 * @param title
	 * @param persistent
	 * @return
	 */
	public static DatabaseConnection loadConnection(final String title, final boolean persistent) {

		return getImplInstance().loadConnection(title, persistent);
	}

	/**
	 * Get the connection object by the given id.
	 * 
	 * @param id
	 * @param persistent
	 * @return
	 */
	public static DatabaseConnection loadConnection(final int id, final boolean persistent) {

		return getImplInstance().loadConnection(id, persistent);
	}

	/**
	 * Get the table by the given title and connection id.
	 * 
	 * @param id
	 * @param title
	 * @param persistent
	 * @return
	 */
	public static DatabaseTable loadTableByTitle(final int id, final String title, final boolean persistent) {
		return getImplInstance().loadTableByTitle(id, title, persistent);
	}

	/**
	 * Get the view by the given title and connection id.
	 * 
	 * @param id
	 * @param title
	 * @param persistent
	 * @return
	 */
	public static DatabaseView loadViewByTitle(final int id, final String title, final boolean persistent) {
		return getImplInstance().loadViewByTitle(id, title, persistent);
	}

	/**
	 * Update (insert or save) the given connection object. The session will closed.
	 * 
	 * @param entity
	 * @return
	 */
	public static DatabaseConnection updateConnection(final DatabaseConnection entity) {
		return getImplInstance().updateConnection(entity);
	}

	/**
	 * Update (insert or save) the given connection object. The session will not closed.
	 * 
	 * @param entity
	 * @param closeSession
	 * @return
	 */
	public static DatabaseConnection updateConnection(final DatabaseConnection entity, final boolean closeSession) {

		return getImplInstance().updateConnection(entity, closeSession);
	}

	/**
	 * Remove the given conection object (hibernate).
	 * 
	 * @param entity
	 */
	public static void removeConnection(final DatabaseConnection entity) {
		getImplInstance().removeConnection(entity);
	}

	/**
	 * Notify all observers the connection list has changed. For example, this can happen if a connection was created,
	 * removed or edited.
	 */
	public static void notifyConnectionsChanged() {
		getImplInstance().notifyConnectionsChanged();
	}

	/**
	 * Add the given object to the observer list. See {@link Observable#addObserver(Observer)}
	 * 
	 * @param observer
	 */
	public static void addObserverObject(final Observer observer) {
		getImplInstance().addObserver(observer);
	}

	/**
	 * Export the given graph to the given file object. The written format is DOT (Graphviz).
	 * 
	 * @param file
	 * @param graph
	 * @throws IOException
	 */
	public static void exportGraphAsDotFile(final File file, final AbstractDatabaseGraph graph) throws IOException {
		getImplInstance().exportGraphAsDotFile(file, graph);
	}

	/**
	 * Load and return all triggers identified by the given titles.
	 * 
	 * @param triggers
	 * @return
	 */
	public static List<DatabaseTrigger> getTriggerByTitles(final List<String> triggers) {
		return getImplInstance().getTriggersByTitles(triggers);
	}
}
