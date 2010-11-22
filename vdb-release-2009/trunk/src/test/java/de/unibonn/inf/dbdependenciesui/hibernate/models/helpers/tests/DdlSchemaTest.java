package de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.tests;

import java.io.IOException;
import java.io.NotSerializableException;

import junit.framework.Assert;
import junit.framework.JUnit4TestAdapter;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.Column;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.DdlSchema;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.DdlSchemaEditable;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.ForeignKey;
import de.unibonn.inf.dbdependenciesui.misc.XmlHelperTools;
import de.unibonn.inf.dbdependenciesui.ui.helpers.FileTools;

public class DdlSchemaTest {

	@Before
	public void setUpBefore() throws Exception {
	// Need no hibernate setup.
	// HibernateTestingUtil.overrideConfiguration(true);
	}

	/**
	 * Return the suite adapter. This is used for test suites - a workaround for jUnit 3.x Test Suites.
	 * 
	 * @return
	 */
	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(DdlSchemaTest.class);
	}

	/**
	 * Test case for an empty/invalid constructor call. parameter string: null/"" result: The result xml is 102
	 * characters long.
	 * 
	 * @throws NotSerializableException
	 */
	@Test
	public void testEmptyObject1() throws NotSerializableException {

		final DdlSchema ddlschema = new DdlSchema("");
		Assert.assertEquals("Serialization of an empty object.", 118, ddlschema.serialize().length());
	}

	/**
	 * Test case for an empty/invalid constructor call. parameter string: null/"" result: The result xml is 102
	 * characters long.
	 * 
	 * @throws NotSerializableException
	 */
	@Test
	public void testEmptyObject2() throws NotSerializableException {

		final DdlSchema ddlschema = new DdlSchema(null);

		Assert.assertEquals("Serialization of an empty object.", 118, ddlschema.serialize().length());
	}

	/**
	 * (Re-test) Test case for an empty/invalid constructor call. parameter string: null/"" result: The result xml is
	 * 102 characters long.
	 * 
	 * @throws NotSerializableException
	 */
	@Test
	public void testEditableEmptyObject1() throws NotSerializableException {

		final DdlSchemaEditable ddlschema = new DdlSchemaEditable("");
		Assert.assertEquals("Serialization of an empty object.", 118, ddlschema.serialize().length());
	}

	/**
	 * (Re-test) Test case for an empty/invalid constructor call. parameter string: null/"" result: The result xml is
	 * 102 characters long.
	 * 
	 * @throws NotSerializableException
	 */
	@Test
	public void testEditableEmptyObject2() throws NotSerializableException {

		final DdlSchemaEditable ddlschema = new DdlSchemaEditable(null);
		Assert.assertEquals("Serialization of an empty object.", 118, ddlschema.serialize().length());
	}

	@Test
	public void verifyByString() throws IOException {
		// Read example and sort all nodes in ascending order.
		final Document doc = XmlHelperTools.newDocument(FileTools.readFileAsString("examples/table.xml"));
		XmlHelperTools.sortChildNodes(doc);
		final String content = XmlHelperTools.toXML(doc);

		// Serialize object and sort all nodes in ascending order.
		final DdlSchema schema = new DdlSchema(content);
		String serialized = schema.serialize();
		final Document doc2 = XmlHelperTools.newDocument(FileTools.readFileAsString("examples/table.xml"));
		XmlHelperTools.sortChildNodes(doc2);
		serialized = XmlHelperTools.toXML(doc2);

		Assert.assertEquals("Formatted string output should be equal.", content, serialized);
	}

	/**
	 * Check existence of primary keys of example file. primary keys: 2, first name is fgh
	 * 
	 * @throws IOException
	 */
	@Test
	public void checkPrimaryKeys() throws IOException {
		final Document doc = XmlHelperTools.newDocument(FileTools.readFileAsString("examples/table.xml"));
		final String content = XmlHelperTools.toXML(doc);
		final DdlSchema schema = new DdlSchema(content);

		Assert.assertEquals(2, schema.getPrimaryKeys().size());

		Assert.assertEquals(1, schema.getForeignKeys().size());
		final ForeignKey foreignKey = schema.getForeignKeys().get(0);
		Assert.assertEquals("fgh", foreignKey.getName());

		Assert.assertEquals(2, schema.getUniqueKeys().size());
	}

	/**
	 * Check adding of simple columns. Verify list of columns.
	 */
	@Test
	public void addColumns() {
		final DdlSchemaEditable ddlschema = new DdlSchemaEditable("");

		Assert.assertEquals("There should be no columns.", 0, ddlschema.getColumns().size());

		ddlschema.addColumn(new Column("Column1"));
		Assert.assertEquals("There should be one column.", 1, ddlschema.getColumns().size());

		ddlschema.addColumn(new Column("Column2"));
		Assert.assertEquals("There should be two columns.", 2, ddlschema.getColumns().size());
	}

	/**
	 * Check adding and removing of simple columns. Verify list of columns.
	 */
	@Test
	public void removeColumns() {
		final DdlSchemaEditable ddlschema = new DdlSchemaEditable("");

		Assert.assertEquals("There should be no columns.", 0, ddlschema.getColumns().size());

		ddlschema.addColumn(new Column("Column1"));
		Assert.assertEquals("There should be one column.", 1, ddlschema.getColumns().size());

		ddlschema.addColumn(new Column("Column2"));
		Assert.assertEquals("There should be two columns.", 2, ddlschema.getColumns().size());

		ddlschema.removeColumn(new Column("Column2"));
		Assert.assertEquals("There should be one column.", 1, ddlschema.getColumns().size());

	}

	/**
	 * Check adding o simple columns with serialization and deserialization.
	 * 
	 * @throws NotSerializableException
	 */
	@Test
	public void addColumnsReImport() throws NotSerializableException {
		final DdlSchemaEditable ddlschema = new DdlSchemaEditable("");

		Assert.assertEquals("There should be no columns.", 0, ddlschema.getColumns().size());

		ddlschema.addColumn(new Column("Column1"));
		Assert.assertEquals("There should be one column.", 1, ddlschema.getColumns().size());

		ddlschema.addColumn(new Column("Column2"));
		Assert.assertEquals("There should be two columns.", 2, ddlschema.getColumns().size());

		final DdlSchema ddlschema2 = new DdlSchema(ddlschema.serialize());

		Assert.assertEquals("There should be two columns.", 2, ddlschema2.getColumns().size());
	}
}
