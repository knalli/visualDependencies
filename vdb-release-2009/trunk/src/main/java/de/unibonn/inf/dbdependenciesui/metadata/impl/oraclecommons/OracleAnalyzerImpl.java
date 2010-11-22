/**
 * $Id$
 */
package de.unibonn.inf.dbdependenciesui.metadata.impl.oraclecommons;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.unibonn.inf.dbdependenciesui.hibernate.HibernateDAOFactory;
import de.unibonn.inf.dbdependenciesui.hibernate.IGenericDAO;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseProcedure;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseTable;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseTrigger;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseView;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.DdlSchemaEditable;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.ForeignKey;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.Index;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.PrimaryKey;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.ProcedureSchemaEditable;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.TriggerSchemaEditable;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.UniqueKey;
import de.unibonn.inf.dbdependenciesui.metadata.MetaDataFactory.Vendor;
import de.unibonn.inf.dbdependenciesui.metadata.impl.AbstractAnalyzer;

/**
 * Abstract implementation of an oracle analyzer, designed for versions 9, 10 and 11.
 * 
 * @author Andre Kasper
 * @author Jan Philipp
 * @author Marc Kastleiner
 */
public abstract class OracleAnalyzerImpl extends AbstractAnalyzer {

	protected PreparedStatement statementViewSelect;
	protected PreparedStatement statementMaterializedViewSelect;
	protected PreparedStatement statementViewKeysSelect;
	protected PreparedStatement statementIndexSelect;

	protected final String selectViewSelect = "SELECT text FROM user_views WHERE UPPER(view_name) LIKE UPPER(?)";
	protected final String selectMaterializedViewSelect = "SELECT query FROM user_mviews WHERE UPPER(mview_name) LIKE UPPER(?)";
	protected final String viewKeysSelect = "SELECT ucc1.column_name, uc1.constraint_name, uc1.constraint_type, ucc2.column_name, ucc2.table_name FROM user_cons_columns ucc1, user_constraints uc1, user_cons_columns ucc2 WHERE uc1.r_constraint_name = ucc2.constraint_name(+) AND uc1.constraint_name = ucc1.constraint_name AND uc1.table_name LIKE UPPER(?)";
	protected final String materializedViewsSelect = "SELECT mview_name FROM USER_MVIEWS";
	protected final String indexSelect = "SELECT ucc1.column_name, uc1.constraint_name, uc1.constraint_type, ucc2.column_name, ucc2.table_name FROM user_cons_columns ucc1, user_constraints uc1, user_cons_columns ucc2 WHERE (uc1.constraint_type LIKE 'C' OR uc1.constraint_type LIKE 'U') AND uc1.r_constraint_name = ucc2.constraint_name(+) AND uc1.constraint_name = ucc1.constraint_name AND uc1.table_name LIKE UPPER(?)";

	protected final String procedureSelect = "SELECT distinct s.name, s.type, o.status, o.created FROM user_source s, user_objects o WHERE s.name = o.object_name AND o.object_type in ('PROCEDURE' , 'FUNCTION') ORDER BY name";

	protected List<String> materializedViews = null;

	/**
	 * trigger data object (working object)
	 */
	protected Map<String, String[]> triggerData;

	/**
	 * procedure data object (working object)
	 */
	protected Map<String, String[]> procedureData;

	public OracleAnalyzerImpl(final Vendor vendor) {
		super(vendor);
	}

	@Override
	protected void initialize() {
		super.initialize();
		materializedViews = null;
	}

	@Override
	protected void prepareSharedStatements() throws SQLException {
		statementViewSelect = sqlConnection.prepareStatement(selectViewSelect);
		statementMaterializedViewSelect = sqlConnection.prepareStatement(selectMaterializedViewSelect);
		statementViewKeysSelect = sqlConnection.prepareStatement(viewKeysSelect);
		statementIndexSelect = sqlConnection.prepareStatement(indexSelect);
		super.prepareSharedStatements();
	}

	@Override
	protected void preloadProcedures() throws SQLException {
		DatabaseProcedure procedure;
		final IGenericDAO<DatabaseProcedure, Serializable> procedureDAO = HibernateDAOFactory.getProcedureDAO();

		final Statement stm = sqlConnection.createStatement();
		stm.setFetchSize(fetchSize);
		final ResultSet resultSet = stm.executeQuery(procedureSelect);

		final Statement stm2 = sqlConnection.createStatement();
		stm2.setFetchSize(fetchSize);

		// Create a new trigger data map.
		procedureData = new HashMap<String, String[]>();

		ResultSet textSet;
		String text = "";

		while (resultSet.next()) {
			procedure = procedureDAO.create();
			procedure.setTitle(resultSet.getString(1));
			procedure.setConnection(databaseConnection);
			databaseConnection.addProcedure(procedure);
			procedures.add(procedure);

			textSet = stm2.executeQuery("SELECT text FROM user_source WHERE name = '" + resultSet.getString(1)
					+ "' ORDER BY line");

			text = "";
			while (textSet.next()) {
				text += textSet.getString(1);
			}
			textSet.close();

			// 1 name 2 type 3 status 4 created

			procedureData.put(procedure.getTitle(), new String[] {
					resultSet.getString(1), resultSet.getString(2), resultSet.getString(3), resultSet.getString(4),
					text });
		}

		// Clear all resources.
		resultSet.close();
		stm.close();

		firePropertyChange("numberOfProcedures", procedures.size());
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

			schema.setName(trimStringOrNull(data[1 - 1]));
			schema.setType(trimStringOrNull(data[2 - 1]));
			schema.setEvents(Arrays.asList(trimStringOrNull(data[3 - 1]).split("(OR)")));
			schema.setTableOwner(trimStringOrNull(data[4 - 1]));
			schema.setBaseObjectType(trimStringOrNull(data[5 - 1]));
			schema.setTableName(trimStringOrNull(data[6 - 1]));
			schema.setColumnName(trimStringOrNull(data[7 - 1]));
			schema.setReferencingNames(trimStringOrNull(data[8 - 1]));
			schema.setWhenClause(trimStringOrNull(data[9 - 1]));
			schema.setEnabled("ENABLED".equalsIgnoreCase(trimStringOrNull(trimStringOrNull(data[10 - 1]))));
			schema.setDescription(trimStringOrNull(data[11 - 1]));
			schema.setActionType(trimStringOrNull(data[12 - 1]));
			schema.setBody(trimStringOrNull(data[13 - 1]));
			schema.setCrossEdition(!"NO".equalsIgnoreCase(trimStringOrNull(data[14 - 1])));
			// schema.setInsteadOfRow(!"NO".equalsIgnoreCase(trimStringOrNull(data[19 - 1])));

			// This columns are always "NO" and seem not containing the right information. As workaround, we scan the
			// type column's content.
			// schema.setBeforeStatement(!"NO".equalsIgnoreCase(trimStringOrNull(data[15 - 1])));
			// schema.setBeforeRow(!"NO".equalsIgnoreCase(trimStringOrNull(data[16 - 1])));
			// schema.setAfterRow(!"NO".equalsIgnoreCase(trimStringOrNull(data[17 - 1])));
			// schema.setAfterStatement(!"NO".equalsIgnoreCase(trimStringOrNull(data[18 - 1])));
			schema.setBeforeStatement(compareModeStrings(data[2 - 1], "BEFORE STATEMENT"));
			schema.setBeforeRow(compareModeStrings(data[2 - 1], "BEFORE EACH ROW"));
			schema.setAfterRow(compareModeStrings(data[2 - 1], "AFTER EACH ROW"));
			schema.setAfterStatement(compareModeStrings(data[2 - 1], "AFTER STATEMENT"));
			schema.setInsteadOfRow(compareModeStrings(data[2 - 1], "INSTEAD OF"));
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

	@Override
	protected void analyzeProcedures() throws SQLException {
		for (final DatabaseProcedure procedure : procedures) {
			firePropertyChange("analyzingProcedures", procedure.getTitle());

			// Create a new ddlschema object.
			final ProcedureSchemaEditable schema = procedure.createProcedureSchemaEditableObject();

			// Get all trigger data.
			final String[] data = procedureData.get(procedure.getTitle());

			// 1 name 2 type 3 status 4 created

			schema.setName(trimStringOrNull(data[1 - 1]));
			schema.setType(trimStringOrNull(data[2 - 1]));
			schema.setState(trimStringOrNull(data[3 - 1]));
			schema.setCreationDate(trimStringOrNull(data[4 - 1]));
			schema.setBody(trimStringOrNull(data[5 - 1]));
			// Save the trigger schema.
			procedure.setProcedureSchemaObject(schema);
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
		return !resultSet.getString(3).contains("BIN$");
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
		return !resultSet.getString(3).contains("BIN$");
	}

	/**
	 * Return the default oracle schema space pattern.
	 */
	@Override
	protected String getSchemaSpacePattern() throws SQLException {
		String schema = databaseConnection.getSchema();
		if ((schema == null) || schema.isEmpty()) {
			schema = dbmd.getUserName();
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
		if (views.contains(table)) {
			// buildPrimaryKeysOfView(table);
		} else {
			buildPrimaryKeysOfTable(table);
		}
	}

	protected void buildPrimaryKeysOfTable(final DatabaseTable table) throws SQLException {
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
		if (views.contains(table)) {
			// buildForeignKeysOfView(table);
		} else {
			buildForeignKeysOfTable(table);
		}
	}

	protected void buildForeignKeysOfTable(final DatabaseTable table) throws SQLException {
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
	 * Create unique keys from table.
	 * 
	 * @param table
	 * @uses schema, uniques
	 * @throws SQLException
	 */
	@Override
	protected void buildUniqueKeys(final DatabaseTable table) throws SQLException {
		if (views.contains(table)) {
			// buildUniqueKeysOfView(table);
		} else {
			buildUniqueKeysOfTable(table);
		}
	}

	protected void buildUniqueKeysOfTable(final DatabaseTable table) throws SQLException {
		final List<UniqueKey> uniqueKeys = new ArrayList<UniqueKey>();
		final List<Index> indices = new ArrayList<Index>();
		statementIndexSelect.setString(1, table.getTitle());
		statementIndexSelect.setFetchSize(fetchSize);
		final ResultSet resultSet = statementIndexSelect.executeQuery();
		// if (databaseConnection.getSysdba()) {
		// try {
		// resultSet = dbmd.getIndexInfo(null, getSchemaSpacePattern(), table.getTitle(), false, false);
		// } catch (final Exception e) {}
		// } else {
		// resultSet = dbmd.getIndexInfo(null, getSchemaSpacePattern(), table.getTitle(), false, true);
		// }
		if (resultSet != null) {
			// For each result, create a key if 9th value (name) is present and not already exists.
			while (resultSet.next()) {
				if (resultSet.getString(1) != null) {
					if ("C".equals(resultSet.getString(3))) {
						Index index = new Index(resultSet.getString(2));
						if (indices.contains(index)) {
							index = indices.get(indices.indexOf(index));
						} else {
							indices.add(index);
						}
						index.addColumn(resultSet.getString(1));
					} else {
						UniqueKey uniqueKey = new UniqueKey(resultSet.getString(2));
						if (uniqueKeys.contains(uniqueKey)) {
							// If already in the list, get that key instead of the
							// one
							// above.
							uniqueKey = uniqueKeys.get(uniqueKeys.indexOf(uniqueKey));
						} else {
							uniqueKeys.add(uniqueKey);
						}
						uniqueKey.addColumn(resultSet.getString(1));
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
	}

	/**
	 * Create all keys from table.
	 * 
	 * @param view
	 * @uses schema, uniques
	 * @throws SQLException
	 */
	protected void buildAllKeysOnlyForView(final DatabaseView view) throws SQLException {

		statementViewKeysSelect.setString(1, view.getTitle());
		final ResultSet resultSet = statementViewKeysSelect.executeQuery();

		final List<UniqueKey> uniqueKeys = new ArrayList<UniqueKey>();
		PrimaryKey primaryKey = null;

		// For each result, create a key if it is one.
		while (resultSet.next()) {
			if (resultSet.getString(1) != null) {
				if (resultSet.getString(3).toUpperCase().equals("P")) {
					// If the type is p, it is a primary key.
					// It is possible, that a primary key is build by more than
					// one columns.
					if (primaryKey == null) {
						primaryKey = new PrimaryKey(resultSet.getString(2));
					}
					primaryKey.addColumn(resultSet.getString(1));
					UniqueKey uniqueKey = new UniqueKey(resultSet.getString(2));
					if (uniqueKeys.contains(uniqueKey)) {
						// If already in the list, get that key instead of the
						// one above.
						uniqueKey = uniqueKeys.get(uniqueKeys.indexOf(uniqueKey));
					} else {
						uniqueKeys.add(uniqueKey);
					}
					uniqueKey.addColumn(resultSet.getString(1));
				} else if (resultSet.getString(3).toUpperCase().equals("U")) {
					// If the type is a u, it is a unique key.
					UniqueKey uniqueKey = new UniqueKey(resultSet.getString(2));
					if (uniqueKeys.contains(uniqueKey)) {
						// If already in the list, get that key instead of the
						// one above.
						uniqueKey = uniqueKeys.get(uniqueKeys.indexOf(uniqueKey));
					} else {
						uniqueKeys.add(uniqueKey);
					}
					uniqueKey.addColumn(resultSet.getString(1));
				} else if (resultSet.getString(3).toUpperCase().equals("R")) {
					// If the type is a r (referred), it is a foreign key.
					final ForeignKey foreignKey = new ForeignKey(resultSet.getString(2));
					foreignKey.setColumn(resultSet.getString(1));
					foreignKey.setReferToTable(resultSet.getString(5));
					foreignKey.setReferToColumn(resultSet.getString(4));
					schema.addForeignKey(foreignKey);
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
		if (loadMaterializedViewsAsList().contains(view)) {
			statementMaterializedViewSelect.setString(1, view);
			resultSet = statementMaterializedViewSelect.executeQuery();
		} else {
			statementViewSelect.setString(1, view);
			resultSet = statementViewSelect.executeQuery();
		}

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

		if (materializedViews == null) {
			materializedViews = new ArrayList<String>();

			final Statement stm = sqlConnection.createStatement();
			final ResultSet resultSet = stm.executeQuery(materializedViewsSelect);

			while (resultSet.next()) {
				materializedViews.add(resultSet.getString(1));
			}

			resultSet.close();
			stm.close();
		}

		return materializedViews;
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

		for (final String view : loadMaterializedViewsAsList()) {
			tables.remove(view);
		}

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

		for (final String view : loadMaterializedViewsAsList()) {
			views.add(view);
		}

		return views;
	}

	@Override
	protected void hookAfterAnalyzeView(final DatabaseView view) throws SQLException {
		// Build keys (for views: unique keys).
		buildAllKeysOnlyForView(view);
		// Replace the schema object.
		view.setDdlSchemaObject(schema);

		// Set isMaterialized attribute.
		if (materializedViews != null) {
			if (materializedViews.contains(view.getTitle())) {
				view.setMaterialized(true);
			}
		}
	}
}
