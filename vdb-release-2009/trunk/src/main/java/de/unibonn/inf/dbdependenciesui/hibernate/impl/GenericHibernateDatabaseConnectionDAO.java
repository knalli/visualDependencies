package de.unibonn.inf.dbdependenciesui.hibernate.impl;

import java.io.Serializable;

import org.hibernate.Session;

import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseConnection;

/**
 * This is the concrete implementation of a connection dao.
 * 
 * @author Andre Kasper
 * @author Jan Philipp
 * 
 */
final public class GenericHibernateDatabaseConnectionDAO extends
		AbstractGenericHibernateDAO<DatabaseConnection, Serializable> {

	public GenericHibernateDatabaseConnectionDAO(final Session session) {
		super(session);
	}

	@Override
	public DatabaseConnection create() {
		return new DatabaseConnection();
	}

}
