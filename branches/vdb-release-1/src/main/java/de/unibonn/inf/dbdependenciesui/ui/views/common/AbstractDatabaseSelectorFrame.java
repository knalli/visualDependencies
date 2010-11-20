package de.unibonn.inf.dbdependenciesui.ui.views.common;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import de.unibonn.inf.dbdependenciesui.misc.Internationalization;
import de.unibonn.inf.dbdependenciesui.ui.factory.ViewFactory;

public abstract class AbstractDatabaseSelectorFrame extends JFrame {

	private static final long serialVersionUID = -6767981284671755695L;
	private JScrollPane scrollPane;

	private static String appKey = "application.graph.sidebar.controls.selectwindow.";

	/**
	 * Return the used table model.
	 * 
	 * @return
	 */
	protected abstract AbstractDatabaseObjectTableModel getTableModel();

	/**
	 * Execute a refresh action.
	 */
	protected abstract void executeRefresh();

	/**
	 * Execute a select-all action.
	 */
	protected void executeSelectAll() {
		final AbstractDatabaseObjectTableModel model = getTableModel();
		final int column = model.getBooleanColumnIndex();

		for (int row = 0; row < model.getRowCount(); row++) {
			model.setValueAt(true, row, column);
		}
	}

	/**
	 * Execute a select-none action.
	 */
	protected void executeSelectNone() {
		final AbstractDatabaseObjectTableModel model = getTableModel();
		final int column = model.getBooleanColumnIndex();

		for (int row = 0; row < model.getRowCount(); row++) {
			model.setValueAt(false, row, column);
		}
	}

	protected void initialize() {
		final JPanel contentPane = new JPanel(new BorderLayout());
		setContentPane(contentPane);

		contentPane.add(getScrollableTable(), BorderLayout.CENTER);
		contentPane.add(getButtons(), BorderLayout.SOUTH);

		pack();
		setLocationRelativeTo(null);
	}

	private Component getScrollableTable() {
		if (scrollPane == null) {
			final JTable table = ViewFactory.createTable(getTableModel());
			ViewFactory.makeSortableTable(table);
			scrollPane = ViewFactory.createScrollableTable(table);
		}
		return scrollPane;
	}

	private JPanel getButtons() {
		final JPanel panel = new JPanel(new BorderLayout());
		final JPanel buttonPanelLeft = new JPanel();
		final JPanel buttonPanelRight = new JPanel();
		final ActionListener al = new MyActionListener(this);

		final JButton btnCancel = new JButton(Internationalization.getText(appKey + "cancel"));
		final JButton btnOk = new JButton(Internationalization.getText(appKey + "ok"));
		final JButton btnSelectAll = new JButton(Internationalization.getText(appKey + "selectAll"));
		final JButton btnSelectNone = new JButton(Internationalization.getText(appKey + "selectNone"));

		btnCancel.setActionCommand(Command.CANCEL.toString());
		btnOk.setActionCommand(Command.REFRESH.toString());
		btnSelectAll.setActionCommand(Command.SELECTALL.toString());
		btnSelectNone.setActionCommand(Command.SELECTNONE.toString());

		btnCancel.addActionListener(al);
		btnOk.addActionListener(al);
		btnSelectAll.addActionListener(al);
		btnSelectNone.addActionListener(al);

		buttonPanelLeft.setLayout(new BorderLayout());
		buttonPanelRight.setLayout(new BorderLayout());

		buttonPanelLeft.add(btnCancel, BorderLayout.WEST);
		buttonPanelLeft.add(btnSelectNone, BorderLayout.EAST);
		buttonPanelRight.add(btnSelectAll, BorderLayout.WEST);
		buttonPanelRight.add(btnOk, BorderLayout.EAST);

		panel.add(buttonPanelLeft, BorderLayout.WEST);
		panel.add(buttonPanelRight, BorderLayout.EAST);

		return panel;
	}

	protected static enum Command {
		CANCEL, REFRESH, SELECTALL, SELECTNONE
	}

	protected static class MyActionListener implements ActionListener {

		private final AbstractDatabaseSelectorFrame frame;

		public MyActionListener(final AbstractDatabaseSelectorFrame frame) {
			this.frame = frame;
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			Command command = null;
			try {
				command = Command.valueOf(e.getActionCommand());
			} catch (final Exception e1) {}

			switch (command) {
			case CANCEL:
				// Close & dispose everything.
				frame.dispose();
				break;
			case REFRESH:
				// Refresh
				frame.executeRefresh();
				break;
			case SELECTALL:
				frame.executeSelectAll();
				break;
			case SELECTNONE:
				frame.executeSelectNone();
				break;
			}
		}
	}
}
