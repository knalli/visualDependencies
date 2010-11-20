package de.unibonn.inf.dbdependenciesui.graph.viewhierarchy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.unibonn.inf.dbdependenciesui.graph.common.AbstractDatabaseModelGraphTransformer;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseConnection;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseTable;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseView;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.DdlSchema;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.Relation;

/**
 * This class transforms a model (given by the root {@link DatabaseConnection} object) and creates the corresponding
 * {@link DatabaseGraph}.
 * 
 * @author Andre Kasper
 * @author Jan Philipp
 */
public class DatabaseModelGraphTransformer extends AbstractDatabaseModelGraphTransformer {

	private DatabaseGraph graph;
	private final DatabaseConnection connection;

	// CACHES

	private final Map<DatabaseTable, DdlSchema> cachedDdlSchemaObjects = new HashMap<DatabaseTable, DdlSchema>();
	private final Map<String, DatabaseTable> cachedTableObjects = new HashMap<String, DatabaseTable>();

	public DatabaseModelGraphTransformer(final DatabaseConnection connection) {
		this(connection, null);
	}

	public DatabaseModelGraphTransformer(final DatabaseConnection connection, final List<String> includeList) {
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
			for (final DatabaseView view : connection.getViews()) {
				this.includeList.add(view.getTitle());
			}
		}
	}

	@Override
	protected void initialize() {
		// Create a new graph
		graph = new DatabaseGraph();

		// Create a list of possible root nodes.
		final Map<DatabaseTable, Boolean> rootCandidates = new HashMap<DatabaseTable, Boolean>();

		// Load all tables and views.
		final Set<DatabaseTable> tables = new HashSet<DatabaseTable>();
		for (final DatabaseTable table : connection.getTables()) {
			if (isTableAllowed(table)) {
				cachedTableObjects.put(table.getTitle(), table);
				tables.add(table);
			}
		}

		final Set<DatabaseView> views = new HashSet<DatabaseView>();
		for (final DatabaseView view : connection.getViews()) {
			if (isTableAllowed(view)) {
				cachedTableObjects.put(view.getTitle(), view);
				views.add(view);
			}
		}

		// Filter: Tables/views that have no visible relation to another object will be hidden and removed from the
		// list.

		boolean breakBecauseRestart = true;

		while (breakBecauseRestart && (includeList.size() > 0)) {
			for (final String title : includeList) {
				final DatabaseTable table = cachedTableObjects.get(title);
				if (table != null) {
					final DdlSchema ddlschema = getCachedDdlSchema(table);
					boolean visible = false;

					for (final Relation relation : ddlschema.getRelations()) {

						if (relation.isFromSource()) {
							visible = (includeList.contains(relation.getSourceName()));
						} else {
							visible = (includeList.contains(relation.getTargetName()));
						}

						if (!visible) {
							break;
						}
					}

					breakBecauseRestart = true;
					if (!visible) {
						includeList.remove(title);
						cachedTableObjects.remove(table);
						if (table instanceof DatabaseView) {
							views.remove(table);
						} else {
							tables.remove(table);
						}

						break;
					}
					breakBecauseRestart = false;
				}
			}
		}

		// Add all tables.
		for (final DatabaseTable table : tables) {
			graph.addVertex(table);

			// At least one table is a root
			rootCandidates.put(table, true);
		}

		// Add all views.
		for (final DatabaseView view : views) {
			graph.addVertex(view);

			// At least one view is a root
			rootCandidates.put(view, true);
		}

		// For each view, add all its relations to other tables and views.
		for (final DatabaseView view : views) {
			AbstractDatabaseModelGraphTransformer.log.info("Process view " + view);
			for (final Relation relation : getCachedDdlSchema(view).getSourceRelations()) {

				// Continue the for loop if the table is not allowed to display.
				if (!this.isTableAllowed(relation.getTargetName())) {
					continue;
				}

				final DatabaseTable target = cachedTableObjects.get(relation.getTargetName());
				AbstractDatabaseModelGraphTransformer.log.info("Process target " + target);

				if (graph.isNeighbor(view, target)) {
					AbstractDatabaseModelGraphTransformer.log.info(String.format("Already neighbours: %s and %s", view,
							target));
				} else {
					AbstractDatabaseModelGraphTransformer.log.info(String.format("Add edge %s (%s to %s) [%s]",
							relation, view, target, relation.isFromSource() ? "source" : "target"));
					graph.addEdge(relation, view, target);
				}
				rootCandidates.put(target, false);
			}
		}

		// Get the root candidates
		final List<DatabaseTable> roots = new ArrayList<DatabaseTable>();
		for (final DatabaseTable key : rootCandidates.keySet()) {
			if (rootCandidates.get(key).booleanValue()) {
				roots.add(key);
			}
		}

		// If all not as root available, we have to decide: select a random view or a
		// random table.
		if (roots.isEmpty()) {
			if (views.size() > 0) {
				roots.add(views.iterator().next());
			} else if (tables.size() > 0) {
				roots.add(tables.iterator().next());
			}
		}

		for (final DatabaseTable root : roots) {
			AbstractDatabaseModelGraphTransformer.log.info(root.getTitle() + " is one root.");
		}
		graph.setRoot(roots);
	}

	/**
	 * @param table
	 * @return
	 */
	private DdlSchema getCachedDdlSchema(final DatabaseTable table) {
		DdlSchema ddlschema = cachedDdlSchemaObjects.get(table);
		if (ddlschema == null) {
			ddlschema = table.getDdlSchemaObject();
			cachedDdlSchemaObjects.put(table, ddlschema);
		}
		return ddlschema;
	}

	@Override
	public DatabaseGraph getGraph() {
		return graph;
	}

}
