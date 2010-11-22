package de.unibonn.inf.dbdependenciesui.ui.views.entityrelations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.unibonn.inf.dbdependenciesui.controller.Controller;
import de.unibonn.inf.dbdependenciesui.graph.entityrelations.DatabaseERDGraph;
import de.unibonn.inf.dbdependenciesui.graph.entityrelations.DatabaseERDModelGraphTransformer;
import de.unibonn.inf.dbdependenciesui.hibernate.HibernateDAOFactory;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseConnection;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseTable;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseView;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.ConnectionAttributesMap;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.ConnectionProperties;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.ConnectionProperties.AttributeMapSet;
import de.unibonn.inf.dbdependenciesui.misc.Internationalization;
import de.unibonn.inf.dbdependenciesui.ui.controller.ViewController;
import de.unibonn.inf.dbdependenciesui.ui.controller.ViewController.ViewMode;
import de.unibonn.inf.dbdependenciesui.ui.misc.WaitProgressWindow.WaitProgressTask;
import de.unibonn.inf.dbdependenciesui.ui.views.common.AbstractDatabaseSelectorFrame;
import de.unibonn.inf.dbdependenciesui.ui.views.common.AbstractViewData;
import de.unibonn.inf.dbdependenciesui.ui.views.common.graph.AbstractGraphScene.LayoutType;
import de.unibonn.inf.dbdependenciesui.ui.views.entityrelations.graph.ERDGraphScene;

public class ERDViewData extends AbstractViewData {

	/**
	 * instances of the multi-singletons
	 */
	private static Map<Type, ERDViewData> instances = new HashMap<Type, ERDViewData>();

	/**
	 * current database graph
	 */
	private DatabaseERDGraph graph;

	/**
	 * current selected tables and views
	 */
	private Map<DatabaseTable, Boolean> selectedTables;

	/**
	 * Return and create a multi-singleton-pattern-like instance.
	 * 
	 * @param type
	 * @return
	 */
	public synchronized static AbstractViewData getInstance(final Type type) {
		if (!instances.containsKey(type)) {
			instances.put(type, new ERDViewData());
		}
		return instances.get(type);
	}

	protected ERDViewData() {
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
				if (!(table instanceof DatabaseView)) {
					if (properties.getAttributesMap(AttributeMapSet.ENTITYRELATIONS, table).isVisible()) {
						getSelectedTables().put(table, true);
					} else {
						getSelectedTables().put(table, false);
					}
				}
			}
		}
	}

	/**
	 * Initialize the current database connection and update the graph.
	 */
	@Override
	protected synchronized void updateGraph() { // Local ref-copy, because selectedTables of the class is out of scope
		// in the task.
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
						DatabaseERDModelGraphTransformer transformer;
						transformer = new DatabaseERDModelGraphTransformer(connection, includeList);
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
	private synchronized void setGraph(final DatabaseERDGraph graph) {
		setCurrentLayout(loadLayoutType(connection, graph));
		this.graph = graph;
		log.info("Setting a new GraphScene...");
		scene = new ERDGraphScene(this, graph, connection.getPropertiesObject());
		log.info("A new GraphScene was created.");

		setChanged();
		notifyObservers(Notification.NEW_GRAPH);
	}

	@Override
	protected AbstractDatabaseSelectorFrame getSelectorFrame() {
		return new DatabaseERDSelectorFrame(this);
	}

	/**
	 * This action updates the selected table and vertices states and forces updating the views. Only if graph has been
	 * added new vertices the graph has to be rebuild; otherwise it is only be repainted.
	 */
	public synchronized void actionUpdateSelections() {

		boolean atLeastOneVertexAdded = false;

		// viewer.getGraphLayout().getGraph().removeVertex(selectedTable);
		final ConnectionProperties properties = connection.getPropertiesObject();

		for (final DatabaseTable table : connection.getTables()) {
			final ConnectionAttributesMap attributes = properties.getAttributesMap(AttributeMapSet.ENTITYRELATIONS,
					table);

			// Only for trigger, ignore others.
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

		connection.setPropertiesObject(properties);

		if (saveSelections) {
			final int id = connection.getId();
			final DatabaseConnection connection2 = Controller.loadConnection(id, false);
			connection2.setPropertiesObject(properties);
			Controller.updateConnection(connection2);
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
		for (final DatabaseTable table : connection.getTables()) {
			final ConnectionAttributesMap attributes;
			attributes = properties.getAttributesMap(AttributeMapSet.ENTITYRELATIONS, table);
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
	public AttributeMapSet getAttributeMapSet() {
		return AttributeMapSet.ENTITYRELATIONS;
	}

	@Override
	protected ViewMode getViewMode() {
		return ViewMode.ERD;
	}

	@Override
	public LayoutType getDefaultLayout() {
		return LayoutType.FR_LAYOUT;
	}
}
