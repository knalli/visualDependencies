/**
 *
 */
package de.unibonn.inf.dbdependenciesui.metadata.tests;

import java.sql.Connection;
import java.util.List;

import javax.transaction.NotSupportedException;

import junit.framework.JUnit4TestAdapter;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.unibonn.inf.dbdependenciesui.Configuration;
import de.unibonn.inf.dbdependenciesui.TestFixtures;
import de.unibonn.inf.dbdependenciesui.hibernate.HibernateDAOFactory;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseConnection;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseTable;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseTrigger;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseView;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.DdlSchema;
import de.unibonn.inf.dbdependenciesui.metadata.IAnalyzer;
import de.unibonn.inf.dbdependenciesui.metadata.MetaDataFactory;
import de.unibonn.inf.dbdependenciesui.metadata.MetaDataFactory.Vendor;
import de.unibonn.inf.dbdependenciesui.metadata.impl.AbstractAnalyzer;
import de.unibonn.inf.dbdependenciesui.metadata.impl.oracle11.Oracle11AnalyzerImpl;
import de.unibonn.inf.dbdependenciesui.tests.HibernateTestingUtil;

/**
 * Test cases for oracle analyzer.
 * 
 * @author Jan Philipp
 */
public class OracleAnalyzerImplTest {

	private static Connection conn;

	private static DatabaseConnection connection;

	private static IAnalyzer analyser;

	private static List<DatabaseTable> databaseTables;
	private static List<DatabaseView> databaseViews;
	private static List<DatabaseTrigger> databaseTriggers;
	private String objectName;
	private DatabaseTable table;
	private DatabaseView view;
	private DatabaseTrigger trigger;
	private DdlSchema schema;

	private final int amountTables = 7;
	private final int amountViews = 17;
	private final int amountTriggers = 15;

	/**
	 * Return the suite adapter. This is used for test suites - a workaround for jUnit 3.x Test Suites.
	 * 
	 * @return
	 */
	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(OracleAnalyzerImplTest.class);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		HibernateTestingUtil.overrideConfiguration(true);

		Configuration.configure();
			Class.forName("oracle.jdbc.OracleDriver");
			
		if (!TestFixtures.REAL_DB_ENABLED) {
			throw new NotSupportedException(
					"Debug connection is not available.");
		}
			
		connection = new DatabaseConnection();
		connection.setHost(TestFixtures.REAL_DB_CONN_HOST);
		connection.setDatabase(TestFixtures.REAL_DB_CONN_SCHM);
		connection.setPort(TestFixtures.REAL_DB_CONN_PORT);
		connection.setUsername(TestFixtures.REAL_DB_CONN_USER);
		connection.setPassword(TestFixtures.REAL_DB_CONN_PASS);

		conn = MetaDataFactory.getConnectionBuilder(Vendor.ORACLE).createConnection(connection);
		analyser = new Oracle11AnalyzerImpl();
		analyser.analyze(conn, connection);
		databaseTables = analyser.getTables();
		databaseViews = analyser.getViews();
		databaseTriggers = analyser.getTriggers();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		conn.close();
		HibernateDAOFactory.closeSession();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {}

	/**
	 * Test method for
	 * {@link de.unibonn.inf.dbdependenciesui.metadata.impl.oraclecommons.OracleAnalyzerImpl#OracleAnalyzerImpl()} .
	 */
	@Test
	public final void testOracleAnalyzerImpl() {
		Assert.assertTrue("ANALYZER IS AN ABSTRACT ", (analyser instanceof AbstractAnalyzer));
	}

	/**
	 * Test method for
	 * {@link de.unibonn.inf.dbdependenciesui.metadata.impl.oraclecommons.OracleAnalyzerImpl#analyze(java.sql.Connection, de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseConnection)}
	 * .
	 */
	@Test
	public final void testAnalyze() {
	// Assert.fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link de.unibonn.inf.dbdependenciesui.metadata.impl.oraclecommons.OracleAnalyzerImpl#analyzeDatabase()} .
	 */
	@Test
	public final void testAnalyzeDatabase() {
		Assert.assertNotNull(analyser);
		Assert.assertNotNull(conn);
		Assert.assertNotNull(connection);
		Assert.assertFalse(databaseTables.isEmpty());
		Assert.assertFalse(databaseViews.isEmpty());
		Assert.assertFalse(databaseTriggers.isEmpty());
		Assert.assertEquals("Number of tables: " + databaseTables.size(), amountTables + 23, databaseTables.size());
		Assert.assertEquals("Number of views", amountViews, databaseViews.size());
		Assert.assertEquals("Number of triggers", amountTriggers, databaseTriggers.size());
	}

	/**
	 * Test method for
	 * {@link de.unibonn.inf.dbdependenciesui.metadata.impl.oraclecommons.OracleAnalyzerImpl#getTables()} .
	 */
	@Test
	public final void testGetTables() {
		for (int i = 1; i < amountTables + 1; i++) {
			objectName = "TABLENAME" + i;
			table = new DatabaseTable();
			table.setTitle(objectName);
			Assert.assertTrue(databaseTables.contains(table));
		}
		for (int i = 1; i < amountViews + 1; i++) {
			objectName = "VIEW" + i;
			view = new DatabaseView();
			view.setTitle(objectName);
			Assert.assertFalse(databaseTables.contains(view));
		}
		verifyOtherTables();
	}

	/**
	 * Test method for {@link de.unibonn.inf.dbdependenciesui.metadata.impl.oraclecommons.OracleAnalyzerImpl#getViews()}
	 * .
	 */
	@Test
	public final void testGetViews() {
		for (int i = 1; i < amountTables + 1; i++) {
			objectName = "TABLENAME" + i;
			table = new DatabaseTable();
			table.setTitle(objectName);
			Assert.assertFalse("Table " + table.getTitle() + " does not exist in list.", databaseViews.contains(table));
		}
		for (int i = 1; i < amountViews - 1; i++) {
			objectName = "VIEW" + i;
			view = new DatabaseView();
			view.setTitle(objectName);
			Assert.assertTrue("View " + view.getTitle() + " does exist in list.", databaseViews.contains(view));
		}
		objectName = "MVIEW01";
		view = new DatabaseView();
		view.setTitle(objectName);
		Assert.assertTrue("View " + view.getTitle() + " does exist in list.", databaseViews.contains(view));
	}

	/**
	 * Test method for
	 * {@link de.unibonn.inf.dbdependenciesui.metadata.impl.oraclecommons.OracleAnalyzerImpl#getTriggers()} .
	 */
	@Test
	public final void testGetTriggers() {
		trigger = new DatabaseTrigger();
		trigger.setTitle("TEST_TRIGGER");
		Assert.assertTrue("List contains " + trigger, databaseTriggers.contains(trigger));
		trigger.setTitle("IF_TEST");
		Assert.assertTrue("List contains " + trigger, databaseTriggers.contains(trigger));
		trigger.setTitle("EXE_TEST");
		Assert.assertTrue("List contains " + trigger, databaseTriggers.contains(trigger));
		trigger.setTitle("MVIEW_TESTER");
		Assert.assertTrue("List contains " + trigger, databaseTriggers.contains(trigger));
		trigger.setTitle("TABLENAME1TRIGGER");
		Assert.assertTrue("List contains " + trigger, databaseTriggers.contains(trigger));
		trigger.setTitle("TEST_TRIGGER2");
		Assert.assertTrue("List contains " + trigger, databaseTriggers.contains(trigger));
		trigger.setTitle("MUTATING_TABLE_TEST");
		Assert.assertTrue("List contains " + trigger, databaseTriggers.contains(trigger));
		trigger.setTitle("TEST_3");
		Assert.assertTrue("List contains " + trigger, databaseTriggers.contains(trigger));
		trigger.setTitle("TEST_3_1");
		Assert.assertTrue("List contains " + trigger, databaseTriggers.contains(trigger));
		trigger.setTitle("TRIGGER1");
		Assert.assertTrue("List contains " + trigger, databaseTriggers.contains(trigger));
		trigger.setTitle("TRIGGER2");
		Assert.assertTrue("List contains " + trigger, databaseTriggers.contains(trigger));
		trigger.setTitle("TRIGGER3");
		Assert.assertTrue("List contains " + trigger, databaseTriggers.contains(trigger));
		trigger.setTitle("WHEN_TEST");
		Assert.assertTrue("List contains " + trigger, databaseTriggers.contains(trigger));
		trigger.setTitle("TEST4");
		Assert.assertTrue("List contains " + trigger, databaseTriggers.contains(trigger));
		trigger.setTitle("TEST5");
		Assert.assertTrue("List contains " + trigger, databaseTriggers.contains(trigger));
	}

	/**
	 * Test method for
	 * {@link de.unibonn.inf.dbdependenciesui.metadata.impl.AbstractAnalyzer#AbstractAnalyzer(de.unibonn.inf.dbdependenciesui.metadata.MetaDataFactory.Vendor)}
	 * .
	 */
	@Test
	public final void testAbstractAnalyzer() {
		Assert.assertTrue("ANALYZER IS AN ABSTRACT ", (analyser instanceof AbstractAnalyzer));
	}

	/**
	 * Test method for {@link de.unibonn.inf.dbdependenciesui.metadata.impl.AbstractAnalyzer#getVendor()} .
	 */
	@Test
	public final void testGetVendor() {
		Assert.assertEquals("ORACLE VENDOR", Vendor.ORACLE, analyser.getVendor());
	}

	@Test
	public void testGetKeysTable() {
		int index = 0;
		for (int i = 0; i < databaseTables.size(); i++) {
			table = databaseTables.get(i);
			objectName = table.getTitle();
			schema = table.getDdlSchemaObject();
			if (!objectName.equals("ERKLAERUNGEN") && !objectName.equals("FRAGEN") && !objectName.equals("ANTWORTEN")
					&& !objectName.equals("HILFE")) {
				Assert.assertEquals(objectName, 1, schema.getPrimaryKeys().size());
				if (objectName.equals("TABLENAME1")) {
					Assert.assertEquals(objectName + " PK", 1, schema.getPrimaryKeys().size());
					Assert.assertEquals(1, schema.getPrimaryKeys().get(0).getColumns().size());
					Assert.assertEquals(objectName + " UK", 2, schema.getUniqueKeys().size());
					for (int j = 0; j < 2; j++) {
						Assert.assertEquals(1, schema.getUniqueKeys().get(j).getColumns().size());
					}
					Assert.assertEquals(objectName + " FK", 0, schema.getForeignKeys().size());
					index++;
				} else if (objectName.equals("TABLENAME2")) {
					Assert.assertEquals(objectName + " PK", 1, schema.getPrimaryKeys().size());
					Assert.assertEquals(1, schema.getPrimaryKeys().get(0).getColumns().size());
					Assert.assertEquals(objectName + " UK", 1, schema.getUniqueKeys().size());
					Assert.assertEquals(1, schema.getUniqueKeys().get(0).getColumns().size());
					Assert.assertEquals(objectName + " FK", 0, schema.getForeignKeys().size());
					index++;
				} else if (objectName.equals("TABLENAME3")) {
					Assert.assertEquals(objectName + " PK", 1, schema.getPrimaryKeys().size());
					Assert.assertEquals(1, schema.getPrimaryKeys().get(0).getColumns().size());
					Assert.assertEquals(objectName + " UK", 1, schema.getUniqueKeys().size());
					Assert.assertEquals(1, schema.getUniqueKeys().get(0).getColumns().size());
					Assert.assertEquals(objectName + " FK", 0, schema.getForeignKeys().size());
					index++;
				} else if (objectName.equals("TABLENAME4")) {
					Assert.assertEquals(objectName + " PK", 1, schema.getPrimaryKeys().size());
					Assert.assertEquals(1, schema.getPrimaryKeys().get(0).getColumns().size());
					Assert.assertEquals(objectName + " UK", 1, schema.getUniqueKeys().size());
					Assert.assertEquals(1, schema.getUniqueKeys().get(0).getColumns().size());
					Assert.assertEquals(objectName + " FK", 1, schema.getForeignKeys().size());
					index++;
				} else if (objectName.equals("TABLENAME5")) {
					Assert.assertEquals(2, schema.getPrimaryKeys().get(0).getColumns().size());
					Assert.assertEquals(objectName + " UK", 1, schema.getUniqueKeys().size());
					Assert.assertEquals(1, schema.getUniqueKeys().get(0).getColumns().size());
					Assert.assertEquals(objectName + " FK", 0, schema.getForeignKeys().size());
					index++;
				} else if (objectName.equals("TABLENAME6")) {
					Assert.assertEquals(objectName + " PK", 1, schema.getPrimaryKeys().size());
					Assert.assertEquals(1, schema.getPrimaryKeys().get(0).getColumns().size());
					Assert.assertEquals(objectName + " UK", 0, schema.getUniqueKeys().size());
					Assert.assertEquals(objectName + " FK", 0, schema.getForeignKeys().size());
					index++;
				} else if (objectName.equals("TABLENAME7")) {
					Assert.assertEquals(objectName + " PK", 1, schema.getPrimaryKeys().size());
					Assert.assertEquals(1, schema.getPrimaryKeys().get(0).getColumns().size());
					Assert.assertEquals(objectName + " UK", 0, schema.getUniqueKeys().size());
					Assert.assertEquals(objectName + " FK", 3, schema.getForeignKeys().size());
					index++;
				} else if (objectName.equals("BUSSE")) {
					Assert.assertEquals(schema.getPrimaryKeys().get(0).getColumns().size() + "", 1, schema
							.getPrimaryKeys().get(0).getColumns().size());
					Assert.assertEquals(objectName + " UK", 0, schema.getUniqueKeys().size());
					Assert.assertEquals(objectName + " FK", 0, schema.getForeignKeys().size());
					index++;
				} else if (objectName.equals("EINSATZPLAN")) {
					Assert.assertEquals(objectName + " PK", 1, schema.getPrimaryKeys().size());
					Assert.assertEquals(1, schema.getPrimaryKeys().get(0).getColumns().size());
					Assert.assertEquals(objectName + " UK", 0, schema.getUniqueKeys().size());
					Assert.assertEquals(objectName + " FK", schema.getForeignKeys().size(), 3);
					index++;
				} else if (objectName.equals("NICHTBUSFAHRER")) {
					Assert.assertEquals(objectName + " PK", 1, schema.getPrimaryKeys().size());
					Assert.assertEquals(1, schema.getPrimaryKeys().get(0).getColumns().size());
					Assert.assertEquals(objectName + " UK", 0, schema.getUniqueKeys().size());
					Assert.assertEquals(objectName + " FK", 1, schema.getForeignKeys().size());
					index++;
				} else if (objectName.equals("LINIE")) {
					Assert.assertEquals(objectName + " PK", 1, schema.getPrimaryKeys().size());
					Assert.assertEquals(1, schema.getPrimaryKeys().get(0).getColumns().size());
					Assert.assertEquals(objectName + " UK", 0, schema.getUniqueKeys().size());
					Assert.assertEquals(objectName + " FK", 0, schema.getForeignKeys().size());
					index++;
				} else if (objectName.equals("BESITZT_FUEHRERSCHEIN")) {
					Assert.assertEquals(objectName + " PK", 1, schema.getPrimaryKeys().size());
					Assert.assertEquals(1, schema.getPrimaryKeys().get(0).getColumns().size());
					Assert.assertEquals(objectName + " UK", 0, schema.getUniqueKeys().size());
					Assert.assertEquals(objectName + " FK", 2, schema.getForeignKeys().size());
					index++;
				} else if (objectName.equals("INSPEKTIONEN")) {
					Assert.assertEquals(objectName + " PK", 1, schema.getPrimaryKeys().size());
					Assert.assertEquals(1, schema.getPrimaryKeys().get(0).getColumns().size());
					Assert.assertEquals(objectName + " UK", 0, schema.getUniqueKeys().size());
					Assert.assertEquals(objectName + " FK", 1, schema.getForeignKeys().size());
					index++;
				} else if (objectName.equals("FUEHRERSCHEINKLASSEN")) {
					Assert.assertEquals(objectName + " PK", 1, schema.getPrimaryKeys().size());
					Assert.assertEquals(1, schema.getPrimaryKeys().get(0).getColumns().size());
					Assert.assertEquals(objectName + " UK", 0, schema.getUniqueKeys().size());
					Assert.assertEquals(objectName + " FK", 0, schema.getForeignKeys().size());
					index++;
				} else if (objectName.equals("HALTESTELLE")) {
					Assert.assertEquals(objectName + " PK", 1, schema.getPrimaryKeys().size());
					Assert.assertEquals(1, schema.getPrimaryKeys().get(0).getColumns().size());
					Assert.assertEquals(objectName + " UK", 0, schema.getUniqueKeys().size());
					Assert.assertEquals(objectName + " FK", 0, schema.getForeignKeys().size());
					index++;
				} else if (objectName.equals("MITARBEITER")) {
					Assert.assertEquals(objectName + " PK", 1, schema.getPrimaryKeys().size());
					Assert.assertEquals(1, schema.getPrimaryKeys().get(0).getColumns().size());
					Assert.assertEquals(objectName + " UK", 0, schema.getUniqueKeys().size());
					Assert.assertEquals(objectName + " FK", 0, schema.getForeignKeys().size());
					index++;
				} else if (objectName.equals("BESCHRAENKUNGEN")) {
					Assert.assertEquals(objectName + " PK", 1, schema.getPrimaryKeys().size());
					Assert.assertEquals(1, schema.getPrimaryKeys().get(0).getColumns().size());
					Assert.assertEquals(objectName + " UK", 0, schema.getUniqueKeys().size());
					Assert.assertEquals(objectName + " FK", 0, schema.getForeignKeys().size());
					index++;
				} else if (objectName.equals("BUSFAHRER")) {
					Assert.assertEquals(objectName + " PK", 1, schema.getPrimaryKeys().size());
					Assert.assertEquals(1, schema.getPrimaryKeys().get(0).getColumns().size());
					Assert.assertEquals(objectName + " UK", 0, schema.getUniqueKeys().size());
					Assert.assertEquals(objectName + " FK", 1, schema.getForeignKeys().size());
					index++;
				} else if (objectName.equals("VERBINDUNG")) {
					Assert.assertEquals(objectName + " PK", 1, schema.getPrimaryKeys().size());
					Assert.assertEquals(1, schema.getPrimaryKeys().get(0).getColumns().size());
					Assert.assertEquals(objectName + " UK", 0, schema.getUniqueKeys().size());
					Assert.assertEquals(objectName + " FK", 2, schema.getForeignKeys().size());
					index++;
				} else if (objectName.equals("FAHRTEN")) {
					Assert.assertEquals(objectName + " PK", 1, schema.getPrimaryKeys().size());
					Assert.assertEquals(1, schema.getPrimaryKeys().get(0).getColumns().size());
					Assert.assertEquals(objectName + " UK", 0, schema.getUniqueKeys().size());
					Assert.assertEquals(objectName + " FK", 1, schema.getForeignKeys().size());
					index++;
				} else if (objectName.equals("LINIE_BESCHRAENKUNGEN")) {
					Assert.assertEquals(2, schema.getPrimaryKeys().get(0).getColumns().size());
					Assert.assertEquals(objectName + " UK", 0, schema.getUniqueKeys().size());
					Assert.assertEquals(objectName + " FK", schema.getForeignKeys().size(), 2);
					index++;
				} else if (objectName.equals("BESTEHEN")) {
					Assert.assertEquals(objectName + " PK", 1, schema.getPrimaryKeys().size());
					Assert.assertEquals(1, schema.getPrimaryKeys().get(0).getColumns().size());
					Assert.assertEquals(objectName + " UK", 0, schema.getUniqueKeys().size());
					Assert.assertEquals(objectName + " FK", 2, schema.getForeignKeys().size());
					index++;
				}
			} else {
				Assert.assertEquals(objectName + " PK", 0, schema.getPrimaryKeys().size());
				Assert.assertEquals(objectName + " UK", 0, schema.getUniqueKeys().size());
				Assert.assertEquals(objectName + " FK", 0, schema.getForeignKeys().size());
				index++;
			}
		}
		Assert.assertEquals("Number of indexes", 26, index);
	}

	@Test
	public void testGetKeysViews() {
		int index = 0;
		for (int i = 0; i < databaseViews.size(); i++) {
			view = databaseViews.get(i);
			objectName = view.getTitle();
			schema = view.getDdlSchemaObject();
			if (objectName.equals("VIEW1") || objectName.equals("VIEW2") || objectName.equals("VIEW3")) {
				Assert.assertEquals(objectName + " PK", 1, schema.getPrimaryKeys().size());
				Assert.assertEquals(1, schema.getPrimaryKeys().get(0).getColumns().size());
				Assert.assertEquals(objectName + " UK", 1, schema.getUniqueKeys().size());
				Assert.assertEquals(1, schema.getUniqueKeys().get(0).getColumns().size());
				Assert.assertEquals(objectName + " FK", 0, schema.getForeignKeys().size());
				index++;
			} else if (objectName.equals("VIEW4")) {
				Assert.assertEquals(objectName + " PK", 1, schema.getPrimaryKeys().size());
				Assert.assertEquals(1, schema.getPrimaryKeys().get(0).getColumns().size());
				Assert.assertEquals(objectName + " UK", 2, schema.getUniqueKeys().size());
				for (int j = 0; j < 2; j++) {
					Assert.assertEquals(1, schema.getUniqueKeys().get(j).getColumns().size());
				}
				Assert.assertEquals(objectName + " FK", 1, schema.getForeignKeys().size());
				index++;
			} else if (objectName.equals("VIEW5")) {
				Assert.assertEquals(objectName + " PK", 1, schema.getPrimaryKeys().size());
				Assert.assertEquals(1, schema.getPrimaryKeys().get(0).getColumns().size());
				Assert.assertEquals(objectName + " UK", 1, schema.getUniqueKeys().size());
				Assert.assertEquals(1, schema.getUniqueKeys().get(0).getColumns().size());
				Assert.assertEquals(objectName + " FK", 0, schema.getForeignKeys().size());
				index++;
			} else if (objectName.equals("VIEW6")) {
				Assert.assertEquals(objectName + " PK", 1, schema.getPrimaryKeys().size());
				Assert.assertEquals(1, schema.getPrimaryKeys().get(0).getColumns().size());
				Assert.assertEquals(objectName + " UK", 1, schema.getUniqueKeys().size());
				Assert.assertEquals(1, schema.getUniqueKeys().get(0).getColumns().size());
				Assert.assertEquals(objectName + " FK", 0, schema.getForeignKeys().size());
				index++;
			} else if (objectName.equals("VIEW7")) {
				Assert.assertEquals(objectName + " PK", 1, schema.getPrimaryKeys().size());
				Assert.assertEquals(1, schema.getPrimaryKeys().get(0).getColumns().size());
				Assert.assertEquals(objectName + " UK", 1, schema.getUniqueKeys().size());
				Assert.assertEquals(1, schema.getUniqueKeys().get(0).getColumns().size());
				Assert.assertEquals(objectName + " FK", 0, schema.getForeignKeys().size());
				index++;
			} else if (objectName.equals("VIEW8")) {
				Assert.assertEquals(objectName + " PK", 1, schema.getPrimaryKeys().size());
				Assert.assertEquals(1, schema.getPrimaryKeys().get(0).getColumns().size());
				Assert.assertEquals(objectName + " UK", 1, schema.getUniqueKeys().size());
				Assert.assertEquals(1, schema.getUniqueKeys().get(0).getColumns().size());
				Assert.assertEquals(objectName + " FK", 0, schema.getForeignKeys().size());
				index++;
			} else if (objectName.equals("VIEW9")) {
				Assert.assertEquals(objectName + " PK", 1, schema.getPrimaryKeys().size());
				Assert.assertEquals(1, schema.getPrimaryKeys().get(0).getColumns().size());
				Assert.assertEquals(objectName + " UK", 2, schema.getUniqueKeys().size());
				Assert.assertEquals(1, schema.getUniqueKeys().get(0).getColumns().size());
				Assert.assertEquals(2, schema.getUniqueKeys().get(1).getColumns().size());
				Assert.assertEquals(objectName + " FK", 0, schema.getForeignKeys().size());
				index++;
			} else if (objectName.equals("VIEW10")) {
				Assert.assertEquals(objectName + " PK", 1, schema.getPrimaryKeys().size());
				Assert.assertEquals(2, schema.getPrimaryKeys().get(0).getColumns().size());
				Assert.assertEquals(objectName + " UK", 1, schema.getUniqueKeys().size());
				Assert.assertEquals(2, schema.getUniqueKeys().get(0).getColumns().size());
				Assert.assertEquals(objectName + " FK", 0, schema.getForeignKeys().size());
				index++;
			} else if (objectName.equals("VIEW11")) {
				Assert.assertEquals(objectName + " PK", 1, schema.getPrimaryKeys().size());
				Assert.assertEquals(1, schema.getPrimaryKeys().get(0).getColumns().size());
				Assert.assertEquals(objectName + " UK", 1, schema.getUniqueKeys().size());
				Assert.assertEquals(1, schema.getUniqueKeys().get(0).getColumns().size());
				Assert.assertEquals(objectName + " FK", 0, schema.getForeignKeys().size());
				index++;
			} else if (objectName.equals("VIEW12")) {
				Assert.assertEquals(objectName + " PK", 1, schema.getPrimaryKeys().size());
				Assert.assertEquals(1, schema.getPrimaryKeys().get(0).getColumns().size());
				Assert.assertEquals(objectName + " UK", 1, schema.getUniqueKeys().size());
				Assert.assertEquals(1, schema.getUniqueKeys().get(0).getColumns().size());
				Assert.assertEquals(objectName + " FK", 0, schema.getForeignKeys().size());
				index++;
			} else if (objectName.equals("VIEW13")) {
				Assert.assertEquals(objectName + " PK", 0, schema.getPrimaryKeys().size());
				Assert.assertEquals(objectName + " UK", 0, schema.getUniqueKeys().size());
				Assert.assertEquals(objectName + " FK", 0, schema.getForeignKeys().size());
				index++;
			} else if (objectName.equals("VIEW14")) {
				Assert.assertEquals(objectName + " PK", 1, schema.getPrimaryKeys().size());
				Assert.assertEquals(1, schema.getPrimaryKeys().get(0).getColumns().size());
				Assert.assertEquals(objectName + " UK", 1, schema.getUniqueKeys().size());
				Assert.assertEquals(1, schema.getUniqueKeys().get(0).getColumns().size());
				Assert.assertEquals(objectName + " FK", 0, schema.getForeignKeys().size());
				index++;
			} else if (objectName.equals("VIEW15")) {
				Assert.assertEquals(objectName + " PK", 1, schema.getPrimaryKeys().size());
				Assert.assertEquals(1, schema.getPrimaryKeys().get(0).getColumns().size());
				Assert.assertEquals(objectName + " UK", 1, schema.getUniqueKeys().size());
				Assert.assertEquals(1, schema.getUniqueKeys().get(0).getColumns().size());
				Assert.assertEquals(objectName + " FK", 0, schema.getForeignKeys().size());
				index++;
			} else if (objectName.equals("MVIEW01")) {
				Assert.assertEquals(objectName + " PK", 0, schema.getPrimaryKeys().size());
				Assert.assertEquals(objectName + " UK", 0, schema.getUniqueKeys().size());
				Assert.assertEquals(objectName + " FK", 0, schema.getForeignKeys().size());
				index++;
			}
		}
		// One view was ignored.
		Assert.assertEquals("Number of views", amountViews - 1, index);
	}

	private void verifyOtherTables() {
		objectName = "ANTWORTEN";
		table = new DatabaseTable();
		table.setTitle(objectName);
		Assert.assertTrue(databaseTables.contains(table));
		Assert.assertFalse(databaseViews.contains(table));

		objectName = "BESCHRAENKUNGEN";
		table = new DatabaseTable();
		table.setTitle(objectName);
		Assert.assertTrue(databaseTables.contains(table));
		Assert.assertFalse(databaseViews.contains(table));

		objectName = "BESITZT_FUEHRERSCHEIN";
		table = new DatabaseTable();
		table.setTitle(objectName);
		Assert.assertTrue(databaseTables.contains(table));
		Assert.assertFalse(databaseViews.contains(table));

		objectName = "BESTEHEN";
		table = new DatabaseTable();
		table.setTitle(objectName);
		Assert.assertTrue(databaseTables.contains(table));
		Assert.assertFalse(databaseViews.contains(table));

		objectName = "BUSFAHRER";
		table = new DatabaseTable();
		table.setTitle(objectName);
		Assert.assertTrue(databaseTables.contains(table));
		Assert.assertFalse(databaseViews.contains(table));

		objectName = "BUSSE";
		table = new DatabaseTable();
		table.setTitle(objectName);
		Assert.assertTrue(databaseTables.contains(table));
		Assert.assertFalse(databaseViews.contains(table));

		objectName = "EINSATZPLAN";
		table = new DatabaseTable();
		table.setTitle(objectName);
		Assert.assertTrue(databaseTables.contains(table));
		Assert.assertFalse(databaseViews.contains(table));

		objectName = "ERKLAERUNGEN";
		table = new DatabaseTable();
		table.setTitle(objectName);
		Assert.assertTrue(databaseTables.contains(table));
		Assert.assertFalse(databaseViews.contains(table));

		objectName = "FAHRTEN";
		table = new DatabaseTable();
		table.setTitle(objectName);
		Assert.assertTrue(databaseTables.contains(table));
		Assert.assertFalse(databaseViews.contains(table));

		objectName = "FRAGEN";
		table = new DatabaseTable();
		table.setTitle(objectName);
		Assert.assertTrue(databaseTables.contains(table));
		Assert.assertFalse(databaseViews.contains(table));

		objectName = "FUEHRERSCHEINKLASSEN";
		table = new DatabaseTable();
		table.setTitle(objectName);
		Assert.assertTrue(databaseTables.contains(table));
		Assert.assertFalse(databaseViews.contains(table));

		objectName = "HALTESTELLE";
		table = new DatabaseTable();
		table.setTitle(objectName);
		Assert.assertTrue(databaseTables.contains(table));
		Assert.assertFalse(databaseViews.contains(table));

		objectName = "HILFE";
		table = new DatabaseTable();
		table.setTitle(objectName);
		Assert.assertTrue(databaseTables.contains(table));
		Assert.assertFalse(databaseViews.contains(table));

		objectName = "INSPEKTIONEN";
		table = new DatabaseTable();
		table.setTitle(objectName);
		Assert.assertTrue(databaseTables.contains(table));
		Assert.assertFalse(databaseViews.contains(table));

		objectName = "LINIE";
		table = new DatabaseTable();
		table.setTitle(objectName);
		Assert.assertTrue(databaseTables.contains(table));
		Assert.assertFalse(databaseViews.contains(table));

		objectName = "LINIE_BESCHRAENKUNGEN";
		table = new DatabaseTable();
		table.setTitle(objectName);
		Assert.assertTrue(databaseTables.contains(table));
		Assert.assertFalse(databaseViews.contains(table));

		objectName = "MITARBEITER";
		table = new DatabaseTable();
		table.setTitle(objectName);
		Assert.assertTrue(databaseTables.contains(table));
		Assert.assertFalse(databaseViews.contains(table));

		objectName = "NICHTBUSFAHRER";
		table = new DatabaseTable();
		table.setTitle(objectName);
		Assert.assertTrue(databaseTables.contains(table));
		Assert.assertFalse(databaseViews.contains(table));

		objectName = "VERBINDUNG";
		table = new DatabaseTable();
		table.setTitle(objectName);
		Assert.assertTrue(databaseTables.contains(table));
		Assert.assertFalse(databaseViews.contains(table));
	}

}
