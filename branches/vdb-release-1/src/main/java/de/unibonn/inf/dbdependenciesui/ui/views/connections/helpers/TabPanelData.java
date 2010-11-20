package de.unibonn.inf.dbdependenciesui.ui.views.connections.helpers;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;

import de.unibonn.inf.dbdependenciesui.Configuration;
import de.unibonn.inf.dbdependenciesui.controller.ThreadExecutor;
import de.unibonn.inf.dbdependenciesui.helpers.TableDataPaginator;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseConnection;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseTable;
import de.unibonn.inf.dbdependenciesui.misc.Internationalization;
import de.unibonn.inf.dbdependenciesui.ui.controller.ViewController;
import de.unibonn.inf.dbdependenciesui.ui.factory.ViewFactory;
import de.unibonn.inf.dbdependenciesui.ui.misc.WaitProgressWindow.WaitProgressTask;

/**
 * This data panel shows the data of a table or view.
 * 
 * @author Andre Kasper
 * @author Jan Philipp
 */
public class TabPanelData extends JPanel {

	private static final long serialVersionUID = -9162247063554681221L;

	private static final Logger log = Logger.getLogger(Configuration.LOGGER);

	private final int rowsPerPage = 200;

	private final TableDataPaginator loader;

	private final String appKeyLoading = "application.connections.loading.data";
	private final String appKeyBack = "application.connections.data.back";
	private final String appKeyNext = "application.connections.data.next";

	private ConnectionViewMainTableModel tableModel;

	private JButton buttonBack;

	private JButton buttonNext;

	public TabPanelData(final DatabaseTable table, final DatabaseConnection connection) {
		super(new BorderLayout());
		loader = new TableDataPaginator(connection, table);
		initialize();
		connectDatabaseAndloadTableData();
	}

	private void initialize() {
		// Create and add a hierarchy listener. If a new event recived, it will checks if hierarchy has changed (when
		// opening/closing the tab) and if the tab is now hidden (when closing the tab). The connection will closed.
		addHierarchyListener(new HierarchyListener() {
			@Override
			public void hierarchyChanged(final HierarchyEvent e) {
				if ((e.getID() == HierarchyEvent.HIERARCHY_CHANGED)
						&& ((e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0)) {
					if (TabPanelData.this.getHeight() != 0) {
						if (loader != null) {
							ThreadExecutor.execute(new Runnable() {
								@Override
								public void run() {
									loader.closeTable();
								}
							});
						}
					}
				}
			}
		});
	}

	/**
	 * Initialize the panel. If the os system is a mac, itunes stylish tables will installed.
	 */
	private void initializeTable() {
		tableModel = new ConnectionViewMainTableModel(loader.getData(), loader.getColumnsHeader());

		final JTable table = ViewFactory.createTable(tableModel);
		ViewFactory.makeSortableTable(table);

		add(ViewFactory.createScrollableTable(table), BorderLayout.CENTER);
	}

	/**
	 * Creates and display the buttons.
	 */
	private void initializeButtons() {
		final JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BorderLayout());
		buttonBack = new JButton(Internationalization.getText(appKeyBack));
		buttonBack.setIcon(Internationalization.getScaledIcon(appKeyBack, 16));
		buttonNext = new JButton(Internationalization.getText(appKeyNext));
		buttonNext.setIcon(Internationalization.getScaledIcon(appKeyNext, 16));

		final ActionListener al = new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				if (e.getSource() == buttonBack) {
					gotoPage(loader.getCurrentPage() - 1);
				} else if (e.getSource() == buttonNext) {
					gotoPage(loader.getCurrentPage() + 1);
				}
			}
		};

		buttonBack.addActionListener(al);
		buttonNext.addActionListener(al);

		handleButtonUsage();

		buttonPanel.add(buttonBack, BorderLayout.WEST);
		buttonPanel.add(buttonNext, BorderLayout.EAST);

		add(buttonPanel, BorderLayout.NORTH);
	}

	/**
	 * Check the visibility of the buttons. At the first page, back is n/a. At the last page, next is n/a.
	 */
	private void handleButtonUsage() {
		buttonBack.setEnabled(!loader.isFirstPage());
		buttonNext.setEnabled(!loader.isLastPage());
	}

	private void connectDatabaseAndloadTableData() {
		final WaitProgressTask task = new WaitProgressTask(ViewController.getApplicationView(), Internationalization
				.getText(appKeyLoading)) {

			@Override
			protected Void doInBackground() throws Exception {
				try {
					loader.setRowsPerPage(rowsPerPage);
					setProgress(100);
					loader.openTable();
					loader.loadTableRowsByPage(1);
					initializeTable();
					initializeButtons();
					updateUI();
				} catch (final Exception e) {
					log.warning(e.getLocalizedMessage());
					ViewFactory.showMessageDialog(TabPanelData.this, e.getLocalizedMessage());
				}
				return null;
			}
		};
		ViewController.showWaitProgress(task);
	}

	/**
	 * Execute a task showing a specific page. This will load new data from the open database connection in the
	 * background.
	 * 
	 * @param page
	 */
	private void gotoPage(final int page) {
		final WaitProgressTask task = new WaitProgressTask(ViewController.getApplicationView(), Internationalization
				.getText(appKeyLoading)) {

			@Override
			protected Void doInBackground() throws Exception {
				try {
					setProgress(100);
					loader.loadTableRowsByPage(page);
					tableModel.setData(loader.getData());
					handleButtonUsage();
					updateUI();
				} catch (final Exception e) {
					log.warning(e.getLocalizedMessage());
					ViewFactory.showMessageDialog(TabPanelData.this, e.getLocalizedMessage());
				}
				return null;
			}
		};
		ViewController.showWaitProgress(task);
	}
}
