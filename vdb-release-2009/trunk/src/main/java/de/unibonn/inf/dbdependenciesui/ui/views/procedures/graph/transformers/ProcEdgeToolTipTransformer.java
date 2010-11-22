package de.unibonn.inf.dbdependenciesui.ui.views.procedures.graph.transformers;

import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.Relation;
import de.unibonn.inf.dbdependenciesui.ui.views.common.graph.transformers.AbstractEdgeToolTipTransformer;

public class ProcEdgeToolTipTransformer extends AbstractEdgeToolTipTransformer {

	@Override
	public String transform(final Relation relation) {
		return relation.getCondition();
	}

}
