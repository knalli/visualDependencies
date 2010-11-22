package de.unibonn.inf.dbdependenciesui.ui.views.triggers;

import java.util.Arrays;
import java.util.List;

import de.unibonn.inf.dbdependenciesui.ui.views.common.AbstractViewData;
import de.unibonn.inf.dbdependenciesui.ui.views.common.AbstractViewSidebar;
import de.unibonn.inf.dbdependenciesui.ui.views.common.AbstractViewData.Type;
import de.unibonn.inf.dbdependenciesui.ui.views.common.graph.AbstractGraphScene.LayoutType;

/**
 * This sidebar component is designed for the trigger-graph view.
 * 
 * @author Andre Kasper
 * @author Jan Philipp
 */
public class TriggerViewSidebar extends AbstractViewSidebar {
	private static final long serialVersionUID = 1913760645959058168L;

	public TriggerViewSidebar() {
		initialize();
	}

	@Override
	protected AbstractViewData getViewData() {
		return TriggerViewData.getInstance(Type.DEFAULT);
	}

	@Override
	protected List<LayoutType> getAvailableLayouts() {
		return Arrays.asList(new LayoutType[] {
				LayoutType.AGGREGATE_LAYOUT, LayoutType.CIRCLE_LAYOUT, LayoutType.FR_LAYOUT, LayoutType.FR_LAYOUT2,
				LayoutType.ISOM_LAYOUT, LayoutType.KK_LAYOUT });
	}
}
