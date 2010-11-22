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

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.org.apache.xerces.internal.dom.DocumentImpl;

import de.unibonn.inf.dbdependenciesui.Configuration;
import de.unibonn.inf.dbdependenciesui.controller.Controller;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseConnection;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseTrigger;
import de.unibonn.inf.dbdependenciesui.misc.XmlHelperTools;

/**
 * This is the object representation of a ddl schema. A ddl schema contains the schema structure of a table or view. The
 * object will be deserialized by an xml string and will be serialized to a string. It contains: A list of columns, keys
 * (primary, unique, foreign) and relations. If you want an editable object, see {@link DdlSchemaEditable}.
 * 
 * @uses {@link Column}
 * @uses {@link ForeignKey}
 * @uses {@link PrimaryKey}
 * @uses {@link Relation}
 * @uses {@link UniqueKey}
 * @author Andre Kasper
 * @author Jan Philipp
 */
public class DdlSchema implements Serializable {
	private static final long serialVersionUID = 4518248797429710312L;

	protected transient static Logger log = Logger.getLogger(Configuration.LOGGER);

	/**
	 * list of columns
	 */
	protected final List<Column> columns = new ArrayList<Column>();

	/**
	 * list of primary keys
	 */
	protected final List<PrimaryKey> primaryKeys = new ArrayList<PrimaryKey>();

	/**
	 * list of foreign keys
	 */
	protected final List<ForeignKey> foreignKeys = new ArrayList<ForeignKey>();

	/**
	 * list of unique keys
	 */
	protected final List<UniqueKey> uniqueKeys = new ArrayList<UniqueKey>();

	/**
	 * list of indices
	 */
	protected final List<Index> indices = new ArrayList<Index>();

	/**
	 * list of relations
	 */
	protected final List<Relation> relations = new ArrayList<Relation>();

	/**
	 * list of triggers
	 */
	protected final List<String> triggers = new ArrayList<String>();

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
	 * Creates a new ddl schema object with empty values.
	 * 
	 * @param xml
	 */
	public DdlSchema() {

		// Reset all variables, create and configure xpath
		initialize();

	}

	/**
	 * Creates a new ddl schema object by the given xml string.
	 * 
	 * @see #deserialize(String)
	 * @param xml
	 */
	public DdlSchema(final String xml) {
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
			readColumns((NodeList) xPath.evaluate("/schema/columns/column", doc, XPathConstants.NODESET));
			readPrimaryKeys((NodeList) xPath.evaluate("/schema/keys/primary", doc, XPathConstants.NODESET));
			readForeignKeys((NodeList) xPath.evaluate("/schema/keys/foreign", doc, XPathConstants.NODESET));
			readUniqueKeys((NodeList) xPath.evaluate("/schema/keys/unique", doc, XPathConstants.NODESET));
			readIndices((NodeList) xPath.evaluate("/schema/keys/indices", doc, XPathConstants.NODESET));
			readRelations((NodeList) xPath.evaluate("/schema/relations/*", doc, XPathConstants.NODESET));
			readTriggers((NodeList) xPath.evaluate("/schema/triggers/*", doc, XPathConstants.NODESET));
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
	final private void readRelations(final NodeList nodes) throws XPathExpressionException {
		for (int i = 0; i < nodes.getLength(); ++i) {
			final Node node = nodes.item(i);
			final String type = node.getNodeName();
			final String name = (String) xPath.evaluate("name/text()", node, XPathConstants.STRING);
			final String source = (String) xPath.evaluate("source/text()", node, XPathConstants.STRING);
			final String target = (String) xPath.evaluate("target/text()", node, XPathConstants.STRING);
			final String column = (String) xPath.evaluate("column/text()", node, XPathConstants.STRING);
			final String condition = (String) xPath.evaluate("condition/text()", node, XPathConstants.STRING);
			final String isPositive = (String) xPath.evaluate("is-positive/text()", node, XPathConstants.STRING);
			final String isAndCondition = (String) xPath.evaluate("is-and/text()", node, XPathConstants.STRING);

			final boolean isFromSource = name.equals(source);
			final Relation relation = new Relation(source, target, isFromSource);
			relation.setColumn(column);
			relation.setView(type.equals("view"));
			relation.setCondition(condition);
			relation.setPositive(Boolean.parseBoolean(isPositive));
			relation.setAndCondition(Boolean.parseBoolean(isAndCondition));
			relation.setConnection(connection);

			relations.add(relation);
		}
	}

	/**
	 * Retrive the unique keys from the given list of nodes.
	 * 
	 * @param nodes
	 * @throws XPathExpressionException
	 */
	private void readUniqueKeys(final NodeList nodes) throws XPathExpressionException {
		for (int i = 0; i < nodes.getLength(); ++i) {
			final Node node = nodes.item(i);
			final String name = (String) xPath.evaluate("name/text()", node, XPathConstants.STRING);
			final NodeList keys = (NodeList) xPath.evaluate("column", node, XPathConstants.NODESET);

			final UniqueKey key = new UniqueKey(name);
			for (int j = 0; j < keys.getLength(); ++j) {
				final String column = (String) xPath.evaluate("text()", keys.item(j), XPathConstants.STRING);
				key.addColumn(column);
			}

			uniqueKeys.add(key);
		}
	}

	/**
	 * Retrive the unique keys from the given list of nodes.
	 * 
	 * @param nodes
	 * @throws XPathExpressionException
	 */
	private void readIndices(final NodeList nodes) throws XPathExpressionException {
		for (int i = 0; i < nodes.getLength(); ++i) {
			final Node node = nodes.item(i);
			final String name = (String) xPath.evaluate("name/text()", node, XPathConstants.STRING);
			final NodeList keys = (NodeList) xPath.evaluate("column", node, XPathConstants.NODESET);

			final Index key = new Index(name);
			for (int j = 0; j < keys.getLength(); ++j) {
				final String column = (String) xPath.evaluate("text()", keys.item(j), XPathConstants.STRING);
				key.addColumn(column);
			}

			indices.add(key);
		}
	}

	/**
	 * Retrive the foreign keys from the given list of nodes.
	 * 
	 * @param nodes
	 * @throws XPathExpressionException
	 */
	private void readForeignKeys(final NodeList nodes) throws XPathExpressionException {
		for (int i = 0; i < nodes.getLength(); ++i) {
			final Node node = nodes.item(i);
			final String name = (String) xPath.evaluate("name/text()", node, XPathConstants.STRING);
			final String column = (String) xPath.evaluate("column/name/text()", node, XPathConstants.STRING);
			final String referToTable = (String) xPath.evaluate("column/refer-to/table/text()", node,
					XPathConstants.STRING);
			final String referToColumn = (String) xPath.evaluate("column/refer-to/column/text()", node,
					XPathConstants.STRING);

			final ForeignKey key = new ForeignKey(name);
			key.setColumn(column);
			key.setReferToTable(referToTable);
			key.setReferToColumn(referToColumn);

			foreignKeys.add(key);
		}
	}

	/**
	 * Retrive the primary keys from the given list of nodes.
	 * 
	 * @param nodes
	 * @throws XPathExpressionException
	 */
	private void readPrimaryKeys(final NodeList nodes) throws XPathExpressionException {
		for (int i = 0; i < nodes.getLength(); ++i) {
			final Node node = nodes.item(i);
			final String name = (String) xPath.evaluate("name/text()", node, XPathConstants.STRING);
			final NodeList keys = (NodeList) xPath.evaluate("column", node, XPathConstants.NODESET);

			final PrimaryKey key = new PrimaryKey(name);
			for (int j = 0; j < keys.getLength(); ++j) {
				final String column = (String) xPath.evaluate("text()", keys.item(j), XPathConstants.STRING);
				key.addColumn(column);
			}

			primaryKeys.add(key);
		}
	}

	/**
	 * Retrive the columns from the given list of nodes.
	 * 
	 * @param nodes
	 * @throws XPathExpressionException
	 */
	private void readColumns(final NodeList nodes) throws XPathExpressionException {
		for (int i = 0; i < nodes.getLength(); ++i) {
			final Node node = nodes.item(i);
			final String name = (String) xPath.evaluate("name/text()", node, XPathConstants.STRING);
			final String type = (String) xPath.evaluate("type/text()", node, XPathConstants.STRING);
			final Integer size = ((Double) xPath.evaluate("length/size/text()", node, XPathConstants.NUMBER))
					.intValue();
			final Integer fractionalDigits = ((Double) xPath.evaluate("length/fractionaldigits/text()", node,
					XPathConstants.NUMBER)).intValue();
			final String isNullableString = (String) xPath.evaluate("is-nullable/text()", node, XPathConstants.STRING);
			final Boolean isNullable = Boolean.parseBoolean(isNullableString);

			final Column column = new Column(name);
			column.setType(type);
			column.setSize(size);
			column.setFractionalDigits(fractionalDigits);
			column.setNullable(isNullable);

			columns.add(column);
		}
	}

	/**
	 * Retrive the triggers from the given list of nodes.
	 * 
	 * @param nodes
	 * @throws XPathExpressionException
	 */
	private void readTriggers(final NodeList nodes) throws XPathExpressionException {
		for (int i = 0; i < nodes.getLength(); ++i) {
			final Node node = nodes.item(i);
			final String trigger = (String) xPath.evaluate("name/text()", node, XPathConstants.STRING);

			triggers.add(trigger);
		}
	}

	/**
	 * Reset all local states.
	 */
	private void initialize() {
		columns.clear();
		primaryKeys.clear();
		foreignKeys.clear();
		uniqueKeys.clear();
		indices.clear();
		relations.clear();
		triggers.clear();

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

		root.appendChild(buildColumnNodes(doc));
		root.appendChild(buildKeyNodes(doc));
		root.appendChild(buildRelationNodes(doc));
		root.appendChild(buildTriggerNodes(doc));

		return XmlHelperTools.toXML(doc);
	}

	/**
	 * Build up a node named "relations" with subnodes of all relational tables and views.
	 * 
	 * @param doc
	 * @return
	 */
	private Node buildRelationNodes(final Document doc) {
		final Element nodes = doc.createElement("relations");

		for (final Relation relation : relations) {
			final String nodename = relation.isView() ? "view" : "table";
			final Element node = doc.createElement(nodename);

			Node subnode;

			// /relations/view|table/name/text()
			subnode = doc.createElement("name");
			subnode.setTextContent(relation.getName());
			node.appendChild(subnode);

			// /relations/view|table/source/text()
			subnode = doc.createElement("source");
			subnode.setTextContent(relation.getSourceName());
			node.appendChild(subnode);

			// /relations/view|table/target/text()
			subnode = doc.createElement("target");
			subnode.setTextContent(relation.getTargetName());
			node.appendChild(subnode);

			// /relations/view|table/column/text()
			subnode = doc.createElement("column");
			subnode.setTextContent(relation.getColumn());
			node.appendChild(subnode);

			// /relations/view|table/condition/text()
			subnode = doc.createElement("condition");
			subnode.setTextContent(relation.getCondition());
			node.appendChild(subnode);

			// /relations/view|table/is-positive/text()
			subnode = doc.createElement("is-positive");
			subnode.setTextContent(Boolean.valueOf(relation.isPositive()).toString());
			node.appendChild(subnode);

			// /relations/view|table/is-and/text()
			subnode = doc.createElement("is-and");
			subnode.setTextContent(Boolean.valueOf(relation.isAndCondition()).toString());
			node.appendChild(subnode);

			nodes.appendChild(node);
		}

		return nodes;
	}

	/**
	 * Build up a node "keys" with subnodes of all kind of keys.
	 * 
	 * @param doc
	 * @return
	 */
	private Node buildKeyNodes(final Document doc) {
		final Element nodes = doc.createElement("keys");

		// Build all primary keys.
		for (final PrimaryKey key : primaryKeys) {
			final Element node = doc.createElement("primary");

			Node subnode;

			// /keys/primary/name/text()
			subnode = doc.createElement("name");
			subnode.setTextContent(key.getName());
			node.appendChild(subnode);

			for (final String column : key.getColumns()) {
				// /keys/primary/column/text()
				subnode = doc.createElement("column");
				subnode.setTextContent(column);
				node.appendChild(subnode);
			}

			nodes.appendChild(node);
		}

		// Build all foreign keys.
		for (final ForeignKey key : foreignKeys) {
			final Element node = doc.createElement("foreign");

			Node subnode, subnode2, subnode3;

			// /keys/foreign/name/text()
			subnode = doc.createElement("name");
			subnode.setTextContent(key.getName());
			node.appendChild(subnode);

			// /keys/foreign/column
			subnode = doc.createElement("column");
			node.appendChild(subnode);

			// /keys/foreign/column/name/text()
			subnode2 = doc.createElement("name");
			subnode2.setTextContent(key.getColumn());
			subnode.appendChild(subnode2);

			// /keys/foreign/column/refer-to
			subnode2 = doc.createElement("refer-to");
			subnode.appendChild(subnode2);

			// /keys/foreign/column/refer-to/table/text()
			subnode3 = doc.createElement("table");
			subnode3.setTextContent(key.getReferToTable());
			subnode2.appendChild(subnode3);

			// /keys/foreign/column/refer-to/column/text()
			subnode3 = doc.createElement("column");
			subnode3.setTextContent(key.getReferToColumn());
			subnode2.appendChild(subnode3);

			nodes.appendChild(node);
		}

		// Build all unique keys.
		for (final UniqueKey key : uniqueKeys) {
			final Element node = doc.createElement("unique");

			Node subnode;

			// /keys/unique/name/text()
			subnode = doc.createElement("name");
			subnode.setTextContent(key.getName());
			node.appendChild(subnode);

			for (final String column : key.getColumns()) {
				// /keys/unique/column/text()
				subnode = doc.createElement("column");
				subnode.setTextContent(column);
				node.appendChild(subnode);
			}

			nodes.appendChild(node);
		}

		// Build all indices
		for (final Index key : indices) {
			final Element node = doc.createElement("indices");

			Node subnode;

			// /keys/unique/name/text()
			subnode = doc.createElement("name");
			subnode.setTextContent(key.getName());
			node.appendChild(subnode);

			for (final String column : key.getColumns()) {
				// /keys/unique/column/text()
				subnode = doc.createElement("column");
				subnode.setTextContent(column);
				node.appendChild(subnode);
			}

			nodes.appendChild(node);
		}

		return nodes;
	}

	/**
	 * Build up a node "columns" with subnodes of all columns.
	 * 
	 * @param doc
	 * @return
	 */
	private Node buildColumnNodes(final Document doc) {
		final Element nodes = doc.createElement("columns");

		for (final Column column : columns) {
			final Element node = doc.createElement("column");

			Node subnode;

			subnode = doc.createElement("name");
			subnode.setTextContent(column.getName());
			node.appendChild(subnode);

			subnode = doc.createElement("type");
			subnode.setTextContent(column.getType());
			node.appendChild(subnode);

			subnode = doc.createElement("length");
			node.appendChild(subnode);

			Node subnode2;
			subnode2 = doc.createElement("size");
			subnode2.setTextContent(Integer.valueOf(column.getSize()).toString());
			subnode.appendChild(subnode2);

			subnode2 = doc.createElement("fractionaldigits");
			subnode2.setTextContent(Integer.valueOf(column.getFractionalDigits()).toString());
			subnode.appendChild(subnode2);

			subnode = doc.createElement("is-nullable");
			subnode.setTextContent(Boolean.valueOf(column.isNullable()).toString());
			node.appendChild(subnode);

			nodes.appendChild(node);
		}

		return nodes;
	}

	/**
	 * Build up a node "triggers" with subnodes of all triggers.
	 * 
	 * @param doc
	 * @return
	 */
	private Node buildTriggerNodes(final Document doc) {
		final Element nodes = doc.createElement("triggers");

		for (final String trigger : triggers) {
			final Element node = doc.createElement("trigger");
			final Element subnode = doc.createElement("name");
			subnode.setTextContent(trigger);
			node.appendChild(subnode);
			nodes.appendChild(node);
		}

		return nodes;
	}

	/**
	 * @return the columns
	 */
	public List<Column> getColumns() {
		return Collections.unmodifiableList(columns);
	}

	/**
	 * Return the triggers' titles of this table. A trigger in this list a trigger that is linked to the table by the ON
	 * statement.
	 * 
	 * @return the triggers
	 */
	public List<String> getTriggerTitles() {
		return Collections.unmodifiableList(triggers);
	}

	/**
	 * Return the triggers of this table. A trigger in this list a trigger that is linked to the table by the ON
	 * statement.
	 * 
	 * @return the triggers
	 */
	public List<DatabaseTrigger> getTriggers() {
		return Controller.getTriggerByTitles(triggers);
	}

	/**
	 * @return the primaryKeys
	 */
	public List<PrimaryKey> getPrimaryKeys() {
		return Collections.unmodifiableList(primaryKeys);
	}

	/**
	 * @return the foreignKeys
	 */
	public List<ForeignKey> getForeignKeys() {
		return Collections.unmodifiableList(foreignKeys);
	}

	/**
	 * @return the uniqueKeys
	 */
	public List<UniqueKey> getUniqueKeys() {
		return Collections.unmodifiableList(uniqueKeys);
	}

	/**
	 * @return the indices
	 */
	public List<Index> getIndices() {
		return Collections.unmodifiableList(indices);
	}

	/**
	 * @return the relations
	 */
	public List<Relation> getRelations() {
		return Collections.unmodifiableList(relations);
	}

	/**
	 * Return all relations where this object is the source.
	 * 
	 * @return the relations
	 */
	public List<Relation> getSourceRelations() {
		final List<Relation> source = new ArrayList<Relation>();
		for (final Relation relation : relations) {
			if (relation.getName().equals(relation.getSourceName())) {
				source.add(relation);
			}
		}
		return Collections.unmodifiableList(source);
	}

	/**
	 * Return all relations where this object is the target.
	 * 
	 * @return the relations
	 */
	public List<Relation> getTargetRelations() {
		final List<Relation> target = new ArrayList<Relation>();
		for (final Relation relation : relations) {
			if (!relation.getName().equals(relation.getSourceName())) {
				target.add(relation);
			}
		}
		return Collections.unmodifiableList(target);
	}

	/**
	 * Set the current connection.
	 * 
	 * @param connection
	 */
	public void setConnection(final DatabaseConnection connection) {
		this.connection = connection;

		for (final Relation relation : relations) {
			relation.setConnection(connection);
		}
	}
}
