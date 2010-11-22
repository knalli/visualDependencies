/**
 * $Id: Main.java 41 2009-03-01 16:21:20Z philipp $
 */
package de.unibonn.inf.dbdependenciesui.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.IOException;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import com.explodingpixels.macwidgets.IAppWidgetFactory;

import de.unibonn.inf.dbdependenciesui.misc.Internationalization;
import de.unibonn.inf.dbdependenciesui.ui.helpers.SystemTools;

/**
 * The overall standard log view window frame. This log table shows all log records produces by the application. It
 * lists all these records which are defined (see LogLevel). Each row gets a doubleclick event providing a detail
 * information dialog.
 * 
 * @author Andre Kasper
 * @author Jan Philipp
 */
public class HelpView extends JFrame {
	private static final long serialVersionUID = 3931005500785784753L;
	private static HelpView instance;

	public static HelpView getInstance() {
		if (instance == null) {
			instance = new HelpView();
		}
		return instance;
	}

	public HelpView() {
		initialize();
	}

	private void initialize() {
		setTitle(Internationalization.getText("application.help"));

		// Create custom content pane
		final JPanel c = new JPanel();
		setContentPane(c);
		c.setLayout(new BorderLayout());

		try {
			final ClassLoader classLoader = Internationalization.class.getClassLoader();
			final URL url = classLoader.getResource("resources/help/help.html");
			final JEditorPane editorPane = getEditorPane(url);
			final JScrollPane scrollPane = new JScrollPane(editorPane);
			editorPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

			if (SystemTools.isMac()) {
				IAppWidgetFactory.makeIAppScrollPane(scrollPane);
			}

			c.add(scrollPane, BorderLayout.CENTER);
		} catch (final IOException e) {
			c.add(new JLabel("No help file."), BorderLayout.CENTER);
		}

		pack();
		setLocationRelativeTo(null);
		setResizable(false);

		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
	}

	/**
	 * @param url
	 * @return
	 * @throws IOException
	 */
	private JEditorPane getEditorPane(final URL url) throws IOException {
		final JEditorPane editorPane = new JEditorPane(url);
		editorPane.setEditable(false);
		editorPane.setPreferredSize(new Dimension(600, 300));

		editorPane.addHyperlinkListener(new HyperlinkListener() {
			public void hyperlinkUpdate(final HyperlinkEvent e) {
				if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
					try {
						java.awt.Desktop.getDesktop().browse(new java.net.URI(e.getURL().toString()));
					} catch (final Exception ex) {}
				}
			}
		});

		return editorPane;
	}
}