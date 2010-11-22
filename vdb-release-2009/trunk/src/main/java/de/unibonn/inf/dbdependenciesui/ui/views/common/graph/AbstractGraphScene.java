package de.unibonn.inf.dbdependenciesui.ui.views.common.graph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import org.apache.commons.collections15.functors.ConstantTransformer;

import de.unibonn.inf.dbdependenciesui.graph.common.AbstractDatabaseGraph;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseObject;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.ConnectionProperties;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.Relation;
import de.unibonn.inf.dbdependenciesui.ui.views.common.AbstractViewData;
import de.unibonn.inf.dbdependenciesui.ui.views.common.graph.renderer.VertexRendererImpl;
import de.unibonn.inf.dbdependenciesui.ui.views.common.graph.transformers.AbstractEdgeToolTipTransformer;
import de.unibonn.inf.dbdependenciesui.ui.views.viewhierarchy.graph.transformers.EdgeLabelTransformer;
import de.unibonn.inf.dbdependenciesui.ui.views.viewhierarchy.graph.transformers.EdgeWeightStrokeFunction;
import de.unibonn.inf.dbdependenciesui.ui.views.viewhierarchy.graph.transformers.GradientPickedEdgePaintFunction;
import de.unibonn.inf.dbdependenciesui.ui.views.viewhierarchy.graph.transformers.VertexShapeTransformer;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.AbstractPopupGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.GraphMouseListener;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.control.SatelliteVisualizationViewer;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;
import edu.uci.ics.jung.visualization.decorators.DirectionalEdgeArrowTransformer;
import edu.uci.ics.jung.visualization.renderers.Renderer;

public abstract class AbstractGraphScene {
	protected static final long serialVersionUID = 6225413855637558386L;

	protected AbstractDatabaseGraph graph;
	protected VisualizationViewer<DatabaseObject, Relation> mainView;
	protected VisualizationViewer<DatabaseObject, Relation> satelliteView;
	protected Layout<DatabaseObject, Relation> layout;

	protected static final int GRADIENT_NONE = 0;
	protected static final int GRADIENT_RELATIVE = 1;
	protected static int gradient_level = AbstractGraphScene.GRADIENT_RELATIVE;

	protected final int sceneWidth = 800;
	protected final int sceneHeight = 600;

	protected final int satelliteWidth = 200;
	protected final int satelliteHeight = 200;

	protected final float zoomIn = 1.1f;

	protected final float zoomOut = 1 / 1.1f;

	protected final Color backgroundColor = Color.white;

	protected DefaultModalGraphMouse<Integer, String> graphMouse;

	protected float zoom = 100;
	protected ScalingControl scaler;
	protected AbstractViewData data;
	protected ConnectionProperties properties;
	protected Mode mode;
	protected final VertexRendererImpl<DatabaseObject, Relation> vertexRenderer = new VertexRendererImpl<DatabaseObject, Relation>();
	protected VertexShapeTransformer<DatabaseObject, Relation> vertexShapeTf;
	protected GradientPickedEdgePaintFunction edgeDrawPaint;
	protected GradientPickedEdgePaintFunction edgeFillPaint;
	protected EdgeWeightStrokeFunction<Relation> edgeStrokeTf;
	protected EdgeLabelTransformer edgeLabelTf;
	protected DirectionalEdgeArrowTransformer<DatabaseObject, Relation> edgeArrowTf;

	protected AbstractEdgeToolTipTransformer edgeToolTipTf;

	protected LayoutType layoutType;

	protected void initialize() {
		// The initializing steps: First, create the used layouter with the given graph.
		initializeLayout();

		// .. after that create the visualization views (main and its copy-of as satellit) ..
		createMainView();
		createSatelliteView();

		// .. create the transformer object we used later ..
		initializeTransformers();

		// .. modify the renderers and attach the initialized transformer objects (1)
		initializeMainViewRenderer();
		initializeMainViewRenderContext();
		initializeMouse();

		// .. modify the renderers and attach the initialized transformer objects (2)
		initializeSatelliteViewRenderer();
		initializeSatelliteViewRendererContext();
		initializeSatelliteScaling();
	}

	protected void reinitialize() {
		initialize();
	}

	/**
	 * This create all transformer objects used by jung as strategy pattern objects.
	 */
	protected void initializeTransformers() {
		// uses a gradient edge if unpicked, otherwise uses picked selection
		edgeDrawPaint = new GradientPickedEdgePaintFunction(mainView);
		edgeFillPaint = new GradientPickedEdgePaintFunction(mainView);
		edgeStrokeTf = new EdgeWeightStrokeFunction<Relation>();
		edgeLabelTf = new EdgeLabelTransformer(graph);
		edgeArrowTf = new DirectionalEdgeArrowTransformer<DatabaseObject, Relation>(18, 12, 2);

		// vertexShapeTf = new VertexShapeTransformer<DatabaseObject, Relation>(graph, this);
		vertexShapeTf = getVertexShapeTransformer();
		vertexShapeTf.setScaling(true);

		getEdgeToolTipTransformer();
	}

	protected abstract void getEdgeToolTipTransformer();

	// {
	// edgeToolTipTf = new EdgeToolTipTransformer();
	// }

	protected abstract VertexShapeTransformer<DatabaseObject, Relation> getVertexShapeTransformer();

	protected void initializeSatelliteScaling() {
		final ScalingControl satelliteScaler = new CrossoverScalingControl();
		satelliteView.scaleToLayout(satelliteScaler);
	}

	/**
	 * Initialize the special tree layout.
	 */
	protected abstract void initializeLayout();

	protected void createSatelliteView() {
		satelliteView = new SatelliteVisualizationViewer<DatabaseObject, Relation>(mainView, new Dimension(
				satelliteWidth, satelliteHeight));
	}

	/**
	 * Create a graph mouse and add it to the visualization component
	 */
	protected void initializeMouse() {
		graphMouse = new DefaultModalGraphMouse<Integer, String>(1.1f, 1 / 1.1f);
		graphMouse.setZoomAtMouse(true);
		setMouseTransformingMode();
		mainView.setGraphMouse(graphMouse);
		graphMouse.add(getPopupGraphMousePlugin());
	}

	protected abstract AbstractPopupGraphMousePlugin getPopupGraphMousePlugin();

	protected void initializeSatelliteViewRenderer() {
		final Renderer<DatabaseObject, Relation> renderer = satelliteView.getRenderer();
		renderer.setVertexRenderer(vertexRenderer);
	}

	@SuppressWarnings("unchecked")
	protected void initializeSatelliteViewRendererContext() {
		final RenderContext<DatabaseObject, Relation> renderContext;
		renderContext = satelliteView.getRenderContext();

		renderContext.setVertexShapeTransformer(vertexShapeTf);
		renderContext.setEdgeDrawPaintTransformer(edgeDrawPaint);
		renderContext.setEdgeStrokeTransformer(new ConstantTransformer(new BasicStroke(2.5f)));
		// renderContext.setEdgeShapeTransformer(new EdgeShape.Line<DatabaseObject, Relation>());
	}

	/**
	 * customize the renderer
	 */
	protected void initializeMainViewRenderer() {
		final Renderer<DatabaseObject, Relation> renderer;
		renderer = mainView.getRenderer();
		renderer.setVertexRenderer(vertexRenderer);
	}

	/**
	 * this class will provide both label drawing and vertex shapes
	 */
	@SuppressWarnings("unchecked")
	protected void initializeMainViewRenderContext() {
		final RenderContext<DatabaseObject, Relation> renderContext;
		renderContext = mainView.getRenderContext();

		renderContext.setArrowFillPaintTransformer(new ConstantTransformer(Color.darkGray));
		renderContext.setArrowDrawPaintTransformer(new ConstantTransformer(Color.lightGray));
		renderContext.setEdgeArrowTransformer(edgeArrowTf);
		// renderContext.setEdgeArrowStrokeTransformer(edgeArrowStrokeTf);

		renderContext.setVertexShapeTransformer(vertexShapeTf);
		renderContext.setEdgeDrawPaintTransformer(edgeDrawPaint);
		renderContext.setEdgeStrokeTransformer(edgeStrokeTf);
		// renderContext.setEdgeShapeTransformer(new EdgeShape.Line<DatabaseObject, Relation>());
		// renderContext.setEdgeFillPaintTransformer(edgeFillPaint);
		// renderContext.setEdgeLabelTransformer(edgeLabelTf);

		mainView.setEdgeToolTipTransformer(edgeToolTipTf);
	}

	/**
	 * Create the scene.
	 */
	protected void createMainView() {
		mainView = new VisualizationViewer<DatabaseObject, Relation>(layout, layout.getSize());
		setSceneSize(sceneWidth, sceneHeight);

		mainView.setOpaque(true);

		zoom = 100;
		scaler = new CrossoverScalingControl();

		final GraphMouseListener<DatabaseObject> mouseListener = new GraphMouseListener<DatabaseObject>() {

			@Override
			public void graphClicked(final DatabaseObject table, final MouseEvent e) {}

			@Override
			public void graphPressed(final DatabaseObject table, final MouseEvent e) {}

			@Override
			public void graphReleased(final DatabaseObject table, final MouseEvent e) {
				if (mode.equals(ModalGraphMouse.Mode.PICKING)) {
					data.actionUpdatePosition(table, e.getX(), e.getY());
				}
			}

		};

		final MouseWheelListener mouseWheelListener = new MouseWheelListener() {

			@Override
			public void mouseWheelMoved(final MouseWheelEvent e) {
				final int rotation = e.getWheelRotation();
				// final int amount = e.getScrollAmount();

				if (rotation == -1) {
					AbstractGraphScene.this.setZoom(zoom * zoomOut, true);
				} else if (rotation == 1) {
					AbstractGraphScene.this.setZoom(zoom * zoomIn, true);
				}
			}

		};

		mainView.addGraphMouseListener(mouseListener);
		mainView.addMouseWheelListener(mouseWheelListener);

		mainView.setBackground(backgroundColor);
	}

	/**
	 * Set a new scene size.
	 * 
	 * @param sceneWidth
	 * @param sceneHeight
	 */
	public void setSceneSize(final int sceneWidth, final int sceneHeight) {
		mainView.setSize(sceneWidth, sceneHeight);
		mainView.setPreferredSize(new Dimension(sceneWidth, sceneHeight));
	}

	/**
	 * Enable the transforming mouse mode. This mode will moves the complete graph in the scene while moving the mouse.
	 */
	public void setMouseTransformingMode() {
		mode = ModalGraphMouse.Mode.TRANSFORMING;
		mainView.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		graphMouse.setMode(ModalGraphMouse.Mode.TRANSFORMING);
	}

	/**
	 * Enable the transforming mouse mode. This mode will moves the current object in the scene while moving the mouse.
	 */
	public void setMousePickingMode() {
		mode = ModalGraphMouse.Mode.PICKING;
		mainView.setCursor(new Cursor(Cursor.HAND_CURSOR));
		graphMouse.setMode(ModalGraphMouse.Mode.PICKING);
	}

	/**
	 * Set a new zoom factor and update the scaling.
	 * 
	 * @param value
	 */
	public void setZoom(final float value) {
		this.setZoom(value, false);
	}

	/**
	 * Set a new zoom factor. Use updateOnlyValue for disabled scaling.
	 * 
	 * @param value
	 * @param updateOnlyValue
	 */
	public void setZoom(final float value, final boolean updateOnlyValue) {
		// Compute the scale-factor determines by the current and the new zoom
		// factor.
		final float scale = new Double(value / zoom).floatValue();
		if (!updateOnlyValue) {
			scaler.scale(mainView, scale, mainView.getCenter());
		}
		zoom = value;
		data.refreshZoom();
	}

	/**
	 * Get the current zoom factor.
	 * 
	 * @return
	 */
	public float getZoom() {
		return zoom;
	}

	protected Layout<DatabaseObject, Relation> getLayout() {
		return layout;
	}

	/**
	 * Get the main scene view (the actual graph).
	 * 
	 * @return
	 */
	public VisualizationViewer<DatabaseObject, Relation> getMainView() {
		return mainView;
	}

	/**
	 * Get the satellite view (small version of the graph).
	 * 
	 * @return
	 */
	public VisualizationViewer<DatabaseObject, Relation> getSatelliteView() {
		return satelliteView;
	}

	public void forceLayoutUpdate() {
		layout.reset();
	}

	public void setLayoutType(final LayoutType layoutType) {
		this.layoutType = layoutType;

		reinitialize();
	}

	public LayoutType getLayoutType() {
		return layoutType;
	}

	public AbstractDatabaseGraph getGraph() {
		return graph;
	}

	/**
	 * Internal layout type enum.
	 */
	public static enum LayoutType {
		DEFAULT_TREELAYOUT, DATABASE_FOREST_LAYOUT, DAG_LAYOUT, CIRCLE_LAYOUT, AGGREGATE_LAYOUT, FR_LAYOUT, FR_LAYOUT2, ISOM_LAYOUT, KK_LAYOUT, RADIAL_TREELAYOUT, SPRING_LAYOUT2;

		@Override
		public String toString() {
			switch (this) {
			case DATABASE_FOREST_LAYOUT:
				return "Level-Layout";
			case DAG_LAYOUT:
				return "DAG-Layout";
			case DEFAULT_TREELAYOUT:
				return "Tree-Layout";
			case CIRCLE_LAYOUT:
				return "Circle-Layout";
				// Ab hier nur zum ausprobieren
			case AGGREGATE_LAYOUT:
				return "Aggregate-Layout";
			case FR_LAYOUT:
				return "FR-Layout";
			case FR_LAYOUT2:
				return "FR-Layout2";
			case ISOM_LAYOUT:
				return "ISOM-Layout";
			case KK_LAYOUT:
				return "KK-Layout";
			case RADIAL_TREELAYOUT:
				return "Radial-Tree-Layout";
			case SPRING_LAYOUT2:
				return "Spring-Layout2";
			}
			return "UNKNOWN";
		}

	}
}
