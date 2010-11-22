package de.unibonn.inf.dbdependenciesui.graph.entityrelations;

import de.unibonn.inf.dbdependenciesui.graph.common.AbstractDatabaseGraphTree;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseObject;

public class DatabaseERDGraphTree extends AbstractDatabaseGraphTree {

	private static final long serialVersionUID = 6935831392245582073L;

	public DatabaseERDGraphTree(final DatabaseERDGraph graph, final DatabaseObject root) {
		this.graph = graph;
		this.root = root;
	}

}
