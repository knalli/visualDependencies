package de.unibonn.inf.dbdependenciesui.ui.views.triggers.graph.transformers;

import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.Relation;
import de.unibonn.inf.dbdependenciesui.ui.views.common.graph.transformers.AbstractEdgeToolTipTransformer;

public class TriggerEdgeToolTipTransformer extends AbstractEdgeToolTipTransformer {

	@Override
	public String transform(final Relation relation) {
		return relation.getCondition();
	}
}
