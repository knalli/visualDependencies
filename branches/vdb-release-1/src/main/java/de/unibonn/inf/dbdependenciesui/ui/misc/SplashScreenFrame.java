/**
 * $Id: Main.java 41 2009-03-01 16:21:20Z philipp $
 */
package de.unibonn.inf.dbdependenciesui.ui.misc;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

import de.unibonn.inf.dbdependenciesui.Configuration;
import de.unibonn.inf.dbdependenciesui.misc.Internationalization;
import de.unibonn.inf.dbdependenciesui.ui.helpers.SystemTools;

/**
 * The splash screen frame object. The splash screen provides a modal frame (window) with an image, a status label and
 * an optional progress bar. It is used for unbreakable operations like the application's startup.
 * 
 * @author Andre Kasper
 * @author Jan Philipp
 */
final public class SplashScreenFrame extends JFrame {

	private static final Logger log = Logger.getLogger(Configuration.LOGGER);

	private static final long serialVersionUID = 4463172915021076788L;

	/**
	 * status label object
	 */
	private final JLabel statusLabel;

	/**
	 * default status string
	 */
	private String defaultStatus = "Starting...";

	private boolean useProgressBar = true;

	private JProgressBar progressBar = null;

	private List<String> statusMessage;

	private int currentStatusMessage = 0;

	private String finalStatus;

	/**
	 * Construct the splash frame.
	 */
	public SplashScreenFrame() {
		this(null, null);
	}

	/**
	 * Construct the splash frame and set an alternative default status.
	 * 
	 * @param defaultStatus
	 */
	public SplashScreenFrame(final String defaultStatus) {
		this(defaultStatus, null);
	}

	/**
	 * Construct the splash frame and provide a dynamic progress bar.
	 * 
	 * @param statusMessages
	 */
	public SplashScreenFrame(final List<String> statusMessages) {
		this(null, statusMessages);
	}

	/**
	 * Construct the splash frame, set an alternative default status and provide a dynamic progress bar.
	 * 
	 * @param defaultStatus
	 * @param statusMessages
	 */
	public SplashScreenFrame(final String defaultStatus, final List<String> statusMessages) {
		if (defaultStatus != null) {
			this.defaultStatus = defaultStatus;
		}

		if ((statusMessages != null) && (statusMessages.size() > 0)) {
			useProgressBar = true;
			statusMessage = statusMessages;
		}

		setVisible(false);

		// Reduce the shadow and make the window "smaller". Less present.
		if (SystemTools.isMac()) {
			getRootPane().putClientProperty("Window.style", "small");
		}

		setBackground(new Color(160, 160, 160));
		getContentPane().setBackground(getBackground());

		final Icon icon = Internationalization.getIcon("application.splashscreen");
		final JLabel imageLabel = new JLabel(icon);
		imageLabel.setOpaque(true);
		imageLabel.setBackground(getBackground());
		imageLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
		getContentPane().add(imageLabel, BorderLayout.NORTH);

		// status label
		statusLabel = new JLabel(this.defaultStatus, SwingConstants.CENTER);
		final Dimension statusSize = new Dimension(450, 20);
		statusLabel.setBackground(getBackground());
		statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
		statusLabel.setPreferredSize(statusSize);
		statusLabel.setMaximumSize(statusSize);
		statusLabel.setHorizontalTextPosition(SwingConstants.CENTER);

		if (useProgressBar) {
			progressBar = new JProgressBar();
			progressBar.setMaximum(statusMessages.size());
			progressBar.setBackground(getBackground());
			progressBar.setBorder(BorderFactory.createEmptyBorder(5, 10, 0, 10));
			getContentPane().add(progressBar, BorderLayout.CENTER);
			setStatus(statusMessages.get(0));
		}

		getContentPane().add(statusLabel, BorderLayout.SOUTH);

		// frame settings - no decoration
		setUndecorated(true);

		// auto fit size
		pack();

		// splash screen always on top
		setAlwaysOnTop(true);

		// center splash
		setLocationRelativeTo(null);

		// define what to do if this frame instance will closed: remove it
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	}

	/**
	 * Set a new status string.
	 * 
	 * @param text
	 */
	public void setStatus(final String text) {
		statusLabel.setText(text);
		setTitle(text);
		SplashScreenFrame.log.info("Status set: " + text);
	}

	/**
	 * Set to the next status and change the progress bar value.
	 */
	public void showNextStatus() {
		if ((statusMessage != null) && (statusMessage.size() > 0)) {
			final int items = statusMessage.size();
			if (currentStatusMessage < items - 1) {
				currentStatusMessage++;
				setStatus(statusMessage.get(currentStatusMessage));
				progressBar.setValue(currentStatusMessage);
			}
		}
	}

	/**
	 * Finalize the status and the progress bar.
	 */
	public void showFinalStatus() {
		if (finalStatus != null) {
			setStatus(finalStatus);
			progressBar.setValue(progressBar.getMaximum());
		}
	}

	/**
	 * Show the frame with a fade-in effect. This feature works with java >=1.6.10 or Mac OS X 10.5+Java. Otherwise, it
	 * will called {@link #setVisible(true)}. The fade operation is performed in a separated thread.
	 * 
	 * @uses AWTUtilities wrapper class for java's AWTUtilities
	 */
	public void fadeIn() {
		FadeEffectHelper.fadeIn(this);
	}

	/**
	 * Show the frame with a fade-out effect. This feature works with java >=1.6.10 or Mac OS X 10.5+Java. Otherwise, it
	 * will called {@link #setVisible(false)}. The fade operation is performed in a separated thread.
	 * 
	 * @uses AWTUtilities wrapper class for java's AWTUtilities
	 */
	public void fadeOut() {
		FadeEffectHelper.fadeOut(this);
	}

	/**
	 * Set an alternative finale/ready status text.
	 * 
	 * @param finalStatus
	 */
	public void setFinalStatus(final String finalStatus) {
		this.finalStatus = finalStatus;
	}

	/**
	 * Sets the <code>indeterminate</code> property of the progress bar, which determines whether the progress bar is in
	 * determinate or indeterminate mode. An indeterminate progress bar continuously displays animation indicating that
	 * an operation of unknown length is occurring. By default, this property is <code>false</code>. Some look and feels
	 * might not support indeterminate progress bars; they will ignore this property.
	 * 
	 * @see JProgressBar#setIndeterminate(boolean)
	 * @param newValue
	 */
	public void setIndeterminate(final boolean newValue) {
		if (progressBar != null) {
			progressBar.setIndeterminate(newValue);
		}
	}
}
