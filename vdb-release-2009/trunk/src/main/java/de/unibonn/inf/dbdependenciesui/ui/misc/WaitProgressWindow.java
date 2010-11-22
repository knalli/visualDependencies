package de.unibonn.inf.dbdependenciesui.ui.misc;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import de.unibonn.inf.dbdependenciesui.ui.helpers.SystemTools;

public class WaitProgressWindow extends JFrame {

	private static final long serialVersionUID = 4229774719759106904L;
	private JProgressBar progressBar;
	private JLabel label;
	private WaitProgressTask task;
	private String message;

	public WaitProgressWindow(final Window owner) {
		super();

		initialize();
	}

	private void initialize() {
		setLayout(new BorderLayout());

		// Reduce the shadow and make the window "smaller". Less present.
		if (SystemTools.isMac()) {
			getRootPane().putClientProperty("Window.style", "small");
		}

		final JPanel container = new JPanel();
		container.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		container.setLayout(new BorderLayout());
		this.add(container, BorderLayout.CENTER);

		label = new JLabel("");
		progressBar = new JProgressBar();
		progressBar.setIndeterminate(true);

		container.add(label, BorderLayout.NORTH);
		container.add(progressBar, BorderLayout.SOUTH);
		pack();
		setLocationRelativeTo(getOwner());

		setResizable(false);

		addWindowListener(new WindowAdapter() {

			/*
			 * (non-Javadoc)
			 * @see java.awt.event.WindowAdapter#windowClosing(java.awt.event.WindowEvent)
			 */
			@Override
			public void windowClosing(final WindowEvent e) {
				task.cancel(true);
			}
		});
	}

	public void execute() {
		label.setText(message);
		pack();

		setVisible(true);
		task.execute();
	}

	public void setTask(final WaitProgressTask task) {
		this.task = task;
	}

	public void setMessage(final String message) {
		this.message = message;
	}

	abstract public static class WaitProgressTask extends SwingWorker<Void, Void> {

		private final WaitProgressWindow frame;

		public WaitProgressTask(final Frame owner, final String message) {
			super();

			frame = new WaitProgressWindow(owner);
			frame.setTask(this);
			frame.setMessage(message);
		}

		public WaitProgressWindow getFrame() {
			return frame;
		}

		@Override
		protected void done() {
			frame.dispose();
		}

	}
}
