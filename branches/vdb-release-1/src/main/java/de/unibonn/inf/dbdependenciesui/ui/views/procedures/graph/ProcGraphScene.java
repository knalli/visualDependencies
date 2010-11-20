package de.unibonn.inf.dbdependenciesui.ui.views.procedures.graph;

import de.unibonn.inf.dbdependenciesui.graph.procedures.DatabaseProcGraph;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseObject;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.ConnectionProperties;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.Relation;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.ConnectionProperties.AttributeMapSet;
import de.unibonn.inf.dbdependenciesui.ui.views.common.AbstractViewData;
import de.unibonn.inf.dbdependenciesui.ui.views.common.graph.AbstractGraphScene;
import de.unibonn.inf.dbdependenciesui.ui.views.common.graph.layout.GraphLayoutFactory;
import de.unibonn.inf.dbdependenciesui.ui.views.procedures.ProcViewData;
import de.unibonn.inf.dbdependenciesui.ui.views.procedures.graph.plugins.PopupMousePlugin;
import de.unibonn.inf.dbdependenciesui.ui.views.procedures.graph.transformers.ProcEdgeToolTipTransformer;
import de.unibonn.inf.dbdependenciesui.ui.views.viewhierarchy.graph.transformers.VertexShapeTransformer;
import edu.uci.ics.jung.visualization.control.AbstractPopupGraphMousePlugin;

public class ProcGraphScene extends AbstractGraphScene {

	public ProcGraphScene(final AbstractViewData data, final DatabaseProcGraph graph,
			final ConnectionProperties properties) {
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
		layout = GraphLayoutFactory.createLayout(layoutType, graph, 250, 250, properties, AttributeMapSet.PROCEDURES);
	}

	@Override
	protected void getEdgeToolTipTransformer() {
		edgeToolTipTf = new ProcEdgeToolTipTransformer();
	}

	@Override
	protected AbstractPopupGraphMousePlugin getPopupGraphMousePlugin() {
		return new PopupMousePlugin(vertexRenderer, (ProcViewData) data);
	}

	@Override
	protected VertexShapeTransformer<DatabaseObject, Relation> getVertexShapeTransformer() {
		return new VertexShapeTransformer<DatabaseObject, Relation>(graph, this);
	}

}
