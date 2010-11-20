package de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.tests;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.explodingpixels.macwidgets.IAppWidgetFactory;
import com.explodingpixels.macwidgets.UnifiedToolBar;

import de.unibonn.inf.dbdependenciesui.misc.XmlHelperTools;

public class TriggerSqlRegularExpressionTest {

	private static Pattern pattern;
	private static Pattern pattern2;
	private static Pattern pattern3;
	private static Pattern pattern4;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

		// ([\\w]*) match ANY characters (=word)
		// (?:.*) match but IGNORE ALL newlines
		// (?:[\\s]*) match but IGNORE ALL spaces
		pattern = Pattern.compile(getRegExp(), Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.COMMENTS);

		pattern2 = Pattern.compile(getCommentsRegExp());

		pattern3 = Pattern.compile(getConditionalsRegExp(), Pattern.CASE_INSENSITIVE | Pattern.DOTALL
				| Pattern.COMMENTS);

		pattern4 = Pattern.compile(getConditionalsReverseRegExp(), Pattern.CASE_INSENSITIVE | Pattern.DOTALL
				| Pattern.COMMENTS);
	}

	private static String getConditionalsRegExp() {
		return "(?:(E)(N)(D)(?:[\\s]*)(IF|WHILE|FOR|LOOP|DO))";
	}

	private static String getConditionalsReverseRegExp() {
		return "(?:_E_N_D__(IF|WHILE|FOR|LOOP|DO)_)";
	}

	private static String getCommentsRegExp() {
		final StringBuilder sb = new StringBuilder();

		sb.append("(([\\s])*--.*([\\n]+))");

		return sb.toString();
	}

	private static String getRegExp() {
		final StringBuilder sb = new StringBuilder();

		// CREATE [OR REPLACE]
		sb.append("(CREATE(?:[\\s]*)OR(?:[\\s]*)REPLACE|CREATE)(?:[\\s]*)");

		// TRIGGER name
		sb.append("TRIGGER(?:[\\s]*)([\\w]*)(?:[\\s]*)(?:[\\n]*)");

		// BEFORE/AFTER DELETE/INSERT/UPDATE
		sb
				.append("(?:[\\s]*)(BEFORE|AFTER)(?:[\\n]*)(?:[\\s]*)(?:(DELETE|INSERT|UPDATE)(?:(?:[\\s]*)OR(?:[\\s]*)(DELETE|INSERT|UPDATE)(?:(?:[\\s]*)OR(?:[\\s]*)(DELETE|INSERT|UPDATE))?)?)(?:[\\s]*)(?:[\\n]*)");

		// OF column?
		sb.append("(?:[\\s]*)(?:(OF)(?:[\\s]*)(?:[\\n]*)(?:[\\s]*)([\\w]*))?(?:[\\s]*)(?:[\\n]*)");

		// ON table
		sb.append("(?:[\\s]*)(ON)(?:[\\s]*)(?:[\\n]*)(?:[\\s]*)([\\w]*)(?:[\\s]*)(?:[\\n]*)");

		// referencing
		sb
				.append("(?:[\\s]*)(REFERENCING(?:[\\s]*)NEW(?:[\\s]*)AS(?:[\\s]*)([\\w]*)(?:[\\s]*)OLD(?:[\\s]*)AS(?:[\\s]*)([\\w]*))?(?:[\\s]*)(?:[\\n]*)");

		// [FOR EACH ROW]
		sb.append("(?:[\\s]*)(FOR(?:[\\s]*)EACH(?:[\\s]*)(ROW|STATEMENT))?(?:[\\s]*)(?:[\\n]*)");

		// FOLLOWS
		sb.append("(?:[\\s]*)(?:(FOLLOWS)(?:[\\s]*)(?:[\\n]*)(?:[\\s]*)([^\\n]*))?(?:[\\s]*)(?:[\\n]*)");

		// referencing
		sb
				.append("(?:[\\s]*)(REFERENCING(?:[\\s]*)NEW(?:[\\s]*)AS(?:[\\s]*)([\\w]*)(?:[\\s]*)OLD(?:[\\s]*)AS(?:[\\s]*)([\\w]*))?(?:[\\s]*)(?:[\\n]*)");

		// when
		sb.append("(?:[\\s]*)(?:(WHEN)(?:[\\s]*)([^\\n]*)(?:[\\n]*))?(?:[\\s]*)(?:[\\n]*)");

		// DECLARE?
		sb.append("(?:[\\s]*)(?:(DECLARE)(?:[\\s]*)(?:[\\n]*)(?:[\\s]*)(.*)(?=BEGIN))?(?:[\\s]*)(?:[\\n]*)");

		// // BEGIN: Inner block for optional exception part
		// (.*?) non greedy!
		final String innerBody = "(.*?)";
		final String outerBody = "(?:"
				+ innerBody
				+ "(?:[\\n]*)(?:[\\s]*)(?:(EXCEPTION)(?:[\\s]*)(?:[\\n]*)(?:[\\s]*)((WHEN)(?:[\\s]*)([^\\n]*))?(?:[\\n]*)(?:[\\s]*)(.*))?(?:[\\s]*)(?:[\\n]*))";

		sb.append("(?:[\\s]*)(?:(BEGIN)(?:[\\s]*)(?:[\\n]*)(?:[\\s]*)");
		sb.append(outerBody);
		sb.append("(?=END))?(?:[\\s]*)(?:[\\n]*)");

		// END
		sb.append("(?:[\\s]*)(END)(?:[\\s]*)(\\2)?");

		System.out.println(sb.toString());
		System.out.println(sb.toString().length());
		return sb.toString();
	}

	@Test
	public void testDefinitions() throws FileNotFoundException, XPathExpressionException {
		final Document doc = XmlHelperTools.newDocument(new FileReader("examples/triggerExamples.xml"));
		final XPath xPath = XPathFactory.newInstance().newXPath();

		final NodeList nodes = (NodeList) xPath.evaluate("/triggers/trigger", doc, XPathConstants.NODESET);

		for (int i = 0, c = nodes.getLength(); i < c; i++) {
			final Node node = nodes.item(i);

			String title = "Definition " + (i + 1);
			try {
				title = title + ": " + node.getAttributes().getNamedItem("name").getNodeValue();
			} catch (final Exception e) {}
			testDefinition(title, node.getTextContent());
		}
	}

	private void testDefinition(final String title, final String definition) {
		System.out.println("================================================");
		System.out.println(title);
		System.out.println(definition);
		System.out.println("------------------------------------------------");
		String definition2 = pattern2.matcher(definition).replaceAll("\n");
		definition2 = pattern3.matcher(definition2).replaceAll("_$1_$2_$3_$4_");
		System.out.println(definition2);
		System.out.println("------------------------------------------------");

		final Matcher matches = pattern.matcher(definition2);

		Assert.assertTrue(title, matches.find());
		final int c = matches.groupCount();

		for (int i = 0; i < c; i++) {
			String match = "";
			try {
				match = matches.group(i).trim();
			} catch (final Exception e) {}
			System.out.println(i + ": >>>" + match + "<<<");
		}
		System.out.println();
		System.out.println();
		System.out.println();
	}

	private static JFrame frame;
	private static JLabel label;

	/**
	 * @param args
	 * @throws Exception
	 * @throws IOException
	 * @throws TransformerException
	 */
	public static void main(final String[] args) throws Exception {
		setUpBeforeClass();

		frame = new JFrame("Trigger parser");

		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(getToolBar(), BorderLayout.PAGE_START);
		frame.getContentPane().add(getEditor(), BorderLayout.CENTER);

		frame.pack();
		frame.setLocationRelativeTo(null);

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	private static Component getToolBar() {
		final UnifiedToolBar toolBar = new UnifiedToolBar();
		toolBar.installWindowDraggerOnWindow(frame);

		final JButton btnCopy = new JButton("Use from clipboard");
		btnCopy.putClientProperty("JButton.buttonType", "textured");
		toolBar.addComponentToLeft(btnCopy);

		btnCopy.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				try {
					final String data = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(
							DataFlavor.stringFlavor);
					label.setText(parseText(data));
					System.out.println(data);
				} catch (final Exception e1) {
					e1.printStackTrace();
				}
			}
		});

		return toolBar.getComponent();
	}

	protected static Component getEditor() {
		label = new JLabel(getText());
		label.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		final JScrollPane scrollPane = new JScrollPane(label);
		scrollPane.setPreferredSize(new Dimension(920, 600));
		IAppWidgetFactory.makeIAppScrollPane(scrollPane);

		return scrollPane;
	}

	private static String parseText(String trigger) {
		trigger = pattern2.matcher(trigger).replaceAll("\n");
		trigger = pattern3.matcher(trigger).replaceAll("_$1_$2_$3__$4_");

		final Matcher matches = pattern.matcher(trigger);

		matches.find();
		final int c = matches.groupCount();
		System.out.println(c);

		for (int i = 0; i < c; i++) {
			String match = "";
			try {
				match = matches.group(i).trim();
				System.out.println(i + ". " + match);
				switch (i) {
				case 1:
				case 2:
				case 3:
				case 4:
				case 5:
				case 6:
				case 7:
				case 8:
				case 11:
				case 12:
				case 13:
				case 15:
				case 17:
				case 19:
				case 24:
					trigger = trigger.replace(match, "<b>" + match + "</b>");
					break;
				}

				switch (i) {
				case 1:
				case 15:
				case 17:
				case 19:
				case 21:
				case 24:
					trigger = trigger.replace(match, "<span style=\"color:red;\">" + match + "</span>");
				}

				switch (i) {
				case 2:
				case 7:
				case 11:
				case 12:
				case 14:
				case 16:
				case 22:
					trigger = trigger.replace(match, "<span style=\"color:blue;\">" + match + "</span>");
				}
			} catch (final Exception e) {}
		}

		trigger = pattern4.matcher(trigger).replaceAll("END $1");

		return "<html><pre>" + trigger;
	}

	private static String getText() {
		NodeList nodes;
		try {
			final Document doc = XmlHelperTools.newDocument(new FileReader("examples/triggerExamples.xml"));
			final XPath xPath = XPathFactory.newInstance().newXPath();

			nodes = (NodeList) xPath.evaluate("/triggers/trigger/text()", doc, XPathConstants.NODESET);

			if (nodes.getLength() < 8) { return "NOTHING HERE 1"; }
		} catch (final Exception e) {
			return "NOTHING HERE 2";
		}
		final Node node = nodes.item(7);
		final String trigger = node.getNodeValue();

		return parseText(trigger);
	}
}
