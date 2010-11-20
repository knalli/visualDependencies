package de.unibonn.inf.dbdependenciesui.ui.graphs.tests;

import javax.transaction.NotSupportedException;

import junit.framework.Assert;
import junit.framework.JUnit4TestAdapter;

import org.junit.Before;
import org.junit.Test;

import de.unibonn.inf.dbdependenciesui.TestFixtures;
import de.unibonn.inf.dbdependenciesui.hibernate.HibernateDAOFactory;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseConnection;
import de.unibonn.inf.dbdependenciesui.tests.HibernateTestingUtil;

public class GraphFixturesTest {

	@Before
	public void setUpBefore() throws Exception {
		HibernateTestingUtil.overrideConfiguration(true);
	}

	/**
	 * Return the suite adapter.
	 * 
	 * This is used for test suites - a workaround for jUnit 3.x Test Suites.
	 * 
	 * @return
	 */
	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(GraphFixturesTest.class);
	}

	@Test
	public final void testCreateDatabaseModels02() throws NotSupportedException {
		DatabaseConnection connection = null;
		if (connection == null) {
			connection = TestFixtures.createOnlineDatabaseModels02();
			HibernateDAOFactory.closeSession();

			Assert.assertNotNull("Connection object exists.", connection);
		}
	}

}
