package de.unibonn.inf.dbdependenciesui.graph.common;

import java.util.Collection;

import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseObject;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseTable;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseView;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.Relation;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Tree;

public abstract class AbstractDatabaseGraphTree extends DirectedSparseGraph<DatabaseObject, Relation> implements
		Tree<DatabaseObject, Relation> {
	protected static final long serialVersionUID = 1881824741895827766L;
	protected AbstractDatabaseGraph graph;
	protected transient DatabaseObject root;

	@Override
	public int getChildCount(final DatabaseObject vertex) {
		return graph.getOutEdges(vertex).size();
	}

	@Override
	public Collection<Relation> getChildEdges(final DatabaseObject vertex) {
		return graph.getOutEdges(vertex);
	}

	@Override
	public Collection<DatabaseObject> getChildren(final DatabaseObject vertex) {
		return graph.getSuccessors(vertex);
	}

	@Override
	public int getDepth(final DatabaseObject vertex) {
		if (vertex instanceof DatabaseView) {
			return graph.getPredecessorCount(vertex);
		} else {
			int max = 0;
			for (final DatabaseObject object : graph.getVertices()) {
				if (object instanceof DatabaseTable) {
					max = Math.max(max, getVertexDepth(vertex));
				}
			}
			return max;
		}
	}

	private int getVertexDepth(final DatabaseObject vertex) {
		DatabaseObject i = vertex;
		int depth = 1;
		while (!getParent(i).equals(root)) {
			depth++;
			i = getParent(i);
		}
		return depth;
	}

	@Override
	public int getHeight() {
		int max = 0;
		for (final DatabaseObject vertex : graph.getVertices()) {
			max = Math.max(max, getDepth(vertex));
		}
		return max;
	}

	@Override
	public DatabaseObject getParent(final DatabaseObject vertex) {
		return graph.getPredecessors(vertex).iterator().next();
	}

	@Override
	public Relation getParentEdge(final DatabaseObject vertex) {
		return graph.getIncidentEdges(vertex).iterator().next();
	}

	@Override
	public DatabaseObject getRoot() {
		return root;
	}

	@Override
	public Collection<Tree<DatabaseObject, Relation>> getTrees() {
		return null;
	}

}
