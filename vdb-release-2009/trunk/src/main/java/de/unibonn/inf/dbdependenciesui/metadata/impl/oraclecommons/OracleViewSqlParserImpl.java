/**
 * $Id$
 */
package de.unibonn.inf.dbdependenciesui.metadata.impl.oraclecommons;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import org.medfoster.sqljep.Parser;
import org.medfoster.sqljep.ParserConstants;
import org.medfoster.sqljep.Token;

import de.unibonn.inf.dbdependenciesui.Configuration;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseTable;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseView;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.Column;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.DdlSchemaEditable;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.Relation;
import de.unibonn.inf.dbdependenciesui.metadata.MetaDataFactory.Vendor;
import de.unibonn.inf.dbdependenciesui.metadata.impl.AbstractViewSqlParser;

/**
 * Concrete implementation of an oracle view sql parser.
 * 
 * @author Andre Kasper
 * @author Jan Philipp
 */
public abstract class OracleViewSqlParserImpl extends AbstractViewSqlParser {

	protected static final Logger log = Logger.getLogger(Configuration.LOGGER);

	// private final boolean newNegativRelations = true;

	private List<DatabaseTable> positiveRelationViews, negativeRelationViews, affectedViews;
	private Parser parser;
	private HashMap<String, String> assignment;
	private boolean from = false, where = false;
	private int subselect = 0;
	private List<String> subselectTables;

	private List<DatabaseTable> tables;

	private List<DatabaseView> views;

	private DatabaseView view;

	private boolean positiv;

	private String existsOrNot;

	private boolean minus = false;

	private DdlSchemaEditable schemaTarget;

	private String errorMessage;

	private int notExists;

	public OracleViewSqlParserImpl(final Vendor vendor) {
		super(vendor);
	}

	public boolean parse(final List<DatabaseTable> tables, final List<DatabaseView> views, final DatabaseView view) {
		// log.setLevel(Level.INFO);

		positiveRelationViews = new ArrayList<DatabaseTable>();
		negativeRelationViews = new ArrayList<DatabaseTable>();
		affectedViews = new ArrayList<DatabaseTable>();
		assignment = new HashMap<String, String>();
		subselectTables = new ArrayList<String>();
		this.tables = tables;
		this.views = views;
		this.view = view;
		notExists = 0;

		if (view == null) {
			errorMessage = "view cannot be null";
			return false;
		}
		if (tables == null) {
			errorMessage = "table list cannot be null";
			return false;
		}
		if (views == null) {
			errorMessage = "view list cannot be null";
			return false;
		}
		try {
			return parseViews();
		} catch (final Exception e) {
			errorMessage = e.getLocalizedMessage();
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see de.unibonn.inf.dbdependenciesui.sqlparser.IViewSqlParser#getAffectedViews ()
	 */
	@Override
	public List<DatabaseTable> getAffectedViews() {
		return (affectedViews != null) ? affectedViews : Collections.<DatabaseTable> emptyList();
	}

	/*
	 * (non-Javadoc)
	 * @seede.unibonn.inf.dbdependenciesui.sqlparser.IViewSqlParser# getNegativeRelationViews()
	 */
	@Override
	public List<DatabaseTable> getNegativeRelationViews() {
		return (negativeRelationViews != null) ? negativeRelationViews : Collections.<DatabaseTable> emptyList();
	}

	/*
	 * (non-Javadoc)
	 * @seede.unibonn.inf.dbdependenciesui.sqlparser.IViewSqlParser# getPositiveRelationViews()
	 */
	@Override
	public List<DatabaseTable> getPositiveRelationViews() {
		return (positiveRelationViews != null) ? positiveRelationViews : Collections.<DatabaseTable> emptyList();
	}

	/*
	 * (non-Javadoc)
	 * @see de.unibonn.inf.dbdependenciesui.sqlparser.ISqlParser#clear()
	 */
	@Override
	public void clear() {
		positiveRelationViews.clear();
		negativeRelationViews.clear();
		affectedViews.clear();
		from = false;
		where = false;
		assignment.clear();
		subselectTables.clear();
		subselect = 0;
		schemaTarget = null;
		minus = false;
		positiv = true;
		notExists = 0;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	private boolean parseViews() throws Exception {
		clear();
		String definition = view.getSelectStatement();
		definition = definition.replaceAll("\"", "");

		firePropertyChange("parsingView", view.getTitle());

		schemaTarget = view.createDdlSchemaEditableObject();
		parser = new Parser(new StringReader(definition));
		Token t;
		while ((t = parser.getNextToken()).kind != ParserConstants.EOF) {
			if (t.kind == ParserConstants.IDENTIFIER) {
				if (t.toString().toUpperCase().equals("FROM")) {
					from = true;
					t = parser.getNextToken();
				} else if (t.toString().toUpperCase().equals("WHERE") || t.toString().toUpperCase().equals("HAVING")) {
					from = false;
					where = true;
					if (notExists > 0) {
						checkAllViews();
					}
					t = parser.getNextToken();
				}
			}
			if (from) {
				t = parseFrom(t);
			}
			if (where) {
				// t = (newNegativRelations) ? newParseWhere(t, false, "") : parseWhere(t, false, "");
				t = newParseWhere(t, false, "");
			}
		}
		checkAllViews();
		view.setDdlSchemaObject(schemaTarget);
		return true;
	}

	private Token parseFrom(Token t) {
		if (t.toString().equals("(")) {
			subselect++;
		}
		if (t.toString().toUpperCase().equals("LEFT") || t.toString().toUpperCase().equals("RIGHT")
				|| t.toString().toUpperCase().equals("CROSS") || t.toString().toUpperCase().equals("FULL")
				|| t.toString().toUpperCase().equals("NATURAL") || t.toString().toUpperCase().equals("JOIN")) { return leftOrRightJoin(t); }
		if (t.kind == ParserConstants.IDENTIFIER) {
			final Token temp = t;
			t = parser.getNextToken();
			if (t.kind == ParserConstants.IDENTIFIER) {
				t = checkToken(t, temp);
			}
			if (t.toString().equals(")")) {
				t = makeSubselect(t, temp);
			}
			setView(temp);
		}
		return t;
	}

	private Token checkToken(Token t, final Token temp) {
		final String string = t.toString().toUpperCase();
		if (string.equals("WHERE") || string.equals("GROUP")) {
			from = false;
			where = true;
			t = parser.getNextToken();
		} else if (string.equals("MINUS") || string.equals("INTERSECT") || string.equals("UNION")) {
			from = false;
			if (string.equals("MINUS")) {
				setView(temp);
				checkAllViews();
				minus = true;
			}
		} else if (t.toString().toUpperCase().equals("LEFT") || t.toString().toUpperCase().equals("RIGHT")
				|| t.toString().toUpperCase().equals("CROSS") || t.toString().toUpperCase().equals("FULL")
				|| t.toString().toUpperCase().equals("NATURAL") || t.toString().toUpperCase().equals("JOIN")) {
			setView(temp);
			return leftOrRightJoin(t);
		} else {
			assignment.put(temp.toString().toUpperCase(), t.toString().toUpperCase());
		}
		return t;
	}

	private Token makeSubselect(Token t, final Token temp) {
		subselect--;
		subselectTables.add(temp.toString().toUpperCase());
		if (subselect == 0) {
			t = parser.getNextToken();
			for (int i = 0; i < subselectTables.size(); i++) {
				assignment.put(subselectTables.get(i), t.toString().toUpperCase());
			}
			subselectTables.clear();
		}
		return t;
	}

	private void setView(final Token temp) {
		final DatabaseTable tv = new DatabaseTable();
		tv.setTitle(temp.toString().toUpperCase());
		if (!affectedViews.contains(tv)) {
			addViewTableFromList(tv);
		}
	}

	private Token newParseWhere(Token t, final boolean joinDetected, final String leftRightJoin) {
		if (!(t.kind == ParserConstants.AND) && !(t.kind == ParserConstants.OR)) {
			String relationString = "";
			Token temp = t;
			boolean negativRelation = false;
			boolean exists = false;
			if (temp.kind != ParserConstants.NOT_LIKE) {
				if (temp.toString().toUpperCase().equals("NOT")) {
					negativRelation = true;
					temp = parser.getNextToken();
					if (temp.toString().equals("(")) {
						temp = parser.getNextToken();
					} else if (temp.toString().toUpperCase().equals("EXISTS")) {
						log.info(temp.toString().toUpperCase());
						exists = true;
						existsOrNot = "NOT EXISTS (";
						temp = parser.getNextToken();
						if (temp.toString().equals("(")) {
							temp = parser.getNextToken();
						}
						notExists++;
						log.info(notExists + "");
						return t;
					}
				}
			}
			if (temp.toString().toUpperCase().equals("EXISTS")) {
				exists = true;
				existsOrNot = "EXISTS (";
				if (temp.toString().equals("(")) {
					temp = parser.getNextToken();
				}
				return t;
			}
			t = parser.getNextToken();
			if (t.kind == ParserConstants.IDENTIFIER) {
				if (t.toString().toUpperCase().equals("FROM")) {
					where = false;
					from = true;
				}
			}
			switch (t.kind) {
			case ParserConstants.NE:
				t = parser.getNextToken();
				relationString = temp.toString() + " != " + t.toString();
				break;
			case ParserConstants.NOT_LIKE:
				t = parser.getNextToken();
				relationString = temp.toString() + " NOT LIKE " + t.toString();
				break;
			case ParserConstants.EQ:
				t = parser.getNextToken();
				relationString = temp.toString() + " = " + t.toString();
				break;
			case ParserConstants.LIKE:
				t = parser.getNextToken();
				relationString = temp.toString() + " LIKE " + t.toString();
				break;
			case ParserConstants.BETWEEN:
				t = parser.getNextToken();
				parser.getNextToken();
				final Token tempBetween = t;
				t = parser.getNextToken();
				relationString = temp.toString() + " BETWEEN " + tempBetween.toString() + " AND " + t.toString();
				if (tempBetween.kind == ParserConstants.IDENTIFIER) {
					makeRelation(tempBetween, relationString);
				}
				break;
			case ParserConstants.GE:
				t = parser.getNextToken();
				relationString = temp.toString() + " => " + t.toString();
				break;
			case ParserConstants.GT:
				t = parser.getNextToken();
				relationString = temp.toString() + " > " + t.toString();
				break;
			case ParserConstants.IN:
				t = inDetected(t, temp, relationString);
				break;
			case ParserConstants.LE:
				t = parser.getNextToken();
				relationString = temp.toString() + " <= " + t.toString();
				break;
			case ParserConstants.LT:
				t = parser.getNextToken();
				if (t.kind == ParserConstants.GT) {
					t = parser.getNextToken();
					relationString = temp.toString() + " != " + t.toString();
				} else {
					relationString = temp.toString() + " < " + t.toString();
				}
				break;
			case ParserConstants.NOT:
				// Falls NOT IN und NOT BETWEEN doch negativ sein sollen, einkommentieren
				// positiv = false;
				t = parser.getNextToken();
				if (t.kind == ParserConstants.BETWEEN) {
					t = parser.getNextToken();
					parser.getNextToken();
					final Token temp2 = t;
					t = parser.getNextToken();
					relationString = temp.toString() + " NOT BETWEEN " + temp2.toString() + " AND " + t.toString();
					if (temp2.kind == ParserConstants.IDENTIFIER) {
						makeRelation(temp2, relationString);
					}
				} else if (t.kind == ParserConstants.IN) {
					t = inDetected(t, temp, relationString);
					relationString = "NOT(" + relationString + ")";
				}
				break;
			default:
				temp = new Token();
				t = new Token();
				break;
			}
			if (negativRelation) {
				negativRelation = false;
				if (!exists) {
					relationString = "NOT(" + relationString + ")";
				}
			}
			if (joinDetected) {
				relationString = leftRightJoin + " JOIN ON(" + relationString + ")";
			}
			if (minus) {
				positiv = !positiv;
			}
			if (t.kind == ParserConstants.IDENTIFIER) {
				if (exists) {
					relationString = existsOrNot + relationString + ")";
					exists = false;
				}
				if (t.toString().toUpperCase().equals("ANY") || t.toString().toUpperCase().equals("ALL")) {
					anyOrAllDetected(t, temp, relationString);
				} else {
					makeRelation(t, relationString);
				}
			}
			if (temp.kind == ParserConstants.IDENTIFIER) {
				if (exists) {
					relationString = existsOrNot + relationString + ")";
					exists = false;
				}
				makeRelation(temp, relationString);
			}
		}
		return t;
	}

	private Token leftOrRightJoin(Token t) {
		String leftRightJoin = t.toString();
		t = parser.getNextToken();
		if (!leftRightJoin.toString().equals("JOIN")) {
			t = parser.getNextToken();
		} else {
			leftRightJoin = "";
		}
		if (leftRightJoin.toString().equals("FULL")) {
			t = parser.getNextToken();
		}
		final Token temp = t;
		t = parser.getNextToken();
		if (!t.toString().toUpperCase().equals("ON")) {
			assignment.put(temp.toString().toUpperCase(), t.toString().toUpperCase());
			t = parser.getNextToken();
		}
		t = parser.getNextToken();
		final DatabaseTable tv = new DatabaseTable();
		tv.setTitle(temp.toString().toUpperCase());
		if (!affectedViews.contains(tv)) {
			addViewTableFromList(tv);
		}
		if (t.toString().equals("(")) {
			t = parser.getNextToken();
		}
		// return (newNegativRelations) ? newParseWhere(t, true, leftRightJoin) : parseWhere(t, true, leftRightJoin);
		return newParseWhere(t, true, leftRightJoin);
	}

	private Token inDetected(Token t, final Token temp, String relationString) {
		t = parser.getNextToken();
		if (t.kind == ParserConstants.IDENTIFIER) {
			relationString = temp.toString() + " IN (" + t.toString() + ")";
		} else {
			t = parser.getNextToken();
			if (t.toString().toUpperCase().equals("SELECT")) {
				t = parser.getNextToken();
				parser.getNextToken();
				final Token temp2 = parser.getNextToken();
				if (temp2.kind == ParserConstants.IDENTIFIER) {
					relationString = temp.toString() + " IN (" + t.toString() + ")";
					final DatabaseTable tv = new DatabaseTable();
					tv.setTitle(temp2.toString().toUpperCase());
					setRelation(t.toString(), tv, relationString);
					if (!affectedViews.contains(tv)) {
						addViewTableFromList(tv);
					}
					t = new Token();
				}
			} else {
				relationString = temp.toString() + " IN (" + t.toString() + ")";
			}
		}
		return t;
	}

	private Token anyOrAllDetected(Token t, final Token temp, String relationString) {
		String tableName = "";
		String columnName = "";
		parser.getNextToken();
		t = parser.getNextToken();
		if (t.toString().equals("SELECT")) {
			t = parser.getNextToken();
			columnName = t.toString();
			t = parser.getNextToken();
			if (t.toString().equals("FROM")) {
				t = parser.getNextToken();
				if (t.kind == ParserConstants.IDENTIFIER) {
					tableName = t.toString();
				}
			}
			relationString += "(SELECT " + columnName + " FROM " + tableName + ")";
		}
		final DatabaseTable tv = new DatabaseTable();
		tv.setTitle(tableName.toString().toUpperCase());
		setRelation(columnName, tv, relationString);
		return t;
	}

	private DatabaseTable getAssignedTableView(final DatabaseTable tv, final Column column, StringBuffer tableName) {
		if (assignment.containsValue(tv.getTitle())) {
			for (final String table : assignment.keySet()) {
				final String value = assignment.get(table);
				if (value.equals(tv.getTitle())) {
					final DatabaseTable temp2 = new DatabaseTable();
					temp2.setTitle(table.toUpperCase());
					if ((affectedViews.indexOf(temp2) > -1)
							&& affectedViews.get(affectedViews.indexOf(temp2)).getDdlSchemaObject().getColumns()
									.contains(column)) {
						tableName = new StringBuffer();
						tableName.append(table.toUpperCase());
						tv.setTitle(tableName.toString().toUpperCase());

					}
				}
			}
		}
		return tv;
	}

	private StringBuffer emptyColumn(final StringBuffer columnName, StringBuffer tableName) {
		tableName = new StringBuffer();
		List<Column> columns;
		for (int i = 0; i < affectedViews.size(); i++) {
			columns = affectedViews.get(i).getDdlSchemaObject().getColumns();
			for (int j = 0; j < columns.size(); j++) {
				if (columns.get(j).getName().equals(columnName.toString().toUpperCase())) {
					tableName = tableName.append(affectedViews.get(i).getTitle());
					break;
				}
			}
			if (!tableName.toString().isEmpty()) {
				break;
			}
		}
		return tableName;
	}

	private void makeRelation(final Token t, final String relationString) {
		final char[] name = t.toString().toCharArray();
		StringBuffer tableName = new StringBuffer();
		StringBuffer columnName = new StringBuffer();
		boolean breaker = false;
		for (final char element : name) {
			if (element == '.') {
				breaker = true;
				continue;
			}
			if (!breaker) {
				tableName.append(element);
			} else {
				columnName.append(element);
			}
		}
		if (columnName.toString().isEmpty()) {
			columnName = tableName;
			tableName = emptyColumn(columnName, tableName);
		}
		DatabaseTable tv = new DatabaseTable();
		tv.setTitle(tableName.toString().toUpperCase());
		final Column column = new Column(columnName.toString().toUpperCase());
		if (!affectedViews.contains(tv)) {
			tv = getAssignedTableView(tv, column, tableName);
		}
		setRelation(columnName.toString(), tv, relationString);
	}

	private void setRelation(final String columnName, DatabaseTable tv, final String relationString) {
		if (notExists % 2 != 0) {
			positiv = !positiv;
			log.info(positiv + "");
		}
		final Relation target = new Relation(view.getTitle(), tv.getTitle().toUpperCase(), true);
		target.setColumn(columnName.toString().toUpperCase());
		target.setCondition(relationString);
		target.setPositive(positiv);
		target.setAndCondition(false);
		target.setView(true);
		schemaTarget.addRelation(target);
		if (!affectedViews.contains(tv)) {
			addViewTableFromList(tv);
		}
		if (affectedViews.contains(tv)) {
			final Relation source = new Relation(view, tv, false);
			source.setColumn(columnName.toString().toUpperCase());
			source.setCondition(relationString);
			source.setPositive(positiv);
			source.setAndCondition(false);
			if (tv instanceof DatabaseView) {
				source.setView(true);
			} else {
				source.setView(false);
			}
			tv = affectedViews.get(affectedViews.indexOf(tv));
			final DdlSchemaEditable schemaSource = tv.createDdlSchemaEditableObject();
			schemaSource.addRelation(source);
			tv.setDdlSchemaObject(schemaSource);
		}
		addRelationToList(tv);
	}

	private void addViewTableFromList(final DatabaseTable tv) {
		if (views.contains(tv)) {
			affectedViews.add(views.get(views.indexOf(tv)));
			if (subselect > 0) {
				subselectTables.add(tv.getTitle().toUpperCase());
			}
		} else if (tables.contains(tv)) {
			affectedViews.add(tables.get(tables.indexOf(tv)));
			if (subselect > 0) {
				subselectTables.add(tv.getTitle().toUpperCase());
			}
		}
	}

	private void checkAllViews() {
		for (int i = 0; i < affectedViews.size(); i++) {
			final DatabaseTable temp = affectedViews.get(i);
			if (!(positiveRelationViews.contains(temp)) && !(negativeRelationViews.contains(temp))) {
				makeRelationWithoutWhere(temp);
			}
		}
		positiv = true;
	}

	private void makeRelationWithoutWhere(DatabaseTable dt) {
		if (minus) {
			positiv = false;
		}
		if (notExists % 2 != 0) {
			positiv = false;
		}
		final Relation target = new Relation(view.getTitle(), dt.getTitle().toUpperCase(), true);
		target.setCondition("complete");
		target.setPositive(positiv);
		target.setAndCondition(false);
		target.setView(true);
		schemaTarget.addRelation(target);
		if (affectedViews.contains(dt)) {
			final Relation source = new Relation(view, dt, false);
			source.setCondition("complete");
			source.setPositive(positiv);
			source.setAndCondition(false);
			if (dt instanceof DatabaseView) {
				source.setView(true);
			} else {
				source.setView(false);
			}
			dt = affectedViews.get(affectedViews.indexOf(dt));
			final DdlSchemaEditable schemaSource = dt.createDdlSchemaEditableObject();
			schemaSource.addRelation(source);
			dt.setDdlSchemaObject(schemaSource);
		}
		addRelationToList(dt);
	}

	private void addRelationToList(final DatabaseTable dt) {
		if (positiv) {
			if (!positiveRelationViews.contains(dt)) {
				positiveRelationViews.add(dt);
			}
		} else {
			if (!negativeRelationViews.contains(dt)) {
				negativeRelationViews.add(dt);
			}
		}
	}

	/*
	 * private Token parseWhere(Token t, final boolean joinDetected, final String leftRightJoin) { if (!(t.kind ==
	 * ParserConstants.AND) && !(t.kind == ParserConstants.OR)) { String relationString = ""; Token temp = t; boolean
	 * negativRelation = false; boolean exists = false; if (temp.kind != ParserConstants.NOT_LIKE) { if
	 * (temp.toString().toUpperCase().equals("NOT")) { negativRelation = true; temp = parser.getNextToken(); if
	 * (temp.toString().equals("(")) { temp = parser.getNextToken(); } else if
	 * (temp.toString().toUpperCase().equals("EXISTS")) { existsOrNot = "NOT EXISTS ("; temp = parser.getNextToken(); if
	 * (temp.toString().equals("(")) { temp = parser.getNextToken(); } return t; } } } if
	 * (temp.toString().toUpperCase().equals("EXISTS")) { exists = true; existsOrNot = "EXISTS ("; if
	 * (temp.toString().equals("(")) { temp = parser.getNextToken(); } return t; } t = parser.getNextToken(); if (t.kind
	 * == ParserConstants.IDENTIFIER) { if (t.toString().toUpperCase().equals("FROM")) { where = false; from = true; } }
	 * switch (t.kind) { case ParserConstants.NE: positiv = false; t = parser.getNextToken(); relationString =
	 * temp.toString() + " != " + t.toString(); break; case ParserConstants.NOT_LIKE: positiv = false; t =
	 * parser.getNextToken(); relationString = temp.toString() + " NOT LIKE " + t.toString(); break; case
	 * ParserConstants.EQ: positiv = true; t = parser.getNextToken(); relationString = temp.toString() + " = " +
	 * t.toString(); break; case ParserConstants.LIKE: positiv = true; t = parser.getNextToken(); relationString =
	 * temp.toString() + " LIKE " + t.toString(); break; case ParserConstants.BETWEEN: positiv = true; t =
	 * parser.getNextToken(); parser.getNextToken(); final Token tempBetween = t; t = parser.getNextToken();
	 * relationString = temp.toString() + " BETWEEN " + tempBetween.toString() + " AND " + t.toString(); if
	 * (tempBetween.kind == ParserConstants.IDENTIFIER) { makeRelation(tempBetween, relationString); } break; case
	 * ParserConstants.GE: positiv = true; t = parser.getNextToken(); relationString = temp.toString() + " => " +
	 * t.toString(); break; case ParserConstants.GT: positiv = true; t = parser.getNextToken(); relationString =
	 * temp.toString() + " > " + t.toString(); break; case ParserConstants.IN: positiv = true; t = inDetected(t, temp,
	 * relationString); break; case ParserConstants.LE: positiv = true; t = parser.getNextToken(); relationString =
	 * temp.toString() + " <= " + t.toString(); break; case ParserConstants.LT: t = parser.getNextToken(); if (t.kind ==
	 * ParserConstants.GT) { positiv = false; t = parser.getNextToken(); relationString = temp.toString() + " != " +
	 * t.toString(); } else { positiv = true; relationString = temp.toString() + " < " + t.toString(); } break; case
	 * ParserConstants.NOT: positiv = false; t = parser.getNextToken(); if (t.kind == ParserConstants.BETWEEN) { t =
	 * parser.getNextToken(); parser.getNextToken(); final Token temp2 = t; t = parser.getNextToken(); relationString =
	 * temp.toString() + " NOT BETWEEN " + temp2.toString() + " AND " + t.toString(); if (temp2.kind ==
	 * ParserConstants.IDENTIFIER) { makeRelation(temp2, relationString); } } else if (t.kind == ParserConstants.IN) { t
	 * = inDetected(t, temp, relationString); relationString = "NOT(" + relationString + ")"; } break; default: temp =
	 * new Token(); t = new Token(); break; } if (negativRelation) { positiv = !positiv; negativRelation = false; if
	 * (!exists) { relationString = "NOT(" + relationString + ")"; } } if (joinDetected) { relationString =
	 * leftRightJoin + " JOIN ON(" + relationString + ")"; } if (minus) { positiv = !positiv; } if (t.kind ==
	 * ParserConstants.IDENTIFIER) { if (exists) { relationString = existsOrNot + relationString + ")"; exists = false;
	 * } if (t.toString().toUpperCase().equals("ANY") || t.toString().toUpperCase().equals("ALL")) { anyOrAllDetected(t,
	 * temp, relationString); } else { makeRelation(t, relationString); } } if (temp.kind == ParserConstants.IDENTIFIER)
	 * { if (exists) { relationString = existsOrNot + relationString + ")"; exists = false; } makeRelation(temp,
	 * relationString); } } return t; }
	 */

}
