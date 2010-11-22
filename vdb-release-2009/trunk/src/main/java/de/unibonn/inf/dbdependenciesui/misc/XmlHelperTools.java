package de.unibonn.inf.dbdependenciesui.misc;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

/**
 * Several xml helper tools.
 * 
 * @author Jan Philipp <knallisworld@googlemail.com>
 * @version $Id: XmlHelperTools.java 203 2008-12-09 19:27:59Z philipp $
 */
public class XmlHelperTools {

	/**
	 * Generates a dom document of the given xml string.
	 * 
	 * If the given string is not a valid xml it will throw an exception.
	 * 
	 * @param string
	 * @return
	 * @throws RuntimeException
	 * @see {@link #newDocument(Reader)}
	 */
	public static Document newDocument(final String string) {
		return XmlHelperTools.newDocument(new StringReader(string));
	}

	/**
	 * Generates a dom document of the given reader.
	 * 
	 * If the given reader does not represent a valid xml it will throw an
	 * exception.
	 * 
	 * @param reader
	 * @return
	 * @throws RuntimeException
	 * @see {@link DocumentBuilderFactory}
	 */
	public static Document newDocument(final Reader reader) {
		final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setValidating(false);

		try {
			return dbf.newDocumentBuilder().parse(new InputSource(reader));
		} catch (final IOException e) {
			throw new RuntimeException(e);
		} catch (final SAXException e) {
			throw new RuntimeException(e);
		} catch (final ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Converts a dom document to an xml String.
	 * 
	 * @param document
	 * @return
	 * @see XMLSerializer
	 */
	public static String toXML(final Document document) {
		final XMLSerializer xmlSerializer = new XMLSerializer();

		final OutputFormat outputFormat = new OutputFormat(document);
		outputFormat.setOmitXMLDeclaration(false);
		outputFormat.setEncoding("UTF-8");
		outputFormat.setIndenting(true);

		final StringWriter stringWriter = new StringWriter();
		xmlSerializer.setOutputCharStream(stringWriter);
		xmlSerializer.setOutputFormat(outputFormat);
		try {
			xmlSerializer.serialize(document);
			stringWriter.flush();
			stringWriter.close();
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}

		return stringWriter.toString();
	}

	/**
	 * Sorts the children the given document. The given node's subnodes will
	 * sorted in ascending order (depth 20).
	 * 
	 * @link http://programmatica.blogspot.com/2006/12/sorting-xml-in-java.html
	 * 
	 * @param doc
	 *            doc whose children will be sorted
	 */
	public static void sortChildNodes(final Document doc) {
		XmlHelperTools.sortChildNodes(doc.getFirstChild());
	}

	/**
	 * Sorts the children the given document. The given node's subnodes will
	 * sorted in ascending order (depth 20).
	 * 
	 * @link http://programmatica.blogspot.com/2006/12/sorting-xml-in-java.html
	 * 
	 * @param doc
	 *            doc whose children will be sorted
	 * @param comparator
	 *            comparator used to sort, if null a default NodeName comparator
	 *            is used.
	 */
	public static void sortChildNodes(final Document doc,
			final Comparator<Node> comparator) {
		XmlHelperTools.sortChildNodes(doc.getFirstChild(), false, 20,
				comparator);
	}

	/**
	 * Sorts the children of the given node up to the specified depth if
	 * available. The given node's subnodes will sorted in ascending order
	 * (depth 20).
	 * 
	 * @link http://programmatica.blogspot.com/2006/12/sorting-xml-in-java.html
	 * 
	 * @param node
	 *            node whose children will be sorted
	 */
	public static void sortChildNodes(final Node node) {
		XmlHelperTools.sortChildNodes(node, false, 20);
	}

	/**
	 * Sorts the children of the given node up to the specified depth if
	 * available.
	 * 
	 * @link http://programmatica.blogspot.com/2006/12/sorting-xml-in-java.html
	 * 
	 * @param node
	 *            node whose children will be sorted
	 * @param descending
	 *            true for sorting in descending order
	 * @param depth
	 *            depth up to which to sort in DOM
	 */
	public static void sortChildNodes(final Node node,
			final boolean descending, final int depth) {
		XmlHelperTools.sortChildNodes(node, descending, depth, null);
	}

	/**
	 * Sorts the children of the given node up to the specified depth if
	 * available.
	 * 
	 * @link http://programmatica.blogspot.com/2006/12/sorting-xml-in-java.html
	 * 
	 * @param node
	 *            node whose children will be sorted
	 * @param descending
	 *            true for sorting in descending order
	 * @param depth
	 *            depth up to which to sort in DOM
	 * @param comparator
	 *            comparator used to sort, if null a default NodeName comparator
	 *            is used.
	 */
	public static void sortChildNodes(final Node node,
			final boolean descending, final int depth,
			final Comparator<Node> comparator) {

		final List<Node> nodes = new ArrayList<Node>();
		final NodeList childNodeList = node.getChildNodes();

		if ((depth > 0) && (childNodeList.getLength() > 0)) {
			for (int i = 0; i < childNodeList.getLength(); i++) {
				final Node tNode = childNodeList.item(i);
				XmlHelperTools.sortChildNodes(tNode, descending, depth - 1,
						comparator);

				// Remove empty text nodes
				if ((!(tNode instanceof Text))
						|| ((tNode instanceof Text) && (((Text) tNode)
								.getTextContent().trim().length() > 1))) {
					nodes.add(tNode);
				}
			}

			Comparator<Node> comp = comparator;
			if (comp == null) {
				comp = new Comparator<Node>() {

					public final int compare(final Node arg0, final Node arg1) {
						return arg0.getNodeName().compareTo(arg1.getNodeName());
					}

				};
			}

			if (descending) {
				// if descending is true, get the reverse ordered comparator
				Collections.sort(nodes, Collections.reverseOrder(comp));
			} else {
				Collections.sort(nodes, comp);
			}

			for (final Node element : nodes) {
				node.appendChild(element);
			}
		}
	}
}
