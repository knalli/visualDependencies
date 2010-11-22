package de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.tests;

import java.io.FileNotFoundException;
import java.io.FileReader;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.unibonn.inf.dbdependenciesui.metadata.impl.oracle11.Oracle11TriggerSqlParserImpl;
import de.unibonn.inf.dbdependenciesui.metadata.impl.oraclecommons.OracleTriggerSqlParserImpl;
import de.unibonn.inf.dbdependenciesui.misc.XmlHelperTools;

public class TriggerSqlParserTest {

	@Test
	public void testParser() throws XPathExpressionException, FileNotFoundException {

		final OracleTriggerSqlParserImpl parser = new Oracle11TriggerSqlParserImpl();

		final Document doc = XmlHelperTools.newDocument(new FileReader("examples/triggerExamples.xml"));
		final XPath xPath = XPathFactory.newInstance().newXPath();

		final NodeList nodes = (NodeList) xPath.evaluate("/triggers/body", doc, XPathConstants.NODESET);

		for (int i = 0, c = nodes.getLength(); i < c; i++) {
			final Node node = nodes.item(i);

			String title = "Definition " + (i + 1);
			try {
				title = title + ": " + node.getAttributes().getNamedItem("name").getNodeValue();
			} catch (final Exception e) {}
			parser.initializeTriggerSqlResults();
			parser.parseTriggerSql(node.getTextContent());
			System.out.println("Used: " + parser.getUsedTables());
			System.out.println("Affected: " + parser.getModifiedTables());
		}

	}
}
