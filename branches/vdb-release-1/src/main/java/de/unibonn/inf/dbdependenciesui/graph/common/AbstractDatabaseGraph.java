package de.unibonn.inf.dbdependenciesui.graph.common;

import java.util.Collection;
import java.util.logging.Logger;

import de.unibonn.inf.dbdependenciesui.Configuration;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseObject;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.Relation;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Forest;

public abstract class AbstractDatabaseGraph extends DirectedSparseMultigraph<DatabaseObject, Relation> implements
		Forest<DatabaseObject, Relation> {

	protected static final Logger log = Logger.getLogger(Configuration.LOGGER);

	private static final long serialVersionUID = -7617873137259168625L;

	@Override
	public int getChildCount(final DatabaseObject arg0) {
		return 0;
	}

	@Override
	public Collection<Relation> getChildEdges(final DatabaseObject arg0) {
		return null;
	}

	@Override
	public Collection<DatabaseObject> getChildren(final DatabaseObject arg0) {
		return null;
	}

	@Override
	public DatabaseObject getParent(final DatabaseObject arg0) {
		return null;
	}

	@Override
	public Relation getParentEdge(final DatabaseObject arg0) {
		return null;
	}

}
