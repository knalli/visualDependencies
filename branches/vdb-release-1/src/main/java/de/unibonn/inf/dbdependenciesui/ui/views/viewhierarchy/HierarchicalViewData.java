/**
 * $Id: Main.java 41 2009-03-01 16:21:20Z philipp $
 */
package de.unibonn.inf.dbdependenciesui.ui.views.viewhierarchy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.unibonn.inf.dbdependenciesui.controller.Controller;
import de.unibonn.inf.dbdependenciesui.graph.viewhierarchy.DatabaseGraph;
import de.unibonn.inf.dbdependenciesui.graph.viewhierarchy.DatabaseModelGraphTransformer;
import de.unibonn.inf.dbdependenciesui.hibernate.HibernateDAOFactory;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseConnection;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseObject;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseTable;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseView;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.ConnectionAttributesMap;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.ConnectionProperties;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.Relation;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.ConnectionProperties.AttributeMapSet;
import de.unibonn.inf.dbdependenciesui.misc.Internationalization;
import de.unibonn.inf.dbdependenciesui.ui.controller.ViewController;
import de.unibonn.inf.dbdependenciesui.ui.controller.ViewController.ViewMode;
import de.unibonn.inf.dbdependenciesui.ui.misc.WaitProgressWindow.WaitProgressTask;
import de.unibonn.inf.dbdependenciesui.ui.views.common.AbstractDatabaseSelectorFrame;
import de.unibonn.inf.dbdependenciesui.ui.views.common.AbstractViewData;
import de.unibonn.inf.dbdependenciesui.ui.views.common.graph.AbstractGraphScene.LayoutType;
import de.unibonn.inf.dbdependenciesui.ui.views.viewhierarchy.graph.GraphScene;

/**
 * An observable model container for the hierarchiv views.
 * 
 * @author Andre Kasper
 * @author Jan Philipp
 */
public class HierarchicalViewData extends AbstractViewData {

	/**
	 * instances of the multi-singletons
	 */
	private static Map<Type, HierarchicalViewData> instances = new HashMap<Type, HierarchicalViewData>();

	/**
	 * current selected tables and views
	 */
	protected Map<DatabaseTable, Boolean> selectedTables;

	/**
	 * Return and create a multi-singleton-pattern-like instance.
	 * 
	 * @param type
	 * @return
	 */
	public synchronized static HierarchicalViewData getInstance(final Type type) {
		if (!instances.containsKey(type)) {
			instances.put(type, new HierarchicalViewData());
		}
		return instances.get(type);
	}

	protected HierarchicalViewData() {
		super();
	}

	@Override
	protected void initialize() {
		getSelectedTables();
	}

	/**
	 * Initialize the given connection to be the current one. This method loads the selection of tables and other user
	 * defined settings.
	 * 
	 * @param connectionTitle
	 */
	@Override
	protected synchronized void updateConnection(final int connectionId) {
		connection = Controller.loadConnection(connectionId, false);

		if (connection != null) {
			// Get all table and view names.
			getSelectedTables().clear();

			final List<DatabaseTable> tables;
			tables = Controller.getTablesAndViews(connection);

			final ConnectionProperties properties = connection.getPropertiesObject();

			for (final DatabaseTable table : tables) {
				if (properties.getAttributesMap(AttributeMapSet.VIEWS, table).isVisible()) {
					getSelectedTables().put(table, true);
				} else {
					getSelectedTables().put(table, false);
				}
			}
		}
	}

	/**
	 * Initialize the current database connection and update the graph.
	 */
	@Override
	protected synchronized void updateGraph() {
		// Local ref-copy, because selectedTables of the class is out of scope in the task.
		final Map<DatabaseTable, Boolean> selectedTables = getSelectedTables();
		final DatabaseConnection connection = this.connection;

		final WaitProgressTask task = new WaitProgressTask(ViewController.getApplicationView(), Internationalization
				.getText("application.graph.loading.title")) {

			@Override
			protected Void doInBackground() throws Exception {
				try {
					log.info("Starting (re) initializing the model-to-graph and display it.");

					if (connection != null) {

						log.info("Display Graph.");
						final List<String> includeList = new ArrayList<String>();
						for (final DatabaseTable table : selectedTables.keySet()) {
							if (selectedTables.get(table).booleanValue()) {
								includeList.add(table.getTitle());
							}
						}
						DatabaseModelGraphTransformer transformer;
						transformer = new DatabaseModelGraphTransformer(connection, includeList);
						setGraph(transformer.getGraph());
						HibernateDAOFactory.closeSession();
						log.info("Graph should be there.");

					} else {

						// If the connection is unset, exit this task.
						log.warning("Invalid connection title in ViewController.");
						ViewController.setDatabaseConnection(null);
						ViewController.setViewMode(ViewMode.CONNECTIONS);

					}
					setProgress(100);
				} catch (final Exception e) {
					log.warning(e.getLocalizedMessage());
					throw e;
				}
				return null;
			}
		};

		ViewController.showWaitProgress(task);

	}

	/**
	 * Set a new graph object. This will notifiy all observers.
	 * 
	 * @param graph
	 */
	private synchronized void setGraph(final DatabaseGraph graph) {
		setCurrentLayout(loadLayoutType(connection, graph));
		this.graph = graph;
		log.info("Setting a new GraphScene...");
		scene = new GraphScene(this, graph, connection.getPropertiesObject());
		log.info("A new GraphScene was created.");

		setChanged();
		notifyObservers(Notification.NEW_GRAPH);
	}

	@Override
	protected AbstractDatabaseSelectorFrame getSelectorFrame() {
		return new DatabaseTableSelectorFrame(this);
	}

	/**
	 * This method will check the graph if there are any vertices left alone. This algorithm will check and force that:
	 * 1) Any table has at least one associaction to an upper view (view is the source, this table is the target); 2)
	 * Any view has at least one association to another under view/table (this view is the source, the other the
	 * target)).
	 */
	protected synchronized void fixGraphVertexSelections() {

		// Select all tables and create a new graph unless one exists.
		if (graph == null) {
			updateGraph();
			return;
		}

		boolean stop = false;
		while (!stop) {

			// If the for loop goes through, we won't need a repeat.
			stop = true;

			for (final DatabaseObject object : graph.getVertices()) {

				if (!(object instanceof DatabaseTable)) {
					continue;
				}
				final DatabaseTable table = (DatabaseTable) object;

				// If the table is already hidden, we will ignore it.
				if (!getSelectedTables().get(table)) {
					continue;
				}

				if (table instanceof DatabaseView) {
					// Check for views that are "alone" w/o a connection down.
					boolean anyTargetVisible = false;
					for (final Relation relation : table.getDdlSchemaObject().getSourceRelations()) {
						if (getSelectedTables().get(relation.getTargetTable())) {
							anyTargetVisible = true;
							// If one exist, we will exit this loop.
							break;
						}
					}
					if (!anyTargetVisible) {
						getSelectedTables().put(table, false);
						// Modification of the vertices, repeat all.
						stop = false;
						break;
					}
				} else {
					// Check for tables that are alone w/o connection up.
					boolean anySourceVisible = false;
					for (final Relation relation : table.getDdlSchemaObject().getTargetRelations()) {
						if (getSelectedTables().get(relation.getSourceTable())) {
							anySourceVisible = true;
							// If one exist, we will exit this loop.
							break;
						}
					}
					if (!anySourceVisible) {
						getSelectedTables().put(table, false);
						// Modification of the vertices, repeat all.
						stop = false;
						break;
					}
				}
			}
		}
	}

	/**
	 * This action updates the selected table and vertices states and forces updating the views. Only if graph has been
	 * added new vertices the graph has to be rebuild; otherwise it is only be repainted.
	 */
	public synchronized void actionUpdateSelections() {

		fixGraphVertexSelections();

		boolean atLeastOneVertexAdded = false;

		// viewer.getGraphLayout().getGraph().removeVertex(selectedTable);
		final ConnectionProperties properties = connection.getPropertiesObject();

		for (final DatabaseTable table : connection.getTables()) {
			final ConnectionAttributesMap attributes = properties.getAttributesMap(AttributeMapSet.VIEWS, table);

			// Only for tables, ignore views.
			if (getSelectedTables().containsKey(table)) {
				// Change state if it was hidden and should be visible now. Otherwise remove the vertex.
				if (!attributes.isVisible() && getSelectedTables().get(table)) {
					atLeastOneVertexAdded = true;
				} else if (attributes.isVisible() && !getSelectedTables().get(table)) {
					graph.removeVertex(table);
				}
				attributes.setVisible(getSelectedTables().get(table));
			}
		}

		for (final DatabaseView view : connection.getViews()) {
			final ConnectionAttributesMap attributes = properties.getAttributesMap(AttributeMapSet.VIEWS, view);

			// Only for views, ignore tables.
			if (getSelectedTables().containsKey(view)) {
				// Change state if it was hidden and should be visible now. Otherwise remove the vertex.
				if (!attributes.isVisible() && getSelectedTables().get(view)) {
					atLeastOneVertexAdded = true;
				} else if (attributes.isVisible() && !getSelectedTables().get(view)) {
					graph.removeVertex(view);
				}
				attributes.setVisible(getSelectedTables().get(view));
			}
		}

		connection.setPropertiesObject(properties);

		if (saveSelections) {
			final int id = connection.getId();
			final DatabaseConnection connection = Controller.loadConnection(id, false);
			connection.setPropertiesObject(properties);
			Controller.updateConnection(connection);
		}

		if (atLeastOneVertexAdded) {
			updateGraph();
		} else {
			scene.getMainView().repaint();
		}
	}

	@Override
	public synchronized void actionUpdateLayout(final LayoutType layoutType) {

		final ConnectionProperties properties = connection.getPropertiesObject();

		for (final DatabaseTable table : connection.getTables()) {
			properties.getAttributesMap(getAttributeMapSet(), table).setLayout(layoutType.toString());
		}
		for (final DatabaseTable table : connection.getViews()) {
			properties.getAttributesMap(getAttributeMapSet(), table).setLayout(layoutType.toString());
		}
		connection.setPropertiesObject(properties);

		if (savePositions) {
			final int id = connection.getId();
			final DatabaseConnection connection2 = Controller.loadConnection(id, false);
			connection2.setPropertiesObject(properties);
			Controller.updateConnection(connection2);
		}

		super.actionUpdateLayout(layoutType);
	}

	@Override
	protected void resetObjectsPositions(final ConnectionProperties properties) {
		ConnectionAttributesMap attributes;

		for (final DatabaseTable table : connection.getTables()) {
			attributes = properties.getAttributesMap(AttributeMapSet.VIEWS, table);
			attributes.clearPositions();
			attributes.setLayout("");
		}

		for (final DatabaseView view : connection.getViews()) {
			attributes = properties.getAttributesMap(AttributeMapSet.VIEWS, view);
			attributes.clearPositions();
			attributes.setLayout("");
		}
	}

	public synchronized Map<DatabaseTable, Boolean> getSelectedTables() {
		if (selectedTables == null) {
			selectedTables = new HashMap<DatabaseTable, Boolean>();
		}
		return selectedTables;
	}

	/**
	 * Get the number of vies of the current connection.
	 * 
	 * @return
	 */
	public synchronized int getNumberOfViews() {
		return connection.getViews().size();
	}

	/**
	 * Get the number of tables of the current connection.
	 * 
	 * @return
	 */
	public synchronized int getNumberOfTables() {
		return connection.getTables().size();
	}

	/**
	 * Get the number of selected tables/views.
	 * 
	 * @return
	 */
	public synchronized int getNumberOfSelected() {
		int count = 0;

		for (final boolean selected : getSelectedTables().values()) {
			if (selected) {
				++count;
			}
		}

		return count;
	}

	@Override
	protected AttributeMapSet getAttributeMapSet() {
		return AttributeMapSet.VIEWS;
	}

	@Override
	protected ViewMode getViewMode() {
		return ViewMode.HIERARCHY;
	}

	@Override
	public LayoutType getDefaultLayout() {
		return LayoutType.DATABASE_FOREST_LAYOUT;
	}

}
