package de.unibonn.inf.dbdependenciesui.ui.misc;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

import de.unibonn.inf.dbdependenciesui.Configuration;
import de.unibonn.inf.dbdependenciesui.ui.controller.ViewController;
import de.unibonn.inf.dbdependenciesui.ui.helpers.SystemTools;

public class ProgressScreenFrame extends JFrame {

	private static final Logger log = Logger.getLogger(Configuration.LOGGER);

	private static final long serialVersionUID = 7402200610497803763L;

	private JProgressBar progressBar = null;

	private JLabel labelStatusText = null;

	private final List<String> messages;

	private int currentMessage = 0;

	private String finalStatus;

	public ProgressScreenFrame() {
		this(null);
	}

	public ProgressScreenFrame(final List<String> messages) {
		this.messages = messages;
		initialize();
	}

	private void initialize() {
		// Reduce the shadow and make the window "smaller". Less present.
		if (SystemTools.isMac()) {
			getRootPane().putClientProperty("Window.style", "small");
		}

		if ((messages != null) && (messages.size() > 0)) {
			final Container content = getContentPane();
			content.setLayout(new BorderLayout());
			content.add(getProgressBar(), BorderLayout.NORTH);
			content.add(getStatusLabel(), BorderLayout.SOUTH);

			setSettings();
		}
	}

	private void setSettings() {
		setAlwaysOnTop(true);
		setResizable(false);
		pack();

		final JFrame mainFrame = ViewController.getApplicationView();
		setLocationRelativeTo(mainFrame);

		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
	}

	private JProgressBar getProgressBar() {
		if (progressBar == null) {
			progressBar = new JProgressBar();
			progressBar.setMaximum(messages.size());
		}
		return progressBar;
	}

	private JLabel getStatusLabel() {
		if (labelStatusText == null) {
			final Dimension size = new Dimension(450, 20);
			labelStatusText = new JLabel("", SwingConstants.CENTER);
			labelStatusText.setBackground(Color.WHITE);
			labelStatusText.setPreferredSize(size);
			labelStatusText.setMaximumSize(size);
			labelStatusText.setHorizontalAlignment(SwingConstants.CENTER);
		}
		return labelStatusText;
	}

	public void setFinalStatusText(final String finalStatus) {
		this.finalStatus = finalStatus;
	}

	public void showFinalStatus() {
		if (finalStatus != null) {
			labelStatusText.setText(finalStatus);
			progressBar.setValue(progressBar.getMaximum());
		}
	}

	private void setMessage(final String text) {
		labelStatusText.setText(text);
	}

	public void showNextStatus() {
		if ((messages != null) && (messages.size() > 0)) {
			if (currentMessage < messages.size()) {
				setMessage(messages.get(currentMessage));
				ProgressScreenFrame.log.info(messages.get(currentMessage));
				progressBar.setValue(currentMessage);
				currentMessage++;
			}
		}
	}
}
