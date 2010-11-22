package de.unibonn.inf.dbdependenciesui.ui.views.entityrelations.graph;

import de.unibonn.inf.dbdependenciesui.graph.entityrelations.DatabaseERDGraph;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseObject;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.ConnectionProperties;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.Relation;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.ConnectionProperties.AttributeMapSet;
import de.unibonn.inf.dbdependenciesui.ui.views.common.AbstractViewData;
import de.unibonn.inf.dbdependenciesui.ui.views.common.graph.AbstractGraphScene;
import de.unibonn.inf.dbdependenciesui.ui.views.common.graph.layout.GraphLayoutFactory;
import de.unibonn.inf.dbdependenciesui.ui.views.entityrelations.ERDViewData;
import de.unibonn.inf.dbdependenciesui.ui.views.entityrelations.graph.plugins.PopupMousePlugin;
import de.unibonn.inf.dbdependenciesui.ui.views.entityrelations.graph.transformers.ERDEdgeToolTipTransformer;
import de.unibonn.inf.dbdependenciesui.ui.views.viewhierarchy.graph.transformers.VertexShapeTransformer;
import edu.uci.ics.jung.visualization.control.AbstractPopupGraphMousePlugin;

public class ERDGraphScene extends AbstractGraphScene {

	public ERDGraphScene(final AbstractViewData data, final DatabaseERDGraph graph, final ConnectionProperties properties) {
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
		layout = GraphLayoutFactory.createLayout(layoutType, graph, 250, 250, properties,
				AttributeMapSet.ENTITYRELATIONS);
	}

	@Override
	protected AbstractPopupGraphMousePlugin getPopupGraphMousePlugin() {
		return new PopupMousePlugin(vertexRenderer, (ERDViewData) data);
	}

	@Override
	protected VertexShapeTransformer<DatabaseObject, Relation> getVertexShapeTransformer() {
		return new VertexShapeTransformer<DatabaseObject, Relation>(graph, this);
	}

	@Override
	protected void getEdgeToolTipTransformer() {
		edgeToolTipTf = new ERDEdgeToolTipTransformer();
	}

}
