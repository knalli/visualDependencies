package de.unibonn.inf.dbdependenciesui.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import de.unibonn.inf.dbdependenciesui.Configuration;
import de.unibonn.inf.dbdependenciesui.misc.Internationalization;
import de.unibonn.inf.dbdependenciesui.ui.misc.FadeEffectHelper;

public class AboutView extends JFrame {
	private static final long serialVersionUID = 1797580154356602962L;

	private static AboutView instance;

	public static AboutView getInstance() {
		if (AboutView.instance == null) {
			AboutView.instance = new AboutView();
		}
		return AboutView.instance;
	}

	public AboutView() {
		initialize();
	}

	private void initialize() {
		setTitle(Internationalization.getText("application.about"));

		// Create custom content pane
		final JPanel c = new JPanel();
		setContentPane(c);
		c.setLayout(new BorderLayout());
		c.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		// Add logo
		final Icon icon = Internationalization.getIcon("application.splashscreen");
		final JLabel imageLabel = new JLabel(icon);
		imageLabel.setOpaque(true);
		imageLabel.setBackground(Color.white);
		imageLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
		attachEastereggListener(imageLabel);
		c.add(imageLabel, BorderLayout.NORTH);

		final String title = Internationalization.getText("application.title");
		String datetime = "";
		try {
			datetime = new SimpleDateFormat("dd.MM.yyyy").format(Configuration.build);
		} catch (final Exception e) {}
		String version = null;
		if (Configuration.REVISION > 0) {
			version = Internationalization.getTextFormatted("application.about.version", Configuration.VERSION,
					Configuration.REVISION, datetime);
		} else {
			version = "Developer Version";
		}
		version = version + "<br /> " + Internationalization.getText("application.about.license");
		final String message = Internationalization.getText("application.about.text");
		final String message2 = Internationalization.getText("application.about.text2");

		JLabel label;
		String text;

		// Add title label
		text = String.format("<html><h1>%s</h1><h2>%s</h2>", title, version);
		label = new JLabel(text);
		c.add(label, BorderLayout.CENTER);

		// Add text label
		text = String.format("<html>%s<br />%s", message, message2);
		label = new JLabel(text);
		c.add(label, BorderLayout.SOUTH);

		pack();
		setLocationRelativeTo(null);
		setResizable(false);

		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
	}

	/**
	 * Attach an easter egg action: Close the frame and open another one. An easter egg is visible with a double click,
	 * it is closed after an one click.
	 * 
	 * @param component
	 */
	private void attachEastereggListener(final JLabel component) {
		component.addMouseListener(new MyMouseAdapter(this));
	}

	private class MyMouseAdapter extends MouseAdapter {
		private final Frame frame;

		public MyMouseAdapter(final Frame frame) {
			this.frame = frame;
		}

		@Override
		public void mouseClicked(final MouseEvent e) {
			// No action unless double click
			if (e.getClickCount() != 2) { return; }

			frame.dispose();

			// Build components.
			final Icon icon = Internationalization.getIcon("application.splashscreen2");
			final JFrame window = new JFrame();
			final JLabel label = new JLabel(icon);

			// Attach self-closing event handler.
			window.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(final MouseEvent e) {
					window.dispose();
				}
			});

			// Bind components and show them.
			window.setUndecorated(true);
			window.add(label);
			window.pack();
			window.setLocationRelativeTo(null);
			FadeEffectHelper.fadeIn(window);
		}
	}
}
