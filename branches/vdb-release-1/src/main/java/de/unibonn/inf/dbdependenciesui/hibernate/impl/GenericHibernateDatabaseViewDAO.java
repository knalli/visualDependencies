package de.unibonn.inf.dbdependenciesui.hibernate.impl;

import java.io.Serializable;

import org.hibernate.Session;

import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseView;

/**
 * This is the concrete implementation of a view dao.
 * 
 * @author Andre Kasper
 * @author Jan Philipp
 * 
 */
public class GenericHibernateDatabaseViewDAO extends
		AbstractGenericHibernateDAO<DatabaseView, Serializable> {

	public GenericHibernateDatabaseViewDAO(final Session session) {
		super(session);
	}

	@Override
	public DatabaseView create() {
		return new DatabaseView();
	}

}
