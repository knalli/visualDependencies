package de.unibonn.inf.dbdependenciesui.metadata.tests;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
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
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseView;
import de.unibonn.inf.dbdependenciesui.metadata.IAnalyzer;
import de.unibonn.inf.dbdependenciesui.metadata.IViewSqlParser;
import de.unibonn.inf.dbdependenciesui.metadata.MetaDataFactory;
import de.unibonn.inf.dbdependenciesui.metadata.MetaDataFactory.Vendor;
import de.unibonn.inf.dbdependenciesui.metadata.impl.AbstractViewSqlParser;
import de.unibonn.inf.dbdependenciesui.metadata.impl.oracle11.Oracle11AnalyzerImpl;
import de.unibonn.inf.dbdependenciesui.metadata.impl.oraclecommons.OracleViewSqlParserImpl;
import de.unibonn.inf.dbdependenciesui.tests.HibernateTestingUtil;

public class OracleViewSqlParserImplTest {

	private static List<DatabaseTable> tables;
	private static List<DatabaseView> views;
	private DatabaseView actualView;

	private IViewSqlParser parser;

	private static IAnalyzer analyzer;

	/**
	 * Return the suite adapter. This is used for test suites - a workaround for jUnit 3.x Test Suites.
	 * 
	 * @return
	 */
	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(OracleViewSqlParserImplTest.class);
	}

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Connection conn;

		DatabaseConnection connection;
		
		if (!TestFixtures.REAL_DB_ENABLED) {
			throw new NotSupportedException("Debug connection is not available.");
		}

		Configuration.configure();
			Class.forName("oracle.jdbc.OracleDriver");
			
		connection = new DatabaseConnection();
		connection.setHost(TestFixtures.REAL_DB_CONN_HOST);
		connection.setDatabase(TestFixtures.REAL_DB_CONN_SCHM);
		connection.setPort(TestFixtures.REAL_DB_CONN_PORT);
		connection.setUsername(TestFixtures.REAL_DB_CONN_USER);
		connection.setPassword(TestFixtures.REAL_DB_CONN_PASS);

		conn = MetaDataFactory.getConnectionBuilder(Vendor.ORACLE10).createConnection(connection);

		OracleViewSqlParserImplTest.analyzer = new Oracle11AnalyzerImpl();
		OracleViewSqlParserImplTest.analyzer.analyze(conn, connection);
		OracleViewSqlParserImplTest.tables = OracleViewSqlParserImplTest.analyzer.getTables();
		OracleViewSqlParserImplTest.views = OracleViewSqlParserImplTest.analyzer.getViews();
		conn.close();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {}

	@Before
	public void setUp() throws Exception {
		HibernateTestingUtil.overrideConfiguration(true);
		actualView = OracleViewSqlParserImplTest.views.get(0);
		parser = MetaDataFactory.createViewSqlParser(Vendor.ORACLE);
	}

	@After
	public void tearDown() throws Exception {
		HibernateDAOFactory.closeSession();
	}

	@Test
	public final void testOracleViewSqlParserImpl() {
		Assert.assertTrue("PARSER IS INSTANCE OF OracleViewSqlParserImpl", (parser instanceof OracleViewSqlParserImpl));
		Assert.assertNotNull("PARSER NOT NULL", parser);
		Assert.assertNotNull("VIEW NOT NULL", actualView);
		Assert.assertNotNull("TABLES NOT NULL", OracleViewSqlParserImplTest.tables);
		Assert.assertNotNull("VIEWS NOT NULL", OracleViewSqlParserImplTest.views);
	}

	@Test
	public final void testParse() {
		Assert.assertFalse("VIEW SHOULD BE NULL", parser.parse(OracleViewSqlParserImplTest.tables,
				OracleViewSqlParserImplTest.views, null));
		Assert.assertEquals("ERROR MESSAGE", "view cannot be null", parser.getErrorMessage());
		parser.clear();
		Assert.assertFalse("TABLES SHOULD BE EMPTY", parser.parse(null, OracleViewSqlParserImplTest.views, actualView));
		Assert.assertEquals("ERROR MESSAGE", "table list cannot be null", parser.getErrorMessage());
		parser.clear();
		Assert.assertFalse("VIEWS SHOULD BE EMPTY", parser.parse(OracleViewSqlParserImplTest.tables, null, actualView));
		Assert.assertEquals("ERROR MESSAGE", "view list cannot be null", parser.getErrorMessage());
		parser.clear();
	}

	@Test
	public final void testAbstractViewSqlParser() {
		Assert.assertTrue("PARSER EXTENDS AbstractParser", (parser instanceof AbstractViewSqlParser));
	}

	@Test
	public final void testGetVendor() {
		Assert.assertEquals("ORACLE VENDOR", Vendor.ORACLE, parser.getVendor());
	}

	@Test
	public final void testClear() {
		parser.parse(OracleViewSqlParserImplTest.tables, OracleViewSqlParserImplTest.views, actualView);
		parser.clear();
		Assert.assertTrue("EMPTY AFFECTED", parser.getAffectedViews().isEmpty());
		Assert.assertTrue("EMPTY NEGATIV", parser.getNegativeRelationViews().isEmpty());
		Assert.assertTrue("EMPTY POSITIV", parser.getPositiveRelationViews().isEmpty());
	}

	// @Test
	// public final void testParseViews() {
	// Assert.fail("Not yet implemented");
	// }

	@Test
	public final void testGetAffectedViews() {
		final List<String> tableNames = new ArrayList<String>();
		for (final DatabaseView view : OracleViewSqlParserImplTest.views) {
			tableNames.clear();
			actualView = view;
			parser.parse(OracleViewSqlParserImplTest.tables, OracleViewSqlParserImplTest.views, actualView);
			for (final DatabaseTable affected : parser.getAffectedViews()) {
				tableNames.add(affected.getTitle());
			}
			if (view.getTitle().toUpperCase().equals("VIEW1")) {
				Assert.assertEquals("AMOUNT AFFECTED", 4, parser.getAffectedViews().size());
				Assert.assertTrue("TABLENAME1 MUST BE IN AFFECTED VIEWS ", tableNames.contains("TABLENAME1"));
				Assert.assertTrue("TABLENAME2 MUST BE IN AFFECTED VIEWS ", tableNames.contains("TABLENAME2"));
				Assert.assertTrue("TABLENAME3 MUST BE IN AFFECTED VIEWS ", tableNames.contains("TABLENAME3"));
				Assert.assertTrue("TABLENAME4 MUST BE IN AFFECTED VIEWS ", tableNames.contains("TABLENAME4"));
			} else if (view.getTitle().toUpperCase().equals("VIEW2")) {
				Assert.assertEquals("AMOUNT AFFECTED", 2, parser.getAffectedViews().size());
				Assert.assertTrue("TABLENAME1 MUST BE IN AFFECTED VIEWS ", tableNames.contains("TABLENAME1"));
				Assert.assertTrue("TABLENAME2 MUST BE IN AFFECTED VIEWS ", tableNames.contains("TABLENAME2"));
			} else if (view.getTitle().toUpperCase().equals("VIEW3")) {
				Assert.assertEquals("AMOUNT AFFECTED", 2, parser.getAffectedViews().size());
				Assert.assertTrue("TABLENAME3 MUST BE IN AFFECTED VIEWS ", tableNames.contains("TABLENAME3"));
				Assert.assertTrue("TABLENAME4 MUST BE IN AFFECTED VIEWS ", tableNames.contains("TABLENAME4"));
			} else if (view.getTitle().toUpperCase().equals("VIEW4")) {
				Assert.assertEquals("AMOUNT AFFECTED", 4, parser.getAffectedViews().size());
				Assert.assertTrue("TABLENAME1 MUST BE IN AFFECTED VIEWS ", tableNames.contains("TABLENAME1"));
				Assert.assertTrue("TABLENAME2 MUST BE IN AFFECTED VIEWS ", tableNames.contains("TABLENAME2"));
				Assert.assertTrue("VIEW2 MUST BE IN AFFECTED VIEWS ", tableNames.contains("VIEW2"));
				Assert.assertTrue("VIEW1 MUST BE IN AFFECTED VIEWS ", tableNames.contains("VIEW1"));
			} else if (view.getTitle().toUpperCase().equals("VIEW5")) {
				Assert.assertEquals("AMOUNT AFFECTED", 2, parser.getAffectedViews().size());
				Assert.assertTrue("VIEW4 MUST BE IN AFFECTED VIEWS ", tableNames.contains("VIEW4"));
				Assert.assertTrue("VIEW2 MUST BE IN AFFECTED VIEWS ", tableNames.contains("VIEW2"));
			} else if (view.getTitle().toUpperCase().equals("VIEW6")) {
				Assert.assertEquals("AMOUNT AFFECTED", 2, parser.getAffectedViews().size());
				Assert.assertTrue("TABLENAME1 MUST BE IN AFFECTED VIEWS ", tableNames.contains("TABLENAME1"));
				Assert.assertTrue("VIEW4 MUST BE IN AFFECTED VIEWS ", tableNames.contains("VIEW4"));
			} else if (view.getTitle().toUpperCase().equals("VIEW7")) {
				Assert.assertEquals("AMOUNT AFFECTED", 2, parser.getAffectedViews().size());
				Assert.assertTrue("TABLENAME1 MUST BE IN AFFECTED VIEWS ", tableNames.contains("TABLENAME1"));
				Assert.assertTrue("TABLENAME2 MUST BE IN AFFECTED VIEWS ", tableNames.contains("TABLENAME2"));
			} else if (view.getTitle().toUpperCase().equals("VIEW8")) {
				Assert.assertEquals("AMOUNT AFFECTED", 2, parser.getAffectedViews().size());
				Assert.assertTrue("TABLENAME1 MUST BE IN AFFECTED VIEWS ", tableNames.contains("TABLENAME1"));
				Assert.assertTrue("TABLENAME2 MUST BE IN AFFECTED VIEWS ", tableNames.contains("TABLENAME2"));
			} else if (view.getTitle().toUpperCase().equals("VIEW9")) {
				Assert.assertEquals("AMOUNT AFFECTED", 3, parser.getAffectedViews().size());
				Assert.assertTrue("TABLENAME1 MUST BE IN AFFECTED VIEWS ", tableNames.contains("TABLENAME1"));
				Assert.assertTrue("TABLENAME2 MUST BE IN AFFECTED VIEWS ", tableNames.contains("TABLENAME2"));
				Assert.assertTrue("TABLENAME5 MUST BE IN AFFECTED VIEWS ", tableNames.contains("TABLENAME5"));
			} else if (view.getTitle().toUpperCase().equals("VIEW10")) {
				Assert.assertEquals("AMOUNT AFFECTED", 2, parser.getAffectedViews().size());
				Assert.assertTrue("TABLENAME3 MUST BE IN AFFECTED VIEWS ", tableNames.contains("TABLENAME3"));
				Assert.assertTrue("TABLENAME2 MUST BE IN AFFECTED VIEWS ", tableNames.contains("TABLENAME2"));
			} else if (view.getTitle().toUpperCase().equals("VIEW11")) {
				Assert.assertEquals("AMOUNT AFFECTED", 2, parser.getAffectedViews().size());
				Assert.assertTrue("TABLENAME3 MUST BE IN AFFECTED VIEWS ", tableNames.contains("TABLENAME3"));
				Assert.assertTrue("TABLENAME2 MUST BE IN AFFECTED VIEWS ", tableNames.contains("TABLENAME2"));
			} else if (view.getTitle().toUpperCase().equals("VIEW12")) {
				Assert.assertEquals("AMOUNT AFFECTED", 2, parser.getAffectedViews().size());
				Assert.assertTrue("TABLENAME3 MUST BE IN AFFECTED VIEWS ", tableNames.contains("TABLENAME3"));
				Assert.assertTrue("TABLENAME2 MUST BE IN AFFECTED VIEWS ", tableNames.contains("TABLENAME2"));
			} else if (view.getTitle().toUpperCase().equals("VIEW13")) {
				Assert.assertEquals("AMOUNT AFFECTED", 2, parser.getAffectedViews().size());
				Assert.assertTrue("TABLENAME6 MUST BE IN AFFECTED VIEWS ", tableNames.contains("TABLENAME6"));
				Assert.assertTrue("TABLENAME7 MUST BE IN AFFECTED VIEWS ", tableNames.contains("TABLENAME7"));
			} else if (view.getTitle().toUpperCase().equals("VIEW14")) {
				Assert.assertEquals("AMOUNT AFFECTED", 2, parser.getAffectedViews().size());
				Assert.assertTrue("VIEW11 MUST BE IN AFFECTED VIEWS ", tableNames.contains("VIEW11"));
				Assert.assertTrue("VIEW12 MUST BE IN AFFECTED VIEWS ", tableNames.contains("VIEW12"));
			} else if (view.getTitle().toUpperCase().equals("VIEW15")) {
				Assert.assertEquals("AMOUNT AFFECTED", 4, parser.getAffectedViews().size());
				Assert.assertTrue("VIEW12 MUST BE IN AFFECTED VIEWS ", tableNames.contains("VIEW12"));
				Assert.assertTrue("VIEW11 MUST BE IN AFFECTED VIEWS ", tableNames.contains("VIEW11"));
				Assert.assertTrue("VIEW10 MUST BE IN AFFECTED VIEWS ", tableNames.contains("VIEW10"));
				Assert.assertTrue("VIEW9 MUST BE IN AFFECTED VIEWS ", tableNames.contains("VIEW9"));
			}
			parser.clear();
		}
	}

	@Test
	public final void testGetNegativeRelationViews() {
		final List<String> tableNames = new ArrayList<String>();
		for (final DatabaseView view : OracleViewSqlParserImplTest.views) {
			tableNames.clear();
			actualView = view;
			parser.parse(OracleViewSqlParserImplTest.tables, OracleViewSqlParserImplTest.views, actualView);
			for (final DatabaseTable negativ : parser.getNegativeRelationViews()) {
				tableNames.add(negativ.getTitle());
			}
			if (view.getTitle().toUpperCase().equals("VIEW1")) {
				Assert.assertEquals("AMOUNT NEGATIVE", 0, parser.getNegativeRelationViews().size());
			} else if (view.getTitle().toUpperCase().equals("VIEW2")) {
				Assert.assertEquals("AMOUNT NEGATIVE", 0, parser.getNegativeRelationViews().size());
			} else if (view.getTitle().toUpperCase().equals("VIEW3")) {
				Assert.assertEquals("AMOUNT NEGATIVE", 0, parser.getNegativeRelationViews().size());
			} else if (view.getTitle().toUpperCase().equals("VIEW4")) {
				Assert.assertEquals("AMOUNT NEGATIVE", 0, parser.getNegativeRelationViews().size());
			} else if (view.getTitle().toUpperCase().equals("VIEW5")) {
				Assert.assertEquals("AMOUNT NEGATIVE", 0, parser.getNegativeRelationViews().size());
			} else if (view.getTitle().toUpperCase().equals("VIEW6")) {
				Assert.assertEquals("AMOUNT NEGATIVE", 0, parser.getNegativeRelationViews().size());
			} else if (view.getTitle().toUpperCase().equals("VIEW7")) {
				Assert.assertEquals("AMOUNT NEGATIVE", 0, parser.getNegativeRelationViews().size());
			} else if (view.getTitle().toUpperCase().equals("VIEW8")) {
				Assert.assertEquals("AMOUNT NEGATIVE", 0, parser.getNegativeRelationViews().size());
			} else if (view.getTitle().toUpperCase().equals("VIEW9")) {
				Assert.assertEquals("AMOUNT NEGATIVE", 0, parser.getNegativeRelationViews().size());
			} else if (view.getTitle().toUpperCase().equals("VIEW10")) {
				Assert.assertEquals("AMOUNT NEGATIVE", 0, parser.getNegativeRelationViews().size());
			} else if (view.getTitle().toUpperCase().equals("VIEW11")) {
				Assert.assertEquals("AMOUNT NEGATIVE", 1, parser.getNegativeRelationViews().size());
				Assert.assertTrue("TABLENAME2 MUST BE IN NEGATIVE VIEWS ", tableNames.contains("TABLENAME2"));
			} else if (view.getTitle().toUpperCase().equals("VIEW12")) {
				Assert.assertEquals("AMOUNT NEGATIVE", 0, parser.getNegativeRelationViews().size());
			} else if (view.getTitle().toUpperCase().equals("VIEW13")) {
				Assert.assertEquals("AMOUNT NEGATIVE", 0, parser.getNegativeRelationViews().size());
			} else if (view.getTitle().toUpperCase().equals("VIEW14")) {
				Assert.assertEquals("AMOUNT NEGATIVE", 0, parser.getNegativeRelationViews().size());
			} else if (view.getTitle().toUpperCase().equals("VIEW15")) {
				Assert.assertEquals("AMOUNT NEGATIVE", 2, parser.getNegativeRelationViews().size());
				Assert.assertTrue("VIEW10 MUST BE IN NEGATIVE VIEWS ", tableNames.contains("VIEW10"));
				Assert.assertTrue("VIEW11 MUST BE IN NEGATIVE VIEWS ", tableNames.contains("VIEW11"));
			} else if (view.getTitle().toUpperCase().equals("MVIEW01")) {
				Assert.assertEquals("AMOUNT NEGATIVE", 0, parser.getNegativeRelationViews().size());
			}
			parser.clear();
		}
	}

	@Test
	public final void testGetPositiveRelationViews() {
		final List<String> tableNames = new ArrayList<String>();
		for (final DatabaseView view : OracleViewSqlParserImplTest.views) {
			tableNames.clear();
			actualView = view;
			parser.parse(OracleViewSqlParserImplTest.tables, OracleViewSqlParserImplTest.views, actualView);
			for (final DatabaseTable negativ : parser.getPositiveRelationViews()) {
				tableNames.add(negativ.getTitle());
			}
			if (view.getTitle().toUpperCase().equals("VIEW1")) {
				Assert.assertEquals("AMOUNT POSITIVE", 4, parser.getPositiveRelationViews().size());
				Assert.assertTrue("TABLENAME1 MUST BE IN POSITIVE VIEWS ", tableNames.contains("TABLENAME1"));
				Assert.assertTrue("TABLENAME2 MUST BE IN POSITIVE VIEWS ", tableNames.contains("TABLENAME2"));
				Assert.assertTrue("TABLENAME3 MUST BE IN POSITIVE VIEWS ", tableNames.contains("TABLENAME3"));
				Assert.assertTrue("TABLENAME4 MUST BE IN POSITIVE VIEWS ", tableNames.contains("TABLENAME4"));
			} else if (view.getTitle().toUpperCase().equals("VIEW2")) {
				Assert.assertEquals("AMOUNT POSITIVE", 2, parser.getPositiveRelationViews().size());
				Assert.assertTrue("TABLENAME1 MUST BE IN POSITIVE VIEWS ", tableNames.contains("TABLENAME1"));
				Assert.assertTrue("TABLENAME2 MUST BE IN POSITIVE VIEWS ", tableNames.contains("TABLENAME2"));
			} else if (view.getTitle().toUpperCase().equals("VIEW3")) {
				Assert.assertEquals("AMOUNT POSITIVE", 2, parser.getPositiveRelationViews().size());
				Assert.assertTrue("TABLENAME3 MUST BE IN POSITIVE VIEWS ", tableNames.contains("TABLENAME3"));
				Assert.assertTrue("TABLENAME4 MUST BE IN POSITIVE VIEWS ", tableNames.contains("TABLENAME4"));
			} else if (view.getTitle().toUpperCase().equals("VIEW4")) {
				Assert.assertEquals("AMOUNT POSITIVE", 4, parser.getPositiveRelationViews().size());
				Assert.assertTrue("TABLENAME1 MUST BE IN POSITIVE VIEWS ", tableNames.contains("TABLENAME1"));
				Assert.assertTrue("TABLENAME2 MUST BE IN POSITIVE VIEWS ", tableNames.contains("TABLENAME2"));
				Assert.assertTrue("VIEW1 MUST BE IN POSITIVE VIEWS ", tableNames.contains("VIEW1"));
				Assert.assertTrue("VIEW2 MUST BE IN POSITIVE VIEWS ", tableNames.contains("VIEW2"));
			} else if (view.getTitle().toUpperCase().equals("VIEW5")) {
				Assert.assertEquals("AMOUNT POSITIVE", 2, parser.getPositiveRelationViews().size());
				Assert.assertTrue("VIEW2 MUST BE IN POSITIVE VIEWS ", tableNames.contains("VIEW2"));
				Assert.assertTrue("VIEW4 MUST BE IN POSITIVE VIEWS ", tableNames.contains("VIEW4"));
			} else if (view.getTitle().toUpperCase().equals("VIEW6")) {
				Assert.assertEquals("AMOUNT POSITIVE", 2, parser.getPositiveRelationViews().size());
				Assert.assertTrue("TABLENAME1 MUST BE IN POSITIVE VIEWS ", tableNames.contains("TABLENAME1"));
				Assert.assertTrue("VIEW4 MUST BE IN POSITIVE VIEWS ", tableNames.contains("VIEW4"));
			} else if (view.getTitle().toUpperCase().equals("VIEW7")) {
				Assert.assertEquals("AMOUNT POSITIVE", 2, parser.getPositiveRelationViews().size());
				Assert.assertTrue("TABLENAME1 MUST BE IN POSITIVE VIEWS ", tableNames.contains("TABLENAME1"));
				Assert.assertTrue("TABLENAME2 MUST BE IN POSITIVE VIEWS ", tableNames.contains("TABLENAME2"));
			} else if (view.getTitle().toUpperCase().equals("VIEW8")) {
				Assert.assertEquals("AMOUNT POSITIVE", 2, parser.getPositiveRelationViews().size());
				Assert.assertTrue("TABLENAME1 MUST BE IN POSITIVE VIEWS ", tableNames.contains("TABLENAME1"));
				Assert.assertTrue("TABLENAME2 MUST BE IN POSITIVE VIEWS ", tableNames.contains("TABLENAME2"));
			} else if (view.getTitle().toUpperCase().equals("VIEW9")) {
				Assert.assertEquals("AMOUNT POSITIVE", 3, parser.getPositiveRelationViews().size());
				Assert.assertTrue("TABLENAME1 MUST BE IN POSITIVE VIEWS ", tableNames.contains("TABLENAME1"));
				Assert.assertTrue("TABLENAME2 MUST BE IN POSITIVE VIEWS ", tableNames.contains("TABLENAME2"));
				Assert.assertTrue("TABLENAME5 MUST BE IN POSITIVE VIEWS ", tableNames.contains("TABLENAME5"));
			} else if (view.getTitle().toUpperCase().equals("VIEW10")) {
				Assert.assertEquals("AMOUNT POSITIVE", 2, parser.getPositiveRelationViews().size());
				Assert.assertTrue("TABLENAME3 MUST BE IN POSITIVE VIEWS ", tableNames.contains("TABLENAME3"));
				Assert.assertTrue("TABLENAME2 MUST BE IN POSITIVE VIEWS ", tableNames.contains("TABLENAME2"));
			} else if (view.getTitle().toUpperCase().equals("VIEW11")) {
				Assert.assertEquals("AMOUNT POSITIVE", 1, parser.getPositiveRelationViews().size());
				Assert.assertTrue("TABLENAME3 MUST BE IN POSITIVE VIEWS ", tableNames.contains("TABLENAME3"));
			} else if (view.getTitle().toUpperCase().equals("VIEW12")) {
				Assert.assertEquals("AMOUNT POSITIVE", 2, parser.getPositiveRelationViews().size());
				Assert.assertTrue("TABLENAME3 MUST BE IN POSITIVE VIEWS ", tableNames.contains("TABLENAME3"));
				Assert.assertTrue("TABLENAME2 MUST BE IN POSITIVE VIEWS ", tableNames.contains("TABLENAME2"));
			} else if (view.getTitle().toUpperCase().equals("VIEW13")) {
				Assert.assertEquals("AMOUNT POSITIVE", 2, parser.getPositiveRelationViews().size());
				Assert.assertTrue("TABLENAME6 MUST BE IN POSITIVE VIEWS ", tableNames.contains("TABLENAME6"));
				Assert.assertTrue("TABLENAME7 MUST BE IN POSITIVE VIEWS ", tableNames.contains("TABLENAME7"));
			} else if (view.getTitle().toUpperCase().equals("VIEW14")) {
				Assert.assertEquals("AMOUNT POSITIVE", 2, parser.getPositiveRelationViews().size());
				Assert.assertTrue("VIEW12 MUST BE IN POSITIVE VIEWS ", tableNames.contains("VIEW12"));
				Assert.assertTrue("VIEW11 MUST BE IN POSITIVE VIEWS ", tableNames.contains("VIEW11"));
			} else if (view.getTitle().toUpperCase().equals("VIEW15")) {
				Assert.assertEquals("AMOUNT POSITIVE", 4, parser.getPositiveRelationViews().size());
				Assert.assertTrue("VIEW10 MUST BE IN POSITIVE VIEWS ", tableNames.contains("VIEW10"));
				Assert.assertTrue("VIEW9 MUST BE IN POSITIVE VIEWS ", tableNames.contains("VIEW9"));
				Assert.assertTrue("VIEW12 MUST BE IN POSITIVE VIEWS ", tableNames.contains("VIEW12"));
				Assert.assertTrue("VIEW11 MUST BE IN POSITIVE VIEWS ", tableNames.contains("VIEW11"));
			} else if (view.getTitle().toUpperCase().equals("MVIEW01")) {
				Assert.assertEquals("AMOUNT POSITIVE", 1, parser.getPositiveRelationViews().size());
				Assert.assertTrue("VIEW1 MUST BE IN POSITIVE VIEWS ", tableNames.contains("VIEW1"));
			}
			parser.clear();
		}
	}

}
