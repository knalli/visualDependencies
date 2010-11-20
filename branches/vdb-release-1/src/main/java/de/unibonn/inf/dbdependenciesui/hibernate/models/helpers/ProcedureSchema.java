/**
 * $Id: Main.java 41 2009-03-01 16:21:20Z philipp $
 */
package de.unibonn.inf.dbdependenciesui.hibernate.models.helpers;

import java.io.NotSerializableException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.org.apache.xerces.internal.dom.DocumentImpl;

import de.unibonn.inf.dbdependenciesui.Configuration;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseConnection;
import de.unibonn.inf.dbdependenciesui.misc.XmlHelperTools;

/**
 * @author Marc Kastleiner
 */
public class ProcedureSchema implements Serializable {
	private static final long serialVersionUID = -6232818704543106780L;

	protected transient static Logger log = Logger.getLogger(Configuration.LOGGER);

	// 1 trigger_name, 2 trigger_type, 3 triggering_event, 4 table_owner, 5 base_object_type, 6 table_name, 7
	// column_name, 8 referencing_names, 9 when_clause, 10 status, 11 description, 12 action_type, 13
	// trigger_body, 14 crossedition, 15 before_statement, 16 before_row, 17 after_row, 18 after_statement, 19
	// instead_of_row

	protected String name;

	protected String type;

	protected String body;

	protected String state;

	protected String creationDate;

	protected List<String> affectedDeletedTables;

	protected List<String> affectedInsertedTables;

	protected List<String> affectedUpdatedTables;

	protected List<String> usedTables;

	/**
	 * instance of a xpath factory
	 */
	private transient XPathFactory xPathFactory;

	/**
	 * instance of a xpath object
	 */
	private transient XPath xPath;

	/**
	 * current connection
	 */
	private DatabaseConnection connection = null;

	/**
	 * Creates a new trigger schema object with empty values.
	 * 
	 * @param xml
	 */
	public ProcedureSchema() {

		// Reset all variables, create and configure xpath
		initialize();
	}

	/**
	 * Creates a new trigger schema object by the given xml string.
	 * 
	 * @see #deserialize(String)
	 * @param xml
	 */
	public ProcedureSchema(final String xml) {
		try {
			deserialize(xml);
		} catch (final Exception e) {
			initialize();
			if ((xml != null) && !xml.isEmpty()) {
				final String msg = "Invalid input string. Could not unserialize it.";
				DdlSchema.log.warning(msg);
			}
		}
	}

	/**
	 * Deserialize the given xml string.
	 * 
	 * @param xml
	 * @throws IllegalArgumentException
	 */
	final private void deserialize(final String xml) throws IllegalArgumentException {
		final Document doc = XmlHelperTools.newDocument(xml);
		if (doc == null) { throw new IllegalArgumentException(); }

		// Reset all variables, create and configure xpath
		initialize();

		try {
			readSchema((Node) xPath.evaluate("/schema/attributes", doc, XPathConstants.NODE));
		} catch (final XPathExpressionException e) {
			final String msg = "Could not compile xpath expression.";
			DdlSchema.log.warning(msg);
			throw new IllegalArgumentException(msg);
		}
	}

	/**
	 * Retrive the relations from the given list of nodes.
	 * 
	 * @param nodes
	 * @throws XPathExpressionException
	 */
	final private void readSchema(final Node node) throws XPathExpressionException {
		NodeList sublist;

		sublist = this.<NodeList> evaluateXPath("affected-tables/table", node, XPathConstants.NODESET);
		if (sublist != null) {
			for (int j = 0; j < sublist.getLength(); j++) {
				final Node subitem = sublist.item(j);
				final Node attribute = subitem.getAttributes().getNamedItem("type");
				final String attributeValue = attribute.getNodeValue();
				final String title = subitem.getTextContent().trim();

				if ("DELETE".equalsIgnoreCase(attributeValue)) {
					affectedDeletedTables.add(title);
				} else if ("INSERT".equalsIgnoreCase(attributeValue)) {
					affectedInsertedTables.add(title);
				} else if ("UPDATE".equalsIgnoreCase(attributeValue)) {
					affectedUpdatedTables.add(title);
				}
			}
		}

		body = evaluateXPathToString("body/text()", node);

		name = evaluateXPathToString("name/text()", node);

		type = evaluateXPathToString("type/text()", node);

		state = evaluateXPathToString("state/text()", node);

		creationDate = evaluateXPathToString("creation-date/text()", node);

		sublist = this.<NodeList> evaluateXPath("used-tables/table", node, XPathConstants.NODESET);
		if (sublist != null) {
			for (int j = 0; j < sublist.getLength(); j++) {
				final Node subitem = sublist.item(j);
				usedTables.add(subitem.getTextContent().trim());
			}
		}
	}

	@SuppressWarnings("unchecked")
	protected <T> T evaluateXPath(final String query, final Node node, final QName constant)
			throws XPathExpressionException {
		return (T) xPath.evaluate(query, node, constant);
	}

	protected boolean evaluateXPathToBoolean(final String query, final Node node) throws XPathExpressionException {
		return Boolean.parseBoolean(this.<String> evaluateXPath(query, node, XPathConstants.STRING));
	}

	protected String evaluateXPathToString(final String query, final Node node) throws XPathExpressionException {
		return this.<String> evaluateXPath(query, node, XPathConstants.STRING);
	}

	/**
	 * Reset all local states.
	 */
	private void initialize() {
		affectedDeletedTables = new ArrayList<String>();
		affectedInsertedTables = new ArrayList<String>();
		affectedUpdatedTables = new ArrayList<String>();
		name = null;
		type = null;
		body = null;
		state = null;
		creationDate = null;

		usedTables = new ArrayList<String>();

		if (xPathFactory == null) {
			xPathFactory = XPathFactory.newInstance();
			xPath = xPathFactory.newXPath();
		}
	}

	/**
	 * Serialize the current state of this object into a xml string.
	 * 
	 * @return
	 * @throws NotSerializableException
	 */
	public String serialize() throws NotSerializableException {
		final Document doc = new DocumentImpl();

		final Element root = doc.createElement("schema");
		doc.appendChild(root);

		root.appendChild(buildSchema(doc));

		return XmlHelperTools.toXML(doc);
	}

	/**
	 * Build up a node named "relations" with subnodes of all relational tables and views.
	 * 
	 * @param doc
	 * @return
	 */
	private Node buildSchema(final Document doc) {
		final Element schema = doc.createElement("attributes");

		Node node;
		Node nodes;

		nodes = doc.createElement("affected-tables");
		for (final String affectedTable : affectedDeletedTables) {
			node = doc.createElement("table");
			final Attr attribute = doc.createAttribute("type");
			attribute.setTextContent("DELETE");
			node.getAttributes().setNamedItem(attribute);
			node.setTextContent(affectedTable);
			nodes.appendChild(node);
		}
		for (final String affectedTable : affectedInsertedTables) {
			node = doc.createElement("table");
			final Attr attribute = doc.createAttribute("type");
			attribute.setTextContent("INSERT");
			node.getAttributes().setNamedItem(attribute);
			node.setTextContent(affectedTable);
			nodes.appendChild(node);
		}
		for (final String affectedTable : affectedUpdatedTables) {
			node = doc.createElement("table");
			final Attr attribute = doc.createAttribute("type");
			attribute.setTextContent("UPDATE");
			node.getAttributes().setNamedItem(attribute);
			node.setTextContent(affectedTable);
			nodes.appendChild(node);
		}
		schema.appendChild(nodes);

		node = doc.createElement("body");
		node.setTextContent(body);
		schema.appendChild(node);

		node = doc.createElement("name");
		node.setTextContent(name);
		schema.appendChild(node);

		node = doc.createElement("type");
		node.setTextContent(type);
		schema.appendChild(node);

		node = doc.createElement("state");
		node.setTextContent(state);
		schema.appendChild(node);

		node = doc.createElement("creation-date");
		node.setTextContent(creationDate);
		schema.appendChild(node);

		nodes = doc.createElement("used-tables");
		for (final String affectedTable : usedTables) {
			node = doc.createElement("table");
			node.setTextContent(affectedTable);
			nodes.appendChild(node);
		}
		schema.appendChild(nodes);

		return schema;
	}

	/**
	 * Set the current connection.
	 * 
	 * @param connection
	 */
	public void setConnection(final DatabaseConnection connection) {
		this.connection = connection;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @return the body
	 */
	public String getBody() {
		return body;
	}

	/**
	 * @return the state
	 */
	public String getState() {
		return state;
	}

	/**
	 * @return the creation date
	 */
	public String getCreationDate() {
		return creationDate;
	}

	/**
	 * @return the affectedTables
	 */
	public List<String> getAffectedTables() {
		final List<String> result = new ArrayList<String>();

		result.addAll(getAffectedAlteredTable());
		result.addAll(getAffectedDeletedTable());
		result.addAll(getAffectedInsertedTable());
		result.addAll(getAffectedUpdatedTable());

		return result;
	}

	public List<String> getAffectedAlteredTable() {
		return Collections.<String> emptyList();
	}

	public List<String> getAffectedDeletedTable() {
		return affectedDeletedTables;
	}

	public List<String> getAffectedInsertedTable() {
		return affectedInsertedTables;
	}

	public List<String> getAffectedUpdatedTable() {
		return affectedUpdatedTables;
	}

	/**
	 * @return the usedTables
	 */
	public List<String> getUsedTables() {
		return Collections.unmodifiableList(usedTables);
	}

}
