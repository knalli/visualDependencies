package de.unibonn.inf.dbdependenciesui.hibernate.impl;

import java.io.Serializable;

import org.hibernate.Session;

import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseProcedure;

/**
 * This is the concrete implementation of a procedure dao.
 * 
 * @author Marc Kastleiner
 */
public class GenericHibernateDatabaseProcedureDAO extends AbstractGenericHibernateDAO<DatabaseProcedure, Serializable> {

	public GenericHibernateDatabaseProcedureDAO(final Session session) {
		super(session);
	}

	@Override
	public DatabaseProcedure create() {
		return new DatabaseProcedure();
	}

}
