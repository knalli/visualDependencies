package de.unibonn.inf.dbdependenciesui.ui.views.viewhierarchy.graph.transformers;

import java.awt.BasicStroke;
import java.awt.Stroke;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.visualization.RenderContext;

public class EdgeWeightStrokeFunction<E> implements Transformer<E, Stroke> {
	protected static final Stroke basic = new BasicStroke(1);
	protected static final Stroke heavy = new BasicStroke(4);
	protected static final Stroke dotted = RenderContext.DOTTED;

	protected boolean weighted = true;

	public EdgeWeightStrokeFunction() {}

	public void setWeighted(final boolean weighted) {
		this.weighted = weighted;
	}

	public Stroke transform(final E e) {
		if (weighted) {
			if (drawHeavy(e)) {
				return EdgeWeightStrokeFunction.heavy;
			} else {
				return EdgeWeightStrokeFunction.dotted;
			}
		} else {
			return EdgeWeightStrokeFunction.basic;
		}
	}

	protected boolean drawHeavy(final E e) {
		return true;
	}

}