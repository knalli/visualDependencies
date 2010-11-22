package de.unibonn.inf.dbdependenciesui.ui.views.viewhierarchy.graph.transformers;

import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.Relation;
import de.unibonn.inf.dbdependenciesui.misc.Internationalization;
import de.unibonn.inf.dbdependenciesui.ui.views.common.graph.transformers.AbstractEdgeToolTipTransformer;

public class EdgeToolTipTransformer extends AbstractEdgeToolTipTransformer {

	private static String appPrefix = "application.graph.hierarchicalview.edge.tooltip.";

	@Override
	public String transform(final Relation relation) {
		String result = null;

		final String source = relation.getSourceName();
		final String target = relation.getTargetName();

		String condition = null;
		if ("COMPLETE".equalsIgnoreCase(relation.getCondition())) {
			if (relation.isPositive()) {
				condition = Internationalization.getText(appPrefix + "positive.conditioncomplete");
			} else {
				condition = Internationalization.getText(appPrefix + "negative.conditioncomplete");
			}
		} else {
			if (relation.isPositive()) {
				condition = Internationalization.getTextFormatted(appPrefix + "positive.condition", relation
						.getCondition());
			} else {
				condition = Internationalization.getTextFormatted(appPrefix + "negative.condition", relation
						.getCondition());
			}
		}

		if (relation.isPositive()) {
			result = Internationalization.getTextFormatted(appPrefix + "positive", source, target, condition);
		} else {
			result = Internationalization.getTextFormatted(appPrefix + "negative", source, target, condition);
		}

		return result;
	}
}
