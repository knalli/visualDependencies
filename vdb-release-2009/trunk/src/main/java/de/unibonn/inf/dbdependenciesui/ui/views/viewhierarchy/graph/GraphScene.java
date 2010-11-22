package de.unibonn.inf.dbdependenciesui.ui.views.viewhierarchy.graph;

import de.unibonn.inf.dbdependenciesui.graph.viewhierarchy.DatabaseGraph;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseObject;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.ConnectionProperties;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.Relation;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.ConnectionProperties.AttributeMapSet;
import de.unibonn.inf.dbdependenciesui.ui.views.common.graph.AbstractGraphScene;
import de.unibonn.inf.dbdependenciesui.ui.views.common.graph.layout.GraphLayoutFactory;
import de.unibonn.inf.dbdependenciesui.ui.views.viewhierarchy.HierarchicalViewData;
import de.unibonn.inf.dbdependenciesui.ui.views.viewhierarchy.graph.layout.LeveledForestLayout;
import de.unibonn.inf.dbdependenciesui.ui.views.viewhierarchy.graph.plugins.PopupMousePlugin;
import de.unibonn.inf.dbdependenciesui.ui.views.viewhierarchy.graph.transformers.EdgeToolTipTransformer;
import de.unibonn.inf.dbdependenciesui.ui.views.viewhierarchy.graph.transformers.VertexShapeTransformer;

/**
 * This is the wrapper class for the visualisation in JUNG 2. The main components of a visualization are: the main view
 * (see {@link #getMainView()}), the actual graph (see
 * {@link #GraphScene(HierarchicalViewData, DatabaseGraph, ConnectionProperties)}) and the used layout (see
 * #getLayout()).
 * 
 * @author Andre Kasper
 * @author Jan Philipp
 * @use {@link LeveledForestLayout}, {@link DatabaseGraph}
 */
public class GraphScene extends AbstractGraphScene {

	public GraphScene(final HierarchicalViewData data, final DatabaseGraph graph, final ConnectionProperties properties) {
		layoutType = data.getCurrentLayout();
		this.data = data;
		this.graph = graph;
		this.properties = properties;
		initialize();
	}

	/**
	 * Initialize the special tree layout.
	 */
	@Override
	protected void initializeLayout() {
		layout = GraphLayoutFactory.createLayout(layoutType, graph, 250, 250, properties, AttributeMapSet.VIEWS);
	}

	@Override
	protected PopupMousePlugin getPopupGraphMousePlugin() {
		return new PopupMousePlugin(vertexRenderer, (HierarchicalViewData) data);
	}

	@Override
	protected VertexShapeTransformer<DatabaseObject, Relation> getVertexShapeTransformer() {
		return new VertexShapeTransformer<DatabaseObject, Relation>(graph, this);
	}

	@Override
	protected void getEdgeToolTipTransformer() {
		edgeToolTipTf = new EdgeToolTipTransformer();
	}

}
