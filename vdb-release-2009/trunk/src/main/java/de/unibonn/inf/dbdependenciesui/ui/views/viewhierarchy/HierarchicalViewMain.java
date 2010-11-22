/**
 * $Id: Main.java 41 2009-03-01 16:21:20Z philipp $
 */
package de.unibonn.inf.dbdependenciesui.ui.views.viewhierarchy;

import de.unibonn.inf.dbdependenciesui.ui.views.common.AbstractViewData;
import de.unibonn.inf.dbdependenciesui.ui.views.common.AbstractViewMain;
import de.unibonn.inf.dbdependenciesui.ui.views.common.AbstractViewData.Type;

/**
 * The main panel of the connection view displays all available connections and their contents. The layout constraints
 * are realized with BorderLayout. Because of that, all resize events will transport to all subcomponents.
 * 
 * @author Andre Kasper
 * @author Jan Philipp
 */
public class HierarchicalViewMain extends AbstractViewMain {
	private static final long serialVersionUID = -4396085450579578100L;

	public HierarchicalViewMain() {
		super.initialize();
	}

	@Override
	protected AbstractViewData getViewData() {
		return HierarchicalViewData.getInstance(Type.DEFAULT);
	}

}
