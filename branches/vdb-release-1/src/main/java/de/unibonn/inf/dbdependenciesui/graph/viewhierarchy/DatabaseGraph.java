package de.unibonn.inf.dbdependenciesui.graph.viewhierarchy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import de.unibonn.inf.dbdependenciesui.Configuration;
import de.unibonn.inf.dbdependenciesui.graph.common.AbstractDatabaseGraph;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseObject;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseTable;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.Relation;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Tree;

/**
 * This is the actual database object graph class. This {@link DirectedSparseGraph} contains objects of the types
 * {@link DatabaseObject} and {@link Relation}.
 * 
 * @author Andre Kasper
 * @author Jan Philipp
 */
public class DatabaseGraph extends AbstractDatabaseGraph {

	private static final Logger log = Logger.getLogger(Configuration.LOGGER);

	private static final long serialVersionUID = -7617873137259168625L;

	private List<DatabaseTable> roots;

	public DatabaseGraph() {}

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
			result.add(new DatabaseGraphTree(this, root));
		}

		DatabaseGraph.log.info("count(trees) = " + result.size());

		return result;
	}
}
