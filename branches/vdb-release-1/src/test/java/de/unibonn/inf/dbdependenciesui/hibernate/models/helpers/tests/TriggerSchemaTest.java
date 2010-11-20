package de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.tests;

import java.io.IOException;
import java.io.NotSerializableException;

import junit.framework.Assert;
import junit.framework.JUnit4TestAdapter;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.TriggerSchema;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.TriggerSchemaEditable;
import de.unibonn.inf.dbdependenciesui.misc.XmlHelperTools;
import de.unibonn.inf.dbdependenciesui.ui.helpers.FileTools;

public class TriggerSchemaTest {

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
		return new JUnit4TestAdapter(TriggerSchemaTest.class);
	}

	/**
	 * Test case for an empty/invalid constructor call. parameter string: null/"" result: The result xml is 102
	 * characters long.
	 * 
	 * @throws NotSerializableException
	 */
	@Test
	public void testEmptyObject1() throws NotSerializableException {

		final TriggerSchema triggerSchema = new TriggerSchema("");
		Assert.assertEquals("Serialization of an empty object.", 716, triggerSchema.serialize().length());
	}

	/**
	 * Test case for an empty/invalid constructor call. parameter string: null/"" result: The result xml is 102
	 * characters long.
	 * 
	 * @throws NotSerializableException
	 */
	@Test
	public void testEmptyObject2() throws NotSerializableException {

		final TriggerSchema triggerSchema = new TriggerSchema(null);

		Assert.assertEquals("Serialization of an empty object.", 716, triggerSchema.serialize().length());
	}

	/**
	 * (Re-test) Test case for an empty/invalid constructor call. parameter string: null/"" result: The result xml is
	 * 102 characters long.
	 * 
	 * @throws NotSerializableException
	 */
	@Test
	public void testEditableEmptyObject1() throws NotSerializableException {

		final TriggerSchemaEditable triggerSchema = new TriggerSchemaEditable("");

		Assert.assertEquals("Serialization of an empty object.", 716, triggerSchema.serialize().length());
	}

	/**
	 * (Re-test) Test case for an empty/invalid constructor call. parameter string: null/"" result: The result xml is
	 * 102 characters long.
	 * 
	 * @throws NotSerializableException
	 */
	@Test
	public void testEditableEmptyObject2() throws NotSerializableException {

		final TriggerSchemaEditable triggerSchema = new TriggerSchemaEditable(null);
		Assert.assertEquals("Serialization of an empty object.", 716, triggerSchema.serialize().length());
	}

	@Test
	public void verifyByString() throws IOException {
		// Read example and sort all nodes in ascending order.
		final Document doc = XmlHelperTools.newDocument(FileTools.readFileAsString("examples/trigger.xml"));
		XmlHelperTools.sortChildNodes(doc);
		final String content = XmlHelperTools.toXML(doc);
		Assert.assertTrue("Not an empty object.", 716 < content.length());

		// Serialize object and sort all nodes in ascending order.
		final TriggerSchema schema = new TriggerSchema(content);
		String serialized = schema.serialize();
		final Document doc2 = XmlHelperTools.newDocument(FileTools.readFileAsString("examples/trigger.xml"));
		XmlHelperTools.sortChildNodes(doc2);
		serialized = XmlHelperTools.toXML(doc2);

		Assert.assertEquals("Formatted string output should be equal.", content, serialized);
	}

}
