/**
 * $Id$
 */
package de.unibonn.inf.dbdependenciesui.metadata.impl;

import java.beans.PropertyChangeEvent;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.logging.Logger;

import de.unibonn.inf.dbdependenciesui.Configuration;
import de.unibonn.inf.dbdependenciesui.hibernate.HibernateDAOFactory;
import de.unibonn.inf.dbdependenciesui.hibernate.IGenericDAO;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseConnection;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseProcedure;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseTable;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseTrigger;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseView;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.Column;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.DdlSchemaEditable;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.ForeignKey;
import de.unibonn.inf.dbdependenciesui.metadata.IAnalyzer;
import de.unibonn.inf.dbdependenciesui.metadata.MetaDataFactory.Vendor;

/**
 * Abstract class for an analyzer class. This abstract class provides several method hooks which can be implemented in a
 * concrete implementation for a specific database vendor plugin. Beside them, the already implemented methods can be
 * overridden if this is necessary.
 * 
 * @author Andre Kasper
 * @author Jan Philipp
 * @author Marc Kastleiner
 */
abstract public class AbstractAnalyzer extends Observable implements IAnalyzer {

	protected static final Logger log = Logger.getLogger(Configuration.LOGGER);
	/**
	 * implemented database vendor
	 */
	protected final Vendor vendor;
	/**
	 * current database sql connection
	 */
	protected Connection sqlConnection;
	/**
	 * current database metadata objet (jdbc)
	 */
	protected DatabaseMetaData dbmd;
	/**
	 * list of all analyzed tables
	 */
	protected List<DatabaseTable> tables;
	/**
	 * list of all analyzed views
	 */
	protected List<DatabaseView> views;
	/**
	 * list of all analyzed triggers
	 */
	protected List<DatabaseTrigger> triggers;
	/**
	 * list of all analyzed procedures and functions
	 */
	protected List<DatabaseProcedure> procedures;
	/**
	 * current ddlschema (working object)
	 */
	protected DdlSchemaEditable schema;
	/**
	 * current database connection object (parent)
	 */
	protected DatabaseConnection databaseConnection;
	/**
	 * initial value for the fetch size attribute
	 */
	protected int fetchSize = 200;

	protected Map<String, DatabaseTable> title2table = new HashMap<String, DatabaseTable>();

	public AbstractAnalyzer(final Vendor vendor) {
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

	protected void prepareSharedStatements() throws SQLException {
		dbmd = sqlConnection.getMetaData();
	}

	public List<DatabaseTable> getTables() {
		return (tables != null) ? tables : Collections.<DatabaseTable> emptyList();
	}

	public List<DatabaseView> getViews() {
		return (views != null) ? views : Collections.<DatabaseView> emptyList();
	}

	public List<DatabaseTrigger> getTriggers() {
		return (triggers != null) ? triggers : Collections.<DatabaseTrigger> emptyList();
	}

	public List<DatabaseProcedure> getProcedures() {
		return (procedures != null) ? procedures : Collections.<DatabaseProcedure> emptyList();
	}

	protected void logTableResult(final DatabaseTable table) {
		log.info(schema.getPrimaryKeys().toString());
		log.info(schema.getUniqueKeys().toString());

		for (final ForeignKey foreignKey : schema.getForeignKeys()) {
			log.info(String.format("%s -> %s -> %s", foreignKey.getColumn(), foreignKey.getReferToTable(), foreignKey
					.getReferToColumn()));
		}

		for (final Column column : schema.getColumns()) {
			log.info(String.format("%s -> %s -> %d,%d", column.getName(), column.getType(), column.getSize(), column
					.getFractionalDigits()));
		}

		if (table instanceof DatabaseView) {
			log.info("New view: " + table.getTitle());
		} else {
			log.info("New table: " + table.getTitle());
		}
		log.info("New schema: " + table.getDdlSchema());
	}

	/**
	 * Return a open result set object of a {@link DatabaseMetaData}{@link #getTables()}.
	 * 
	 * @uses {@link #loadViewsFromDbmdAsList()}
	 * @return
	 * @throws SQLException
	 */
	protected ResultSet loadViewsFromDbmd() throws SQLException {
		return dbmd.getTables(null, getSchemaSpacePattern(), "%", new String[] {
			"VIEW" });
	}

	/**
	 * Return a open result set object of a {@link DatabaseMetaData}{@link #getTables()}.
	 * 
	 * @uses {@link #loadTablesFromDbmdAsList()}
	 * @return
	 * @throws SQLException
	 */
	protected ResultSet loadTablesFromDbmd() throws SQLException {
		return dbmd.getTables(null, getSchemaSpacePattern(), "%", new String[] {
			"TABLE" });
	}

	/**
	 *Return a list strings each representing a name of a table in the database. This method is invoked by
	 * {@link #preloadTables()}.
	 * 
	 * @return
	 * @throws SQLException
	 */
	protected List<String> loadTablesFromDbmdAsList() throws SQLException {
		final List<String> tables = new ArrayList<String>();

		final ResultSet resultSet = loadTablesFromDbmd();
		resultSet.setFetchSize(fetchSize);
		while (resultSet.next()) {
			if (isValidTable(resultSet)) {
				tables.add(resultSet.getString(3));
			}
		}
		resultSet.close();

		return tables;
	}

	/**
	 * Return a list strings each representing a name of a view in the database. This method is invoked by
	 * {@link #preloadViews()}.
	 * 
	 * @return
	 * @throws SQLException
	 */
	protected List<String> loadViewsFromDbmdAsList() throws SQLException {
		final List<String> views = new ArrayList<String>();

		final ResultSet resultSet = loadViewsFromDbmd();
		resultSet.setFetchSize(fetchSize);
		while (resultSet.next()) {
			if (isValidView(resultSet)) {
				views.add(resultSet.getString(3));
			}
		}
		resultSet.close();

		return views;
	}

	protected boolean isValidTable(final ResultSet resultSet) throws SQLException {
		return true;
	}

	protected boolean isValidView(final ResultSet resultSet) throws SQLException {
		return true;
	}

	protected String getSchemaSpacePattern() throws SQLException {
		return null;
	}

	protected void preloadViews() throws SQLException {
		DatabaseView view;
		final IGenericDAO<DatabaseView, Serializable> viewDAO = HibernateDAOFactory.getViewDAO();

		// For each result, create a view object.
		for (final String title : loadViewsFromDbmdAsList()) {
			view = viewDAO.create();
			view.setTitle(title);
			view.setConnection(databaseConnection);
			databaseConnection.addView(view);
			views.add(view);
		}

		firePropertyChange("numberOfViews", views.size());
	}

	/**
	 * Preload all triggers. The default preloader has no implementation.
	 * 
	 * @throws SQLException
	 */
	protected void preloadTriggers() throws SQLException {}

	/**
	 * Preload all procedures and functions. The default preloader has no implementation.
	 * 
	 * @throws SQLException
	 */
	protected void preloadProcedures() throws SQLException {}

	/**
	 * Preload all tables via database metadata.
	 * 
	 * @uses dbmd, tables, databaseConnection
	 * @throws SQLException
	 */
	protected void preloadTables() throws SQLException {
		DatabaseTable table;
		final IGenericDAO<DatabaseTable, Serializable> tableDAO = HibernateDAOFactory.getTableDAO();

		for (final String title : loadTablesFromDbmdAsList()) {
			table = tableDAO.create();
			table.setTitle(title);
			table.setConnection(databaseConnection);
			databaseConnection.addTable(table);
			tables.add(table);
		}

		firePropertyChange("numberOfTables", tables.size());
	}

	protected void analyzeViews() throws SQLException {

		// Before hook
		hookBeforeAnalyzeViews();

		for (final DatabaseView view : views) {
			firePropertyChange("analyzingView", view.getTitle());

			// Before hook
			hookBeforeAnalyzeView(view);

			// Create a new schema object.
			schema = view.createDdlSchemaEditableObject();

			// Before hook
			hookBeforeAnalyzeView(schema);

			// Get and process all columns of the view.
			buildColumns(view);

			// Get and process all unique keys of the view.
			// buildAllKeysOnlyForView(view.getTitle());

			// Get and process all primary keys of the table.
			buildPrimaryKeys(view);

			// Get and process all unique keys of the table.
			buildUniqueKeys(view);

			// Get and process all foreign keys of the table.
			buildForeignKeys(view);

			// Get and process the select statement of the view.
			final String selectStatement = loadViewSelectQuery(view.getTitle());
			if ((selectStatement != null) && !selectStatement.isEmpty()) {
				view.setSelectStatement(selectStatement);
			}

			// After hook
			hookAfterAnalyzeView(schema);

			// Save the ddlschema.
			view.setDdlSchemaObject(schema);

			// After hook
			hookAfterAnalyzeView(view);

			logTableResult(view);
		}

		// After hook
		hookAfterAnalyzeViews();
	}

	/**
	 * @param view
	 * @return
	 * @throws SQLException
	 */
	protected String loadViewSelectQuery(final String view) throws SQLException {
		return null;
	}

	protected void analyzeTables() throws SQLException {

		// Before hook
		hookBeforeAnalyzeTables();

		for (final DatabaseTable table : tables) {
			firePropertyChange("analyzingTable", table.getTitle());

			// Before hook
			hookBeforeAnalyzeTable(table);

			// Create a new schema object.
			schema = table.createDdlSchemaEditableObject();

			// Before hook
			hookBeforeAnalyzeView(schema);

			// Get and process all columns of the table.
			buildColumns(table);

			// Get and process all primary keys of the table.
			buildPrimaryKeys(table);

			// Get and process all unique keys of the table.
			buildUniqueKeys(table);

			// Get and process all foreign keys of the table.
			buildForeignKeys(table);

			// After hook
			hookAfterAnalyzeTable(schema);

			// Save the ddlschema.
			table.setDdlSchemaObject(schema);

			// After hook
			hookAfterAnalyzeTable(table);

			logTableResult(table);
		}

		// After hook
		hookAfterAnalyzeTables();
	}

	/**
	 * @return
	 * @throws SQLException
	 */
	protected void analyzeAndBuildColumns() throws SQLException {
		final ResultSet resultSet = dbmd.getColumns(null, getSchemaSpacePattern(), "%", "%");
		resultSet.setFetchSize(fetchSize);

		while (resultSet.next()) {
			final String tableTitle = resultSet.getString("TABLE_NAME");
			final String columnTitle = resultSet.getString("COLUMN_NAME");
			final String columnType = resultSet.getString("TYPE_NAME");
			final String columnNullable = resultSet.getString("IS_NULLABLE");
			final Integer columnSize = resultSet.getInt("COLUMN_SIZE");
			final Integer columnDecimalDigits = resultSet.getInt("DECIMAL_DIGITS");

			final DatabaseTable table = title2table.get(tableTitle);
			final DdlSchemaEditable ddlschema = table.createDdlSchemaEditableObject();
			final Column column = new Column(columnTitle);
			column.setType(columnType);
			column.setNullable("YES".equalsIgnoreCase(columnNullable));
			column.setSize(columnSize);
			column.setFractionalDigits(columnDecimalDigits);
			ddlschema.addColumn(column);
			table.setDdlSchemaObject(ddlschema);
		}

		resultSet.close();
	}

	protected void analyzeTriggers() throws SQLException {}

	protected void analyzeProcedures() throws SQLException {}

	/**
	 * Create primary keys from this table.
	 * 
	 * @param table
	 * @uses schema
	 * @throws SQLException
	 */
	abstract protected void buildPrimaryKeys(final DatabaseTable table) throws SQLException;

	/**
	 * Create columns from the result set.
	 * 
	 * @param table
	 * @uses schema
	 * @throws SQLException
	 */
	protected void buildColumns(final DatabaseTable table) throws SQLException {
		final String tableTitle = table.getTitle();

		final ResultSet resultSet = dbmd.getColumns(null, getSchemaSpacePattern(), tableTitle, "%");
		resultSet.setFetchSize(fetchSize);

		while (resultSet.next()) {
			final String columnTitle = resultSet.getString("COLUMN_NAME");
			final String columnType = resultSet.getString("TYPE_NAME");
			final String columnNullable = resultSet.getString("IS_NULLABLE");
			final Integer columnSize = resultSet.getInt("COLUMN_SIZE");
			final Integer columnDecimalDigits = resultSet.getInt("DECIMAL_DIGITS");

			final Column column = new Column(columnTitle);
			column.setType(columnType);
			column.setNullable("YES".equalsIgnoreCase(columnNullable));
			column.setSize(columnSize);
			column.setFractionalDigits(columnDecimalDigits);
			schema.addColumn(column);
		}

		resultSet.close();
	}

	/**
	 * Create foreign keys from this table.
	 * 
	 * @param table
	 * @uses schema
	 * @throws SQLException
	 */
	abstract protected void buildForeignKeys(final DatabaseTable table) throws SQLException;

	/**
	 * Create unique keys from this table.
	 * 
	 * @param table
	 * @uses schema, uniques
	 * @throws SQLException
	 */
	abstract protected void buildUniqueKeys(final DatabaseTable table) throws SQLException;

	/**
	 * Analyze the specified database schema.
	 * 
	 * @throws SQLException
	 */
	protected void analyzeDatabase() throws SQLException {
		// try {
		prepareSharedStatements();

		// Preload all database's tables, views and triggers.
		preloadTables();
		preloadViews();
		preloadTriggers();
		preloadProcedures();

		for (final DatabaseTable table : tables) {
			title2table.put(table.getTitle(), table);
		}
		for (final DatabaseTable view : views) {
			title2table.put(view.getTitle(), view);
		}

		// Disabled because low performance (xml serialization).
		// analyzeAndBuildColumns();

		// Analyze all found objects.
		analyzeTables();
		analyzeViews();
		analyzeTriggers();
		analyzeProcedures();
		//
		// } catch (final SQLException e) {
		// log.severe(e.getLocalizedMessage());
		// }
	}

	/**
	 * See {@link IAnalyzer#analyze(Connection, DatabaseConnection)}
	 * 
	 * @throws SQLException
	 */
	@Override
	public void analyze(final Connection sqlConnection, final DatabaseConnection connection) throws SQLException {
		this.sqlConnection = sqlConnection;
		databaseConnection = connection;
		initialize();
		analyzeDatabase();
	}

	protected void initialize() {
		tables = new ArrayList<DatabaseTable>();
		views = new ArrayList<DatabaseView>();
		triggers = new ArrayList<DatabaseTrigger>();
		procedures = new ArrayList<DatabaseProcedure>();
	}

	protected String trimStringOrNull(final String input) {
		if (input != null) {
			return input.trim();
		} else {
			return null;
		}
	}

	/**
	 * This method hook will invoked before the analyze of all tables.
	 * 
	 * @param table
	 */
	protected void hookBeforeAnalyzeTables() throws SQLException {}

	/**
	 * This method hook will invoked after the analyze of all tables.
	 * 
	 * @param table
	 */
	protected void hookAfterAnalyzeTables() throws SQLException {}

	/**
	 * This method hook will invoked before the analyze of all views.
	 * 
	 * @param table
	 */
	protected void hookBeforeAnalyzeViews() throws SQLException {}

	/**
	 * This method hook will invoked after the analyze of all views.
	 * 
	 * @param table
	 */
	protected void hookAfterAnalyzeViews() throws SQLException {}

	/**
	 * This method hook will invoked before the analyze of the table at the start of the loop.
	 * 
	 * @param table
	 */
	protected void hookBeforeAnalyzeTable(final DatabaseTable table) throws SQLException {}

	/**
	 * This method hook will invoked before the analyze of the view at the start of the loop.
	 * 
	 * @param table
	 */
	protected void hookBeforeAnalyzeView(final DatabaseView view) throws SQLException {}

	/**
	 * This method hook will invoked before the analyze of the table but after loading the schema from the table object.
	 * 
	 * @param schema
	 */
	protected void hookBeforeAnalyzeTable(final DdlSchemaEditable schema) throws SQLException {}

	/**
	 * This method hook will invoked before the analyze of the view but after loading the schema from the view object.
	 * 
	 * @param schema
	 */
	protected void hookBeforeAnalyzeView(final DdlSchemaEditable schema) throws SQLException {}

	/**
	 * This method hook will invoked after the analyze of the table at the end of the loop.
	 * 
	 * @param table
	 */
	protected void hookAfterAnalyzeTable(final DatabaseTable table) throws SQLException {}

	/**
	 * This method hook will invoked after the analyze of the view at the end of the loop.
	 * 
	 * @param table
	 */
	protected void hookAfterAnalyzeView(final DatabaseView view) throws SQLException {}

	/**
	 * This method hook will invoked after the analyze of the table but before saving the schema into the table object.
	 * 
	 * @param schema
	 */
	protected void hookAfterAnalyzeTable(final DdlSchemaEditable schema) throws SQLException {}

	/**
	 * This method hook will invoked after the analyze of the view but before saving the schema into the view object.
	 * 
	 * @param schema
	 */
	protected void hookAfterAnalyzeView(final DdlSchemaEditable schema) throws SQLException {}

	@Override
	public String getDescription() {
		return null;
	}
}
