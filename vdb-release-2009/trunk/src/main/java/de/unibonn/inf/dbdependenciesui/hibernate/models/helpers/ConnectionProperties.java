/**
 * $Id: Main.java 41 2009-03-01 16:21:20Z philipp $
 */
package de.unibonn.inf.dbdependenciesui.hibernate.models.helpers;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.org.apache.xerces.internal.dom.DocumentImpl;

import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseObject;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseTrigger;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseView;
import de.unibonn.inf.dbdependenciesui.misc.XmlHelperTools;

/**
 * This is the object representation of connection xml properties. It serializes und deserializes xml to object
 * relations. Currently, it holds only a map of attributes.
 * 
 * @author Andre Kasper
 * @author Jan Philipp
 */
public class ConnectionProperties implements Serializable {
	private static final long serialVersionUID = 4410108326390501011L;

	/**
	 * instance of a xpath factory
	 */
	private transient XPathFactory xPathFactory;

	/**
	 * instance of a xpath object
	 */
	private transient XPath xPath;

	/**
	 * properties
	 */
	private final Map<AttributeMapSet, Map<String, ConnectionAttributesMap>> attributesMapSets = new HashMap<AttributeMapSet, Map<String, ConnectionAttributesMap>>();

	public ConnectionProperties() {
		initialize();
	}

	/**
	 * Deserialize the given xml string and build up the objects. If the string is not valid, the object will be empty.
	 * 
	 * @param xml
	 */
	public ConnectionProperties(final String xml) {
		initialize();

		try {
			deserialize(xml);
		} catch (final Exception e) {
			if ((xml != null) && !xml.isEmpty()) {
				final String msg = "Invalid input string. Could not unserialize it.";
				DdlSchema.log.info(msg);
			}
		}
	}

	/**
	 * Initialize the object to the required state.
	 */
	private void initialize() {
		attributesMapSets.clear();
		attributesMapSets.put(AttributeMapSet.VIEWS, new HashMap<String, ConnectionAttributesMap>());
		attributesMapSets.put(AttributeMapSet.TRIGGERS, new HashMap<String, ConnectionAttributesMap>());
		attributesMapSets.put(AttributeMapSet.ENTITYRELATIONS, new HashMap<String, ConnectionAttributesMap>());
		attributesMapSets.put(AttributeMapSet.PROCEDURES, new HashMap<String, ConnectionAttributesMap>());

		if (xPathFactory == null) {
			xPathFactory = XPathFactory.newInstance();
			xPath = xPathFactory.newXPath();
		}
	}

	/**
	 * Serialize the object to a xml string.
	 * 
	 * @return
	 */
	public String serialize() {
		final Document doc = new DocumentImpl();

		final Element root = doc.createElement("properties");
		doc.appendChild(root);

		for (final AttributeMapSet set : attributesMapSets.keySet()) {
			final Map<String, ConnectionAttributesMap> attributesMaps = attributesMapSets.get(set);
			final Element category = doc.createElement(set.toString().toLowerCase());
			for (final String key : attributesMaps.keySet()) {
				final int index = key.indexOf('-');
				final String type = key.substring(0, index);
				final String title = key.substring(index + 1);
				category.appendChild(serializeAttributes(type, title, attributesMaps.get(key), doc));
			}
			root.appendChild(category);
		}

		return XmlHelperTools.toXML(doc);
	}

	/**
	 * Build a nodeset for the given objects.
	 * 
	 * @param type
	 *            valid values are table or view
	 * @param title
	 *            internal name of the table/view
	 * @param attributes
	 * @param doc
	 * @return
	 */
	private Node serializeAttributes(final String type, final String title, final ConnectionAttributesMap attributes,
			final Document doc) {
		Node node;

		node = doc.createElement(type);
		final NamedNodeMap attrs = node.getAttributes();

		final Attr attrName = doc.createAttribute("name");
		attrName.setTextContent(title);
		attrs.setNamedItem(attrName);

		node.appendChild(attributes.serialize(doc));

		return node;
	}

	/**
	 * Deserialize the given xml string.
	 * 
	 * @param xml
	 * @throws IllegalArgumentException
	 */
	private void deserialize(final String xml) throws IllegalArgumentException {
		final Document doc = XmlHelperTools.newDocument(xml);
		if (doc == null) { throw new IllegalArgumentException(); }

		// Reset all variables, create and configure xpath
		initialize();

		try {
			final NodeList categories = (NodeList) xPath.evaluate("/properties/*", doc, XPathConstants.NODESET);
			for (int i = 0; i < categories.getLength(); i++) {
				final Node categoryItem = categories.item(i);
				final AttributeMapSet category = AttributeMapSet.valueOf(categoryItem.getNodeName().toUpperCase());
				final NodeList nodes = (NodeList) xPath.evaluate("*", categoryItem, XPathConstants.NODESET);
				deserializeAttributes(category, nodes);
			}
		} catch (final XPathExpressionException e) {
			final String msg = "Could not compile xpath expression.";
			DdlSchema.log.info(msg);
			throw new IllegalArgumentException(msg);
		}
	}

	/**
	 * Deserialize the nodes and generate the appropriate property keys and values.
	 * 
	 * @param category
	 * @param nodes
	 */
	private void deserializeAttributes(final AttributeMapSet category, final NodeList nodes) {
		final Map<String, ConnectionAttributesMap> attributesMaps = attributesMapSets.get(category);
		for (int i = 0; i < nodes.getLength(); ++i) {
			final Node node = nodes.item(i);
			final String type = node.getNodeName();
			final String title = node.getAttributes().getNamedItem("name").getNodeValue();

			if ("table".equals(type) || "view".equals(type) || "trigger".equals(type)) {
				attributesMaps.put(type + "-" + title, new ConnectionAttributesMap(xPath, node));
			}
		}
	}

	public ConnectionAttributesMap getAttributesMap(final AttributeMapSet category, final DatabaseObject object) {

		String title;
		if (object instanceof DatabaseTrigger) {
			title = "trigger" + '-' + object.getTitle();
		} else if (object instanceof DatabaseView) {
			title = "view" + '-' + object.getTitle();
		} else {
			title = "table" + '-' + object.getTitle();
		}

		// Create set if empty.
		if (!attributesMapSets.containsKey(category)) {
			attributesMapSets.put(category, new HashMap<String, ConnectionAttributesMap>());
		}
		final Map<String, ConnectionAttributesMap> attributesMaps = attributesMapSets.get(category);

		// Create map if empty.
		if (!attributesMaps.containsKey(title)) {
			attributesMaps.put(title, new ConnectionAttributesMap(xPath));
		}
		return attributesMaps.get(title);
	}

	/**
	 * Category
	 */
	public static enum AttributeMapSet {
		DEFAULT, VIEWS, TRIGGERS, ENTITYRELATIONS, PROCEDURES
	}
}
