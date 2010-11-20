/**
 * $Id$
 */
package de.unibonn.inf.dbdependenciesui.metadata.impl.oraclecommons;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.medfoster.sqljep.Parser;
import org.medfoster.sqljep.ParserConstants;
import org.medfoster.sqljep.Token;
import org.medfoster.sqljep.TokenMgrError;

import de.unibonn.inf.dbdependenciesui.Configuration;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseTrigger;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.TriggerSchemaEditable;
import de.unibonn.inf.dbdependenciesui.metadata.MetaDataFactory.Vendor;
import de.unibonn.inf.dbdependenciesui.metadata.impl.AbstractTriggerSqlParser;

/**
 * Concrete implementation of an oracle trigger sql parser. This class parses the trigegr body sql and retrives the used
 * and affected tables.
 * 
 * @author Andre Kasper
 * @author Jan Philipp
 */
public abstract class OracleTriggerSqlParserImpl extends AbstractTriggerSqlParser {

	protected final Logger log = Logger.getLogger(Configuration.LOGGER);

	/**
	 * This is the current parser token.
	 */
	protected Token t;

	/**
	 * This is the internal representation of an semicolon ";". We need this because the sql parser we used cannot
	 * handle that symbol which are not part of a standard sql query.
	 */
	protected String SEMICOLON = "S__E_M__I__C__O__L__O__N";

	/**
	 * This is the internal representation of the symbol ":". We need this because the sql parser we used cannot handle
	 * that symbol which are not part of a standard pl/sql query.
	 */
	protected String COLON = "C__O__L__O__N";

	/**
	 * This is the internal representation of an variable identifier prefix "%.". We need this because the sql parser we
	 * used cannot handle that symbol which are not part of a standard pl/sql query.
	 */
	protected String PERCENT = "P__E__R__C__E__N__T";

	/**
	 * Extension part for {@link ParserConstants}: open brace identifier
	 */
	protected int OPEN_BRACE = 40;

	/**
	 * Extension part for {@link ParserConstants}: close brace identifier
	 */
	protected int CLOSE_BRACE = 41;

	/**
	 * list of all used tables and views
	 */
	protected final List<String> usedTables = new ArrayList<String>();

	protected List<String> affectedDeletedTables = new ArrayList<String>();

	protected List<String> affectedInsertedTables = new ArrayList<String>();

	protected List<String> affectedUpdatedTables = new ArrayList<String>();

	/**
	 * Regular expression object (compiled): matching semicolons
	 */
	protected Pattern patternMatchSemicolons;

	/**
	 * Regular expression object (compiled): matching colons (refs)
	 */
	protected Pattern patternMatchPrefixColons;

	/**
	 * Regular expression object (compiled): matching declarations (:=)
	 */
	protected Pattern patternMatchDeclaration;

	/**
	 * Regular expression object (compiled): matching comments
	 */
	protected Pattern patternMatchComments;

	/**
	 * Regular expression object (compiled): matching semicolons
	 */
	protected Pattern patternMatchPercentSym;

	public OracleTriggerSqlParserImpl(final Vendor vendor) {
		super(vendor);
		initialize();
	}

	protected void initialize() {
		patternMatchSemicolons = Pattern.compile("(;)");
		patternMatchPrefixColons = Pattern.compile("(:)", Pattern.CASE_INSENSITIVE);
		patternMatchPercentSym = Pattern.compile("(%)", Pattern.CASE_INSENSITIVE);
		patternMatchDeclaration = Pattern.compile("(:=)");
		patternMatchComments = Pattern.compile("(([\\s])*--.*([\\n]+))");
	}

	@Override
	public String getErrorMessage() {
		return null;
	}

	@Override
	public boolean parse(final DatabaseTrigger trigger) {
		final TriggerSchemaEditable schema = trigger.createTriggerSchemaEditableObject();

		final String body = schema.getBody();
		initializeTriggerSqlResults();

		try {
			parseTriggerSql(body);

			for (final String tableName : usedTables) {
				schema.addUsedTable(tableName);
			}

			for (final String tableName : affectedDeletedTables) {
				schema.addAffectedDeletedTable(tableName);
			}
			for (final String tableName : affectedInsertedTables) {
				schema.addAffectedInsertedTable(tableName);
			}
			for (final String tableName : affectedUpdatedTables) {
				schema.addAffectedUpdatedTable(tableName);
			}

			trigger.setTriggerSchemaObject(schema);
		} catch (final TokenMgrError e) {
			log.log(Level.WARNING, e.getLocalizedMessage()
					+ String.format("Trigger (Original): %s; Trigger (Transformed): %s", body,
							convertToParserFriendlyString(body)));
			return false;
		}

		return true;
	}

	/**
	 * Convert the given string to a one the parser can handle with. It replaces comments, semcilons, :old and :new
	 * parts as they are not part of the sql query standard. SQLJep is only used for standard sql query.
	 * 
	 * @param string
	 * @return
	 */
	protected String convertToParserFriendlyString(final String string) {
		String result = (string == null) ? "" : string.trim();

		if (result.isEmpty()) { return result; }

		// Assure that the string does not contain a semicolon.
		result = patternMatchSemicolons.matcher(result).replaceAll(" " + SEMICOLON + " ");

		// Assure that there no variable prefixes (percent symbols).
		result = patternMatchPercentSym.matcher(result).replaceAll(PERCENT);

		// Assure that there are no : in declarations. This MUST be done before the colon's matching process.
		result = patternMatchDeclaration.matcher(result).replaceAll("=");

		// Assure that there are no : in new and old prefixes.
		result = patternMatchPrefixColons.matcher(result).replaceAll(COLON);

		// Remove comments
		result = patternMatchComments.matcher(result).replaceAll("\n");

		result.trim();

		return result;
	}

	public void initializeTriggerSqlResults() {
		usedTables.clear();
		affectedDeletedTables.clear();
		affectedInsertedTables.clear();
		affectedUpdatedTables.clear();
	}

	public void parseTriggerSql(String body) throws TokenMgrError {

		body = convertToParserFriendlyString(body);
		final Parser parser = new Parser(new StringReader(body));

		boolean inSelect = false;
		boolean inDelete = false;
		boolean inInsert = false;
		boolean inUpdate = false;
		int openBraces = 0;

		List<Token> tokenStack = null;
		final List<List<Token>> listSelects = new ArrayList<List<Token>>();
		final List<List<Token>> listDeletes = new ArrayList<List<Token>>();
		final List<List<Token>> listInserts = new ArrayList<List<Token>>();
		final List<List<Token>> listUpdates = new ArrayList<List<Token>>();

		// Parser: This part will get all queries as lists of tokens.
		while ((t = parser.getNextToken()).kind != ParserConstants.EOF) {
			final String token = t.toString();

			if (t.kind == OPEN_BRACE) {
				++openBraces;
				if (inSelect || inDelete || inInsert || inUpdate) {
					tokenStack.add(t);
				}
			} else if (t.kind == CLOSE_BRACE) {
				--openBraces;
				if (inSelect || inDelete || inInsert || inUpdate) {
					tokenStack.add(t);
				}
			} else if (inSelect) {
				if (((t.kind == ParserConstants.IDENTIFIER) && SEMICOLON.equals(token))
						|| "INSERT".equalsIgnoreCase(token) || "DELETE".equalsIgnoreCase(token)
						|| "UPDATE".equalsIgnoreCase(token)) {
					inSelect = false;
					listSelects.add(tokenStack);
				} else {
					tokenStack.add(t);
				}
			} else if (inDelete) {
				if (((t.kind == ParserConstants.IDENTIFIER) && SEMICOLON.equals(token))
						|| "INSERT".equalsIgnoreCase(token) || "SELECT".equalsIgnoreCase(token)
						|| "UPDATE".equalsIgnoreCase(token)) {
					inDelete = false;
					listDeletes.add(tokenStack);
				} else {
					tokenStack.add(t);
				}
			} else if (inInsert) {

				if (((t.kind == ParserConstants.IDENTIFIER) && SEMICOLON.equals(token))
						|| "DELETE".equalsIgnoreCase(token) || "SELECT".equalsIgnoreCase(token)
						|| "UPDATE".equalsIgnoreCase(token)) {
					inInsert = false;
					listInserts.add(tokenStack);
				} else {
					tokenStack.add(t);
				}
			} else if (inUpdate) {
				if (((t.kind == ParserConstants.IDENTIFIER) && SEMICOLON.equals(token))
						|| "INSERT".equalsIgnoreCase(token) || "SELECT".equalsIgnoreCase(token)
						|| "DELETE".equalsIgnoreCase(token)) {
					inUpdate = false;
					listUpdates.add(tokenStack);
				} else {
					tokenStack.add(t);
				}
			}

			if ((t.kind == ParserConstants.IDENTIFIER) && "SELECT".equalsIgnoreCase(token)) {
				inSelect = true;
				tokenStack = new ArrayList<Token>();
				tokenStack.add(t);
			} else if ((t.kind == ParserConstants.IDENTIFIER) && "DELETE".equalsIgnoreCase(token)) {
				inDelete = true;
				tokenStack = new ArrayList<Token>();
				tokenStack.add(t);
			} else if ((t.kind == ParserConstants.IDENTIFIER) && "INSERT".equalsIgnoreCase(token)) {
				inInsert = true;
				tokenStack = new ArrayList<Token>();
				tokenStack.add(t);
			} else if ((t.kind == ParserConstants.IDENTIFIER) && "UPDATE".equalsIgnoreCase(token)) {
				inUpdate = true;
				tokenStack = new ArrayList<Token>();
				tokenStack.add(t);
			}
		}

		extractUsedTables(listSelects);
		extractAffectedDeletedTables(listDeletes);
		extractAffectedInsertedTables(listInserts);
		extractAffectedUpdatedTables(listUpdates);
	}

	// /**
	// * If any select queries found, get the tablename and add it to the used tables list unless it exists.
	// *
	// * @param listSelects
	// */
	// private void extractUsedTables(final List<List<Token>> listSelects) {
	// if (listSelects.size() > 0) {
	// for (final List<Token> tokens : listSelects) {
	// boolean alreadyFrom = false;
	// for (final Token token : tokens) {
	// final String value = token.toString().toUpperCase();
	// if ("FROM".equalsIgnoreCase(value)) {
	// alreadyFrom = true;
	// } else if (alreadyFrom) {
	// if (token.kind == ParserConstants.IDENTIFIER) {
	// if (!usedTables.contains(value)) {
	// usedTables.add(value);
	// }
	// break;
	// } else if (token.kind == OPEN_BRACE) {
	// alreadyFrom = false;
	// }
	// }
	// }
	// }
	// }
	// }

	/**
	 * If any select queries found, get the tablenames and add them to the used tables list unless it exists.
	 * 
	 * @param listSelects
	 * @author Marc Kastleiner
	 * @version 2
	 */
	private void extractUsedTables(final List<List<Token>> listSelects) {
		ListIterator<Token> iter;
		int openbraces = 0;
		Boolean inserted = false;
		Token t;
		Boolean commaDetected = false;

		if (listSelects.size() > 0) {
			for (final List<Token> tokens : listSelects) {
				iter = tokens.listIterator();

				while (iter.hasNext()) {
					t = iter.next();
					openbraces = 0;
					if (t.toString().equalsIgnoreCase("FROM") || t.toString().equalsIgnoreCase("JOIN")) {
						t = iter.next();
						while (t.toString().equalsIgnoreCase("(")) {
							t = iter.next();
							openbraces++;
						}
						if (!t.toString().equalsIgnoreCase("SELECT")) {
							if ((t.kind == ParserConstants.IDENTIFIER)
									&& !usedTables.contains(t.toString().toUpperCase())) {
								usedTables.add(t.toString().toUpperCase());
								inserted = true;
							}
							if (iter.hasNext()) {
								t = iter.next();
							} else {
								break;
							}

							while ((openbraces > 0) || commaDetected || t.toString().equalsIgnoreCase(",")
									|| t.toString().equalsIgnoreCase("(")) {
								if (t.toString().equalsIgnoreCase("(")) {
									openbraces++;
									inserted = false;
								} else if (t.toString().equalsIgnoreCase(")")) {
									openbraces--;
									inserted = false;
								} else if ((inserted == false) && (t.kind == ParserConstants.IDENTIFIER)
										&& !usedTables.contains(t.toString().toUpperCase())) {
									usedTables.add(t.toString().toUpperCase());
									inserted = true;
								}

								if (t.toString().equalsIgnoreCase(",")) {
									commaDetected = true;
								} else {
									commaDetected = false;
								}

								if (iter.hasNext()) {
									t = iter.next();
								} else {
									break;
								}

							}
						}
					}
					// } else if (t.toString().equalsIgnoreCase("JOIN")) {
					// t = iter.next();
					// while (t.toString().equalsIgnoreCase("(")) {
					// t = iter.next();
					// openbraces++;
					// }
					// if (!t.toString().equalsIgnoreCase("SELECT")) {
					// if ((t.kind == ParserConstants.IDENTIFIER)
					// && !usedTables.contains(t.toString().toUpperCase())) {
					// usedTables.add(t.toString().toUpperCase());
					// }
					// if (iter.hasNext()) {
					// t = iter.next();
					// } else {
					// break;
					// }
					//
					// while ((openbraces > 0) || commaDetected || t.toString().equalsIgnoreCase(",")
					// || t.toString().equalsIgnoreCase("(")) {
					// if (t.toString().equalsIgnoreCase("(")) {
					// openbraces++;
					// } else if (t.toString().equalsIgnoreCase(")")) {
					// openbraces--;
					// } else if ((t.kind == ParserConstants.IDENTIFIER)
					// && !usedTables.contains(t.toString().toUpperCase())) {
					// usedTables.add(t.toString().toUpperCase());
					// }
					//
					// if (t.toString().equalsIgnoreCase(",")) {
					// commaDetected = true;
					// } else {
					// commaDetected = false;
					// }
					//
					// if (iter.hasNext()) {
					// t = iter.next();
					// } else {
					// break;
					// }
					//
					// }
					// }
					// }
				}
			}
		}
	}

	/**
	 * If any delete queries found, get the tablename and add it to the affected tables list unless it exists.
	 * 
	 * @param listDeletes
	 */
	private void extractAffectedDeletedTables(final List<List<Token>> listDeletes) {
		if (listDeletes.size() > 0) {
			for (final List<Token> tokens : listDeletes) {
				boolean alreadyDelete = false;
				boolean alreadyDeleteFrom = false;
				for (final Token token : tokens) {
					final String value = token.toString().toUpperCase();
					if ("DELETE".equalsIgnoreCase(value)) {
						alreadyDelete = true;
					} else if (alreadyDelete && "FROM".equalsIgnoreCase(value)) {
						alreadyDeleteFrom = true;
					} else if ("(".equalsIgnoreCase(value) || ")".equalsIgnoreCase(value)) {

					} else if (alreadyDeleteFrom && (token.kind == ParserConstants.IDENTIFIER)) {
						if (!affectedDeletedTables.contains(value)) {
							affectedDeletedTables.add(value);
						}
						break;
					}
				}
			}
		}
	}

	/**
	 * If any insert queries found, get the tablename and add it to the affected tables list unless it exists.
	 * 
	 * @param listInserts
	 */
	private void extractAffectedInsertedTables(final List<List<Token>> listInserts) {
		if (listInserts.size() > 0) {
			for (final List<Token> tokens : listInserts) {
				boolean alreadyInserted = false;
				boolean alreadyInsertedInto = false;
				for (final Token token : tokens) {
					final String value = token.toString().toUpperCase();
					if ("INSERT".equalsIgnoreCase(value)) {
						alreadyInserted = true;
					} else if (alreadyInserted && "INTO".equalsIgnoreCase(value)) {
						alreadyInsertedInto = true;
					} else if ("(".equalsIgnoreCase(value) || ")".equalsIgnoreCase(value)) {

					} else if (alreadyInsertedInto && (token.kind == ParserConstants.IDENTIFIER)) {
						if (!affectedInsertedTables.contains(value)) {
							affectedInsertedTables.add(value);
						}
						break;
					}
				}
			}
		}
	}

	/**
	 * If any update queries found, get the tablename and add it to the affected tables list unless it exists.
	 * 
	 * @param listUpdates
	 */
	private void extractAffectedUpdatedTables(final List<List<Token>> listUpdates) {
		if (listUpdates.size() > 0) {
			for (final List<Token> tokens : listUpdates) {
				boolean alreadyUpdated = false;
				for (final Token token : tokens) {
					final String value = token.toString().toUpperCase();
					if ("UPDATE".equalsIgnoreCase(value)) {
						alreadyUpdated = true;
					} else if ("(".equalsIgnoreCase(value) || ")".equalsIgnoreCase(value)) {

					} else if (alreadyUpdated && (token.kind == ParserConstants.IDENTIFIER)) {
						if (!affectedUpdatedTables.contains(value)) {
							affectedUpdatedTables.add(value);
						}
						break;
					}
				}
			}
		}
	}

	@Override
	public void clear() {

	}

	@Override
	public List<String> getUsedTables() {
		return usedTables;
	}

	@Override
	public List<String> getTablesOnDelete() {
		return affectedDeletedTables;
	}

	@Override
	public List<String> getTablesOnInsert() {
		return affectedInsertedTables;
	}

	@Override
	public List<String> getTablesOnUpdate() {
		return affectedUpdatedTables;
	}

}
