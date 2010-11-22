package de.unibonn.inf.dbdependenciesui.ui.views.viewhierarchy.graph.layout;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.unibonn.inf.dbdependenciesui.Configuration;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseObject;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseTable;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseView;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.ConnectionProperties;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.DdlSchema;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.Relation;
import edu.uci.ics.jung.algorithms.layout.TreeLayout;
import edu.uci.ics.jung.graph.Forest;

public class LeveledForestLayout extends TreeLayout<DatabaseObject, Relation> {

	protected static final Logger log = Logger.getLogger(Configuration.LOGGER);

	// Note: Objects and initial vales cannot be set directly here because the super classes' constructor will called
	// directly buildTree.

	protected List<DatabaseTable> tables;
	protected List<DatabaseView> views;

	protected Map<DatabaseTable, Integer> levels;
	protected int maxLevel;

	protected ConnectionProperties properties = null;

	// CACHES

	private Map<DatabaseTable, DdlSchema> cachedDdlSchemaObjects;

	public LeveledForestLayout(final Forest<DatabaseObject, Relation> g) {
		super(g);
	}

	public LeveledForestLayout(final Forest<DatabaseObject, Relation> g, final int distx) {
		super(g, distx);
	}

	public LeveledForestLayout(final Forest<DatabaseObject, Relation> g, final int distx, final int disty) {
		super(g, distx, disty);
	}

	@Override
	public void reset() {
		cachedDdlSchemaObjects = null;
		levels = null;
		maxLevel = 0;
		properties = null;

		buildTree();
	}

	protected void initializeObjects() {
		levels = new HashMap<DatabaseTable, Integer>();

		tables = new ArrayList<DatabaseTable>();
		views = new ArrayList<DatabaseView>();

		for (final DatabaseObject object : graph.getVertices()) {
			if (!(object instanceof DatabaseTable)) {
				continue;
			}
			final DatabaseTable table = (DatabaseTable) object;

			if (table instanceof DatabaseView) {
				views.add((DatabaseView) table);
			} else {
				tables.add(table);
			}
		}

		if (tables.size() > 0) {
			properties = tables.get(0).getConnection().getPropertiesObject();
		}
	}

	protected void initializeLevels() {
		for (final DatabaseTable table : tables) {
			setLevel(table, 0);
		}

		for (final DatabaseTable table : tables) {
			final DdlSchema ddlschema = getCachedDdlSchema(table);
			for (final Relation relation : ddlschema.getTargetRelations()) {
				final DatabaseTable source = relation.getSourceTable();
				if (views.contains(source)) {
					computeLevel(source, table, 1);
				}
			}
		}

		for (final int level : levels.values()) {
			maxLevel = Math.max(maxLevel, level);
		}
	}

	protected void computeLevel(final DatabaseTable source, final DatabaseTable target, final int level) {

		setLevel(source, Math.max(getLevel(source), level));

		final DdlSchema ddlschema = getCachedDdlSchema(source);
		for (final Relation relation : ddlschema.getTargetRelations()) {
			final DatabaseTable source2 = relation.getSourceTable();
			if (views.contains(source2) || tables.contains(source2)) {
				computeLevel(source2, source, level + 1);
			}
		}
	}

	protected void setLevel(final DatabaseTable table, final int level) {
		levels.put(table, level);
	}

	protected int getLevel(final DatabaseTable table) {
		if (levels.containsKey(table)) {
			return levels.get(table);
		} else {
			return -1;
		}
	}

	protected List<DatabaseTable> getTableNamesByLevel(final int level) {
		final List<DatabaseTable> result = new ArrayList<DatabaseTable>();

		for (final DatabaseTable tableName : levels.keySet()) {
			if (levels.get(tableName).intValue() == level) {
				result.add(tableName);
			}
		}

		final Comparator<DatabaseTable> c = new Comparator<DatabaseTable>() {
			@Override
			public int compare(final DatabaseTable o1, final DatabaseTable o2) {
				final int x1 = new Double(locations.get(o1).getX()).intValue();
				final int x2 = new Double(locations.get(o2).getX()).intValue();
				return x1 - x2;
			}
		};
		Collections.sort(result, c);

		return result;
	}

	public Map<DatabaseTable, Integer> getLevels() {
		return levels;
	}

	public int getMaxLevel() {
		return maxLevel;
	}

	/*
	 * (non-Javadoc)
	 * @see edu.uci.ics.jung.algorithms.layout.TreeLayout#buildTree()
	 */
	@Override
	protected void buildTree() {
		initializeObjects();
		initializeLevels();

		super.buildTree();

		postProcessLayout();
	}

	/**
	 * @param table
	 * @return
	 */
	protected DdlSchema getCachedDdlSchema(final DatabaseTable table) {
		if (cachedDdlSchemaObjects == null) {
			cachedDdlSchemaObjects = new HashMap<DatabaseTable, DdlSchema>();
		}

		DdlSchema ddlschema = cachedDdlSchemaObjects.get(table);
		if (ddlschema == null) {
			ddlschema = table.getDdlSchemaObject();
			cachedDdlSchemaObjects.put(table, ddlschema);
		}
		return ddlschema;
	}

	/**
	 * Postprocessing layout: Reposition the objects.
	 */
	protected void postProcessLayout() {

		// For each table/view: Assure that the level is correct. If the level is not correct a new y position will set.
		reloadOldVertexPositions();

		// For each level: Assure final that there are final no overlaps.
		final List<DatabaseTable> alreadyDone = new ArrayList<DatabaseTable>();

		// The following lines will check for each level if the vertices on this line overlaps. For overlapped vertices
		// it'll try to move them to left or to right depending space is available.
		// To avoid an infinite loop, the algorithm can looped so many times as nodes are in this level/line. (like
		// bubblesort).
		List<DatabaseTable> tables = null;

		// Main loop (levels).
		for (int level = getMaxLevel(); level >= 0; level--) {
			final List<DatabaseTable> tablesByLevel = getTableNamesByLevel(level);

			// If set to false, the inner loop was finished correctly and it no further loop needed.
			boolean breakBecauseRestart = true;

			// Contains how many loops are still left (conditionexit).
			int loopsUntilExit = tablesByLevel.size();

			// Outer loop (in one level).
			while (breakBecauseRestart && (loopsUntilExit > 0)) {
				// This set the manage stuff, declare initial values.
				alreadyDone.clear();
				loopsUntilExit--;
				double leftX = Double.NEGATIVE_INFINITY;
				DatabaseTable oldTable = null;

				// Get the tables of this level.
				tables = new ArrayList<DatabaseTable>(tablesByLevel.size());
				tables.addAll(tablesByLevel);

				// Inner loop (in one level).
				for (int i = 0; i < tables.size(); i++) {

					// table is the current table object
					// thisX is the x-position of the table object
					// leftX is the previous x-position of a table object - can be infinity

					final DatabaseTable table = tables.get(i);
					final double thisX = locations.get(table).getX();

					log.log(Level.INFO, String.format("Check %s with %.0f + %d > %.0f %n", table, leftX, distX, thisX));

					// If a break occurs it will restart the outer loop (because the elements moved).
					breakBecauseRestart = true;

					// This is the actual condition checking if this vertex and the vertex to the left are overlapped.
					if (leftX + distX > thisX) {
						// Computing the missing space.
						final double missingSpace = distX - (thisX - leftX);
						log.log(Level.INFO, String.format("Found overlap [%.0f] between vertices %s and %s.%n",
								missingSpace, oldTable, table));

						// moveToVertexToLeft try to move the left vertex about moveLeft pixels, but i can happens that
						// there is not enough space.
						// It returns the actual moving. Override the moveLeft.
						double moveLeft = missingSpace;
						// Try to move the elements to the left. We know that all objects to the left are correct.
						try {
							// Move the vertices to the left. It returns the actual moving.
							moveLeft = moveVertexToLeft(tables.get(i - 1), moveLeft);
						} catch (final Exception e) {}

						// Move the vertices to the right.
						final double moveRight = missingSpace - moveLeft;
						moveVerticesToRight(tables.subList(i, tables.size()), moveRight);

						// Break the inner loop.
						break;
					}

					// All fine, no outer loop restart cause any more.
					breakBecauseRestart = false;

					// Inner loop: Set the values for the next element.
					leftX = thisX;
					oldTable = table;
					alreadyDone.add(table);
				}
			}

		}
	}

	/**
	 * For each table/view: Assure that the level is correct. If the level is not correct a new y position will set.
	 */
	private void reloadOldVertexPositions() {
		for (final DatabaseObject object : graph.getVertices()) {
			if (!(object instanceof DatabaseTable)) {
				continue;
			}
			final DatabaseTable table = (DatabaseTable) object;

			// All tables and views have to be on their appropiate level.
			final Point2D current = transform(table);
			final int currentY = new Double(current.getY()).intValue();
			final int level = getLevel(table);
			final int expectedY = (maxLevel - level) * distY + 20;
			if (currentY != expectedY) {
				setLocation(table, new Point2D.Double(current.getX(), expectedY));
			}

			// // If special positions contained in properties' attributes map, use
			// // them.
			// if (properties != null) {
			// final ConnectionAttributesMap attributes = properties.getAttributesMap(AttributeMapSet.VIEWS, table);
			// final int newPosX = attributes.getPositionX();
			// final int newPosY = attributes.getPositionY();
			// if ((newPosX > 0) && (newPosY > 0)) {
			// setLocation(table, new Point2D.Double(newPosX, newPosY));
			// }
			// }
		}
	}

	protected double moveVertexToLeft(final DatabaseTable vertex, final double moveLeft) {
		final double move = moveLeft;

		if (move > 0) {
			final Point2D currentPoint = locations.get(vertex);
			double newX = currentPoint.getX() - moveLeft;
			if (newX < 0) {
				newX = 0;
			}
			final Point2D newPoint = new Point2D.Double(newX, currentPoint.getY());
			setLocation(vertex, newPoint);
		}

		return move;
	}

	protected void moveVerticesToRight(final List<DatabaseTable> vertices, final double moveRight) {
		if (moveRight > 0) {
			for (final DatabaseTable vertex : vertices) {
				final Point2D currentPoint = locations.get(vertex);
				final Point2D newPoint = new Point2D.Double(currentPoint.getX() + moveRight, currentPoint.getY());
				setLocation(vertex, newPoint);
			}
		}
	}
}
