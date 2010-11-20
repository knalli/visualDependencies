package de.unibonn.inf.dbdependenciesui.ui.views.entityrelations;

import java.util.Arrays;
import java.util.List;

import de.unibonn.inf.dbdependenciesui.ui.views.common.AbstractViewData;
import de.unibonn.inf.dbdependenciesui.ui.views.common.AbstractViewSidebar;
import de.unibonn.inf.dbdependenciesui.ui.views.common.AbstractViewData.Type;
import de.unibonn.inf.dbdependenciesui.ui.views.common.graph.AbstractGraphScene.LayoutType;

/**
 * This sidebar component is designed for the entity-relational-graph view.
 * 
 * @author Andre Kasper
 * @author Jan Philipp
 */
public class ERDViewSidebar extends AbstractViewSidebar {
	private static final long serialVersionUID = 1913760645959058168L;

	public ERDViewSidebar() {
		initialize();
	}

	@Override
	protected AbstractViewData getViewData() {
		return ERDViewData.getInstance(Type.DEFAULT);
	}

	@Override
	protected List<LayoutType> getAvailableLayouts() {
		return Arrays.asList(new LayoutType[] {
				LayoutType.AGGREGATE_LAYOUT, LayoutType.CIRCLE_LAYOUT, LayoutType.FR_LAYOUT, LayoutType.FR_LAYOUT2,
				LayoutType.ISOM_LAYOUT, LayoutType.KK_LAYOUT });
	}
}
