package de.unibonn.inf.dbdependenciesui.metadata.impl.mysql5;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.unibonn.inf.dbdependenciesui.hibernate.HibernateDAOFactory;
import de.unibonn.inf.dbdependenciesui.hibernate.IGenericDAO;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseTable;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseTrigger;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseView;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.DdlSchemaEditable;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.ForeignKey;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.Index;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.PrimaryKey;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.TriggerSchemaEditable;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.UniqueKey;
import de.unibonn.inf.dbdependenciesui.metadata.MetaDataFactory.Vendor;
import de.unibonn.inf.dbdependenciesui.metadata.impl.AbstractAnalyzer;

/**
 * Concrete implementation of a mysql anaylzer.
 * 
 * @author Andre Kasper
 * @author Jan Philipp
 * @author Marc Kastleiner
 * @since 1.3
 */
public class MySqlAnalyzerImpl extends AbstractAnalyzer {

	protected PreparedStatement statementViewSelect;
	protected PreparedStatement statementConstraints;

	// protected final String triggerSelect =
	// "SELECT trigger_name, trigger_type, triggering_event, table_owner, base_object_type, table_name, column_name, referencing_names, when_clause, status, description, action_type, trigger_body, crossedition, before_statement, before_row, after_row, after_statement, instead_of_row FROM user_triggers WHERE table_name IS NOT NULL";
	protected final String selectViewSelect = "SELECT VIEW_DEFINITION FROM INFORMATION_SCHEMA.VIEWS WHERE TABLE_NAME LIKE ? AND TABLE_SCHEMA LIKE ?";
	protected final String constraintsSelect = "SELECT COLUMNS.*, KEY_COLUMN_USAGE.* FROM INFORMATION_SCHEMA.COLUMNS AS COLUMNS LEFT JOIN INFORMATION_SCHEMA.KEY_COLUMN_USAGE AS KEY_COLUMN_USAGE ON (COLUMNS.TABLE_SCHEMA = KEY_COLUMN_USAGE.TABLE_SCHEMA AND COLUMNS.TABLE_NAME = KEY_COLUMN_USAGE.TABLE_NAME AND COLUMNS.COLUMN_NAME = KEY_COLUMN_USAGE.COLUMN_NAME ) WHERE COLUMNS.TABLE_NAME LIKE ? AND COLUMNS.TABLE_SCHEMA LIKE ?";

	/**
	 * trigger data object (working object)
	 */
	protected Map<String, String[]> triggerData;

	public MySqlAnalyzerImpl() {
		super(Vendor.MYSQL);
	}

	@Override
	protected void initialize() {
		super.initialize();
	}

	@Override
	protected void prepareSharedStatements() throws SQLException {
		statementViewSelect = sqlConnection.prepareStatement(selectViewSelect);
		statementConstraints = sqlConnection.prepareStatement(constraintsSelect);
		super.prepareSharedStatements();
	}

	@Override
	protected void preloadTriggers() throws SQLException {
		DatabaseTrigger trigger;
		final IGenericDAO<DatabaseTrigger, Serializable> triggerDAO = HibernateDAOFactory.getTriggerDAO();

		final String triggerSelect = "SELECT trigger_name, event_manipulation, definer, event_object_table, action_statement, action_timing FROM information_schema.triggers WHERE trigger_schema = '"
				+ databaseConnection.getDatabase() + "'";

		final Statement stm = sqlConnection.createStatement();
		stm.setFetchSize(fetchSize);
		final ResultSet resultSet = stm.executeQuery(triggerSelect);

		// Create a new trigger data map.
		triggerData = new HashMap<String, String[]>();

		// For each result (trigger), create a trigger object and store the
		// referring information in triggerData.
		while (resultSet.next()) {
			trigger = triggerDAO.create();
			trigger.setTitle(resultSet.getString(1));
			trigger.setConnection(databaseConnection);
			databaseConnection.addTrigger(trigger);
			triggers.add(trigger);
			// 1 trigger_name, 2 event_manipulation, 3 definer, 4 event_object_table, 5 action_statement, 6
			// action_timing
			triggerData.put(trigger.getTitle(), new String[] {
					resultSet.getString(1), resultSet.getString(2), resultSet.getString(3), resultSet.getString(4),
					resultSet.getString(5), resultSet.getString(6) });
		}

		// Clear all resources.
		resultSet.close();
		stm.close();

		firePropertyChange("numberOfTriggers", triggers.size());
	}

	@Override
	protected void analyzeTriggers() throws SQLException {
		for (final DatabaseTrigger trigger : triggers) {
			firePropertyChange("analyzingTrigger", trigger.getTitle());

			// Create a new ddlschema object.
			final TriggerSchemaEditable schema = trigger.createTriggerSchemaEditableObject();

			// Get all trigger data.
			final String[] data = triggerData.get(trigger.getTitle());

			// 1 trigger_name, 2 trigger_type, 3 triggering_event, 4 table_owner, 5 base_object_type, 6 table_name, 7
			// column_name, 8 referencing_names, 9 when_clause, 10 status, 11 description, 12 action_type, 13
			// trigger_body, 14 crossedition, 15 before_statement, 16 before_row, 17 after_row, 18 after_statement, 19
			// instead_of_row
			// 1 trigger_name, 2 event_manipulation, 3 definer, 4 event_object_table, 5 action_statement, 6
			// action_timing

			schema.setName(trimStringOrNull(data[1 - 1]));
			schema.setType(trimStringOrNull(data[6 - 1]));
			schema.setEvents(Arrays.asList(trimStringOrNull(data[2 - 1]).split("(OR)")));
			schema.setTableOwner(trimStringOrNull(data[3 - 1]));
			// schema.setBaseObjectType(trimStringOrNull(data[5 - 1]));
			schema.setTableName(trimStringOrNull(data[4 - 1]));
			// schema.setColumnName(trimStringOrNull(data[7 - 1]));
			// schema.setReferencingNames(trimStringOrNull(data[8 - 1]));
			// schema.setWhenClause(trimStringOrNull(data[9 - 1]));
			schema.setEnabled(true);
			// schema.setDescription(trimStringOrNull(data[11 - 1]));
			// schema.setActionType(trimStringOrNull(data[12 - 1]));

			// MySQL deletes the ending semicolon of the trigger-body, so we added it again
			schema.setBody(trimStringOrNull(data[5 - 1]) + ";");

			// schema.setCrossEdition(!"NO".equalsIgnoreCase(trimStringOrNull(data[14 - 1])));
			// schema.setInsteadOfRow(!"NO".equalsIgnoreCase(trimStringOrNull(data[19 - 1])));

			// This columns are always "NO" and seem not containing the right information. As workaround, we scan the
			// type column's content.
			// schema.setBeforeStatement(!"NO".equalsIgnoreCase(trimStringOrNull(data[15 - 1])));
			// schema.setBeforeRow(!"NO".equalsIgnoreCase(trimStringOrNull(data[16 - 1])));
			// schema.setAfterRow(!"NO".equalsIgnoreCase(trimStringOrNull(data[17 - 1])));
			// schema.setAfterStatement(!"NO".equalsIgnoreCase(trimStringOrNull(data[18 - 1])));
			// schema.setBeforeStatement(compareModeStrings(data[2 - 1], "BEFORE STATEMENT"));
			// schema.setBeforeRow(compareModeStrings(data[6 - 1], "BEFORE"));
			// schema.setAfterRow(compareModeStrings(data[6 - 1], "AFTER"));
			// schema.setAfterStatement(compareModeStrings(data[2 - 1], "AFTER STATEMENT"));
			// schema.setInsteadOfRow(compareModeStrings(data[2 - 1], "INSTEAD OF"));
			// Save the trigger schema.
			trigger.setTriggerSchemaObject(schema);

			// Add the trigger to the corresponding table. Otherwise ignore this trigger
			final DatabaseTable table = schema.getTable();
			if (table != null) {
				final DdlSchemaEditable ddlschema = table.createDdlSchemaEditableObject();
				ddlschema.addTrigger(schema.getName());
				table.setDdlSchemaObject(ddlschema);
			} else {
				trigger.getConnection().removeTrigger(trigger);
				triggers.remove(trigger);
			}
		}
	}

	/**
	 * Compare the given source string, if the pattern mode will match. For a more easy use the method support
	 * internally multiple spaces between words and ignore case sensitive chars.
	 * 
	 * @param source
	 * @param mode
	 * @return
	 */
	protected boolean compareModeStrings(final String source, String mode) {
		// Allow multiple spaces
		mode = mode.replace(" ", "(?:[\\s]*)");

		// Create pattern object, ignore case sensitive chars.
		final Pattern pattern = Pattern.compile("(" + mode + ")", Pattern.CASE_INSENSITIVE);
		final Matcher matcher = pattern.matcher(trimStringOrNull(source));
		return matcher.find();
	}

	/**
	 * Ignore oracle's binary tables.
	 * 
	 * @param resultSet
	 * @return
	 * @throws SQLException
	 */
	@Override
	protected boolean isValidTable(final ResultSet resultSet) throws SQLException {
		return true;
	}

	/**
	 * Ignore oracle's binary tables.
	 * 
	 * @param resultSet
	 * @return
	 * @throws SQLException
	 */
	@Override
	protected boolean isValidView(final ResultSet resultSet) throws SQLException {
		return true;
	}

	@Override
	protected String getSchemaSpacePattern() throws SQLException {
		String schema = databaseConnection.getSchema();
		if ((schema == null) || schema.isEmpty()) {
			schema = databaseConnection.getDatabase();
		}
		return schema;
	}

	/**
	 * Create primary keys from table.
	 * 
	 * @param table
	 * @uses schema
	 * @throws SQLException
	 */
	@Override
	protected void buildPrimaryKeys(final DatabaseTable table) throws SQLException {
		PrimaryKey primary = null;

		final ResultSet resultSet = dbmd.getPrimaryKeys(null, getSchemaSpacePattern(), table.getTitle());
		while (resultSet.next()) {
			if (primary == null) {
				primary = new PrimaryKey(resultSet.getString(6));
			}
			primary.addColumn(resultSet.getString(4));
		}
		resultSet.close();

		if (primary != null) {
			schema.addPrimaryKey(primary);
		}
	}

	/**
	 * Create foreign keys from table.
	 * 
	 * @param table
	 * @uses schema
	 * @throws SQLException
	 */
	@Override
	protected void buildForeignKeys(final DatabaseTable table) throws SQLException {
		final ResultSet resultSet = dbmd.getImportedKeys(null, getSchemaSpacePattern(), table.getTitle());
		while (resultSet.next()) {
			final ForeignKey foreign = new ForeignKey(resultSet.getString(12));
			foreign.setColumn(resultSet.getString(8));
			foreign.setReferToTable(resultSet.getString(3));
			foreign.setReferToColumn(resultSet.getString(4));
			schema.addForeignKey(foreign);
		}
		resultSet.close();
	}

	/**
	 * Create unique keys keys from table.
	 * 
	 * @param table
	 * @uses schema
	 * @throws SQLException
	 */
	@Override
	protected void buildUniqueKeys(final DatabaseTable table) throws SQLException {

		final List<UniqueKey> uniqueKeys = new ArrayList<UniqueKey>();
		final List<Index> indices = new ArrayList<Index>();

		// For each result, create a key if 9th value (name) is present and not already exists.
		final ResultSet resultSet = dbmd.getIndexInfo(null, getSchemaSpacePattern(), table.getTitle(), false, false);
		while (resultSet.next()) {
			if (resultSet.getString(9) != null) {
				if (resultSet.getBoolean(4)) {
					Index index = new Index(resultSet.getString(6));
					if (indices.contains(index)) {
						index = indices.get(indices.indexOf(index));
					} else {
						indices.add(index);
					}
					index.addColumn(resultSet.getString(9));
				} else {
					UniqueKey uniqueKey = new UniqueKey(resultSet.getString(6));
					if (uniqueKeys.contains(uniqueKey)) {
						// If already in the list, get that key instead of the
						// one
						// above.
						uniqueKey = uniqueKeys.get(uniqueKeys.indexOf(uniqueKey));
					} else {
						uniqueKeys.add(uniqueKey);
					}
					uniqueKey.addColumn(resultSet.getString(9));
				}
			}
		}
		resultSet.close();

		// Add them all to the schema.
		for (final UniqueKey uniqueKey : uniqueKeys) {
			schema.addUniqueKey(uniqueKey);
		}
		for (final Index index : indices) {
			schema.addIndex(index);
		}
	}

	/**
	 * Create unique keys fromt table.
	 * 
	 * @param view
	 * @uses schema, uniques
	 * @throws SQLException
	 */
	protected void buildAllKeysOnlyForView(final DatabaseView view) throws SQLException {

		statementConstraints.setString(1, view.getTitle());
		statementConstraints.setString(2, getSchemaSpacePattern());
		final ResultSet resultSet = statementConstraints.executeQuery();

		final List<UniqueKey> uniqueKeys = new ArrayList<UniqueKey>();
		PrimaryKey primaryKey = null;

		// For each result, create a key if it is one.
		while (resultSet.next()) {
			if (resultSet.getString("COLUMN_NAME") != null) {
				if (resultSet.getString("COLUMN_KEY").equalsIgnoreCase("PRI")) {
					// If the type is PRI, it is a primary key.
					// It is possible, that a primary key is build by more than
					// one columns.
					if (primaryKey == null) {
						primaryKey = new PrimaryKey(resultSet.getString("CONSTRAINT_NAME"));
					}
					primaryKey.addColumn(resultSet.getString("COLUMN_NAME"));
					UniqueKey uniqueKey = new UniqueKey(resultSet.getString("CONSTRAINT_NAME"));
					if (uniqueKeys.contains(uniqueKey)) {
						// If already in the list, get that key instead of the
						// one above.
						uniqueKey = uniqueKeys.get(uniqueKeys.indexOf(uniqueKey));
					} else {
						uniqueKeys.add(uniqueKey);
					}
					uniqueKey.addColumn(resultSet.getString("COLUMN_NAME"));
				} else if (resultSet.getString("COLUMN_KEY").equalsIgnoreCase("UNI")) {
					// If the type is a UNI, it is a unique key.
					UniqueKey uniqueKey = new UniqueKey(resultSet.getString("CONSTRAINT_NAME"));
					if (uniqueKeys.contains(uniqueKey)) {
						// If already in the list, get that key instead of the
						// one above.
						uniqueKey = uniqueKeys.get(uniqueKeys.indexOf(uniqueKey));
					} else {
						uniqueKeys.add(uniqueKey);
					}
					uniqueKey.addColumn(resultSet.getString("COLUMN_NAME"));
					// } else if (resultSet.getString(3).toUpperCase().equals("R")) {
					// // If the type is a r (referred), it is a foreign key.
					// final ForeignKey foreignKey = new ForeignKey(resultSet.getString(2));
					// foreignKey.setColumn(resultSet.getString(1));
					// foreignKey.setReferToTable(resultSet.getString(5));
					// foreignKey.setReferToColumn(resultSet.getString(4));
					// schema.addForeignKey(foreignKey);
				}
			}
		}

		// Add them all to the schema.
		for (final UniqueKey uniqueKey : uniqueKeys) {
			schema.addUniqueKey(uniqueKey);
		}

		// If the primary key to the schema if available.
		if (primaryKey != null) {
			schema.addPrimaryKey(primaryKey);
		}

		resultSet.close();
	}

	/**
	 * @param view
	 * @return
	 * @throws SQLException
	 */
	@Override
	protected String loadViewSelectQuery(final String view) throws SQLException {

		String result = null;

		ResultSet resultSet = null;
		statementViewSelect.setString(1, view);
		statementViewSelect.setString(2, getSchemaSpacePattern());
		resultSet = statementViewSelect.executeQuery();

		if (resultSet.next()) {
			result = resultSet.getString(1);
		}
		resultSet.close();

		return result;
	}

	/**
	 * Load the materialized views names as a list and return them.
	 * 
	 * @return
	 * @throws SQLException
	 */
	protected List<String> loadMaterializedViewsAsList() throws SQLException {
		return Collections.<String> emptyList();
	}

	/**
	 *Return a list strings each representing a name of a table in the database. This method is invoked by
	 * {@link #preloadTables()}.
	 * 
	 * @return
	 * @throws SQLException
	 */
	@Override
	protected List<String> loadTablesFromDbmdAsList() throws SQLException {
		final List<String> tables = super.loadTablesFromDbmdAsList();

		// MySQL has no materialized views yet.
		// for (final String view : loadMaterializedViewsAsList()) {
		// tables.remove(view);
		// }

		return tables;
	}

	/**
	 * Return a list strings each representing a name of a view in the database. This method is invoked by
	 * {@link #preloadViews()}.
	 * 
	 * @return
	 * @throws SQLException
	 */
	@Override
	protected List<String> loadViewsFromDbmdAsList() throws SQLException {
		final List<String> views = super.loadViewsFromDbmdAsList();

		// MySQL has no materialized views yet.
		// for (final String view : loadMaterializedViewsAsList()) {
		// views.add(view);
		// }

		return views;
	}

	@Override
	protected void hookAfterAnalyzeView(final DatabaseView view) throws SQLException {
		// Build keys (for views: unique keys).
		buildAllKeysOnlyForView(view);
		view.setDdlSchemaObject(schema);
	}
}
