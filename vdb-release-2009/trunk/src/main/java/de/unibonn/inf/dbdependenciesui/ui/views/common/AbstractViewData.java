package de.unibonn.inf.dbdependenciesui.ui.views.common;

import java.util.Observable;
import java.util.Observer;
import java.util.logging.Logger;

import javax.swing.JComponent;

import de.unibonn.inf.dbdependenciesui.Configuration;
import de.unibonn.inf.dbdependenciesui.controller.Controller;
import de.unibonn.inf.dbdependenciesui.controller.ThreadExecutor;
import de.unibonn.inf.dbdependenciesui.graph.common.AbstractDatabaseGraph;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseConnection;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseObject;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseTable;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.ConnectionAttributesMap;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.ConnectionProperties;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.ConnectionProperties.AttributeMapSet;
import de.unibonn.inf.dbdependenciesui.ui.controller.ViewController;
import de.unibonn.inf.dbdependenciesui.ui.controller.ViewController.ViewMode;
import de.unibonn.inf.dbdependenciesui.ui.views.common.graph.AbstractGraphScene;
import de.unibonn.inf.dbdependenciesui.ui.views.common.graph.AbstractGraphScene.LayoutType;

public abstract class AbstractViewData extends Observable implements Observer {

	protected static final Logger log = Logger.getLogger(Configuration.LOGGER);

	/**
	 * current database connection
	 */
	protected DatabaseConnection connection = null;

	/**
	 * specify new positions should stored into the model
	 */
	protected boolean savePositions = true;

	/**
	 * specify new selections or diselections should stored into the model
	 */
	protected boolean saveSelections = true;

	/**
	 * If true, the swing components (graph etc) are invalid and have to be rebuild. This can happen, if a connection
	 * was changed while the hierarchy view mode was out of scope. This class member is not synchronized and should only
	 * read in awt thread.
	 */
	protected boolean viewInvalid = false;

	/**
	 * current graph component (scene)
	 */
	protected AbstractGraphScene scene;

	/**
	 * current database graph
	 */
	protected AbstractDatabaseGraph graph;

	private LayoutType currentLayout;

	protected AbstractViewData() {
		initialize();
		reinitializeConnectionAndGraph();
		ViewController.addObserverObject(this);
	}

	/**
	 * Internal initializer. Have to be implemented.
	 */
	protected abstract void initialize();

	/**
	 * Update the current connection with the new connectionId. This will execute a direct reloading of the connection.
	 * 
	 * @param connectionId
	 */
	protected abstract void updateConnection(final int connectionId);

	/**
	 * Return the view mode of this view data.
	 * 
	 * @return
	 */
	protected abstract ViewMode getViewMode();

	/**
	 * Return the corresponding selector frame with the graph objects (select and deselect the items in a list).
	 * 
	 * @return
	 */
	protected abstract AbstractDatabaseSelectorFrame getSelectorFrame();

	/**
	 * Reinitialize the graph.
	 */
	protected abstract void updateGraph();

	/**
	 * Remove all saved positions.
	 * 
	 * @param properties
	 */
	protected abstract void resetObjectsPositions(final ConnectionProperties properties);

	public synchronized void actionResetPositions() {
		final ConnectionProperties properties = connection.getPropertiesObject();

		resetObjectsPositions(properties);

		connection.setPropertiesObject(properties);

		if (saveSelections) {
			final int id = connection.getId();
			final DatabaseConnection connection = Controller.loadConnection(id, false);
			connection.setPropertiesObject(properties);
			Controller.updateConnection(connection);
		}

		updateGraph();
	}

	/**
	 * Return the graph.
	 * 
	 * @return
	 */
	public synchronized AbstractDatabaseGraph getGraph() {
		return graph;
	}

	/**
	 * Set the dimensional params width and height for the graph scene.
	 * 
	 * @param sceneWidth
	 * @param sceneHeight
	 */
	public synchronized void setSceneSize(final int sceneWidth, final int sceneHeight) {
		if (scene != null) {
			scene.setSceneSize(sceneWidth, sceneHeight);

			setChanged();
			notifyObservers(Notification.RESIZE_GRAPH);
		}
	}

	/**
	 * Return the main view of the graph.
	 * 
	 * @return
	 */
	public synchronized JComponent getGraphMainView() {
		return scene.getMainView();
	}

	/**
	 * Return the satellite view of the graph.
	 * 
	 * @return
	 */
	public synchronized JComponent getGraphSatelliteView() {
		return scene.getSatelliteView();
	}

	/**
	 * Return the layout type.
	 * 
	 * @return
	 */
	public synchronized LayoutType getLayoutType() {
		return scene.getLayoutType();
	}

	/**
	 * Update the zoom factor.
	 * 
	 * @param value
	 */
	public synchronized void actionUpdateZoom(final float value) {
		scene.setZoom(value);
	}

	/**
	 * Update the transform mode.
	 * 
	 * @param mode
	 */
	public synchronized void actionSetTransformMode(final boolean mode) {
		if (mode) {
			scene.setMouseTransformingMode();
		} else {
			scene.setMousePickingMode();
		}
	}

	public synchronized void actionSetSavePositions(final boolean selected) {
		savePositions = selected;
	}

	public synchronized void actionSetSaveSelections(final boolean selected) {
		saveSelections = selected;
	}

	/**
	 * Get current zoom factor.
	 * 
	 * @return
	 */
	public synchronized float getZoom() {
		return scene.getZoom();
	}

	public synchronized void actionUpdateLayout(final LayoutType layoutType) {
		scene.setLayoutType(layoutType);
		currentLayout = layoutType;

		setChanged();
		notifyObservers(Notification.NEW_GRAPH);
	}

	public synchronized void actionUpdateTable(final DatabaseTable table) {

	}

	public synchronized void actionExportAsImage() {
		ViewController.exportGraphAsImage(scene);
	}

	public void actionExportAsDot() {
		ViewController.exportGraphAsDot(scene);
	}

	public synchronized void actionChooseObjects() {
		AbstractDatabaseSelectorFrame frame;
		frame = getSelectorFrame();

		frame.setVisible(true);
	}

	/**
	 * Notify all observers zoom factor has changed.
	 */
	public synchronized void refreshZoom() {
		setChanged();
		notifyObservers(Notification.CHANGE_ZOOM);
	}

	/**
	 * Update the current position of the object.
	 * 
	 * @param table
	 * @param posX
	 * @param posY
	 */
	public synchronized void actionUpdatePosition(final DatabaseObject table, final int posX, final int posY) {

		final ConnectionProperties properties = connection.getPropertiesObject();
		final ConnectionAttributesMap attributes = properties.getAttributesMap(getAttributeMapSet(), table);

		attributes.setPositionX(posX);
		attributes.setPositionY(posY);
		attributes.setLayout(scene.getLayoutType().toString());

		if (savePositions) {
			final int id = connection.getId();
			final DatabaseConnection connection2 = Controller.loadConnection(id, false);
			connection2.setPropertiesObject(properties);
			Controller.updateConnection(connection2);

			connection.setPropertiesObject(properties);
		}
	}

	/**
	 * Return the catgory enum type used by graph properties. Example: return AttributeMapSet.DEFAULT;
	 */
	abstract protected AttributeMapSet getAttributeMapSet();

	/**
	 * Load the current selected connection and update the graph.
	 */
	protected void reinitializeConnectionAndGraph() {
		updateConnection(ViewController.getDatabaseConnection());
		updateGraph();
	}

	@Override
	public void update(final Observable o, final Object arg) {
		if ((arg != null) && (arg instanceof ViewController.Notification)) {
			final ViewController.Notification notification = (ViewController.Notification) arg;

			switch (notification) {
			// If a new connection is selected, this data bean load async the
			// new information ONLY if the view mode is getViewMode. Otherwise,
			// the update event will stored locally and will produce an update
			// on the next view change event.
			case VIEW_CHANGED:
				log.info("Notified about a view change.");
				if (isCurrentViewSelected() && viewInvalid) {
					ThreadExecutor.execute(new Runnable() {
						@Override
						public void run() {
							reinitializeConnectionAndGraph();
						}
					});
					viewInvalid = false;
				}
				break;
			case CONNECTION_SELECTED:
				log.info("Notified about a connection change.");
				if (ViewController.isValidConnectionSelected()) {
					if (isCurrentViewSelected()) {
						ThreadExecutor.execute(new Runnable() {
							@Override
							public void run() {
								reinitializeConnectionAndGraph();
							}
						});
					} else {
						viewInvalid = true;
					}
				} else {
					log.warning("Invalid connection selected.");
				}
			}
		}
	}

	/**
	 * @return
	 */
	private boolean isCurrentViewSelected() {
		return ViewController.getViewMode().equals(getViewMode());
	}

	/**
	 * Kinds of model types.
	 * 
	 * @author Andre Kasper
	 * @author Jan Philipp
	 */
	public static enum Type {
		DEFAULT
	}

	/**
	 * Kinds of notifications.
	 * 
	 * @author Andre Kasper
	 * @author Jan Philipp
	 */
	public static enum Notification {
		NEW_GRAPH, RESIZE_GRAPH, CHANGE_ZOOM
	}

	/**
	 * Return the default layout.
	 * 
	 * @return
	 */
	abstract public LayoutType getDefaultLayout();

	/**
	 * Return the current layout.
	 * 
	 * @return
	 */
	public LayoutType getCurrentLayout() {
		if (currentLayout == null) {
			currentLayout = getDefaultLayout();
		}
		return currentLayout;
	}

	/**
	 * Return the layout of the first vertex in the graph. The layout information of the first vertex is enough because
	 * all vertices share the same information about the used layout.
	 * 
	 * @param graph
	 * @param connection
	 * @return
	 */
	protected LayoutType loadLayoutType(final DatabaseConnection connection, final AbstractDatabaseGraph graph) {
		final ConnectionProperties properties = connection.getPropertiesObject();
		final DatabaseObject object = graph.getVertices().iterator().next();

		final ConnectionAttributesMap attributes = properties.getAttributesMap(getAttributeMapSet(), object);
		final String oldLayout = attributes.getLayout();
		if ((oldLayout == null) || oldLayout.isEmpty()) { return getDefaultLayout(); }

		for (final LayoutType layout : LayoutType.values()) {
			if (oldLayout.equalsIgnoreCase(layout.toString())) { return layout; }
		}

		return getDefaultLayout();
	}

	protected void setCurrentLayout(final LayoutType layout) {
		currentLayout = layout;
	}

}
