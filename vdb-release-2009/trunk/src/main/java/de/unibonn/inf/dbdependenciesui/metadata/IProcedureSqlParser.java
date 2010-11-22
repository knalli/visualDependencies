/**
 * $Id$
 */
package de.unibonn.inf.dbdependenciesui.metadata;

import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseProcedure;

/**
 * A parser interface for a sql trigger parser implementation.
 * 
 * @author Andre Kasper
 * @author Jan Philipp
 * @version 1.0
 */
public interface IProcedureSqlParser extends ISqlParser {

	/**
	 * Perform the parsing of the given procedure object.
	 * 
	 * @param procedure
	 * @return
	 */
	public boolean parse(DatabaseProcedure procedure);

	/**
	 * Returns an error message, which can occur during parsing
	 * 
	 * @return String
	 */
	public String getErrorMessage();

}
