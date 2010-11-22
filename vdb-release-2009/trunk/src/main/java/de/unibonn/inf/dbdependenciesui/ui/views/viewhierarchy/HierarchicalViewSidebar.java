/**
 * $Id: Main.java 41 2009-03-01 16:21:20Z philipp $
 */
package de.unibonn.inf.dbdependenciesui.ui.views.viewhierarchy;

import java.util.Arrays;
import java.util.List;

import de.unibonn.inf.dbdependenciesui.ui.views.common.AbstractViewData;
import de.unibonn.inf.dbdependenciesui.ui.views.common.AbstractViewSidebar;
import de.unibonn.inf.dbdependenciesui.ui.views.common.AbstractViewData.Type;
import de.unibonn.inf.dbdependenciesui.ui.views.common.graph.AbstractGraphScene.LayoutType;

/**
 * The sidebar of the hierarchical view. The sidebar has the following sections: transformation actions, selection
 * actions and export actions.
 * 
 * @author Andre Kasper
 * @author Jan Philipp
 */
public class HierarchicalViewSidebar extends AbstractViewSidebar {
	protected static final long serialVersionUID = -8421982454496432274L;

	public HierarchicalViewSidebar() {
		initialize();
	}

	@Override
	protected AbstractViewData getViewData() {
		return HierarchicalViewData.getInstance(Type.DEFAULT);
	}

	@Override
	protected List<LayoutType> getAvailableLayouts() {
		return Arrays.asList(new LayoutType[] {
				LayoutType.DATABASE_FOREST_LAYOUT, LayoutType.DAG_LAYOUT, LayoutType.DEFAULT_TREELAYOUT,
				LayoutType.RADIAL_TREELAYOUT });
	}
}
