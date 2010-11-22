package de.unibonn.inf.dbdependenciesui.ui.views.procedures;

import de.unibonn.inf.dbdependenciesui.ui.views.common.AbstractViewData;
import de.unibonn.inf.dbdependenciesui.ui.views.common.AbstractViewMain;
import de.unibonn.inf.dbdependenciesui.ui.views.common.AbstractViewData.Type;

public class ProcViewMain extends AbstractViewMain {

	private static final long serialVersionUID = -4352737107698227628L;

	public ProcViewMain() {
		super.initialize();
	}

	@Override
	protected AbstractViewData getViewData() {
		return ProcViewData.getInstance(Type.DEFAULT);
	}

}
