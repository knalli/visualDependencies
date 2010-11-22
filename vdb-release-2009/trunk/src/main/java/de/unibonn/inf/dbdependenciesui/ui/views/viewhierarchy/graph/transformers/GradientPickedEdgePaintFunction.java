package de.unibonn.inf.dbdependenciesui.ui.views.viewhierarchy.graph.transformers;

import java.awt.Color;
import java.awt.Paint;

import org.apache.commons.collections15.Predicate;

import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseObject;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.Relation;
import edu.uci.ics.jung.algorithms.util.SelfLoopEdgePredicate;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Context;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.decorators.GradientEdgePaintTransformer;

public class GradientPickedEdgePaintFunction extends GradientEdgePaintTransformer<DatabaseObject, Relation> {
	protected boolean fillEdge = false;
	Predicate<Context<Graph<DatabaseObject, Relation>, Relation>> selfLoop = new SelfLoopEdgePredicate<DatabaseObject, Relation>();

	private static final Color positiveColor1 = Color.green.brighter().brighter();
	private static final Color positiveColor1Picked = positiveColor1.brighter().brighter();
	private static final Color positiveColor2 = Color.green.darker().darker();
	private static final Color positiveColor2Picked = positiveColor2.darker().darker();

	private static final Color negativeColor1 = Color.red.brighter().brighter();
	private static final Color negativeColor1Picked = negativeColor1.brighter().brighter();
	private static final Color negativeColor2 = Color.red.darker().darker();
	private static final Color negativeColor2Picked = negativeColor2.darker().darker();

	public GradientPickedEdgePaintFunction(final VisualizationViewer<DatabaseObject, Relation> vv) {
		super(Color.WHITE, Color.BLACK, vv);
	}

	public void useFill(final boolean b) {
		fillEdge = b;
	}

	@Override
	public Paint transform(final Relation e) {
		if (vv.getPickedEdgeState().isPicked(e)) {
			c1 = e.isPositive() ? positiveColor1Picked : negativeColor1Picked;
			c2 = e.isPositive() ? positiveColor2Picked : negativeColor2Picked;
		} else {
			c1 = e.isPositive() ? positiveColor1 : negativeColor1;
			c2 = e.isPositive() ? positiveColor2 : negativeColor2;
		}

		return super.transform(e);
	}
}
