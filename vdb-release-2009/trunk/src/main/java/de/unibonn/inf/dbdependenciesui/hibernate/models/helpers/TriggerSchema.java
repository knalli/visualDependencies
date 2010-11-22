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
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseTable;
import de.unibonn.inf.dbdependenciesui.misc.XmlHelperTools;

/**
 * This is the object representation of a trigger schema. A trigger schema contains the schema structure of a trigger.
 * The object will be deserialized by an xml string and will be serialized to a string. It contains: A list of columns,
 * keys (primary, unique, foreign) and relations. If you want an editable object, see {@link TriggerSchemaEditable} .
 * 
 * @uses {@link Column}
 * @uses {@link ForeignKey}
 * @uses {@link PrimaryKey}
 * @uses {@link Relation}
 * @uses {@link UniqueKey}
 * @author Andre Kasper
 * @author Jan Philipp
 */
public class TriggerSchema implements Serializable {
	private static final long serialVersionUID = -6232818704543106780L;

	protected transient static Logger log = Logger.getLogger(Configuration.LOGGER);

	// 1 trigger_name, 2 trigger_type, 3 triggering_event, 4 table_owner, 5 base_object_type, 6 table_name, 7
	// column_name, 8 referencing_names, 9 when_clause, 10 status, 11 description, 12 action_type, 13
	// trigger_body, 14 crossedition, 15 before_statement, 16 before_row, 17 after_row, 18 after_statement, 19
	// instead_of_row

	protected String name;

	protected String type;

	protected List<String> events;

	protected String tableOwner;

	protected String baseObjectType;

	protected String tableName;

	protected String columnName;

	protected String referencingNames;

	protected String whenClause;

	protected boolean enabled;

	protected String description;

	protected String actionType;

	protected String body;

	protected boolean crossEdition;

	protected boolean beforeStatement;

	protected boolean beforeRow;

	protected boolean afterStatement;

	protected boolean afterRow;

	protected boolean insteadOfRow;

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
	 * currrent connection
	 */
	private DatabaseConnection connection = null;

	/**
	 * Creates a new trigger schema object with empty values.
	 * 
	 * @param xml
	 */
	public TriggerSchema() {

		// Reset all variables, create and configure xpath
		initialize();
	}

	/**
	 * Creates a new trigger schema object by the given xml string.
	 * 
	 * @see #deserialize(String)
	 * @param xml
	 */
	public TriggerSchema(final String xml) {
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
		actionType = evaluateXPathToString("action-type/text()", node);

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

		afterRow = evaluateXPathToBoolean("is-after-row/text()", node);

		afterStatement = evaluateXPathToBoolean("is-after-statement/text()", node);

		baseObjectType = evaluateXPathToString("base-object-type/text()", node);

		beforeRow = evaluateXPathToBoolean("is-before-row/text()", node);

		beforeStatement = evaluateXPathToBoolean("is-before-statement/text()", node);

		body = evaluateXPathToString("body/text()", node);

		columnName = evaluateXPathToString("column/text()", node);

		crossEdition = evaluateXPathToBoolean("is-crossedition/text()", node);

		description = evaluateXPathToString("description/text()", node);

		sublist = this.<NodeList> evaluateXPath("events/event", node, XPathConstants.NODESET);
		if (sublist != null) {
			for (int j = 0; j < sublist.getLength(); j++) {
				final Node subitem = sublist.item(j);
				events.add(subitem.getTextContent().trim());
			}
		}

		insteadOfRow = evaluateXPathToBoolean("is-instead-of-row/text()", node);

		name = evaluateXPathToString("name/text()", node);

		referencingNames = evaluateXPathToString("referencing-names/text()", node);

		enabled = evaluateXPathToBoolean("is-enabled/text()", node);

		tableName = evaluateXPathToString("table/text()", node);

		tableOwner = evaluateXPathToString("table-owner/text()", node);

		type = evaluateXPathToString("type/text()", node);

		sublist = this.<NodeList> evaluateXPath("used-tables/table", node, XPathConstants.NODESET);
		if (sublist != null) {
			for (int j = 0; j < sublist.getLength(); j++) {
				final Node subitem = sublist.item(j);
				usedTables.add(subitem.getTextContent().trim());
			}
		}

		whenClause = evaluateXPathToString("when-clause/text()", node);

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
		actionType = null;
		affectedDeletedTables = new ArrayList<String>();
		affectedInsertedTables = new ArrayList<String>();
		affectedUpdatedTables = new ArrayList<String>();
		afterRow = false;
		afterStatement = false;
		baseObjectType = null;
		beforeRow = false;
		beforeStatement = false;
		body = null;
		columnName = null;
		crossEdition = false;
		description = null;
		events = new ArrayList<String>();
		insteadOfRow = false;
		name = null;
		referencingNames = null;
		enabled = false;
		tableName = null;
		tableOwner = null;
		type = null;
		usedTables = new ArrayList<String>();
		whenClause = null;

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

		node = doc.createElement("action-type");
		node.setTextContent(actionType);
		schema.appendChild(node);

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

		node = doc.createElement("is-after-row");
		node.setTextContent(Boolean.toString(afterRow));
		schema.appendChild(node);

		node = doc.createElement("is-after-statement");
		node.setTextContent(Boolean.toString(afterStatement));
		schema.appendChild(node);

		node = doc.createElement("base-object-type");
		node.setTextContent(baseObjectType);
		schema.appendChild(node);

		node = doc.createElement("is-before-row");
		node.setTextContent(Boolean.toString(beforeRow));
		schema.appendChild(node);

		node = doc.createElement("is-before-statement");
		node.setTextContent(Boolean.toString(beforeStatement));
		schema.appendChild(node);

		node = doc.createElement("body");
		node.setTextContent(body);
		schema.appendChild(node);

		node = doc.createElement("column");
		node.setTextContent(columnName);
		schema.appendChild(node);

		node = doc.createElement("is-crossedition");
		node.setTextContent(Boolean.toString(crossEdition));
		schema.appendChild(node);

		node = doc.createElement("description");
		node.setTextContent(description);
		schema.appendChild(node);

		nodes = doc.createElement("events");
		for (final String event : events) {
			node = doc.createElement("event");
			node.setTextContent(event);
			nodes.appendChild(node);
		}
		schema.appendChild(nodes);

		node = doc.createElement("is-instead-of-row");
		node.setTextContent(Boolean.toString(insteadOfRow));
		schema.appendChild(node);

		node = doc.createElement("name");
		node.setTextContent(name);
		schema.appendChild(node);

		node = doc.createElement("referencing-names");
		node.setTextContent(referencingNames);
		schema.appendChild(node);

		node = doc.createElement("is-enabled");
		node.setTextContent(Boolean.toString(enabled));
		schema.appendChild(node);

		node = doc.createElement("table");
		node.setTextContent(tableName);
		schema.appendChild(node);

		node = doc.createElement("table-owner");
		node.setTextContent(tableOwner);
		schema.appendChild(node);

		node = doc.createElement("type");
		node.setTextContent(type);
		schema.appendChild(node);

		nodes = doc.createElement("used-tables");
		for (final String affectedTable : usedTables) {
			node = doc.createElement("table");
			node.setTextContent(affectedTable);
			nodes.appendChild(node);
		}
		schema.appendChild(nodes);

		node = doc.createElement("when-clause");
		node.setTextContent(whenClause);
		schema.appendChild(node);

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
	 * @return the events
	 */
	public List<String> getEvents() {
		return events;
	}

	/**
	 * @return the tableOwner
	 */
	public String getTableOwner() {
		return tableOwner;
	}

	/**
	 * @return the baseObjectType
	 */
	public String getBaseObjectType() {
		return baseObjectType;
	}

	/**
	 * @return the tableName
	 */
	public String getTableName() {
		return tableName;
	}

	public DatabaseTable getTable() {
		DatabaseTable result = null;
		if (connection != null) {
			result = connection.getTableByTitle(getTableName());
			if (result == null) {
				result = connection.getViewByTitle(getTableName());
			}
		}
		return result;
	}

	/**
	 * @return the columnName
	 */
	public String getColumnName() {
		return columnName;
	}

	/**
	 * @return the referencingNames
	 */
	public String getReferencingNames() {
		return referencingNames;
	}

	/**
	 * @return the whenClause
	 */
	public String getWhenClause() {
		return whenClause;
	}

	/**
	 * @return the enabled
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @return the actionType
	 */
	public String getActionType() {
		return actionType;
	}

	/**
	 * @return the body
	 */
	public String getBody() {
		return body;
	}

	/**
	 * @return the crossEdition
	 */
	public boolean isCrossEdition() {
		return crossEdition;
	}

	/**
	 * @return the beforeStatement
	 */
	public boolean isBeforeStatement() {
		return beforeStatement;
	}

	/**
	 * @return the beforeRow
	 */
	public boolean isBeforeRow() {
		return beforeRow;
	}

	/**
	 * @return the afterStatement
	 */
	public boolean isAfterStatement() {
		return afterStatement;
	}

	/**
	 * @return the afterRow
	 */
	public boolean isAfterRow() {
		return afterRow;
	}

	/**
	 * @return the insteadOfRow
	 */
	public boolean isInsteadOfRow() {
		return insteadOfRow;
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
