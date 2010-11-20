package de.unibonn.inf.dbdependenciesui.hibernate.impl;

import java.io.Serializable;

import org.hibernate.Session;

import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseTable;

/**
 * This is the concrete implementation of a table dao.
 * 
 * @author Andre Kasper
 * @author Jan Philipp
 * 
 */
public class GenericHibernateDatabaseTableDAO extends
		AbstractGenericHibernateDAO<DatabaseTable, Serializable> {

	public GenericHibernateDatabaseTableDAO(final Session session) {
		super(session);
	}

	@Override
	public DatabaseTable create() {
		return new DatabaseTable();
	}

}
