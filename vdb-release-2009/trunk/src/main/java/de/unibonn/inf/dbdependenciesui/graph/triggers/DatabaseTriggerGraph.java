package de.unibonn.inf.dbdependenciesui.graph.triggers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import de.unibonn.inf.dbdependenciesui.graph.common.AbstractDatabaseGraph;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseObject;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseTrigger;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.Relation;
import edu.uci.ics.jung.graph.Tree;

public class DatabaseTriggerGraph extends AbstractDatabaseGraph {

	private static final long serialVersionUID = -7617873137259168625L;

	private List<DatabaseTrigger> roots;

	public DatabaseTriggerGraph() {}

	public void setRoot(final DatabaseTrigger root) {
		roots = new ArrayList<DatabaseTrigger>();
		roots.add(root);
	}

	public void setRoot(final DatabaseTrigger... roots) {
		this.roots = Arrays.asList(roots);
	}

	public void setRoot(final List<DatabaseTrigger> roots) {
		this.roots = roots;
	}

	@Override
	public Collection<Tree<DatabaseObject, Relation>> getTrees() {
		final List<Tree<DatabaseObject, Relation>> result = new ArrayList<Tree<DatabaseObject, Relation>>(roots.size());
		for (final DatabaseTrigger root : roots) {
			result.add(new DatabaseTriggerGraphTree(this, root));
		}

		AbstractDatabaseGraph.log.info("count(trees) = " + result.size());

		return result;
	}

}
