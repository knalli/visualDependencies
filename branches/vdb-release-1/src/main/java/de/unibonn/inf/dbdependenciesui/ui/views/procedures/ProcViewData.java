/**
 * $Id: Main.java 41 2009-03-01 16:21:20Z philipp $
 */
package de.unibonn.inf.dbdependenciesui.ui.views.procedures;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.unibonn.inf.dbdependenciesui.controller.Controller;
import de.unibonn.inf.dbdependenciesui.graph.procedures.DatabaseProcGraph;
import de.unibonn.inf.dbdependenciesui.graph.procedures.DatabaseProcModelGraphTransformer;
import de.unibonn.inf.dbdependenciesui.hibernate.HibernateDAOFactory;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseConnection;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseTable;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.ConnectionProperties;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.ConnectionProperties.AttributeMapSet;
import de.unibonn.inf.dbdependenciesui.misc.Internationalization;
import de.unibonn.inf.dbdependenciesui.ui.controller.ViewController;
import de.unibonn.inf.dbdependenciesui.ui.controller.ViewController.ViewMode;
import de.unibonn.inf.dbdependenciesui.ui.misc.WaitProgressWindow.WaitProgressTask;
import de.unibonn.inf.dbdependenciesui.ui.views.common.AbstractDatabaseSelectorFrame;
import de.unibonn.inf.dbdependenciesui.ui.views.common.AbstractViewData;
import de.unibonn.inf.dbdependenciesui.ui.views.common.graph.AbstractGraphScene.LayoutType;
import de.unibonn.inf.dbdependenciesui.ui.views.procedures.graph.ProcGraphScene;

/**
 * An observable model container for the hierarchiv views.
 * 
 * @author Andre Kasper
 * @author Jan Philipp
 */
public class ProcViewData extends AbstractViewData {

	/**
	 * instances of the multi-singletons
	 */
	private static Map<Type, ProcViewData> instances = new HashMap<Type, ProcViewData>();

	/**
	 * current database graph
	 */
	private DatabaseProcGraph graph;

	/**
	 * current DatabaseTable
	 */
	private DatabaseTable selectedTable;

	public synchronized static ProcViewData getInstance(final Type type) {
		if (!instances.containsKey(type)) {
			instances.put(type, new ProcViewData());
		}
		return instances.get(type);
	}

	protected ProcViewData() {
		super();
	}

	@Override
	protected AttributeMapSet getAttributeMapSet() {
		return AttributeMapSet.PROCEDURES;
	}

	@Override
	protected AbstractDatabaseSelectorFrame getSelectorFrame() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected ViewMode getViewMode() {
		return ViewMode.PROCEDURES;
	}

	@Override
	protected void initialize() {
		connection = ViewController.loadDatabaseConnection();
		final List<DatabaseTable> tables;
		tables = Controller.getTables(connection);
		selectedTable = tables.get(0);
	}

	@Override
	protected void resetObjectsPositions(final ConnectionProperties properties) {
	// TODO Auto-generated method stub

	}

	/**
	 * Set a new graph object. This will notifiy all observers.
	 * 
	 * @param graph
	 */
	private synchronized void setGraph(final DatabaseProcGraph graph) {
		setCurrentLayout(loadLayoutType(connection, graph));
		this.graph = graph;
		log.info("Setting a new GraphScene...");
		scene = new ProcGraphScene(this, graph, connection.getPropertiesObject());
		log.info("A new GraphScene was created.");

		setChanged();
		notifyObservers(Notification.NEW_GRAPH);
	}

	@Override
	protected void updateConnection(final int connectionId) {
		connection = Controller.loadConnection(connectionId, false);
	}

	@Override
	protected void updateGraph() {

		final DatabaseConnection connection = this.connection;
		// final ConnectionProperties properties = connection.getPropertiesObject();
		final DatabaseTable table = selectedTable;

		final WaitProgressTask task = new WaitProgressTask(ViewController.getApplicationView(), Internationalization
				.getText("application.graph.loading.title")) {

			@Override
			protected Void doInBackground() throws Exception {
				try {
					log.info("Starting (re) initializing the model-to-graph and display it.");

					if (connection != null) {

						log.info("Display Graph.");

						// final ConnectionAttributesMap attributes = properties.getAttributesMap(
						// AttributeMapSet.PROCEDURES, table);
						// attributes.clearPositions();
						// attributes.setLayout("");

						DatabaseProcModelGraphTransformer transformer;
						transformer = new DatabaseProcModelGraphTransformer(connection, table);
						ProcViewData.this.setGraph(transformer.getGraph());
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

	@Override
	public synchronized void actionUpdateLayout(final LayoutType layoutType) {
		super.actionUpdateLayout(layoutType);
	}

	@Override
	public synchronized void actionUpdateTable(final DatabaseTable table) {
		selectedTable = table;
		updateGraph();
	}

	public synchronized DatabaseConnection getConnection() {
		return connection == null ? ViewController.loadDatabaseConnection() : connection;
	}

	@Override
	public LayoutType getDefaultLayout() {
		return LayoutType.CIRCLE_LAYOUT;
	}
}
