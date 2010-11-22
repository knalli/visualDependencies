package de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.tests;

import java.io.IOException;
import java.io.Serializable;
import java.util.Comparator;

import junit.framework.Assert;
import junit.framework.JUnit4TestAdapter;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseTable;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.ConnectionAttributesMap;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.ConnectionProperties;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.ConnectionAttributesMap.Type;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.ConnectionProperties.AttributeMapSet;
import de.unibonn.inf.dbdependenciesui.misc.XmlHelperTools;
import de.unibonn.inf.dbdependenciesui.ui.helpers.FileTools;

public class GraphPropertiesTest {

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
		return new JUnit4TestAdapter(GraphPropertiesTest.class);
	}

	/**
	 * Test case example xml.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testGraphPropertiesString() throws IOException {

		final Comparator<Node> comp = new PropertiesDocumentComparator("key");

		final Document doc = XmlHelperTools.newDocument(FileTools.readFileAsString("examples/graphattributes.xml"));
		XmlHelperTools.sortChildNodes(doc, comp);
		final String expected = XmlHelperTools.toXML(doc);

		ConnectionProperties properties;
		properties = new ConnectionProperties(expected);
		String actual = properties.serialize();
		final Document doc2 = XmlHelperTools.newDocument(actual);
		XmlHelperTools.sortChildNodes(doc2, comp);
		actual = XmlHelperTools.toXML(doc2);

		Assert.assertEquals("Verify properties xml", expected, actual);
	}

	/**
	 * Test case for empty object.
	 */

	@Test
	public void testEmptyObject() {
		final ConnectionProperties properties = new ConnectionProperties();

		Assert.assertEquals("Check length of serialized empty object.", 118, properties.serialize().length());
	}

	/**
	 * Test case for default values of an attributes map.
	 */
	@Test
	public void testGetAttributesMapDefaultValues() {
		final ConnectionProperties properties = new ConnectionProperties();
		final DatabaseTable table1 = new DatabaseTable();
		table1.setTitle("table1");
		final ConnectionAttributesMap map = properties.getAttributesMap(AttributeMapSet.DEFAULT, table1);

		Assert.assertEquals("Check default value of positionX.", 0, map.getPositionX());
		Assert.assertEquals("Check default value of positionY.", 0, map.getPositionY());
		Assert.assertEquals("Check default value of isVisible.", true, map.isVisible());

		Assert.assertEquals("Check default value of positionX (genric).", null, map.getValue(Type.POSITION_X));
		Assert.assertEquals("Check default value of positionY (genric).", null, map.getValue(Type.POSITION_Y));
		Assert.assertEquals("Check default value of isVisible (genric).", null, map.getValue(Type.VISIBLE));
	}

	/**
	 * Test case for getter/setter of an attributes map.
	 */
	@Test
	public void testGetAttributesMapSetters() {
		final ConnectionProperties properties = new ConnectionProperties();
		final DatabaseTable table1 = new DatabaseTable();
		table1.setTitle("table1");
		final ConnectionAttributesMap map = properties.getAttributesMap(AttributeMapSet.DEFAULT, table1);

		map.setPositionX(7);
		map.setPositionY(34);
		map.setVisible(false);

		Assert.assertEquals("Check value of positionX.", 7, map.getPositionX());
		Assert.assertEquals("Check value of positionY.", 34, map.getPositionY());
		Assert.assertEquals("Check value of isVisible.", false, map.isVisible());

		Assert.assertEquals("Check value of positionX (genric).", 7, map.getValue(Type.POSITION_X));
		Assert.assertEquals("Check value of positionY (genric).", 34, map.getValue(Type.POSITION_Y));
		Assert.assertEquals("Check value of isVisible (genric).", false, map.getValue(Type.VISIBLE));
	}

	/**
	 * Special comparator sorting nodes by node name. Equal node will sorted by their key attribute.
	 * 
	 * @author Jan Philipp
	 */
	private static class PropertiesDocumentComparator implements Comparator<Node>, Serializable {

		private static final long serialVersionUID = 5377781628084840964L;

		final String attributeName;

		public PropertiesDocumentComparator(final String attributeName) {
			this.attributeName = attributeName;
		}

		@Override
		public int compare(final Node arg0, final Node arg1) {
			final String attr = attributeName;

			final int nodenames = (arg0).getNodeName().compareTo((arg1).getNodeName());

			if ((nodenames == 0) && (arg0 instanceof Element) && (arg1 instanceof Element)) {
				return ((Element) arg0).getAttribute(attr).compareTo(((Element) arg1).getAttribute(attr));
			} else {
				return nodenames;
			}
		}

	}

}
