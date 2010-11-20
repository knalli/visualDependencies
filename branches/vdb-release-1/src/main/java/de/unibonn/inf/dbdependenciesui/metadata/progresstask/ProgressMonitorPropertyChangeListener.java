/**
 * $Id: Main.java 41 2009-03-01 16:21:20Z philipp $
 */
package de.unibonn.inf.dbdependenciesui.metadata.progresstask;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Logger;

import javax.swing.ProgressMonitor;

import de.unibonn.inf.dbdependenciesui.Configuration;

/**
 * This swing worker task reads, analyzes, parse and saves the database schema
 * structure.
 * 
 * @author Andre Kasper
 * @author Jan Philipp
 * 
 */
public class ProgressMonitorPropertyChangeListener implements
		PropertyChangeListener {

	private static final Logger log = Logger.getLogger(Configuration.LOGGER);

	private final ProgressMonitor progressMonitor;
	private final Task task;

	public ProgressMonitorPropertyChangeListener(
			final ProgressMonitor progressMonitor, final Task task) {
		this.progressMonitor = progressMonitor;
		this.task = task;
	}

	@Override
	public void propertyChange(final PropertyChangeEvent e) {
		if ("progress".equals(e.getPropertyName())) {
			final int progress = (Integer) e.getNewValue();
			final String message = this.task.getMessage();
			this.progressMonitor.setNote(message);
			this.progressMonitor.setProgress(progress);
			ProgressMonitorPropertyChangeListener.log
					.info("New progress monitor note: " + message);

			if (this.progressMonitor.isCanceled() || this.task.isDone()) {
				if (this.progressMonitor.isCanceled()) {
					this.progressMonitor.setNote(this.task.getSucceedMessage());
					this.task.cancel(true);
				} else {
					this.progressMonitor.setNote(this.task.getFailedMessage());
				}
			}
		}
	}

}
