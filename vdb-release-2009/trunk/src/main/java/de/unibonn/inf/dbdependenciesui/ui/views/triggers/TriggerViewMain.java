package de.unibonn.inf.dbdependenciesui.ui.views.triggers;

import de.unibonn.inf.dbdependenciesui.ui.views.common.AbstractViewData;
import de.unibonn.inf.dbdependenciesui.ui.views.common.AbstractViewMain;
import de.unibonn.inf.dbdependenciesui.ui.views.common.AbstractViewData.Type;

/**
 * The main panel of the trigger view displays all available connections and their contents. The layout constraints are
 * realized with BorderLayout. Because of that, all resize events will transport to all subcomponents.
 * 
 * @author Andre Kasper
 * @author Jan Philipp
 */
public class TriggerViewMain extends AbstractViewMain {

	private static final long serialVersionUID = -4396085450579578100L;

	public TriggerViewMain() {
		super.initialize();
	}

	@Override
	protected AbstractViewData getViewData() {
		return TriggerViewData.getInstance(Type.DEFAULT);
	}
}
