package de.unibonn.inf.dbdependenciesui.ui.views.entityrelations;

import de.unibonn.inf.dbdependenciesui.ui.views.common.AbstractViewData;
import de.unibonn.inf.dbdependenciesui.ui.views.common.AbstractViewMain;
import de.unibonn.inf.dbdependenciesui.ui.views.common.AbstractViewData.Type;

public class ERDViewMain extends AbstractViewMain {

	private static final long serialVersionUID = -4396085450579578100L;

	public ERDViewMain() {
		super.initialize();
	}

	@Override
	protected AbstractViewData getViewData() {
		return ERDViewData.getInstance(Type.DEFAULT);
	}

}
