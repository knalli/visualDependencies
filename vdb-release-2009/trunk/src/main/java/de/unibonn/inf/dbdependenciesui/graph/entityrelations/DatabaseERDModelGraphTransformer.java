package de.unibonn.inf.dbdependenciesui.graph.entityrelations;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.unibonn.inf.dbdependenciesui.graph.common.AbstractDatabaseModelGraphTransformer;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseConnection;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseTable;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.ForeignKey;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.Relation;
import de.unibonn.inf.dbdependenciesui.misc.Internationalization;

public class DatabaseERDModelGraphTransformer extends AbstractDatabaseModelGraphTransformer {

	private DatabaseERDGraph graph;
	private final DatabaseConnection connection;

	// CACHES

	private final Map<String, DatabaseTable> cachedTriggerObjects = new HashMap<String, DatabaseTable>();

	public DatabaseERDModelGraphTransformer(final DatabaseConnection connection) {
		this(connection, null);
	}

	public DatabaseERDModelGraphTransformer(final DatabaseConnection connection, final List<String> includeList) {
		this.connection = connection;
		initializeIncludeList(includeList);
		initialize();
	}

	/**
	 * Initialize the include list. If the param <code>includeList</code> is empty the internal includeList will
	 * contains all possible elements.
	 * 
	 * @param includeList
	 */
	@Override
	protected void initializeIncludeList(final List<String> includeList) {
		if (includeList != null) {
			this.includeList.addAll(includeList);
		} else {
			for (final DatabaseTable table : connection.getTables()) {
				this.includeList.add(table.getTitle());
			}
		}
	}

	@Override
	protected void initialize() {
		// Create a new graph
		graph = new DatabaseERDGraph();

		// Load all triggers.
		final Set<DatabaseTable> tables = new HashSet<DatabaseTable>();
		for (final DatabaseTable table : connection.getTables()) {
			if (isTableAllowed(table)) {
				cachedTriggerObjects.put(table.getTitle(), table);
				tables.add(table);
			}
		}

		// Add all triggers.
		for (final DatabaseTable table : tables) {
			graph.addVertex(table);
		}

		for (final DatabaseTable table : tables) {
			final List<ForeignKey> foreigns = table.createDdlSchemaEditableObject().getForeignKeys();
			for (final ForeignKey foreign : foreigns) {
				final Relation rel = new Relation(table.getTitle(), foreign.getReferToTable(), true);
				rel.setCondition(Internationalization.getTextFormatted("application.graph.erd.edge.tooltip", foreign
						.getColumn(), foreign.getReferToColumn()));
				rel.setPositive(true);
				graph.addEdge(rel, table, connection.getTableByTitle(foreign.getReferToTable()));
			}
		}
	}

	@Override
	public DatabaseERDGraph getGraph() {
		return graph;
	}

}
