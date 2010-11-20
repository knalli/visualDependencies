/**
 * $Id$
 */
package de.unibonn.inf.dbdependenciesui.metadata;

import java.util.List;

import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseTable;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseView;

/**
 * A parser interface for a sql view parser implementation.
 * 
 * @author Andre Kasper
 * @author Jan Philipp
 * @version 1.0
 */
public interface IViewSqlParser extends ISqlParser {

	/**
	 * Perform parsing the given objects.
	 * 
	 * @param tables
	 * @param views
	 * @param view
	 * @return
	 */
	public boolean parse(List<DatabaseTable> tables, List<DatabaseView> views, DatabaseView view);

	/**
	 * Returns a list off all positive relations of the the given view (where-condition)
	 * 
	 * @return List<String>
	 */
	public List<DatabaseTable> getPositiveRelationViews();

	/**
	 * Returns a list off all negative relations of the the given view (where-condition)
	 * 
	 * @return List<String>
	 */
	public List<DatabaseTable> getNegativeRelationViews();

	/**
	 * Returns a list off all relations of the the given view (from-condition)
	 * 
	 * @return List<String>
	 */
	public List<DatabaseTable> getAffectedViews();

	/**
	 * Returns an error message, which can occur during parsing
	 * 
	 * @return String
	 */
	public String getErrorMessage();

}
