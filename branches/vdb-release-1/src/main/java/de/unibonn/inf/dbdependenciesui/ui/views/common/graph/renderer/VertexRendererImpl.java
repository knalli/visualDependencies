package de.unibonn.inf.dbdependenciesui.ui.views.common.graph.renderer;

import java.awt.Dimension;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JComponent;

import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseObject;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Context;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.renderers.Renderer.Vertex;
import edu.uci.ics.jung.visualization.transform.MutableTransformer;
import edu.uci.ics.jung.visualization.transform.MutableTransformerDecorator;
import edu.uci.ics.jung.visualization.transform.shape.GraphicsDecorator;

/**
 * This is the renderer implmentation with a special {@link DatabaseObjectComponent} plugin. The actual paint process is
 * delegated to this object.
 * 
 * @author Andre Kasper
 * @author Jan Philipp
 */
public class VertexRendererImpl<V, E> implements Vertex<V, E> {

	private final DatabaseObjectComponent componentRenderer = new DatabaseObjectComponent();

	private final Map<DatabaseObject, DatabaseObjectComponent> maps = new HashMap<DatabaseObject, DatabaseObjectComponent>();

	/**
	 * Test and repaint the vertex behind the given context.
	 */
	@Override
	public void paintVertex(final RenderContext<V, E> rc, final Layout<V, E> layout, final V v) {
		final Graph<V, E> graph = layout.getGraph();
		if (rc.getVertexIncludePredicate().evaluate(Context.<Graph<V, E>, V> getInstance(graph, v))) {
			paintIconForVertex(rc, v, layout);
		}
	}

	/**
	 * Paint <code>v</code>'s icon on <code>g</code> at <code>(x,y)</code>.
	 */
	protected void paintIconForVertex(final RenderContext<V, E> rc, final V v, final Layout<V, E> layout) {
		final GraphicsDecorator g = rc.getGraphicsContext();
		boolean vertexHit = true;
		// get the shape to be rendered
		Shape shape = rc.getVertexShapeTransformer().transform(v);

		Point2D p = layout.transform(v);
		p = rc.getMultiLayerTransformer().transform(Layer.LAYOUT, p);
		final float x = (float) p.getX();
		final float y = (float) p.getY();
		// create a transform that translates to the location of
		// the vertex to be rendered
		final AffineTransform xform = AffineTransform.getTranslateInstance(x, y);
		// transform the vertex shape with xtransform
		shape = xform.createTransformedShape(shape);

		vertexHit = vertexHit(rc, shape);
		// rc.getViewTransformer().transform(shape).intersects(deviceRectangle);

		if (vertexHit) {
			if (rc.getVertexIconTransformer() != null) {
				final Icon icon = rc.getVertexIconTransformer().transform(v);
				if (icon != null) {
					g.draw(icon, rc.getScreenDevice(), shape, (int) x, (int) y);
				} else {
					paintShapeForVertex(rc, v, shape);
				}
			} else {
				paintShapeForVertex(rc, v, shape);
			}
		}
	}

	protected boolean vertexHit(final RenderContext<V, E> rc, final Shape s) {
		final JComponent vv = rc.getScreenDevice();
		Rectangle deviceRectangle = null;
		if (vv != null) {
			final Dimension d = vv.getSize();
			deviceRectangle = new Rectangle(0, 0, d.width, d.height);
		}
		MutableTransformer vt = rc.getMultiLayerTransformer().getTransformer(Layer.VIEW);
		if (vt instanceof MutableTransformerDecorator) {
			vt = ((MutableTransformerDecorator) vt).getDelegate();
		}
		return vt.transform(s).intersects(deviceRectangle);
	}

	/**
	 * Invoke the actual paint process of a vertex.
	 * 
	 * @param rc
	 * @param v
	 * @param shape
	 */
	protected void paintShapeForVertex(final RenderContext<V, E> rc, final V v, final Shape shape) {
		final GraphicsDecorator g = rc.getGraphicsContext();
		final Paint oldPaint = g.getPaint();
		final Paint fillPaint = rc.getVertexFillPaintTransformer().transform(v);
		if (fillPaint != null) {
			g.setPaint(fillPaint);
			g.fill(shape);
			g.setPaint(oldPaint);
		}
		final Paint drawPaint = rc.getVertexDrawPaintTransformer().transform(v);
		if (drawPaint != null) {
			g.setPaint(drawPaint);
			final Stroke oldStroke = g.getStroke();
			final Stroke stroke = rc.getVertexStrokeTransformer().transform(v);
			if (stroke != null) {
				g.setStroke(stroke);
			}
			g.draw(shape);
			g.setPaint(oldPaint);
			g.setStroke(oldStroke);

			// My own painter
			drawDatabaseTable(rc, v, shape);
		}
	}

	protected void drawDatabaseTable(final RenderContext<V, E> rc, final V v, final Shape shape) {
		final GraphicsDecorator g = rc.getGraphicsContext();

		final DatabaseObject obj = getUserObject(v);

		final Rectangle2D rect = shape.getBounds2D();
		final double x = rect.getX();
		final double y = rect.getY();
		final double width = rect.getWidth();
		final double height = rect.getHeight();
		@SuppressWarnings("unused")
		final double centerX = rect.getCenterX();
		@SuppressWarnings("unused")
		final double centerY = rect.getCenterY();

		// Do not create new objects because we reuse it (FLYWEIGHT).
		// componentRenderer = getComponentByUserObject(obj);

		componentRenderer.setPreferredSize(new Dimension((int) width, (int) height));
		componentRenderer.setUserObject(obj, rc.getPickedVertexState().isPicked(v));

		g.draw(componentRenderer, rc.getRendererPane(), (int) x, (int) y, (int) width, (int) height, true);
	}

	/**
	 * Return the given object unless it is null or create a new object if it is null.
	 * 
	 * @param v
	 * @return
	 */
	protected DatabaseObject getUserObject(final V v) {
		DatabaseObject userObject = null;
		if (v instanceof DatabaseObject) {
			userObject = (DatabaseObject) v;
		} else {
			userObject = new DatabaseObject();
			userObject.setTitle("Unkown object");
		}
		return userObject;
	}

	/**
	 * Create and return an object of the type {@link DatabaseObjectComponent} for the given userObject.
	 * 
	 * @param userObject
	 * @return
	 */
	protected DatabaseObjectComponent getComponentByUserObject(final DatabaseObject userObject) {
		if (!maps.containsKey(userObject)) {
			maps.put(userObject, new DatabaseObjectComponent());
		}
		return maps.get(userObject);
	}

	public void setRecursiveTrigger(final List<String> triggerNames) {
		componentRenderer.setRecursiveTrigger(triggerNames);
	}
}
