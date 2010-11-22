package de.unibonn.inf.dbdependenciesui.graph.triggers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.unibonn.inf.dbdependenciesui.graph.common.AbstractDatabaseModelGraphTransformer;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseConnection;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseTrigger;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.Relation;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.TriggerSchema;

public class DatabaseTriggerModelGraphTransformer extends AbstractDatabaseModelGraphTransformer {

	private DatabaseTriggerGraph graph;
	private final DatabaseConnection connection;

	// CACHES

	public DatabaseTriggerModelGraphTransformer(final DatabaseConnection connection) {
		this(connection, null);
	}

	public DatabaseTriggerModelGraphTransformer(final DatabaseConnection connection, final List<String> includeList) {
		this.connection = connection;
		initializeIncludeList(includeList);
		initialize();
	}

	/**
	 * Initialize the include list. If the parameter <code>includeList</code> is empty the internal includeList will
	 * contains all possible elements.
	 * 
	 * @param includeList
	 */
	@Override
	protected void initializeIncludeList(final List<String> includeList) {
		if (includeList != null) {
			this.includeList.addAll(includeList);
		} else {
			for (final DatabaseTrigger trigger : connection.getTriggers()) {
				this.includeList.add(trigger.getTitle());
			}
		}
	}

	@Override
	protected void initialize() {
		// Create a new graph
		graph = new DatabaseTriggerGraph();

		// Load all triggers.
		final Set<DatabaseTrigger> triggers = new HashSet<DatabaseTrigger>();
		for (final DatabaseTrigger trigger : connection.getTriggers()) {
			if (isTableAllowed(trigger)) {
				triggers.add(trigger);
			}
		}

		// Add all triggers.
		for (final DatabaseTrigger trigger : triggers) {
			graph.addVertex(trigger);
		}
		for (final DatabaseTrigger source : triggers) {
			final TriggerSchema schema = source.getTriggerSchemaObject();
			for (final String table : schema.getAffectedInsertedTable()) {
				for (final DatabaseTrigger target : triggers) {
					if (isEventTriggeredOnTable("INSERT", table, target)) {
						buildEdge(source, target, "INSERT", schema);
					}
				}
			}
			for (final String table : schema.getAffectedUpdatedTable()) {
				for (final DatabaseTrigger target : triggers) {
					if (isEventTriggeredOnTable("UPDATE", table, target)) {
						buildEdge(source, target, "UPDATE", schema);
					}
				}
			}
			for (final String table : schema.getAffectedDeletedTable()) {
				for (final DatabaseTrigger target : triggers) {
					if (isEventTriggeredOnTable("DELETE", table, target)) {
						buildEdge(source, target, "DELETE", schema);
					}
				}
			}
		}
	}

	private boolean isEventTriggeredOnTable(final String event, final String table, final DatabaseTrigger target) {
		return getTriggerTableTitle(target).equalsIgnoreCase(table)
				&& getTriggerEvents(target).contains(event);
	}

	private String getTriggerTableTitle(final DatabaseTrigger target) {
		return target.getTriggerSchemaObject().getTable().getTitle();
	}

	private List<String> getTriggerEvents(final DatabaseTrigger target) {
		return target.getTriggerSchemaObject().getEvents();
	}

	private void buildEdge(final DatabaseTrigger source, final DatabaseTrigger target, final String event,
			final TriggerSchema schema) {
		String condition = schema.getType() + " " + event + " (" + target + ")";
		final Relation rel = new Relation(source.getTitle(), target.getTitle(), true);
		if (!getWhenClause(target).isEmpty()) {
			condition += " WHEN (" + getWhenClause(target) + ")";
		}
		rel.setCondition(condition);
		rel.setPositive(true);
		graph.addEdge(rel, source, target);
	}

	private String getWhenClause(final DatabaseTrigger target) {
		return target.getTriggerSchemaObject().getWhenClause();
	}

	@Override
	public DatabaseTriggerGraph getGraph() {
		return graph;
	}
}
