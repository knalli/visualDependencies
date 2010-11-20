package de.unibonn.inf.dbdependenciesui.ui.views.viewhierarchy.graph.transformers;

import java.awt.Shape;
import java.awt.geom.Rectangle2D;

import org.apache.commons.collections15.Transformer;

import de.unibonn.inf.dbdependenciesui.ui.views.common.graph.AbstractGraphScene;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.decorators.AbstractVertexShapeTransformer;

public class VertexShapeTransformer<V, E> extends AbstractVertexShapeTransformer<V> implements Transformer<V, Shape> {

	protected boolean stretch = false;
	protected boolean scale = false;
	protected Graph<V, E> graph;

	public VertexShapeTransformer(final Graph<V, E> graphIn, final AbstractGraphScene graphScene) {
		this.graph = graphIn;
		setSizeTransformer(new Transformer<V, Integer>() {

			public Integer transform(final V v) {
				return (int) (20 * (graphScene.getZoom() / 100));
			}
		});
		setAspectRatioTransformer(new Transformer<V, Float>() {

			public Float transform(final V v) {
				if (stretch) {
					return (float) (graph.inDegree(v) + 1) / (graph.outDegree(v) + 1);
				} else {
					return 1.0f;
				}
			}
		});
	}

	public void setStretching(final boolean stretch) {
		this.stretch = stretch;
	}

	public void setScaling(final boolean scale) {
		this.scale = scale;
	}

	public Shape transform(final V v) {
		final Rectangle2D result = factory.getRectangle(v);
		final double x = result.getX();
		final double y = result.getY();
		result.setRect(x, y, 200, 60);
		return result;
	}

}