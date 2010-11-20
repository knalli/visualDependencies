package de.unibonn.inf.dbdependenciesui.metadata;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseConnection;

/**
 * The connection builder interface. This interface is implemented for each specific vendor.
 * 
 * @author Andre Kasper
 * @author Jan Philipp
 */
public interface IConnectionBuilder {

	/**
	 * Create and return an open connection for the given url. The url can created through
	 * {@link #createUrl(String, int, String)}
	 * 
	 * @param url
	 * @return
	 * @throws SQLException
	 */
	Connection createConnection(String url) throws SQLException;

	/**
	 * Create and return an open connection for the given properties.
	 * 
	 * @param url
	 * @param properties
	 * @return
	 * @throws SQLException
	 */
	Connection createConnection(String url, Properties properties) throws SQLException;

	/**
	 * Create and return an open connection for the given login information.
	 * 
	 * @param url
	 * @param username
	 * @param password
	 * @return
	 * @throws SQLException
	 */
	Connection createConnection(String url, String username, String password) throws SQLException;

	/**
	 * Create and return an open connection for the given login information stored in databaseconnection.
	 * 
	 * @param connection
	 * @return
	 * @throws SQLException
	 */
	Connection createConnection(DatabaseConnection connection) throws SQLException;

	/**
	 * Create and return an open connection for the given login information stored in databaseconnection.
	 * 
	 * @param connection
	 * @return
	 * @throws SQLException
	 */
	Connection createUncachedConnection(DatabaseConnection connection) throws SQLException;
}