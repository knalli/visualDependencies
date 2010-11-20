package de.unibonn.inf.dbdependenciesui.hibernate.models.tests;

import java.io.Serializable;

import junit.framework.JUnit4TestAdapter;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.unibonn.inf.dbdependenciesui.hibernate.HibernateDAOFactory;
import de.unibonn.inf.dbdependenciesui.hibernate.HibernateUtil;
import de.unibonn.inf.dbdependenciesui.hibernate.IGenericDAO;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseConnection;
import de.unibonn.inf.dbdependenciesui.tests.HibernateTestingUtil;

public class DatabaseConnectionTest {

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
		return new JUnit4TestAdapter(DatabaseConnectionTest.class);
	}

	@Test
	public void testDatabaseConnection() {
		final DatabaseConnection conn = new DatabaseConnection();
		conn.setTitle("Test");
		HibernateUtil.getSession().save(conn);
	}

	@Test(expected = HibernateException.class)
	public void testDatabaseConnectionEmptyFailed() {
		final DatabaseConnection conn = new DatabaseConnection();
		HibernateUtil.getSession().save(conn);
	}

	@Test
	public void testGetTitleReloadedViaHibernate() {
		final Session session = HibernateUtil.getSession();
		final DatabaseConnection conn = new DatabaseConnection();
		conn.setTitle("Test1");
		session.save(conn);
		Assert.assertTrue("Entity id is greater zero.", conn.getId() > 0);

		final Object conn2 = session.load(DatabaseConnection.class, conn
				.getId());
		if (conn2 instanceof DatabaseConnection) {
			Assert.assertEquals("Entity title attribute is correct.", "Test1",
					((DatabaseConnection) conn2).getTitle());
		} else {
			Assert.fail("Entity is not a DatabaseConnection.");
		}
	}

	@Test
	public void testGetTitleReloadedViaDAO() {
		IGenericDAO<DatabaseConnection, Serializable> dao = HibernateDAOFactory
				.getConnectionDAO();

		final DatabaseConnection conn = dao.create();
		conn.setTitle("Test2");
		dao.makePersistent(conn);
		Assert.assertTrue(conn.getId() > 0);
		HibernateDAOFactory.closeSession();

		dao = HibernateDAOFactory.getConnectionDAO();
		final Object conn2 = dao.findById(conn.getId(), false);
		if (conn2 instanceof DatabaseConnection) {
			Assert.assertEquals("Test2", ((DatabaseConnection) conn2)
					.getTitle());
		} else {
			Assert.fail("Object is not a DatabaseConnection.");
		}
	}

	@Test
	public void testSetTitleDirectly() {
		final DatabaseConnection conn = new DatabaseConnection();
		conn.setTitle("Test3");
		Assert.assertEquals("Entity title attribute is correct.", "Test3", conn
				.getTitle());
	}

}
