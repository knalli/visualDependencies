/**
 * $Id$
 */
package de.unibonn.inf.dbdependenciesui.metadata;

import java.util.Observable;
import java.util.Observer;

import de.unibonn.inf.dbdependenciesui.metadata.MetaDataFactory.Vendor;

/**
 * A parser interface for a sql parser implementation.
 * 
 * @author Andre Kasper
 * @author Jan Philipp
 * @version 1.0
 */
public interface ISqlParser {
	/**
	 * Return the acutal implemented vendor.
	 * 
	 * @return
	 */
	Vendor getVendor();

	// /**
	// * Parses the given sql query string and returns a result object. Note:
	// The
	// * result is always returned, even if the string is empty or the query is
	// * invalid.
	// *
	// * @param query
	// * a sql query string
	// * @throws IllegalArgumentException
	// * when string is null or could not be parsed
	// * @return true if query-string is parsed successful else false
	// */
	// boolean parse();

	/**
	 * Notifies the parser object disposes all possible cached information about results.
	 */
	void clear();

	/**
	 * Adds an observer to the set of observers for this object, provided that it is not the same as some observer
	 * already in the set. The order in which notifications will be delivered to multiple observers is not specified.
	 * See the class comment.
	 * 
	 * @param o
	 *            an observer to be added.
	 * @throws NullPointerException
	 *             if the parameter o is null.
	 * @see Observable#addObserver(Observer)
	 */
	void addObserver(Observer o);

	/**
	 * Return a individuell description text for the given parser module.
	 * 
	 * @return
	 */
	String getDescription();
}
