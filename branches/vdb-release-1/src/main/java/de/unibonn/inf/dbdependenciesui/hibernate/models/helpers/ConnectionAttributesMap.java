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

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This is the object representation of connection xml attributes. It serializes und deserializes xml to object
 * relations and can (theoretically) hold all kind of data.
 * 
 * @author Andre Kasper
 * @author Jan Philipp
 */
public class ConnectionAttributesMap implements Serializable {
	private static final long serialVersionUID = -1333185108547350277L;

	/**
	 * instance of a xpath object
	 */
	private transient final XPath xPath;

	/**
	 * internal attributesmap
	 */
	private transient final Map<Type, Object> attributes = new HashMap<Type, Object>();

	public ConnectionAttributesMap(final XPath xPath) {
		this.xPath = xPath;
		initialize();
	}

	public ConnectionAttributesMap(final XPath xPath, final Node node) {
		this.xPath = xPath;

		try {
			final NodeList nodes = (NodeList) this.xPath.evaluate("attributes/attribute", node, XPathConstants.NODESET);

			for (int i = 0; i < nodes.getLength(); i++) {
				try {
					final Node node2 = nodes.item(i);
					final NamedNodeMap attrs = node2.getAttributes();

					final String clazz = attrs.getNamedItem("class").getNodeValue();
					final String key = attrs.getNamedItem("key").getNodeValue();
					final String value = node2.getTextContent();

					final Type type = Type.valueOf(key);

					if ("Integer".equals(clazz)) {
						attributes.put(type, Integer.parseInt(value));
					} else if ("Boolean".equals(clazz)) {
						attributes.put(type, Boolean.parseBoolean(value));
					} else {
						attributes.put(type, value.toString());
					}
				} catch (final Exception e) {}
			}
		} catch (final XPathExpressionException e) {}
	}

	public Node serialize(final Document doc) {
		final Node node = doc.createElement("attributes");

		for (final Type type : attributes.keySet()) {
			final Object value = attributes.get(type);

			final Node node2 = doc.createElement("attribute");
			final NamedNodeMap attrs = node2.getAttributes();

			final Attr clazz = doc.createAttribute("class");
			final Attr key = doc.createAttribute("key");

			attrs.setNamedItem(clazz);
			attrs.setNamedItem(key);

			key.setNodeValue(type.toString());
			node2.setTextContent(value.toString());

			if (value instanceof Boolean) {
				clazz.setNodeValue("Boolean");
			} else if (value instanceof Integer) {
				clazz.setNodeValue("Integer");
			} else if (value instanceof String) {
				clazz.setNodeValue("String");
			} else {
				// invalid value
				continue;
			}

			node.appendChild(node2);
		}

		return node;
	}

	public Object getValue(final Type type) {
		return attributes.get(type);
	}

	public void setPositionX(final int value) {
		attributes.put(Type.POSITION_X, value);
	}

	public void setPositionY(final int value) {
		attributes.put(Type.POSITION_Y, value);
	}

	public void setVisible(final boolean value) {
		attributes.put(Type.VISIBLE, value);
	}

	public int getPositionX() {
		final Object value = attributes.get(Type.POSITION_X);
		if (value != null) { return (Integer) value; }
		return 0;
	}

	public int getPositionY() {
		final Object value = attributes.get(Type.POSITION_Y);
		if (value != null) { return (Integer) value; }
		return 0;
	}

	public boolean isVisible() {
		final Object value = attributes.get(Type.VISIBLE);
		if (value != null) { return (Boolean) value; }
		return true;
	}

	private void initialize() {
		clearAttributes();
	}

	/**
	 * Remove all positions.
	 */
	public void clearPositions() {
		attributes.remove(Type.POSITION_X);
		attributes.remove(Type.POSITION_Y);
	}

	/**
	 * Remove all visibilities (selections).
	 */
	public void clearVisibilities() {
		attributes.remove(Type.VISIBLE);
	}

	/**
	 * Remove all attributes.
	 */
	final public void clearAttributes() {
		attributes.clear();
	}

	public String getLayout() {
		final Object value = attributes.get(Type.LAYOUT);
		if (value != null) { return (String) value; }
		return "";
	}

	public void setLayout(final String value) {
		attributes.put(Type.LAYOUT, value);
	}

	public static enum Type {
		/**
		 * position x in the graph
		 */
		POSITION_X,

		/**
		 * position y in the graph
		 */
		POSITION_Y,

		/**
		 * object visible in the graph
		 */
		VISIBLE,

		/**
		 * used layout
		 */
		LAYOUT;
	}
}
