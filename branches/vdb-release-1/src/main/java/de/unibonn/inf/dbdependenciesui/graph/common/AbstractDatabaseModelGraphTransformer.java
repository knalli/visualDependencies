package de.unibonn.inf.dbdependenciesui.graph.common;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import de.unibonn.inf.dbdependenciesui.Configuration;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseObject;

public abstract class AbstractDatabaseModelGraphTransformer {

	protected static final Logger log = Logger.getLogger(Configuration.LOGGER);

	protected final List<String> includeList = new ArrayList<String>();

	/**
	 * Initialize the include list. If the param <code>includeList</code> is empty the internal includeList will
	 * contains all possible elements.
	 * 
	 * @param includeList
	 */
	protected abstract void initializeIncludeList(final List<String> includeList);

	protected abstract void initialize();

	protected abstract AbstractDatabaseGraph getGraph();

	protected boolean isTableAllowed(final DatabaseObject object) {
		return this.isTableAllowed(object.getTitle());
	}

	protected boolean isTableAllowed(final String title) {
		return includeList.contains(title);
	}

}
