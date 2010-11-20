package de.unibonn.inf.dbdependenciesui.ui.views.common.graph.layout;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.util.Collection;

import de.unibonn.inf.dbdependenciesui.graph.common.AbstractDatabaseGraph;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseObject;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.ConnectionAttributesMap;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.ConnectionProperties;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.Relation;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.ConnectionProperties.AttributeMapSet;
import de.unibonn.inf.dbdependenciesui.ui.views.common.graph.AbstractGraphScene.LayoutType;
import de.unibonn.inf.dbdependenciesui.ui.views.viewhierarchy.graph.layout.LeveledForestLayout;
import edu.uci.ics.jung.algorithms.layout.AggregateLayout;
import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.DAGLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout2;
import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import edu.uci.ics.jung.algorithms.layout.KKLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.RadialTreeLayout;
import edu.uci.ics.jung.algorithms.layout.SpringLayout2;
import edu.uci.ics.jung.algorithms.layout.TreeLayout;

/**
 * THis is the factory creating the layouts for graph visualizations.
 * 
 * @author Andre Kasper
 * @author Jan Philipp
 */
public class GraphLayoutFactory {
	/**
	 * Create and return a layout according to the given parameters.
	 * 
	 * @param type
	 *            specified the layout
	 * @param graph
	 *            the actual graph
	 * @param width
	 *            width of the whole scene
	 * @param height
	 *            height of the whole scene
	 * @param properties
	 *            connection properties with the positions
	 * @param category
	 *            special category (view)
	 * @return
	 */
	public static Layout<DatabaseObject, Relation> createLayout(final LayoutType type,
			final AbstractDatabaseGraph graph, final int width, final int height,
			final ConnectionProperties properties, final AttributeMapSet category) {
		Layout<DatabaseObject, Relation> layout = null;

		final Dimension dimension = new Dimension(width, height);

		switch (type) {
		case AGGREGATE_LAYOUT:
			layout = new AggregateLayout<DatabaseObject, Relation>(new CircleLayout<DatabaseObject, Relation>(graph));
			break;
		case CIRCLE_LAYOUT:
			final CircleLayout<DatabaseObject, Relation> circle = new CircleLayout<DatabaseObject, Relation>(graph);
			circle.setRadius(graph.getVertexCount() * 30 + 60);
			layout = circle;
			break;
		case DAG_LAYOUT:
			layout = new DAGLayout<DatabaseObject, Relation>(graph);
			break;
		case DATABASE_FOREST_LAYOUT:
			layout = new LeveledForestLayout(graph, width, height);
			break;
		case DEFAULT_TREELAYOUT:
			layout = new TreeLayout<DatabaseObject, Relation>(graph, width, height);
			break;
		case FR_LAYOUT:
			layout = new FRLayout<DatabaseObject, Relation>(graph, dimension);
			break;
		case FR_LAYOUT2:
			layout = new FRLayout2<DatabaseObject, Relation>(graph, dimension);
			break;
		case ISOM_LAYOUT:
			layout = new ISOMLayout<DatabaseObject, Relation>(graph);
			break;
		case KK_LAYOUT:
			layout = new KKLayout<DatabaseObject, Relation>(graph);
			break;
		case RADIAL_TREELAYOUT:
			layout = new RadialTreeLayout<DatabaseObject, Relation>(graph, 1, 1);
			break;
		case SPRING_LAYOUT2:
			layout = new SpringLayout2<DatabaseObject, Relation>(graph);
			break;
		}

		if (layout != null) {
			reloadAllVerticesPositions(layout, type, properties, category);
		}

		return layout;
	}

	/**
	 * This will reload all positions of all vertices in the layout's graph. It will not reload if the used layout type
	 * does not match the saved one. (the position of x/y in a circle is not valid for x/y in a tree).
	 * 
	 * @param layout
	 * @param type
	 * @param properties
	 * @param category
	 */
	private static void reloadAllVerticesPositions(final Layout<DatabaseObject, Relation> layout,
			final LayoutType type, final ConnectionProperties properties, final AttributeMapSet category) {
		final Collection<DatabaseObject> vertices = layout.getGraph().getVertices();
		if (vertices.isEmpty()) { return; }

		for (final DatabaseObject object : vertices) {
			// If special positions contained in properties' attributes map, use them.
			if (properties != null) {
				final ConnectionAttributesMap attributes = properties.getAttributesMap(category, object);
				final int newPosX = attributes.getPositionX();
				final int newPosY = attributes.getPositionY();
				final String oldLayout = attributes.getLayout();

				// Is this the same layout?
				if (oldLayout.isEmpty() || oldLayout.equalsIgnoreCase(type.toString())) {
					// Are this valid number values?
					if ((newPosX > 0) && (newPosY > 0)) {
						layout.setLocation(object, new Point2D.Double(newPosX, newPosY));
					}
				}
			}
		}
	}
}
