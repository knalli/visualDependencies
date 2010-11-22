/**
 * $Id: Main.java 41 2009-03-01 16:21:20Z philipp $
 */
package de.unibonn.inf.dbdependenciesui.metadata.progresstask;

import java.awt.Toolkit;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingWorker;

import de.unibonn.inf.dbdependenciesui.Configuration;
import de.unibonn.inf.dbdependenciesui.controller.Controller;
import de.unibonn.inf.dbdependenciesui.hibernate.HibernateDAOFactory;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseConnection;
import de.unibonn.inf.dbdependenciesui.metadata.IMetaData;
import de.unibonn.inf.dbdependenciesui.misc.Internationalization;
import de.unibonn.inf.dbdependenciesui.ui.factory.ViewFactory;

/**
 * This swing worker task reads, analyses, parse and saves the database schema structure.
 * 
 * @author Andre Kasper
 * @author Jan Philipp
 */
public class Task extends SwingWorker<Void, Void> {

	private static final Logger log = Logger.getLogger(Configuration.LOGGER);

	private final IMetaData metaData;

	private Status status;

	private String globalKey = "application.metadataprogress.";

	private DatabaseConnection connection;

	protected int numberOfTables;
	protected int numberOfViews;
	protected int numberOfTriggers;
	protected int numberOfProcedures;

	private int analyzingTables = 0;
	private int analyzingViews = 0;
	private int analyzingTriggers = 0;
	private int analyzingProcedures = 0;
	private int parsingTables = 0;
	private int parsingViews = 0;
	private int parsingTriggers = 0;
	private int parsingProcedures = 0;

	/**
	 * current object string identifier
	 */
	private String currentObject = null;

	// statistics
	private long currentTimeStamp = 0l;
	private long usedTimeConnect = 0l;
	private long usedTimeInitialize = 0l;
	private long usedTimeAnalyze = 0l;
	private long usedTimeParse = 0l;
	private long usedTimeSave = 0l;

	public Task(final IMetaData metaData, final DatabaseConnection connection) {
		super();
		this.metaData = metaData;
		this.connection = connection;
		initialize();
		setProgress(0);
	}

	private void initialize() {
		status = Status.START;

		if (metaData != null) {
			metaData.addPropertyChangeListener(new PropertyChangeListener() {

				@Override
				public void propertyChange(final PropertyChangeEvent evt) {
					final String propertyName = evt.getPropertyName();
					if ("numberOfTables".equals(propertyName)) {
						final int newValue = (Integer) evt.getNewValue();
						numberOfTables = newValue;
					}
					if ("numberOfViews".equals(propertyName)) {
						final int newValue = (Integer) evt.getNewValue();
						numberOfViews = newValue;
					}
					if ("numberOfTriggers".equals(propertyName)) {
						final int newValue = (Integer) evt.getNewValue();
						numberOfTriggers = newValue;
					}
					if ("numberOfProcedures".equals(propertyName)) {
						final int newValue = (Integer) evt.getNewValue();
						numberOfProcedures = newValue;
					}
					if ("analyzingTable".equals(propertyName)) {
						analyzingTables++;
						currentObject = (String) evt.getNewValue();

					}
					if ("parsingTable".equals(propertyName)) {
						parsingTables++;
						currentObject = (String) evt.getNewValue();
					}
					if ("analyzingView".equals(propertyName)) {
						analyzingViews++;
						currentObject = (String) evt.getNewValue();
					}
					if ("parsingView".equals(propertyName)) {
						parsingViews++;
						currentObject = (String) evt.getNewValue();

					}
					if ("analyzingTrigger".equals(propertyName)) {
						analyzingTriggers++;
						currentObject = (String) evt.getNewValue();
					}
					if ("parsingTrigger".equals(propertyName)) {
						parsingTriggers++;
						currentObject = (String) evt.getNewValue();
					}
					if ("analyzingProcedures".equals(propertyName)) {
						analyzingProcedures++;
						currentObject = (String) evt.getNewValue();
					}
					if ("parsingProcedures".equals(propertyName)) {
						parsingProcedures++;
						currentObject = (String) evt.getNewValue();
					}
					if ("connection".equals(propertyName)) {
						currentObject = (String) evt.getNewValue();
					}
					if ("metadata".equals(propertyName)) {
						currentObject = (String) evt.getNewValue();
					}
					Task.this.refreshProgress();
				}
			});
		}
	}

	@Override
	protected void done() {
		if (metaData != null) {
			metaData.close();
			final String description = metaData.getDescription();
			if ((description != null) && !description.isEmpty()) {
				ViewFactory.showMessageDialog(null, description);
			}
		}

	}

	private void nextProgressStep() {
		status = status.next();
		currentObject = null;
		refreshProgress();
	}

	@Override
	protected Void doInBackground() throws Exception {
		if (metaData == null) {
			cancel(true);
			status = Status.FAILURE;
			refreshProgress();

		} else {
			try {
				connection = HibernateDAOFactory.getConnectionDAO().findById(connection.getId(), true);
				HibernateDAOFactory.closeSession();
				metaData.setDatabaseConnection(connection);

				// Part 1: Connect.
				nextProgressStep(); // start connect
				nextProgressStep(); // connecting..
				resetTimeStampSpot();
				if (!metaData.connect()) { throw new Exception(metaData.getLastError()); }
				usedTimeConnect = getTimeStampDelta();
				nextProgressStep(); // connected

				// Part 2: Initialize.
				if (!isCancelled()) {
					nextProgressStep();
					resetTimeStampSpot();
					metaData.initialize();
					usedTimeInitialize = getTimeStampDelta();
					nextProgressStep();
				}

				// Part 3: Analyze.
				if (!isCancelled()) {
					nextProgressStep();
					resetTimeStampSpot();
					metaData.analyze();
					usedTimeAnalyze = getTimeStampDelta();
					if (metaData.isErrorOccured()) { throw new Exception(metaData.getLastError()); }
					nextProgressStep();
				}

				// Part 4a: Parse.
				if (!isCancelled()) {
					nextProgressStep();
					resetTimeStampSpot();
					metaData.parseViews();
					usedTimeParse = getTimeStampDelta();
					if (metaData.isErrorOccured()) { throw new Exception(metaData.getLastError()); }
					nextProgressStep();
				}

				// Part 4b: Parse.
				if (!isCancelled()) {
					nextProgressStep();
					resetTimeStampSpot();
					metaData.parseTriggers();
					usedTimeParse += getTimeStampDelta();
					if (metaData.isErrorOccured()) { throw new Exception(metaData.getLastError()); }
					nextProgressStep();
				}

				// Part 4c: Parse.
				if (!isCancelled()) {
					nextProgressStep();
					resetTimeStampSpot();
					metaData.parseProcedures();
					usedTimeParse += getTimeStampDelta();
					if (metaData.isErrorOccured()) { throw new Exception(metaData.getLastError()); }
					nextProgressStep();
				}

				// Part 5: Save.
				if (!isCancelled()) {
					nextProgressStep();
					// Thread.sleep(500);
					resetTimeStampSpot();
					Controller.updateConnection(connection, true);
					usedTimeSave = getTimeStampDelta();
					nextProgressStep();
					// Thread.sleep(500);
				}

				nextProgressStep();
			} catch (final Throwable e) {
				e.printStackTrace();
				log.log(Level.WARNING, e.getLocalizedMessage());
				ViewFactory.showMessageDialog(null, e.getLocalizedMessage());
				setProgress(100);
				if (e instanceof Exception) { throw (Exception) e; }
			}
		}

		// Set a special message if task was cancelled.
		if (isCancelled()) {
			log.info("Metadata task was cancelled.");
		} else if (status.equals(Status.SUCCESS)) {
			log.info("Metadata task has successfull finished.");
		} else {
			log.info("Metadata task has not successfull finished.");
		}

		// Log statistics
		logStats("connect", usedTimeConnect);
		logStats("init", usedTimeInitialize);
		logStats("analyze", usedTimeAnalyze);
		logStats("parse", usedTimeParse);
		logStats("save", usedTimeSave);

		Toolkit.getDefaultToolkit().beep();
		setProgress(100);
		return null;
	}

	private void logStats(final String title, final long stats) {
		log.info(String.format("Time used for %s: %dms or %ds%n", title, stats, Math.round(stats / 1000f)));
	}

	public void setGlobalKey(final String globalKey) {
		this.globalKey = globalKey;
	}

	public String getGlobalKey() {
		return globalKey;
	}

	private void refreshProgress() {
		int progress = 0;
		final int totalItems = numberOfTables + numberOfViews + numberOfTriggers;

		switch (status) {
		case START:
		case BEGIN_CONNECT:
			progress = 0;
			break;
		case CONNECTING:
			progress = 5;
			break;
		case CONNECTED:
		case BEGIN_READ:
			progress = 10;
			break;
		case READ:
			progress = 20;
			break;
		case BEGIN_ANALYZE:
			progress = 20;
			if (totalItems > 0) {
				final int alreadyDone = analyzingTables + analyzingViews + analyzingTriggers + analyzingProcedures;
				progress += 35 * (1.0 * alreadyDone / totalItems);
			}

			// The progress monitor would not recognize the new status if the
			// progress property has not changed. So the progress should be max
			// 54, 1 less than 55.
			if (progress > 54) {
				progress = 54;
			}
			break;
		case ANALYZED:
			progress = 55;
			break;
		case BEGIN_VIEW_PARSE:
			progress = 55;
			if (totalItems > 0) {
				progress += 35 * (1.0 * parsingViews / numberOfViews);
			}

			// The progress monitor would not recognize the new status if the
			// progress property has not changed. So the progress should be max
			// 89, 1 less than 90.
			if (progress > 89) {
				progress = 89;
			}
			break;
		case VIEW_PARSED:
		case BEGIN_SAVE:
			progress = 90;
			break;
		case SAVED:
			progress = 99;
			break;
		case SUCCESS:
			progress = 100;
			break;
		}
		setProgress(progress);
	}

	public String getMessage() {
		switch (status) {
		case START:
		case BEGIN_CONNECT:
		case CONNECTING:
		case BEGIN_READ:
		case BEGIN_ANALYZE:
		case BEGIN_VIEW_PARSE:
		case BEGIN_SAVE:
		case SAVED:
			if (currentObject == null) {
				return Internationalization.getText(globalKey + "steps." + status.toString().toLowerCase());
			} else {
				return Internationalization.getTextFormatted(globalKey + "steps." + status.toString().toLowerCase()
						+ ".increment", currentObject);
			}
		case SUCCESS:
			return getSucceedMessage();
		case FAILURE:
			return getFailedMessage();
		}
		return status.toString();
	}

	public String getSucceedMessage() {
		return Internationalization.getText(globalKey + "steps.success");
	}

	public String getFailedMessage() {
		return Internationalization.getText(globalKey + "steps.failure");
	}

	/**
	 * Reset the time stamp.
	 */
	protected void resetTimeStampSpot() {
		currentTimeStamp = System.currentTimeMillis();
	}

	/**
	 * Return the delta of the time stamps, actually the used time.
	 * 
	 * @return
	 */
	protected long getTimeStampDelta() {
		final long delta = System.currentTimeMillis() - currentTimeStamp;
		currentTimeStamp = 0;
		return delta;
	}

	/**
	 *Current status.
	 */
	private static enum Status {
		START, BEGIN_CONNECT, CONNECTING, CONNECTED, BEGIN_READ, READ, BEGIN_ANALYZE, ANALYZED, BEGIN_VIEW_PARSE, VIEW_PARSED, BEGIN_SAVE, SAVED, SUCCESS, FAILURE;

		public Status next() {
			switch (this) {
			case START:
				return BEGIN_CONNECT;
			case BEGIN_CONNECT:
				return CONNECTING;
			case CONNECTING:
				return CONNECTED;
			case CONNECTED:
				return BEGIN_READ;
			case BEGIN_READ:
				return READ;
			case READ:
				return BEGIN_ANALYZE;
			case BEGIN_ANALYZE:
				return ANALYZED;
			case ANALYZED:
				return BEGIN_VIEW_PARSE;
			case BEGIN_VIEW_PARSE:
				return VIEW_PARSED;
			case VIEW_PARSED:
				return BEGIN_SAVE;
			case BEGIN_SAVE:
				return SAVED;
			case SAVED:
				return SUCCESS;
			case SUCCESS:
				return SUCCESS;
			}
			return FAILURE;
		}
	}

}
