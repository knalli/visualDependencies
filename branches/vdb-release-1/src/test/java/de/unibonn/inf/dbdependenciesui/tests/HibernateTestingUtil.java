/**
 * $Id: Main.java 41 2009-03-01 16:21:20Z philipp $
 */
package de.unibonn.inf.dbdependenciesui.tests;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;

import de.unibonn.inf.dbdependenciesui.hibernate.HibernateUtil;

/**
 * Special variant of the {@link HibernateUtil} wrapper class.
 * 
 * This class can override the global configuration/factory instances for
 * independ tests.
 * 
 * @author Andre Kasper
 * @author Jan Philipp
 */
public class HibernateTestingUtil {

	private static SessionFactory factory;

	private static void initializeFactory(final boolean force) {
		if (force || (HibernateTestingUtil.factory == null)) {
			if (HibernateTestingUtil.factory != null) {
				System.err
						.println("HIBERNATE TEST AREA: OVERRIDING OLD FACTORY..");
				HibernateTestingUtil.factory.close();
			}
			final AnnotationConfiguration configuration = new AnnotationConfiguration()
					.configure();
			configuration.setProperty("hibernate.connection.url",
					"jdbc:hsqldb:mem:dbvisapptest");
			configuration.setProperty("hbm2ddl.auto", "create-drop");
			HibernateTestingUtil.factory = configuration.buildSessionFactory();
		}
	}

	/**
	 * Override the global {@link HibernateUtil} object with a new test
	 * specified memory located session factory.
	 */
	public static void overrideConfiguration() {
		HibernateTestingUtil.overrideConfiguration(false);
	}

	/**
	 * Override the global {@link HibernateUtil} object with a new test
	 * specified memory located session factory.
	 */
	public static void overrideConfiguration(final boolean force) {
		System.err.println("HIBERNATE TEST AREA: OVERRIDING FACTORY..");
		HibernateTestingUtil.initializeFactory(force);
		HibernateUtil.setSessionFactory(HibernateTestingUtil.factory);
	}
}
