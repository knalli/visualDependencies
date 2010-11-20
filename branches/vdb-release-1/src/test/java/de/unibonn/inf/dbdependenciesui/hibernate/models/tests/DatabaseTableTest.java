package de.unibonn.inf.dbdependenciesui.hibernate.models.tests;

import java.io.Serializable;

import junit.framework.JUnit4TestAdapter;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.unibonn.inf.dbdependenciesui.controller.Controller;
import de.unibonn.inf.dbdependenciesui.hibernate.HibernateDAOFactory;
import de.unibonn.inf.dbdependenciesui.hibernate.IGenericDAO;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseConnection;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseTable;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.DdlSchema;
import de.unibonn.inf.dbdependenciesui.tests.HibernateTestingUtil;

public class DatabaseTableTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {}

	@Before
	public void setUpBefore() throws Exception {
		HibernateTestingUtil.overrideConfiguration(true);
	}

	/**
	 * Return the suite adapter. This is used for test suites - a workaround for jUnit 3.x Test Suites.
	 * 
	 * @return
	 */
	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(DatabaseTableTest.class);
	}

	@After
	public void tearDown() throws Exception {}

	@Test
	public void testGetTableByTitleAndConnectionId() {
		final IGenericDAO<DatabaseConnection, Serializable> dao = HibernateDAOFactory.getConnectionDAO();
		final DatabaseConnection connection = dao.create();
		final DatabaseTable table1 = new DatabaseTable();
		final DatabaseTable table2 = new DatabaseTable();

		connection.setTitle("abc");
		table1.setTitle("table1");
		table2.setTitle("table2");

		connection.addTable(table1);
		connection.addTable(table2);

		table1.setConnection(connection);
		table2.setConnection(connection);

		Controller.updateConnection(connection);
		HibernateDAOFactory.closeSession();

		final DatabaseTable table3 = Controller.loadTableByTitle(connection.getId(), table1.getTitle(), false);
		Assert.assertNotNull(table3);
		Assert.assertEquals("table1", table3.getTitle());

		final DatabaseTable table4 = Controller.loadTableByTitle(connection.getId(), "notexist", false);
		Assert.assertNull(table4);

		final DatabaseTable table5 = Controller.loadTableByTitle(0, table2.getTitle(), false);
		Assert.assertNull(table5);
	}

	@Test
	public final void testGetDdlSchemaObject() {
		final IGenericDAO<DatabaseTable, Serializable> dao = HibernateDAOFactory.getTableDAO();
		final DatabaseTable table = dao.create();

		DdlSchema ddlschema = table.createDdlSchemaEditableObject();

		ddlschema = new DdlSchema();
		table.setDdlSchemaObject(ddlschema);
		Assert.assertEquals(ddlschema, table.getDdlSchemaObject());
	}

	@Test
	public final void testCreateDdlSchemaEditableObject() {
		final IGenericDAO<DatabaseTable, Serializable> dao = HibernateDAOFactory.getTableDAO();
		final DatabaseTable table = dao.create();

		final DdlSchema ddlschema = table.createDdlSchemaEditableObject();
		Assert.assertNotNull(ddlschema);
	}

	@Test
	public final void testSetConnection() {
		final IGenericDAO<DatabaseTable, Serializable> dao = HibernateDAOFactory.getTableDAO();
		final DatabaseTable table = dao.create();

		final IGenericDAO<DatabaseConnection, Serializable> dao2 = HibernateDAOFactory.getConnectionDAO();
		final DatabaseConnection connection = dao2.create();

		table.setConnection(connection);
		Assert.assertEquals(connection, table.getConnection());

	}

}
