package de.unibonn.inf.dbdependenciesui.graph.viewhierarchy;

import de.unibonn.inf.dbdependenciesui.graph.common.AbstractDatabaseGraphTree;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseObject;
import edu.uci.ics.jung.graph.Tree;

/**
 * This is the {@link Tree} of {@link DatabaseGraph}.
 * 
 * @author Andre Kasper
 * @author Jan Philipp
 */
public class DatabaseGraphTree extends AbstractDatabaseGraphTree {

	private static final long serialVersionUID = 3050204299326634466L;

	public DatabaseGraphTree(final DatabaseGraph graph, final DatabaseObject root) {
		this.graph = graph;
		this.root = root;
	}

}
