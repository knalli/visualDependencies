package de.unibonn.inf.dbdependenciesui.metadata.impl.mysql5;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.medfoster.sqljep.Parser;
import org.medfoster.sqljep.ParserConstants;
import org.medfoster.sqljep.Token;
import org.medfoster.sqljep.TokenMgrError;

import de.unibonn.inf.dbdependenciesui.Configuration;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseProcedure;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.ProcedureSchemaEditable;
import de.unibonn.inf.dbdependenciesui.metadata.MetaDataFactory.Vendor;
import de.unibonn.inf.dbdependenciesui.metadata.impl.AbstractProcedureSqlParser;

/**
 * Concrete implementation of a mysql trigger sql parser. This class parses the trigger body sql and retrives the used
 * and affected tables.
 * 
 * @author Andre Kasper
 * @author Jan Philipp
 * @author Marc Kastleiner
 * @since 1.3
 */
public class MySqlProcedureSqlParserImpl extends AbstractProcedureSqlParser {

	protected final Logger log = Logger.getLogger(Configuration.LOGGER);

	/**
	 * This is the current parser token.
	 */
	protected Token t;

	/**
	 * This is the internal represenation of an semicolon ";". We need this because the sql parser we used cannot handle
	 * that symbol which are not part of a standard sql query.
	 */
	protected String SEMICOLON = "S__E_M__I__C__O__L__O__N";

	/**
	 * This is the internal represenation of an new prefix ":NEW.". We need this because the sql parser we used cannot
	 * handle that symbol which are not part of a standard pl/sql query.
	 */
	protected String NEW = "N__E__W__.";

	/**
	 * This is the internal represenation of an old prefix ":OLD.". We need this because the sql parser we used cannot
	 * handle that symbol which are not part of a standard pl/sql query.
	 */
	protected String OLD = "O__L__D__.";

	/**
	 * This is the internal represenation of an variable identifier prefix "%.". We need this because the sql parser we
	 * used cannot handle that symbol which are not part of a standard pl/sql query.
	 */
	protected String PERCENT = "PERCENT__";

	/**
	 * Extension part for {@link ParserConstants}: open brace identifier
	 */
	protected int OPEN_BRACE = 40;

	/**
	 * Extension part for {@link ParserConstants}: close brace identifier
	 */
	protected int CLOSE_BRACE = 41;

	private final List<String> usedTables = new ArrayList<String>();

	protected List<String> affectedDeletedTables = new ArrayList<String>();

	protected List<String> affectedInsertedTables = new ArrayList<String>();

	protected List<String> affectedUpdatedTables = new ArrayList<String>();

	private final Pattern patternMatchSemicolons;

	private final Pattern patternMatchPrefixOld;

	private final Pattern patternMatchPrefixNew;

	private final Pattern patternMatchDeclaration;

	private final Pattern patternMatchComments;

	private final Pattern patternMatchPercentSym;

	public MySqlProcedureSqlParserImpl() {
		super(Vendor.MYSQL);

		patternMatchSemicolons = Pattern.compile("(;)");
		patternMatchPrefixOld = Pattern.compile("(:OLD\\.)", Pattern.CASE_INSENSITIVE);
		patternMatchPrefixNew = Pattern.compile("(:NEW\\.)", Pattern.CASE_INSENSITIVE);
		patternMatchPercentSym = Pattern.compile("(%)", Pattern.CASE_INSENSITIVE);
		patternMatchDeclaration = Pattern.compile("(:=)");
		patternMatchComments = Pattern.compile("(([\\s])*--.*([\\n]+))");
	}

	@Override
	public String getErrorMessage() {
		return null;
	}

	@Override
	public boolean parse(final DatabaseProcedure procedure) {
		final ProcedureSchemaEditable schema = procedure.createProcedureSchemaEditableObject();

		final String body = schema.getBody();
		initializeProcedureSqlResults();

		try {
			parseProcedureSql(body);

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

			procedure.setProcedureSchemaObject(schema);
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

		// Assure that there are no : in new and old prefixes.
		result = patternMatchPrefixOld.matcher(result).replaceAll(OLD);
		result = patternMatchPrefixNew.matcher(result).replaceAll(NEW);

		// Assure that there are no : in declarations.
		result = patternMatchDeclaration.matcher(result).replaceAll("=");

		// Remove comments
		result = patternMatchComments.matcher(result).replaceAll("\n");

		result.trim();

		return result;
	}

	protected void initializeProcedureSqlResults() {
		usedTables.clear();
		affectedDeletedTables.clear();
		affectedInsertedTables.clear();
		affectedUpdatedTables.clear();
	}

	public void parseProcedureSql(String body) throws TokenMgrError {

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
				if ((t.kind == ParserConstants.IDENTIFIER) && SEMICOLON.equals(token)) {
					inSelect = false;
					listSelects.add(tokenStack);
				} else {
					tokenStack.add(t);
				}
			} else if (inDelete) {
				if ((t.kind == ParserConstants.IDENTIFIER) && SEMICOLON.equals(token)) {
					inDelete = false;
					listDeletes.add(tokenStack);
				} else {
					tokenStack.add(t);
				}
			} else if (inInsert) {
				if ((t.kind == ParserConstants.IDENTIFIER) && SEMICOLON.equals(token)) {
					inInsert = false;
					listInserts.add(tokenStack);
				} else {
					tokenStack.add(t);
				}
			} else if (inUpdate) {
				if ((t.kind == ParserConstants.IDENTIFIER) && SEMICOLON.equals(token)) {
					inUpdate = false;
					listUpdates.add(tokenStack);
				} else {
					tokenStack.add(t);
				}
			} else if ((t.kind == ParserConstants.IDENTIFIER) && "SELECT".equalsIgnoreCase(token)) {
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

	/**
	 * If any select queries found, get the tablename and add it to the used tables list unless it exists.
	 * 
	 * @param listSelects
	 */
	private void extractUsedTables(final List<List<Token>> listSelects) {
		if (listSelects.size() > 0) {
			for (final List<Token> tokens : listSelects) {
				boolean alreadyFrom = false;
				for (final Token token : tokens) {
					final String value = token.toString().toUpperCase();
					if ("FROM".equalsIgnoreCase(value)) {
						alreadyFrom = true;
					} else if (alreadyFrom) {
						if (token.kind == ParserConstants.IDENTIFIER) {
							if (!usedTables.contains(value)) {
								usedTables.add(value);
							}
							break;
						} else if (token.kind == OPEN_BRACE) {
							alreadyFrom = false;
						}
					}
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
