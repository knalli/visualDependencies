package de.unibonn.inf.dbdependenciesui.metadata;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseConnection;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseProcedure;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseTable;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseTrigger;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseView;
import de.unibonn.inf.dbdependenciesui.metadata.MetaDataFactory.Vendor;

/**
 * An interface for a database analyzer implementation.
 * 
 * @author Andre Kasper
 * @author Jan Philipp
 * @version 1.0
 */
public interface IAnalyzer {
	/**
	 * Return the acutal implemented vendor.
	 * 
	 * @return
	 */
	Vendor getVendor();

	/**
	 * Perform analyzing step.
	 * 
	 * @param sqlConnection
	 * @param connection
	 */
	void analyze(Connection sqlConnection, final DatabaseConnection connection) throws SQLException;

	/**
	 * Returns a list of all tables in the schema
	 * 
	 * @return List all tables
	 */
	List<DatabaseTable> getTables();

	/**
	 * Returns a list of all views in the schema
	 * 
	 * @return List all views
	 */
	List<DatabaseView> getViews();

	/**
	 * Returns a list of all triggers in the schema
	 * 
	 * @return List all trigger
	 */
	List<DatabaseTrigger> getTriggers();

	/**
	 * Returns a list of all procedures in the schema
	 * 
	 * @return List all procedures
	 */
	List<DatabaseProcedure> getProcedures();

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
	 * Return a individuell description text for the given analyzer module.
	 * 
	 * @return
	 */
	String getDescription();
}
