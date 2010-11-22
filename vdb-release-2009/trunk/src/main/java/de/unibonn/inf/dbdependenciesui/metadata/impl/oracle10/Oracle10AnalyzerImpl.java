package de.unibonn.inf.dbdependenciesui.metadata.impl.oracle10;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import de.unibonn.inf.dbdependenciesui.hibernate.HibernateDAOFactory;
import de.unibonn.inf.dbdependenciesui.hibernate.IGenericDAO;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseTrigger;
import de.unibonn.inf.dbdependenciesui.metadata.MetaDataFactory.Vendor;
import de.unibonn.inf.dbdependenciesui.metadata.impl.oraclecommons.OracleAnalyzerImpl;

/**
 * Concrete implementation of an oracle 11g analyzer.
 * 
 * @author Andre Kasper
 * @author Jan Philipp
 */
public class Oracle10AnalyzerImpl extends OracleAnalyzerImpl {

	protected final String triggerSelect = "SELECT trigger_name, trigger_type, triggering_event, table_owner, base_object_type, table_name, column_name, referencing_names, when_clause, status, description, action_type, trigger_body FROM user_triggers WHERE table_name IS NOT NULL";

	public Oracle10AnalyzerImpl() {
		super(Vendor.ORACLE10);
	}

	@Override
	protected void preloadTriggers() throws SQLException {
		DatabaseTrigger trigger;
		final IGenericDAO<DatabaseTrigger, Serializable> triggerDAO = HibernateDAOFactory.getTriggerDAO();

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
			// 1 trigger_name, 2 trigger_type, 3 triggering_event, 4 table_owner, 5 base_object_type, 6 table_name, 7
			// column_name, 8 referencing_names, 9 when_clause, 10 status, 11 description, 12 action_type, 13
			// trigger_body, 14 crossedition, 15 before_statement, 16 before_row, 17 after_row, 18 after_statement, 19
			// instead_of_row
			triggerData.put(trigger.getTitle(), new String[] {
					resultSet.getString(1), resultSet.getString(2), resultSet.getString(3), resultSet.getString(4),
					resultSet.getString(5), resultSet.getString(6), resultSet.getString(7), resultSet.getString(8),
					resultSet.getString(9), resultSet.getString(10), resultSet.getString(11), resultSet.getString(12),
					resultSet.getString(13), "NO", "NO", "NO", "NO", "NO", "NO" });
		}

		// Clear all resources.
		resultSet.close();
		stm.close();

		firePropertyChange("numberOfTriggers", triggers.size());
	}

	// /**
	// * This method use the same implementation code like the superclass' except that the isSysDBA is replaced with an
	// * if(true).
	// */
	// @Override
	// protected void buildUniqueKeysOfTable(final DatabaseTable table) throws SQLException {
	// final List<UniqueKey> uniqueKeys = new ArrayList<UniqueKey>();
	// final List<Index> indices = new ArrayList<Index>();
	//
	// ResultSet resultSet = null;
	// if (true) {
	// try {
	// resultSet = dbmd.getIndexInfo(null, getSchemaSpacePattern(), table.getTitle(), false, false);
	// } catch (final Exception e) {}
	// } else {
	// resultSet = dbmd.getIndexInfo(null, getSchemaSpacePattern(), table.getTitle(), false, true);
	// }
	// if (resultSet != null) {
	// // For each result, create a key if 9th value (name) is present and not already exists.
	// while (resultSet.next()) {
	// if (resultSet.getString(9) != null) {
	// if (resultSet.getBoolean(4)) {
	// Index index = new Index(resultSet.getString(6));
	// if (indices.contains(index)) {
	// index = indices.get(indices.indexOf(index));
	// } else {
	// indices.add(index);
	// }
	// index.addColumn(resultSet.getString(9));
	// } else {
	// UniqueKey uniqueKey = new UniqueKey(resultSet.getString(6));
	// if (uniqueKeys.contains(uniqueKey)) {
	// // If already in the list, get that key instead of the
	// // one
	// // above.
	// uniqueKey = uniqueKeys.get(uniqueKeys.indexOf(uniqueKey));
	// } else {
	// uniqueKeys.add(uniqueKey);
	// }
	// uniqueKey.addColumn(resultSet.getString(9));
	// }
	// }
	// }
	// resultSet.close();
	//
	// // Add them all to the schema.
	// for (final UniqueKey uniqueKey : uniqueKeys) {
	// schema.addUniqueKey(uniqueKey);
	// }
	// for (final Index index : indices) {
	// schema.addIndex(index);
	// }
	// }
	// }
}
