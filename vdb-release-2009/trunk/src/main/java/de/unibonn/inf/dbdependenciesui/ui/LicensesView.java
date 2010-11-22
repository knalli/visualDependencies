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

public class LicensesView extends JFrame {
	private static final long serialVersionUID = 1797580154356602962L;

	private static LicensesView instance;

	public static LicensesView getInstance() {
		if (LicensesView.instance == null) {
			LicensesView.instance = new LicensesView();
		}
		return LicensesView.instance;
	}

	public LicensesView() {
		initialize();
	}

	private void initialize() {
		setTitle(Internationalization.getText("application.licenses"));

		// Create custom content pane
		final JPanel c = new JPanel();
		setContentPane(c);
		c.setLayout(new BorderLayout());

		try {
			final ClassLoader classLoader = Internationalization.class.getClassLoader();
			final URL url = classLoader.getResource("resources/licenses/licenses.html");
			final JEditorPane editorPane = getEditorPane(url);
			final JScrollPane scrollPane = new JScrollPane(editorPane);
			editorPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

			if (SystemTools.isMac()) {
				IAppWidgetFactory.makeIAppScrollPane(scrollPane);
			}

			c.add(scrollPane, BorderLayout.CENTER);
		} catch (final IOException e) {
			c.add(new JLabel("No license file."), BorderLayout.CENTER);
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
