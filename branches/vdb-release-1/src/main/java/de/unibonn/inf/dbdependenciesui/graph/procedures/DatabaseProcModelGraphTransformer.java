package de.unibonn.inf.dbdependenciesui.graph.procedures;

import java.util.List;

import de.unibonn.inf.dbdependenciesui.graph.common.AbstractDatabaseModelGraphTransformer;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseConnection;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseProcedure;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseTable;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseTrigger;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.Relation;

public class DatabaseProcModelGraphTransformer extends AbstractDatabaseModelGraphTransformer {

	private DatabaseProcGraph graph;
	private final DatabaseConnection connection;
	private final DatabaseTable table;

	public DatabaseProcModelGraphTransformer(final DatabaseConnection connection) {
		this(connection, null);
	}

	public DatabaseProcModelGraphTransformer(final DatabaseConnection connection, final DatabaseTable table) {
		this.connection = connection;
		this.table = table;
		initialize();
	}

	@Override
	public DatabaseProcGraph getGraph() {
		return graph;
	}

	@Override
	protected void initialize() {
		graph = new DatabaseProcGraph();

		graph.addVertex(table);

		for (final DatabaseTable view : connection.getViews()) {

			for (final Relation relation : view.getDdlSchemaObject().getSourceRelations()) {

				if (relation.getTargetName().equalsIgnoreCase(table.getTitle())) {
					graph.addVertex(view);
					if (!graph.isNeighbor(view, table)) {
						graph.addEdge(relation, view, table);
					}
				}

			}
		}

		Boolean addObject;
		String condition;
		final String tableName = table.getTitle();
		Relation relation;

		for (final DatabaseTrigger trigger : connection.getTriggers()) {
			addObject = false;
			condition = "";
			relation = null;

			if (trigger.getTriggerSchemaObject().getTableName().equalsIgnoreCase(tableName)) {
				addObject = true;
			}

			for (final String deletedTable : trigger.getTriggerSchemaObject().getUsedTables()) {
				if (deletedTable.equalsIgnoreCase(tableName)) {
					addObject = true;
					condition += "SELECT FROM " + tableName.toLowerCase();
					break;
				}
			}
			for (final String deletedTable : trigger.getTriggerSchemaObject().getAffectedInsertedTable()) {
				if (deletedTable.equalsIgnoreCase(tableName)) {
					addObject = true;
					condition += condition == "" ? "INSERT INTO " + tableName.toLowerCase() : ", INSERT INTO "
							+ tableName.toLowerCase();
					break;
				}
			}
			for (final String deletedTable : trigger.getTriggerSchemaObject().getAffectedUpdatedTable()) {
				if (deletedTable.equalsIgnoreCase(tableName)) {
					addObject = true;
					condition += condition == "" ? "UPDATE " + tableName.toLowerCase() : ", UPDATE "
							+ tableName.toLowerCase();
					break;
				}
			}
			for (final String deletedTable : trigger.getTriggerSchemaObject().getAffectedDeletedTable()) {
				if (deletedTable.equalsIgnoreCase(tableName)) {
					addObject = true;
					condition += condition == "" ? "DELETE FROM " + tableName.toLowerCase() : ", DELETE FROM "
							+ tableName.toLowerCase();
					break;
				}
			}

			if (addObject) {
				graph.addVertex(trigger);
				relation = new Relation(trigger.getTitle(), tableName, true);
				relation.setCondition(condition);
				relation.setPositive(true);
				graph.addEdge(relation, trigger, table);
			}
		}

		for (final DatabaseProcedure procedure : connection.getProcedures()) {
			addObject = false;
			condition = "";
			relation = null;

			for (final String deletedTable : procedure.getProcedureSchemaObject().getUsedTables()) {
				if (deletedTable.equalsIgnoreCase(tableName)) {
					addObject = true;
					condition += "SELECT FROM " + tableName.toLowerCase();
					break;
				}
			}
			for (final String deletedTable : procedure.getProcedureSchemaObject().getAffectedInsertedTable()) {
				if (deletedTable.equalsIgnoreCase(tableName)) {
					addObject = true;
					condition += condition == "" ? "INSERT INTO " + tableName.toLowerCase() : ", INSERT INTO "
							+ tableName.toLowerCase();
					break;
				}
			}
			for (final String deletedTable : procedure.getProcedureSchemaObject().getAffectedUpdatedTable()) {
				if (deletedTable.equalsIgnoreCase(tableName)) {
					addObject = true;
					condition += condition == "" ? "UPDATE " + tableName.toLowerCase() : ", UPDATE "
							+ tableName.toLowerCase();
					break;
				}
			}
			for (final String deletedTable : procedure.getProcedureSchemaObject().getAffectedDeletedTable()) {
				if (deletedTable.equalsIgnoreCase(tableName)) {
					addObject = true;
					condition += condition == "" ? "DELETE FROM " + tableName.toLowerCase() : ", DELETE FROM "
							+ tableName.toLowerCase();
					break;
				}
			}

			if (addObject) {
				graph.addVertex(procedure);
				relation = new Relation(procedure.getTitle(), tableName, true);
				relation.setCondition(condition);
				relation.setPositive(true);
				graph.addEdge(relation, procedure, table);
			}
		}

		graph.setRoot(table);

	}

	@Override
	protected void initializeIncludeList(final List<String> includeList) {

	}

}
