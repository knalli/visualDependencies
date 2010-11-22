package de.unibonn.inf.dbdependenciesui.metadata.tests;

import javax.transaction.NotSupportedException;

import junit.framework.JUnit4TestAdapter;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.unibonn.inf.dbdependenciesui.TestFixtures;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseConnection;
import de.unibonn.inf.dbdependenciesui.metadata.IMetaData;
import de.unibonn.inf.dbdependenciesui.metadata.MetaDataFactory;
import de.unibonn.inf.dbdependenciesui.metadata.MetaDataFactory.Vendor;

public class OracleMetaDataImplTest {

	/**
	 * Return the suite adapter. This is used for test suites - a workaround for jUnit 3.x Test Suites.
	 * 
	 * @return
	 */
	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(OracleMetaDataImplTest.class);
	}

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {}

	@Before
	public void setUp() throws Exception {
	// HibernateTestingUtil.overrideConfiguration(true);
	}

	@After
	public void tearDown() throws Exception {}

	@Test
	public final void testOracleMetaDataImpl() {
		final IMetaData meta = MetaDataFactory.create(Vendor.ORACLE);

		Assert.assertNotNull("Check if object exists", meta);
		Assert.assertEquals("Vendor should be equal.", Vendor.ORACLE, meta.getVendor());
	}

	@Test
	public final void testGetNumberOfTables() {
		final IMetaData meta = MetaDataFactory.create(Vendor.ORACLE);

		Assert.assertEquals("Check empty list.", 0, meta.getNumberOfTables());
	}

	@Test
	public final void testGetNumberOfTriggers() {
		final IMetaData meta = MetaDataFactory.create(Vendor.ORACLE);

		Assert.assertEquals("Check empty list.", 0, meta.getNumberOfTriggers());
	}

	@Test
	public final void testGetNumberOfViews() {
		final IMetaData meta = MetaDataFactory.create(Vendor.ORACLE);

		Assert.assertEquals("Check empty list.", 0, meta.getNumberOfViews());
	}

	/**
	 * See {@link OracleMetaDataImpl2Test}
	 */
	public final void testAnalyze() {}

	/**
	 * See {@link OracleMetaDataImpl2Test}
	 */
	public final void testParseViews() {}

	/**
	 * Already tested in {@link #testOracleMetaDataImpl()}
	 */
	public final void testGetVendor() {}

	/**
	 * See {@link #testConnect()}
	 */
	public final void testClose() {}

	public final void testSetDatabaseConnection() {}

	public final void testSetConnectString() {}

	public final void testSetConnectStringStringString() {}

	@Test
	public final void testSetReplaceMode() {
		final IMetaData meta = MetaDataFactory.create(Vendor.ORACLE);

		Assert.assertTrue("Replace mode is default true.", meta.isReplaceMode());

		meta.setReplaceMode(false);
		Assert.assertFalse("Replace mode is false.", meta.isReplaceMode());
	}

	@Test
	public final void testConnect() throws NotSupportedException {
		final IMetaData meta = MetaDataFactory.create(Vendor.ORACLE);

		if (!TestFixtures.REAL_DB_ENABLED) {
			throw new NotSupportedException("Debug connection is not available.");
		}
		
		final DatabaseConnection connection = new DatabaseConnection();
		connection.setHost(TestFixtures.REAL_DB_CONN_HOST);
		connection.setPort(TestFixtures.REAL_DB_CONN_PORT);
		connection.setUsername(TestFixtures.REAL_DB_CONN_USER);
		connection.setPassword(TestFixtures.REAL_DB_CONN_PASS);
		connection.setSchema(TestFixtures.REAL_DB_CONN_SCHM);
		connection.setDatabase(TestFixtures.REAL_DB_CONN_SCHM);
		connection.setSysdba(false);
		meta.setDatabaseConnection(connection);
		Assert.assertFalse("Should still offline.", meta.isConnected());

		Assert.assertTrue("Should be online (1).", meta.connect());
		Assert.assertTrue("Should be online (2).", meta.isConnected());

		meta.close();
		Assert.assertFalse("Should now be offline again.", meta.isConnected());
	}

	/**
	 * Test case for isConnected(): false. The result of true is tested in {@link #testConnect()}.
	 */
	@Test
	public final void testIsConnected() {
		final IMetaData meta = MetaDataFactory.create(Vendor.ORACLE);

		Assert.assertFalse("There should be no connection active.", meta.isConnected());
	}

	@Test
	public final void testClearErrors() throws NotSupportedException {
		final IMetaData meta = MetaDataFactory.create(Vendor.ORACLE);

		Assert.assertFalse("There should be no errors.", meta.isErrorOccured());

		final DatabaseConnection connection = new DatabaseConnection();
		connection.setHost("not/exist");
		connection.setPort(12345);
		connection.setUsername("user");
		connection.setPassword("pass");
		connection.setSchema(null);
		connection.setDatabase(null);
		connection.setSysdba(false);
		meta.setDatabaseConnection(connection);
		meta.connect();

		Assert.assertTrue("There should be errors.", meta.isErrorOccured());

		meta.clearErrors();
		Assert.assertFalse("There should be no more errors.", meta.isErrorOccured());
	}

	@Test
	public final void testGetLastError() throws NotSupportedException {
		final IMetaData meta = MetaDataFactory.create(Vendor.ORACLE);

		Assert.assertFalse("There should be no errors.", meta.isErrorOccured());

		final DatabaseConnection connection = new DatabaseConnection();
		connection.setHost("not/exist");
		connection.setPort(12345);
		connection.setUsername("user");
		connection.setPassword("pass");
		connection.setSchema(null);
		connection.setDatabase(null);
		connection.setSysdba(false);
		meta.setDatabaseConnection(connection);
		meta.connect();

		Assert.assertTrue("There should be errors.", meta.isErrorOccured());

		// Get error, but don't remove it.
		Assert.assertEquals("The Network Adapter could not establish the connection", meta.getLastError());

		Assert.assertTrue("There should be still that error.", meta.isErrorOccured());

		// Get error and remove it.
		Assert.assertEquals("The Network Adapter could not establish the connection", meta.getNextError());

		Assert.assertFalse("There should be no more errors.", meta.isErrorOccured());

		// Check invalid states.
		Assert.assertNull("If there is no error it return null.", meta.getLastError());
		Assert.assertNull("If there is no error it return null.", meta.getNextError());
	}

	/**
	 * See {@link #testGetLastError()}
	 */
	public final void testGetNextError() {}

	@Test
	public final void testIsErrorOccured() throws NotSupportedException {
		final IMetaData meta = MetaDataFactory.create(Vendor.ORACLE);

		Assert.assertFalse("There should be no errors.", meta.isErrorOccured());

		final DatabaseConnection connection = new DatabaseConnection();
		connection.setHost("not/exist");
		connection.setPort(12345);
		connection.setUsername("user");
		connection.setPassword("pass");
		connection.setSchema(null);
		connection.setDatabase(null);
		connection.setSysdba(false);
		meta.setDatabaseConnection(connection);
		meta.connect();

		Assert.assertTrue("There should be errors.", meta.isErrorOccured());

	}
}
