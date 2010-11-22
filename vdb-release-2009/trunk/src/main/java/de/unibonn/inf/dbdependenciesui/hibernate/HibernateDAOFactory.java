package de.unibonn.inf.dbdependenciesui.hibernate;

import java.io.Serializable;

import org.hibernate.Session;

import de.unibonn.inf.dbdependenciesui.hibernate.impl.GenericHibernateDatabaseConnectionDAO;
import de.unibonn.inf.dbdependenciesui.hibernate.impl.GenericHibernateDatabaseProcedureDAO;
import de.unibonn.inf.dbdependenciesui.hibernate.impl.GenericHibernateDatabaseTableDAO;
import de.unibonn.inf.dbdependenciesui.hibernate.impl.GenericHibernateDatabaseTriggerDAO;
import de.unibonn.inf.dbdependenciesui.hibernate.impl.GenericHibernateDatabaseViewDAO;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseConnection;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseProcedure;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseTable;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseTrigger;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseView;

/**
 * This is the hibernate dao factory object.
 * 
 * A data access object is a design pattern used for capselung the actual data
 * access from the user. In this case, the application should not touch any
 * hibernate relevant issues and interact only with a dao or, better, the main
 * controller.
 * 
 * This object creates all required daos and manages the session management. See
 * {@link HibernateUtil#getSession()}.
 * 
 * @author Andre Kasper
 * @author Jan Philipp
 * 
 */
public class HibernateDAOFactory {
	private static Session session = null;

	public static Session getSession() {
		if (HibernateDAOFactory.session == null) {
			HibernateDAOFactory.session = HibernateUtil.getSession();
		}
		return HibernateDAOFactory.session;
	}

	public static void setSession(final Session session) {
		HibernateDAOFactory.session = session;
	}

	public static IGenericDAO<DatabaseConnection, Serializable> getConnectionDAO() {
		final Session session = HibernateDAOFactory.getSession();
		return new GenericHibernateDatabaseConnectionDAO(session);
	}

	public static IGenericDAO<DatabaseTable, Serializable> getTableDAO() {
		final Session session = HibernateDAOFactory.getSession();
		return new GenericHibernateDatabaseTableDAO(session);
	}

	public static IGenericDAO<DatabaseTrigger, Serializable> getTriggerDAO() {
		final Session session = HibernateDAOFactory.getSession();
		return new GenericHibernateDatabaseTriggerDAO(session);
	}

	public static IGenericDAO<DatabaseProcedure, Serializable> getProcedureDAO() {
		final Session session = HibernateDAOFactory.getSession();
		return new GenericHibernateDatabaseProcedureDAO(session);
	}

	public static IGenericDAO<DatabaseView, Serializable> getViewDAO() {
		final Session session = HibernateDAOFactory.getSession();
		return new GenericHibernateDatabaseViewDAO(session);
	}

	public static void closeSession() {
		if (HibernateDAOFactory.session != null) {
			HibernateDAOFactory.flushSession();
			HibernateDAOFactory.session.close();
			HibernateDAOFactory.session = null;
		}
	}

	public static void flushSession() {
		if (HibernateDAOFactory.session != null) {
			HibernateDAOFactory.session.flush();
		}
	}
}
