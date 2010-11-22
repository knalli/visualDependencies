/**
 *
 */
package de.unibonn.inf.dbdependenciesui.controller;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import de.unibonn.inf.dbdependenciesui.Configuration;
import de.unibonn.inf.dbdependenciesui.graph.common.AbstractDatabaseGraph;
import de.unibonn.inf.dbdependenciesui.graph.common.DatabaseGraphToGraphViz;
import de.unibonn.inf.dbdependenciesui.hibernate.HibernateDAOFactory;
import de.unibonn.inf.dbdependenciesui.hibernate.HibernateUtil;
import de.unibonn.inf.dbdependenciesui.hibernate.IGenericDAO;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseConnection;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseTable;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseTrigger;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseView;
import de.unibonn.inf.dbdependenciesui.metadata.MetaDataFactory.Vendor;
import de.unibonn.inf.dbdependenciesui.misc.Internationalization;

/**
 * This is the global controller object. This object contains the functional requirements of this application. This
 * object is an observable one distributing event information.
 * 
 * @author Andre Kasper
 * @author Jan Philipp
 */
public class ControllerImpl extends Observable {

	protected static final Logger log = Logger.getLogger(Configuration.LOGGER);

	/**
	 * See {@link Controller#createNewConnection(Vendor, String, String, String, String, String, String, String)}
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
	public DatabaseConnection createNewConnection(final Vendor vendor, final String title, final String host,
			final String port, final String username, final String password, final String database,
			final String schema, final boolean isSysdba) {
		DatabaseConnection connection = null;
		try {
			HibernateDAOFactory.setSession(HibernateUtil.getSession());
			final IGenericDAO<DatabaseConnection, Serializable> dao = HibernateDAOFactory.getConnectionDAO();
			connection = dao.create();
			connection.setVendor(vendor);
			connection.setTitle(title);
			connection.setHost(host);
			connection.setPort(Integer.parseInt(port));
			connection.setUsername(username);
			connection.setPassword(password);
			connection.setDatabase(database);
			connection.setSchema(schema);
			connection.setSysdba(isSysdba);

			dao.makePersistent(connection);
			HibernateDAOFactory.closeSession();

			setChanged();
			this.notifyObservers("connections");
		} catch (final NumberFormatException e) {
			e.printStackTrace();
			showErrorMessage(Internationalization.getText("error.onconnectionsaving"));
		} finally {
			HibernateDAOFactory.closeSession();
		}
		return connection;
	}

	/**
	 * Get a list of all connection titles. See {@link Controller#getConnections()}
	 * 
	 * @return
	 */
	public List<String> getConnections() {
		final List<String> results = new ArrayList<String>();

		HibernateDAOFactory.setSession(HibernateUtil.getSession());
		final IGenericDAO<DatabaseConnection, Serializable> dao = HibernateDAOFactory.getConnectionDAO();

		for (final DatabaseConnection connection : dao.findAll()) {
			results.add(connection.getTitle());
		}

		HibernateDAOFactory.closeSession();

		return results;
	}

	/**
	 * Get a list of all table/view names for the given connection. See
	 * {@link Controller#getTablesAndViews(DatabaseConnection)}
	 * 
	 * @param connection
	 * @return
	 */
	public List<DatabaseTable> getTablesAndViews(final DatabaseConnection connection) {
		final List<DatabaseTable> results = new ArrayList<DatabaseTable>();

		HibernateDAOFactory.setSession(HibernateUtil.getSession());
		final IGenericDAO<DatabaseConnection, Serializable> dao = HibernateDAOFactory.getConnectionDAO();

		final DatabaseConnection connection2 = dao.findById(connection.getId(), false);

		for (final DatabaseTable table : connection2.getTables()) {
			results.add(table);
		}

		for (final DatabaseView view : connection2.getViews()) {
			results.add(view);
		}

		HibernateDAOFactory.closeSession();

		return results;
	}

	/**
	 * Get a list of all table names for the given connection. See
	 * {@link Controller#getTablesAndViews(DatabaseConnection)}
	 * 
	 * @param connection
	 * @return
	 */
	public List<DatabaseTable> getTables(final DatabaseConnection connection) {
		final List<DatabaseTable> results = new ArrayList<DatabaseTable>();

		HibernateDAOFactory.setSession(HibernateUtil.getSession());
		final IGenericDAO<DatabaseConnection, Serializable> dao = HibernateDAOFactory.getConnectionDAO();

		final DatabaseConnection connection2 = dao.findById(connection.getId(), false);

		for (final DatabaseTable table : connection2.getTables()) {
			results.add(table);
		}

		HibernateDAOFactory.closeSession();

		return results;
	}

	/**
	 * Get a list of all trigger names for the given connection. See
	 * {@link Controller#getTablesAndViews(DatabaseConnection)}
	 * 
	 * @param connection
	 * @return
	 */
	public List<DatabaseTrigger> getTrigger(final DatabaseConnection connection) {
		final List<DatabaseTrigger> results = new ArrayList<DatabaseTrigger>();

		HibernateDAOFactory.setSession(HibernateUtil.getSession());
		final IGenericDAO<DatabaseConnection, Serializable> dao = HibernateDAOFactory.getConnectionDAO();

		final DatabaseConnection connection2 = dao.findById(connection.getId(), false);

		for (final DatabaseTrigger trigger : connection2.getTriggers()) {
			results.add(trigger);
		}

		HibernateDAOFactory.closeSession();

		return results;
	}

	/**
	 * Display a error message. See {@link Controller#showErrorMessage(String)}
	 * 
	 * @param message
	 */
	public void showErrorMessage(final String message) {
		System.err.println(message);
		ControllerImpl.log.warning(message);
		JOptionPane.showMessageDialog(null, message, "Error Message", JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * Get the connection object by the given title. See {@link Controller#loadConnection(String, boolean)}
	 * 
	 * @param title
	 * @param persistent
	 * @return
	 */
	public DatabaseConnection loadConnection(final String title, final boolean persistent) {

		HibernateDAOFactory.setSession(HibernateUtil.getSession());
		final IGenericDAO<DatabaseConnection, Serializable> dao = HibernateDAOFactory.getConnectionDAO();

		final DatabaseConnection result = dao.findByTitle(title, persistent);

		HibernateDAOFactory.closeSession();

		return result;
	}

	/**
	 * Get the connection object by the given id. See {@link Controller#loadConnection(int, boolean)}
	 * 
	 * @param id
	 * @param persistent
	 * @return
	 */
	public DatabaseConnection loadConnection(final int id, final boolean persistent) {

		HibernateDAOFactory.setSession(HibernateUtil.getSession());
		final IGenericDAO<DatabaseConnection, Serializable> dao = HibernateDAOFactory.getConnectionDAO();

		final DatabaseConnection result = dao.findById(id, persistent);

		HibernateDAOFactory.closeSession();

		return result;
	}

	/**
	 * See {@link Controller#loadTableByTitle(int, String, boolean)}
	 * 
	 * @param id
	 * @param title
	 * @param persistent
	 * @return
	 */
	public DatabaseTable loadTableByTitle(final int id, final String title, final boolean persistent) {

		DatabaseTable entity = null;

		// This builds a criteria query and searches for an entity with the
		// given title and an connection entity with the given id.
		// The result is typesafe when we classified it in the createCriteria()
		// There a two possible exceptions: hibernate relevants (should not) and
		// an IndexOutOfBoundException which is irrelevant (entity is still
		// null).
		final Session session = HibernateDAOFactory.getSession();
		final Criteria criteria = session.createCriteria(DatabaseTable.class);
		criteria.add(Restrictions.like("title", title));
		criteria.createCriteria("connection").add(Restrictions.eq("id", id));
		try {
			entity = (DatabaseTable) criteria.list().get(0);
		} catch (final Exception e) {}
		HibernateDAOFactory.closeSession();

		return entity;
	}

	/**
	 * See {@link Controller#loadViewByTitle(int, String, boolean)}
	 * 
	 * @param id
	 * @param title
	 * @param persistent
	 * @return
	 */
	public DatabaseView loadViewByTitle(final int id, final String title, final boolean persistent) {

		DatabaseView entity = null;

		// This builds a criteria query and searches for an entity with the
		// given title and an connection entity with the given id.
		// The result is typesafe when we classified it in the createCriteria()
		// There a two possible exceptions: hibernate relevants (should not) and
		// an IndexOutOfBoundException which is irrelevant (entity is still
		// null).
		final Session session = HibernateDAOFactory.getSession();
		final Criteria criteria = session.createCriteria(DatabaseView.class);
		criteria.add(Restrictions.like("title", title));
		criteria.createCriteria("connection").add(Restrictions.eq("id", id));
		try {
			entity = (DatabaseView) criteria.list().get(0);
		} catch (final Exception e) {}
		HibernateDAOFactory.closeSession();

		return entity;
	}

	/**
	 * See {@link Controller#updateConnection(DatabaseConnection)}
	 * 
	 * @param entity
	 * @return
	 */
	public DatabaseConnection updateConnection(final DatabaseConnection entity) {
		return this.updateConnection(entity, true);
	}

	/**
	 * See {@link Controller#updateConnection(DatabaseConnection, boolean)}
	 * 
	 * @param entity
	 * @param closeSession
	 * @return
	 */
	public DatabaseConnection updateConnection(DatabaseConnection entity, final boolean closeSession) {

		final IGenericDAO<DatabaseConnection, Serializable> dao = HibernateDAOFactory.getConnectionDAO();
		entity = dao.makePersistent(entity);
		HibernateDAOFactory.flushSession();
		if (closeSession) {
			HibernateDAOFactory.closeSession();
		}

		if (closeSession) {
			notifyConnectionsChanged();
		}
		return entity;
	}

	/**
	 * See {@link Controller#removeConnection(DatabaseConnection)}
	 * 
	 * @param entity
	 */
	public void removeConnection(final DatabaseConnection entity) {
		HibernateDAOFactory.setSession(HibernateUtil.getSession());
		final IGenericDAO<DatabaseConnection, Serializable> dao = HibernateDAOFactory.getConnectionDAO();

		dao.makeTransient(entity);

		HibernateDAOFactory.closeSession();
		notifyConnectionsChanged();
	}

	/**
	 * See {@link Controller#notifyConnectionsChanged()}
	 */
	public void notifyConnectionsChanged() {
		setChanged();
		this.notifyObservers("connections");
	}

	public void exportGraphAsDotFile(final File file, final AbstractDatabaseGraph graph) throws IOException {
		final FileWriter writer = new FileWriter(file);
		writer.write(new DatabaseGraphToGraphViz(graph).getDot());
		writer.close();
	}

	public List<DatabaseTrigger> getTriggersByTitles(final List<String> triggers) {
		final List<DatabaseTrigger> result = new ArrayList<DatabaseTrigger>(triggers.size());

		final IGenericDAO<DatabaseTrigger, Serializable> dao = HibernateDAOFactory.getTriggerDAO();
		for (final String title : triggers) {
			final DatabaseTrigger trigger = dao.findByTitle(title, false);
			if (trigger != null) {
				result.add(trigger);
			}
		}

		HibernateDAOFactory.closeSession();

		return result;
	}
}
