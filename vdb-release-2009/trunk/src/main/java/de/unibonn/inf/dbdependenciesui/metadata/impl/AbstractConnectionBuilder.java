package de.unibonn.inf.dbdependenciesui.metadata.impl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseConnection;
import de.unibonn.inf.dbdependenciesui.metadata.IConnectionBuilder;
import de.unibonn.inf.dbdependenciesui.metadata.MetaDataFactory;

/**
 * Abstract class for a connection builder. Warning: This class IS NOT threadsafe and would permit parallel using the
 * same Connection object. Please be careful.
 * 
 * @author Andre Kasper
 * @author Jan Philipp
 */
public abstract class AbstractConnectionBuilder implements IConnectionBuilder {

	protected final Map<String, Connection> holdConnections;

	public AbstractConnectionBuilder() {
		holdConnections = new HashMap<String, Connection>();
	}

	public synchronized void storeConnection(final String key, final Connection connection) {
		holdConnections.put(key, connection);
	}

	public synchronized Connection loadConnectionByKey(final String key) {
		Connection connection = holdConnections.get(key);

		// Remove closed connections.
		if (connection != null) {
			boolean closed;
			try {
				closed = connection.isClosed();
			} catch (final SQLException e) {
				closed = true;
			}
			if (closed) {
				holdConnections.remove(key);
				connection = null;
			}
		}

		return connection;
	}

	@Override
	public Connection createConnection(final String url, final String username, final String password)
			throws SQLException {
		// Try to load existing one.
		Connection connection = loadConnectionByKey(url + username + password);
		// Try to build new one unless found.
		if (connection == null) {
			connection = DriverManager.getConnection(url, username, password);
		}
		// Throw error unless succeed.
		if (connection == null) {
			throw new SQLException("Connection object could not created (internal application error)");
		} else {
			storeConnection(url + username + password, connection);
		}
		return connection;
	}

	@Override
	public Connection createConnection(final String url, final Properties properties) throws SQLException {
		// Try to load existing one.
		Connection connection = loadConnectionByKey(url + properties.toString());
		// Try to build new one unless found.
		if (connection == null) {
			connection = DriverManager.getConnection(url, properties);
		}
		// Throw error unless succeed.
		if (connection == null) {
			throw new SQLException("Connection object could not created (internal application error)");
		} else {
			storeConnection(url + properties.toString(), connection);
		}
		return connection;
	}

	public Connection createUncachedConnection(final String url, final Properties properties) throws SQLException {
		final Connection connection = DriverManager.getConnection(url, properties);
		// Throw error unless succeed.
		if (connection == null) { throw new SQLException(
				"Connection object could not created (internal application error)"); }
		return connection;
	}

	@Override
	public Connection createConnection(final String url) throws SQLException {
		// Try to load existing one.
		Connection connection = loadConnectionByKey(url);
		// Try to build new one unless found.
		if (connection == null) {
			connection = DriverManager.getConnection(url);
		}
		// Throw error unless succeed.
		if (connection == null) {
			throw new SQLException("Connection object could not created (internal application error)");
		} else {
			storeConnection(url, connection);
		}
		return connection;
	}

	@Override
	public Connection createConnection(final DatabaseConnection connection) throws SQLException {
		final String url = MetaDataFactory.createUrl(connection.getVendor(), connection.getHost(),
				connection.getPort(), connection.getDatabase());
		final Properties properties = new Properties();
		properties.put("user", connection.getUsername());
		properties.put("password", connection.getPassword());
		return createConnection(url, properties);
	}

	@Override
	public Connection createUncachedConnection(final DatabaseConnection connection) throws SQLException {
		final String url = MetaDataFactory.createUrl(connection.getVendor(), connection.getHost(),
				connection.getPort(), connection.getDatabase());
		final Properties properties = new Properties();
		properties.put("user", connection.getUsername());
		properties.put("password", connection.getPassword());
		return createUncachedConnection(url, properties);
	}
}
