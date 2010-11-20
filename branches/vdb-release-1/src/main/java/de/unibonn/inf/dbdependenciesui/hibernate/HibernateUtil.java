/**
 * $Id: Main.java 41 2009-03-01 16:21:20Z philipp $
 */
package de.unibonn.inf.dbdependenciesui.hibernate;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;

import de.unibonn.inf.dbdependenciesui.Configuration;

/**
 * This is the hibernate utilities wrapper class.
 * 
 * It storages the current session factory. A {@link #getSession()}-call will
 * generate a new session.
 * 
 * See {@link HibernateTestingUtil} for more details if you are in a test
 * enviroment.
 * 
 * @author Andre Kasper
 * @author Jan Philipp
 */
public class HibernateUtil {

	private static SessionFactory factory;

	/**
	 * Returns a new session instance of the hibernate session factory. Creates
	 * a new factory if not exists.
	 * 
	 * @return
	 */
	public static synchronized Session getSession() {
		if (HibernateUtil.factory == null) {
			final AnnotationConfiguration configuration = new AnnotationConfiguration()
					.configure();
			if ((Configuration.SETTINGS_PATH != null)
					&& !Configuration.SETTINGS_PATH.isEmpty()) {
				configuration.setProperty("hibernate.connection.url",
						"jdbc:hsqldb:file:" + Configuration.SETTINGS_PATH + "/"
								+ Configuration.INTERNAL_NAME);
			}
			HibernateUtil.factory = configuration.buildSessionFactory();
		}
		return HibernateUtil.factory.openSession();
	}

	/**
	 * Overrides or inits the hibernate session factory.
	 * 
	 * @param factory
	 */
	public static synchronized void setSessionFactory(
			final SessionFactory factory) {
		HibernateUtil.factory = factory;
	}

	/**
	 * Returns the hibernate session factory.
	 */
	public static synchronized SessionFactory getSessionFactory() {
		return HibernateUtil.factory;
	}

	/**
	 * Destroys and removes the hibernate session factory if exists.
	 */
	public static synchronized void destroySessionFactory() {
		if (HibernateUtil.factory != null) {
			HibernateUtil.factory.close();
			HibernateUtil.factory = null;
		}
	}
}
