package de.unibonn.inf.dbdependenciesui.ui.views.procedures.graph.plugins;

import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;

import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseObject;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseTable;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.Relation;
import de.unibonn.inf.dbdependenciesui.ui.views.common.graph.renderer.VertexRendererImpl;
import de.unibonn.inf.dbdependenciesui.ui.views.procedures.ProcViewData;
import edu.uci.ics.jung.algorithms.layout.GraphElementAccessor;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.AbstractPopupGraphMousePlugin;

public class PopupMousePlugin extends AbstractPopupGraphMousePlugin implements MouseListener {

	protected ProcViewData data;

	protected boolean enableVertexPopupMenu = true;

	public PopupMousePlugin(final VertexRendererImpl<DatabaseObject, Relation> vertexRenderer, final ProcViewData data) {
		this(vertexRenderer, InputEvent.BUTTON3_MASK);
		this.data = data;
	}

	public PopupMousePlugin(final VertexRendererImpl<DatabaseObject, Relation> vertexRenderer, final int modifiers) {
		super(modifiers);
	}

	/**
	 * If this event is over a Vertex, pop up a menu to allow the user to increase/decrease the voltage attribute of
	 * this Vertex
	 * 
	 * @param e
	 */
	@Override
	@SuppressWarnings("unchecked")
	protected void handlePopup(final MouseEvent e) {
		final VisualizationViewer<DatabaseTable, Relation> vv = (VisualizationViewer<DatabaseTable, Relation>) e
				.getSource();
		final Point2D p = e.getPoint();// vv.getRenderContext().getBasicTransformer().inverseViewTransform(e.getPoint());

		final GraphElementAccessor<DatabaseTable, Relation> pickSupport = vv.getPickSupport();
		if (pickSupport != null) {

			if (enableVertexPopupMenu) {
				final DatabaseObject vertex = pickSupport.getVertex(vv.getGraphLayout(), p.getX(), p.getY());
				if (vertex != null) {
					new PopupMenu(vv, vertex, data).show(vv, e.getX(), e.getY());
				}
			}
		}
	}

}
