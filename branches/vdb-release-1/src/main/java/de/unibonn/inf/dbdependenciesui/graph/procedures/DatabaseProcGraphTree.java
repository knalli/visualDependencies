package de.unibonn.inf.dbdependenciesui.graph.procedures;

import de.unibonn.inf.dbdependenciesui.graph.common.AbstractDatabaseGraphTree;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseObject;

public class DatabaseProcGraphTree extends AbstractDatabaseGraphTree {

	private static final long serialVersionUID = 6935831392245582073L;

	public DatabaseProcGraphTree(final DatabaseProcGraph graph, final DatabaseObject root) {
		this.graph = graph;
		this.root = root;
	}

}
