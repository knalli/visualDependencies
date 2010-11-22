package de.unibonn.inf.dbdependenciesui.ui.views.viewhierarchy.graph.transformers;

import org.apache.commons.collections15.Transformer;

import de.unibonn.inf.dbdependenciesui.graph.common.AbstractDatabaseGraph;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.Relation;

public class EdgeLabelTransformer implements Transformer<Relation, String> {

	private final AbstractDatabaseGraph graph;

	public EdgeLabelTransformer(final AbstractDatabaseGraph graph) {
		this.graph = graph;
	}

	public String transform(final Relation e) {
		final StringBuilder sb = new StringBuilder();
		sb.append(graph.getEndpoints(e).toString());
		sb.append(" ");
		if (e.isPositive()) {
			sb.append("positive");
		} else {
			sb.append("negative");
		}
		return sb.toString();
	}
}
