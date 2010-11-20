package de.unibonn.inf.dbdependenciesui.metadata.tests;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.NotSupportedException;

import junit.framework.JUnit4TestAdapter;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;

import de.unibonn.inf.dbdependenciesui.TestFixtures;
import de.unibonn.inf.dbdependenciesui.hibernate.HibernateDAOFactory;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseConnection;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseTable;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseView;
import de.unibonn.inf.dbdependenciesui.metadata.IViewSqlParser;
import de.unibonn.inf.dbdependenciesui.metadata.MetaDataFactory;
import de.unibonn.inf.dbdependenciesui.metadata.MetaDataFactory.Vendor;
import de.unibonn.inf.dbdependenciesui.misc.XmlHelperTools;
import de.unibonn.inf.dbdependenciesui.tests.HibernateTestingUtil;

public class OracleMetaDataImpl2Test {

	/**
	 * Return the suite adapter. This is used for test suites - a workaround for jUnit 3.x Test Suites.
	 * 
	 * @return
	 */
	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(OracleMetaDataImpl2Test.class);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		HibernateTestingUtil.overrideConfiguration(true);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		HibernateDAOFactory.closeSession();
	}

	@Test
	public final void testEmptyMetaParameters() {
		final IViewSqlParser parser = MetaDataFactory.createViewSqlParser(Vendor.ORACLE);

		final List<DatabaseTable> tables = new ArrayList<DatabaseTable>();
		final List<DatabaseView> views = new ArrayList<DatabaseView>();

		for (final DatabaseView view : views) {
			parser.parse(tables, views, view);
		}

		Assert.assertEquals(0, parser.getAffectedViews().size());
		Assert.assertEquals(0, parser.getNegativeRelationViews().size());
		Assert.assertEquals(0, parser.getNegativeRelationViews().size());
	}

	/**
	 * Verify both {@link TestFixtures} model constructors for modelset "02".
	 * 
	 * @throws NotSupportedException
	 * @throws IllegalArgumentException
	 * @throws IOException
	 */
	@Test
	public final void testMetaOfModels02() throws NotSupportedException, IllegalArgumentException, IOException {

		final DatabaseConnection actualConnection = TestFixtures.createOnlineDatabaseModels02();
		final DatabaseConnection expectedConnection = TestFixtures.createOfflineDatabaseModels02();

		final Map<String, DatabaseTable> expectedTables = new HashMap<String, DatabaseTable>();
		final Map<String, DatabaseView> expectedViews = new HashMap<String, DatabaseView>();

		// Check number of tables and views.
		Assert.assertEquals(expectedConnection.getTables().size(), actualConnection.getTables().size());
		Assert.assertEquals(expectedConnection.getViews().size(), actualConnection.getViews().size());

		// Build second index map.
		for (final DatabaseTable table : expectedConnection.getTables()) {
			final String title = table.getTitle();
			expectedTables.put(title, table);
		}
		for (final DatabaseView view : expectedConnection.getViews()) {
			final String title = view.getTitle();
			expectedViews.put(title, view);
		}

		// For each actual table or view, check is expected presence and verify
		// the schema.
		for (final DatabaseTable actualTable : actualConnection.getTables()) {
			final String title = actualTable.getTitle();
			final String actualSchema = makeUnified(actualTable.getDdlSchema());

			final DatabaseTable expectedTable = expectedTables.get(title);
			Assert.assertNotNull("Check existence of corresponding object.", expectedTable);

			final String expectedSchema = makeUnified(expectedTable.getDdlSchema());
			Assert.assertEquals("The ddl schema have to be equal.", expectedSchema, actualSchema);
		}
		for (final DatabaseView actualView : actualConnection.getViews()) {
			final String title = actualView.getTitle();
			final String actualSchema = makeUnified(actualView.getDdlSchema());

			final DatabaseView expectedView = expectedViews.get(title);
			Assert.assertNotNull("Check existence of corresponding object.", expectedView);

			final String expectedSchema = makeUnified(expectedView.getDdlSchema());
			Assert.assertEquals("The ddl schema have to be equal.", expectedSchema, actualSchema);
		}

	}

	/**
	 * Transform and resort the given xml string.
	 * 
	 * @param ddlSchemaObject
	 * @return
	 */
	private String makeUnified(final String xml) {
		final Document doc = XmlHelperTools.newDocument(xml);
		XmlHelperTools.sortChildNodes(doc);
		return XmlHelperTools.toXML(doc);
	}
}
