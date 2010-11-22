package de.unibonn.inf.dbdependenciesui.hibernate.impl;

import java.io.Serializable;

import org.hibernate.Session;

import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseTrigger;

/**
 * This is the concrete implementation of a trigger dao.
 * 
 * @author Andre Kasper
 * @author Jan Philipp
 * 
 */
public class GenericHibernateDatabaseTriggerDAO extends
		AbstractGenericHibernateDAO<DatabaseTrigger, Serializable> {

	public GenericHibernateDatabaseTriggerDAO(final Session session) {
		super(session);
	}

	@Override
	public DatabaseTrigger create() {
		return new DatabaseTrigger();
	}

}
