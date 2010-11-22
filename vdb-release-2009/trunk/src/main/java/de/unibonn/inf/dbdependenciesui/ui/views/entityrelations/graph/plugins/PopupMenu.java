package de.unibonn.inf.dbdependenciesui.ui.views.entityrelations.graph.plugins;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingWorker;

import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseTable;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseTrigger;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.Column;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.ForeignKey;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.PrimaryKey;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.Relation;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.UniqueKey;
import de.unibonn.inf.dbdependenciesui.ui.controller.ViewController;
import de.unibonn.inf.dbdependenciesui.ui.factory.ViewFactory;
import de.unibonn.inf.dbdependenciesui.ui.views.entityrelations.ERDViewData;
import edu.uci.ics.jung.visualization.VisualizationViewer;

public class PopupMenu extends JPopupMenu {

	protected static final long serialVersionUID = 8118370265197014992L;
	protected final VisualizationViewer<DatabaseTable, Relation> viewer;
	protected ActionListener actionListener;
	protected final DatabaseTable selectedTable;
	protected final ERDViewData data;

	public PopupMenu(final VisualizationViewer<DatabaseTable, Relation> vv, final DatabaseTable selectedTable,
			final ERDViewData data) {
		super();
		viewer = vv;
		this.selectedTable = selectedTable;
		this.data = data;

		initializeActionListener();
		initialize();
	}

	protected void initializeActionListener() {
		actionListener = new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent e) {
				Command command = null;
				try {
					command = Command.valueOf(e.getActionCommand());
				} catch (final Exception ex) {
					return;
				}

				switch (command) {
				case CLOSE:
					data.getSelectedTables().put(selectedTable, false);
					data.actionUpdateSelections();
					break;
				case SHOW_DATA:
					new SwingWorker<Void, Void>() {
						@Override
						protected Void doInBackground() throws Exception {
							ViewController.changeViewAndShowObject(ViewController.ViewMode.CONNECTIONS, selectedTable);
							return null;
						}
					}.run();
				}
			}
		};
	}

	protected void initialize() {
		final String keyData = "entityrelationsview.vertex.data";
		final String keyClose = "entityrelationsview.vertex.close";
		final String keyColumns = "entityrelationsview.vertex.columns";
		final String keyConstraints = "entityrelationsview.vertex.constraints";
		final String keyTriggers = "entityrelationsview.vertex.triggers";

		JMenuItem item;

		item = ViewFactory.createPopupMenuItem(keyData, actionListener);
		item.setActionCommand(Command.SHOW_DATA.toString());
		add(item);

		// Show columns if exist (but should, actually).
		final List<Column> columns = selectedTable.getDdlSchemaObject().getColumns();
		if (!columns.isEmpty()) {
			final JMenu menu = ViewFactory.createPopupMenu(keyColumns);
			for (final Column column : columns) {
				item = new JMenuItem(column.toString());
				item.setActionCommand(Command.SHOW_COLUMN.toString());
				item.addActionListener(actionListener);
				menu.add(item);
			}
			add(menu);
		}

		// Show constraints if exist.
		final List<PrimaryKey> primaryKeys = selectedTable.getDdlSchemaObject().getPrimaryKeys();
		final List<UniqueKey> uniqueKeys = selectedTable.getDdlSchemaObject().getUniqueKeys();
		final List<ForeignKey> foreignKeys = selectedTable.getDdlSchemaObject().getForeignKeys();
		if (!primaryKeys.isEmpty() || !uniqueKeys.isEmpty() || !foreignKeys.isEmpty()) {
			final JMenu menu = ViewFactory.createPopupMenu(keyConstraints);
			for (final PrimaryKey key : primaryKeys) {
				item = new JMenuItem(key.toString());
				item.setActionCommand(Command.SHOW_PRIMARYKEY.toString());
				item.addActionListener(actionListener);
				menu.add(item);
			}
			for (final UniqueKey key : uniqueKeys) {
				item = new JMenuItem(key.toString());
				item.setActionCommand(Command.SHOW_UNIQUEKEY.toString());
				item.addActionListener(actionListener);
				menu.add(item);
			}
			for (final ForeignKey key : foreignKeys) {
				item = new JMenuItem(key.toString());
				item.setActionCommand(Command.SHOW_FOREIGNKEY.toString());
				item.addActionListener(actionListener);
				menu.add(item);
			}
			add(menu);
		}

		// Show triggers sub menu w/o command if at least one trigger exists.
		final List<DatabaseTrigger> triggers = selectedTable.getDdlSchemaObject().getTriggers();
		if (triggers.size() > 0) {
			final JMenu menu = ViewFactory.createMenu(keyTriggers, ViewFactory.applicationPopupMenuPrefix);
			for (final DatabaseTrigger trigger : triggers) {
				menu.add(trigger.getTitle());
			}
			add(menu);
		}

		addSeparator();

		item = ViewFactory.createPopupMenuItem(keyClose, actionListener);
		item.setActionCommand(Command.CLOSE.toString());
		add(item);
	}

	public static enum Command {
		SHOW_DATA, CLOSE, SHOW_COLUMN, SHOW_FOREIGNKEY, SHOW_UNIQUEKEY, SHOW_PRIMARYKEY
	}

}
