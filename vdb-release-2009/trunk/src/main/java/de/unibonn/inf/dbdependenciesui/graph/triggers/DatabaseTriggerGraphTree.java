package de.unibonn.inf.dbdependenciesui.graph.triggers;

import de.unibonn.inf.dbdependenciesui.graph.common.AbstractDatabaseGraphTree;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseObject;

public class DatabaseTriggerGraphTree extends AbstractDatabaseGraphTree {

	private static final long serialVersionUID = 6935831392245582073L;

	public DatabaseTriggerGraphTree(final DatabaseTriggerGraph graph, final DatabaseObject root) {
		this.graph = graph;
		this.root = root;
	}

}
