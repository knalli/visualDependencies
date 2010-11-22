package de.unibonn.inf.dbdependenciesui.graph.common;

import java.util.HashMap;
import java.util.Map;

import de.unibonn.inf.dbdependenciesui.graph.viewhierarchy.DatabaseGraph;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseObject;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseTable;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseTrigger;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseView;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.Relation;

/**
 * This is a wrapper transforming a {@link DatabaseGraph} into a graphviz' dot-string.
 * 
 * @author Andre Kasper
 * @author Jan Philipp
 */
public class DatabaseGraphToGraphViz {
	protected final AbstractDatabaseGraph graph;

	public DatabaseGraphToGraphViz(final AbstractDatabaseGraph graph) {
		this.graph = graph;
	}

	public String getDot() {
		final StringBuilder sb = new StringBuilder();

		final Map<DatabaseObject, Integer> refs = new HashMap<DatabaseObject, Integer>();

		// Start
		sb.append("digraph G {\n");

		// Settings
		sb.append("graph [ranksep=.6];\n");
		sb.append("node [shape=record,fontname=Helvetica,fontsize=8,style=filled];\n");
		sb.append("edge [fontname=Helvetica,fontsize=8];\n");
		sb.append("\n");

		// all vertices/nodes
		for (final DatabaseObject object : graph.getVertices()) {
			final int ref = refs.size();
			refs.put(object, ref);

			sb.append(ref);
			sb.append(" [");
			sb.append("label=\"").append(object.getTitle()).append("\" ");
			sb.append("color=").append(getBackgroundColor(object)).append(" ");
			sb.append("fontcolor=").append(getForegroundColor(object));
			sb.append("];\n");
		}
		sb.append("\n");

		// all edges
		for (final Relation edge : graph.getEdges()) {
			buildEdge(sb, refs.get(graph.getSource(edge)), edge, refs.get(graph.getDest(edge)));
		}
		sb.append("\n");

		// End
		sb.append("}");

		return sb.toString();
	}

	/**
	 * @param sb
	 * @param refSource
	 * @param relation
	 * @param refTarget
	 */
	private void buildEdge(final StringBuilder sb, final int refSource, final Relation relation, final int refTarget) {
		sb.append(refSource).append(" -> ").append(refTarget);
		sb.append(" [");
		// sb.append("label=\"").append(relation.getCondition()).append("\" ");
		sb.append("color=").append(getEdgeColor(relation));
		sb.append("];\n");
	}

	/**
	 * Return the background color for the object type.
	 * 
	 * @param object
	 * @return
	 */
	protected String getBackgroundColor(final DatabaseObject object) {
		if (object instanceof DatabaseView) {
			return "orange";
		} else if (object instanceof DatabaseTable) {
			return "blue";
		} else if (object instanceof DatabaseTrigger) {
			return "green";
		} else {
			return "gray";
		}
	}

	/**
	 * Return the foreground color for the object type.
	 * 
	 * @param object
	 * @return
	 */
	protected String getForegroundColor(final DatabaseObject object) {
		if (object instanceof DatabaseView) {
			return "black";
		} else if (object instanceof DatabaseTable) {
			return "white";
		} else if (object instanceof DatabaseTrigger) {
			return "black";
		} else {
			return "black";
		}
	}

	/**
	 * Return the edge color for the relation.
	 * 
	 * @param relation
	 * @return
	 */
	protected String getEdgeColor(final Relation relation) {
		if (relation.isPositive()) {
			return "green";
		} else {
			return "red";
		}
	}
}
