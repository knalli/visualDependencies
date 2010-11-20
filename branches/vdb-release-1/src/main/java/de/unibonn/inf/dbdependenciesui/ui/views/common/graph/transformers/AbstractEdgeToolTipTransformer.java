package de.unibonn.inf.dbdependenciesui.ui.views.common.graph.transformers;

import org.apache.commons.collections15.Transformer;

import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.Relation;

public abstract class AbstractEdgeToolTipTransformer implements Transformer<Relation, String> {

	public abstract String transform(final Relation relation);

}
