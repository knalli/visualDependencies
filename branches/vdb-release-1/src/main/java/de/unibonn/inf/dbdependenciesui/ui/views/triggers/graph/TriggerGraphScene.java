package de.unibonn.inf.dbdependenciesui.ui.views.triggers.graph;

import java.util.ArrayList;
import java.util.List;

import de.unibonn.inf.dbdependenciesui.graph.triggers.DatabaseTriggerGraph;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseObject;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.ConnectionProperties;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.Relation;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.ConnectionProperties.AttributeMapSet;
import de.unibonn.inf.dbdependenciesui.misc.Internationalization;
import de.unibonn.inf.dbdependenciesui.ui.views.common.graph.AbstractGraphScene;
import de.unibonn.inf.dbdependenciesui.ui.views.common.graph.layout.GraphLayoutFactory;
import de.unibonn.inf.dbdependenciesui.ui.views.triggers.TriggerViewData;
import de.unibonn.inf.dbdependenciesui.ui.views.triggers.graph.plugins.PopupMousePlugin;
import de.unibonn.inf.dbdependenciesui.ui.views.triggers.graph.transformers.TriggerEdgeToolTipTransformer;
import de.unibonn.inf.dbdependenciesui.ui.views.viewhierarchy.graph.transformers.VertexShapeTransformer;
import edu.uci.ics.jung.visualization.control.AbstractPopupGraphMousePlugin;

public class TriggerGraphScene extends AbstractGraphScene {

	protected final List<DatabaseObject> visitedVertexes = new ArrayList<DatabaseObject>();
	protected final List<String> recursiveTriggerNames = new ArrayList<String>();

	public TriggerGraphScene(final TriggerViewData data, final DatabaseTriggerGraph graph,
			final ConnectionProperties properties) {
		layoutType = data.getCurrentLayout();
		this.data = data;
		this.graph = graph;
		this.properties = properties;
		initialize();
		initializeRecursiveTrigger();
	}

	/**
	 * Initialize the special tree layout.
	 */
	@Override
	protected void initializeLayout() {
		layout = GraphLayoutFactory.createLayout(layoutType, graph, 250, 250, properties, AttributeMapSet.TRIGGERS);
	}

	@Override
	protected AbstractPopupGraphMousePlugin getPopupGraphMousePlugin() {
		return new PopupMousePlugin(vertexRenderer, (TriggerViewData) data);
	}

	@Override
	protected VertexShapeTransformer<DatabaseObject, Relation> getVertexShapeTransformer() {
		return new VertexShapeTransformer<DatabaseObject, Relation>(graph, this);
	}

	@Override
	protected void getEdgeToolTipTransformer() {
		edgeToolTipTf = new TriggerEdgeToolTipTransformer();
	}

	public void setRecursiveTrigger(final List<String> triggerName) {
		vertexRenderer.setRecursiveTrigger(triggerName);
	}

	public void initializeRecursiveTrigger() {
		for (final DatabaseObject object : graph.getVertices()) {
			visitedVertexes.clear();
			testRecursion(object, null);
		}
		setRecursiveTrigger(recursiveTriggerNames);
	}

	private boolean testRecursion(final DatabaseObject start, DatabaseObject actual) {
		if (start.equals(actual)) {
			return true;
		} else {
			// actual erst hier setzen, da sonst die if-abfrage am Anfang direkt true ergibt und abbricht
			if (actual == null) {
				actual = start;
			}
			// Wenn es keine eingehenden Kanten gibt, dann auch keine Rekursion :D
			if (graph.getInEdges(actual) == null) { return false; }
			// Wenn es keine ausgehenden Kanten gibt, dann auch keine Rekursion :D
			if (graph.getOutEdges(actual) != null) {
				for (final Relation rel : graph.getOutEdges(actual)) {
					actual = graph.getDest(rel);
					// Wichtig, da sonst kein Abbruch der Rekursion
					if (visitedVertexes.contains(actual)) {
						return false;
					} else {
						// Besuchten Knoten merken
						visitedVertexes.add(actual);
					}
					// Wenn true, dann Rekursion bei Trigger gefunden
					if (testRecursion(start, actual)) {
						if (rel.isPositive()) {
							rel.setCondition(rel.getCondition() + " "
									+ Internationalization.getText("application.graph.trigger.edge.tooltip"));
						}
						rel.setPositive(false);
						recursiveTriggerNames.add(actual.getTitle());
						return true;
					}
				}
			}
		}
		return false;
	}
}
