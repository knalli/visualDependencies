/**
 * $Id$
 */
package de.unibonn.inf.dbdependenciesui.metadata;

import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseTrigger;

/**
 * A parser interface for a sql trigger parser implementation.
 * 
 * @author Andre Kasper
 * @author Jan Philipp
 * @version 1.0
 */
public interface ITriggerSqlParser extends ISqlParser {

	/**
	 * Perform the parsing of the given trigger object.
	 * 
	 * @param trigger
	 * @return
	 */
	public boolean parse(DatabaseTrigger trigger);

	/**
	 * Returns an error message, which can occur during parsing
	 * 
	 * @return String
	 */
	public String getErrorMessage();

}
