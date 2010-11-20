package de.unibonn.inf.dbdependenciesui.graph.entityrelations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import de.unibonn.inf.dbdependenciesui.graph.common.AbstractDatabaseGraph;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseObject;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseTable;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.Relation;
import edu.uci.ics.jung.graph.Tree;

public class DatabaseERDGraph extends AbstractDatabaseGraph {

	private static final long serialVersionUID = -7617873137259168625L;

	private List<DatabaseTable> roots;

	public DatabaseERDGraph() {}

	public void setRoot(final DatabaseTable root) {
		roots = new ArrayList<DatabaseTable>();
		roots.add(root);
	}

	public void setRoot(final DatabaseTable... roots) {
		this.roots = Arrays.asList(roots);
	}

	public void setRoot(final List<DatabaseTable> roots) {
		this.roots = roots;
	}

	@Override
	public Collection<Tree<DatabaseObject, Relation>> getTrees() {
		final List<Tree<DatabaseObject, Relation>> result = new ArrayList<Tree<DatabaseObject, Relation>>(roots.size());
		for (final DatabaseTable root : roots) {
			result.add(new DatabaseERDGraphTree(this, root));
		}

		AbstractDatabaseGraph.log.info("count(trees) = " + result.size());

		return result;
	}

}
