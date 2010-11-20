/**
 * $Id: Main.java 41 2009-03-01 16:21:20Z philipp $
 */
package de.unibonn.inf.dbdependenciesui.ui.views.triggers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.unibonn.inf.dbdependenciesui.controller.Controller;
import de.unibonn.inf.dbdependenciesui.graph.triggers.DatabaseTriggerGraph;
import de.unibonn.inf.dbdependenciesui.graph.triggers.DatabaseTriggerModelGraphTransformer;
import de.unibonn.inf.dbdependenciesui.hibernate.HibernateDAOFactory;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseConnection;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseTrigger;
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
import de.unibonn.inf.dbdependenciesui.ui.views.triggers.graph.TriggerGraphScene;

/**
 * An observable model container for the hierarchiv views.
 * 
 * @author Andre Kasper
 * @author Jan Philipp
 */
public class TriggerViewData extends AbstractViewData {

	/**
	 * instances of the multi-singletons
	 */
	private static Map<Type, TriggerViewData> instances = new HashMap<Type, TriggerViewData>();

	/**
	 * current database graph
	 */
	protected DatabaseTriggerGraph graph;

	/**
	 * current selected tables and views
	 */
	protected Map<DatabaseTrigger, Boolean> selectedTrigger;

	/**
	 * Return and create a multi-singleton-pattern-like instance.
	 * 
	 * @param type
	 * @return
	 */
	public synchronized static TriggerViewData getInstance(final Type type) {
		if (!instances.containsKey(type)) {
			instances.put(type, new TriggerViewData());
		}
		return instances.get(type);
	}

	protected TriggerViewData() {
		super();
	}

	@Override
	protected void initialize() {
		getSelectedTrigger();
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
			getSelectedTrigger().clear();

			final List<DatabaseTrigger> triggers;
			triggers = Controller.getTrigger(connection);

			final ConnectionProperties properties = connection.getPropertiesObject();

			for (final DatabaseTrigger trigger : triggers) {
				if (properties.getAttributesMap(AttributeMapSet.TRIGGERS, trigger).isVisible()) {
					getSelectedTrigger().put(trigger, true);
				} else {
					getSelectedTrigger().put(trigger, false);
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
		final Map<DatabaseTrigger, Boolean> selectedTrigger = getSelectedTrigger();
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
						for (final DatabaseTrigger trigger : selectedTrigger.keySet()) {
							if (selectedTrigger.get(trigger).booleanValue()) {
								includeList.add(trigger.getTitle());
							}
						}
						DatabaseTriggerModelGraphTransformer transformer;
						transformer = new DatabaseTriggerModelGraphTransformer(connection, includeList);
						TriggerViewData.this.setGraph(transformer.getGraph());
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
	private synchronized void setGraph(final DatabaseTriggerGraph graph) {
		setCurrentLayout(loadLayoutType(connection, graph));
		this.graph = graph;
		log.info("Setting a new GraphScene...");
		scene = new TriggerGraphScene(this, graph, connection.getPropertiesObject());
		log.info("A new GraphScene was created.");

		setChanged();
		notifyObservers(Notification.NEW_GRAPH);
	}

	@Override
	protected AbstractDatabaseSelectorFrame getSelectorFrame() {
		return new DatabaseTriggerSelectorFrame(this);
	}

	/**
	 * This action updates the selected table and vertices states and forces updating the views. Only if graph has been
	 * added new vertices the graph has to be rebuild; otherwise it is only be repainted.
	 */
	public synchronized void actionUpdateSelections() {

		boolean atLeastOneVertexAdded = false;

		// viewer.getGraphLayout().getGraph().removeVertex(selectedTable);
		final ConnectionProperties properties = connection.getPropertiesObject();

		for (final DatabaseTrigger trigger : connection.getTriggers()) {
			final ConnectionAttributesMap attributes = properties.getAttributesMap(AttributeMapSet.TRIGGERS, trigger);

			// Only for trigger, ignore others.
			if (getSelectedTrigger().containsKey(trigger)) {
				// Change state if it was hidden and should be visible now. Otherwise remove the vertex.
				if (!attributes.isVisible() && getSelectedTrigger().get(trigger)) {
					atLeastOneVertexAdded = true;
				} else if (attributes.isVisible() && !getSelectedTrigger().get(trigger)) {
					graph.removeVertex(trigger);
				}
				attributes.setVisible(getSelectedTrigger().get(trigger));
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

		for (final DatabaseTrigger trigger : connection.getTriggers()) {
			properties.getAttributesMap(getAttributeMapSet(), trigger).setLayout(layoutType.toString());
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
		for (final DatabaseTrigger trigger : connection.getTriggers()) {
			final ConnectionAttributesMap attributes;
			attributes = properties.getAttributesMap(AttributeMapSet.TRIGGERS, trigger);
			attributes.clearPositions();
			attributes.setLayout("");
		}
	}

	public synchronized Map<DatabaseTrigger, Boolean> getSelectedTrigger() {
		if (selectedTrigger == null) {
			selectedTrigger = new HashMap<DatabaseTrigger, Boolean>();
		}
		return selectedTrigger;
	}

	/**
	 * Get the number of tables of the current connection.
	 * 
	 * @return
	 */
	public synchronized int getNumberOfTrigger() {
		return connection.getTriggers().size();
	}

	/**
	 * Get the number of selected tables/views.
	 * 
	 * @return
	 */
	public synchronized int getNumberOfSelected() {
		int count = 0;

		for (final boolean selected : getSelectedTrigger().values()) {
			if (selected) {
				++count;
			}
		}

		return count;
	}

	@Override
	protected AttributeMapSet getAttributeMapSet() {
		return AttributeMapSet.TRIGGERS;
	}

	@Override
	protected ViewMode getViewMode() {
		return ViewMode.TRIGGERS;
	}

	@Override
	public LayoutType getDefaultLayout() {
		return LayoutType.AGGREGATE_LAYOUT;
	}
}
