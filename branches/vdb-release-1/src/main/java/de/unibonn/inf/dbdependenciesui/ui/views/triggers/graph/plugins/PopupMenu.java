package de.unibonn.inf.dbdependenciesui.ui.views.triggers.graph.plugins;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import de.unibonn.inf.dbdependenciesui.controller.ThreadExecutor;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseTrigger;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.Relation;
import de.unibonn.inf.dbdependenciesui.ui.controller.ViewController;
import de.unibonn.inf.dbdependenciesui.ui.factory.ViewFactory;
import de.unibonn.inf.dbdependenciesui.ui.views.triggers.TriggerViewData;
import edu.uci.ics.jung.visualization.VisualizationViewer;

public class PopupMenu extends JPopupMenu {

	protected static final long serialVersionUID = 8118370265197014992L;
	protected final VisualizationViewer<DatabaseTrigger, Relation> viewer;
	protected ActionListener actionListener;
	protected final DatabaseTrigger selectedTrigger;
	protected final TriggerViewData data;

	public PopupMenu(final VisualizationViewer<DatabaseTrigger, Relation> vv, final DatabaseTrigger selectedTrigger,
			final TriggerViewData data) {
		super();
		viewer = vv;
		this.selectedTrigger = selectedTrigger;
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
					data.getSelectedTrigger().put(selectedTrigger, false);
					data.actionUpdateSelections();
					break;
				case SHOW_DATA:
					ThreadExecutor.execute(new Runnable() {
						@Override
						public void run() {
							ViewController
									.changeViewAndShowObject(ViewController.ViewMode.CONNECTIONS, selectedTrigger);
						}
					});
					break;
				// case SHOW_TABLE:
				// ThreadExecutor.execute(new Runnable() {
				// @Override
				// public void run() {
				// ViewController
				// .changeViewAndShowObject(ViewController.ViewMode.CONNECTIONS, null);
				// }
				// });
				// break;
				case SHOW_SOURCECODE:
					ViewFactory.showMessageDialog(null, selectedTrigger.getTriggerSchemaObject().getBody());
					break;
				}
			}
		};
	}

	protected void initialize() {
		final String keyData = "triggerview.vertex.data";
		final String keySourcecode = "triggerview.vertex.sourcode";
		final String keyClose = "triggerview.vertex.close";
		final String keyAffectedTables = "triggerview.vertex.tables";

		JMenuItem item;

		item = ViewFactory.createPopupMenuItem(keyData, actionListener);
		item.setActionCommand(Command.SHOW_DATA.toString());
		add(item);

		// Show source code item and command.
		item = ViewFactory.createPopupMenuItem(keySourcecode, actionListener);
		item.setActionCommand(Command.SHOW_SOURCECODE.toString());
		add(item);

		// // Show affected tables submenu if exist at least one table.
		// final List<String> affectedTables = selectedTrigger.getTriggerSchemaObject().getAffectedTables();
		// if (!affectedTables.isEmpty()) {
		// final JMenu menu = ViewFactory.createPopupMenu(keyAffectedTables);
		// add(menu);
		// for (final String table : affectedTables) {
		// item = new JMenuItem(table);
		// item.setActionCommand(Command.SHOW_TABLE.toString());
		// item.addActionListener(actionListener);
		// menu.add(item);
		// }
		// }

		final List<String> insertedTables2 = selectedTrigger.getTriggerSchemaObject().getAffectedInsertedTable();
		final List<String> updatedTables2 = selectedTrigger.getTriggerSchemaObject().getAffectedUpdatedTable();
		final List<String> deletedTables2 = selectedTrigger.getTriggerSchemaObject().getAffectedDeletedTable();
		final List<String> usedTables2 = selectedTrigger.getTriggerSchemaObject().getUsedTables();
		if (!insertedTables2.isEmpty() || !updatedTables2.isEmpty() || !deletedTables2.isEmpty()
				|| !usedTables2.isEmpty()) {
			final JMenu menu = ViewFactory.createPopupMenu(keyAffectedTables);
			add(menu);

			final Map<String, UsedTable> containsList2 = new TreeMap<String, UsedTable>();
			UsedTable usedTable;

			for (final String table : insertedTables2) {
				if (!containsList2.containsKey(table)) {
					containsList2.put(table, new UsedTable(true, false, false, false));
				} else {
					usedTable = containsList2.get(table);
					usedTable.setInserted(true);
					containsList2.put(table, usedTable);
				}
			}
			for (final String table : updatedTables2) {
				if (!containsList2.containsKey(table)) {
					containsList2.put(table, new UsedTable(false, true, false, false));
				} else {
					usedTable = containsList2.get(table);
					usedTable.setUpdated(true);
					containsList2.put(table, usedTable);
				}
			}
			for (final String table : deletedTables2) {
				if (!containsList2.containsKey(table)) {
					containsList2.put(table, new UsedTable(false, false, true, false));
				} else {
					usedTable = containsList2.get(table);
					usedTable.setDeleted(true);
					containsList2.put(table, usedTable);
				}
			}
			for (final String table : usedTables2) {
				if (!containsList2.containsKey(table)) {
					containsList2.put(table, new UsedTable(false, false, false, true));
				} else {
					usedTable = containsList2.get(table);
					usedTable.setUsed(true);
					containsList2.put(table, usedTable);
				}
			}

			for (final String key : containsList2.keySet()) {
				usedTable = containsList2.get(key);
				item = new JMenuItem(key + usedTable.getActionsString());
				item.setActionCommand(Command.SHOW_TABLE.toString());
				item.addActionListener(actionListener);

				menu.add(item);
			}
		}

		addSeparator();

		item = ViewFactory.createPopupMenuItem(keyClose, actionListener);
		item.setActionCommand(Command.CLOSE.toString());
		add(item);
	}

	public static enum Command {
		SHOW_DATA, SHOW_SOURCECODE, CLOSE, SHOW_TABLE
	}

	public class UsedTable {
		private Boolean inserted;
		private Boolean updated;
		private Boolean deleted;
		private Boolean used;

		public UsedTable(final Boolean inserted, final Boolean updated, final Boolean deleted, final Boolean used) {
			this.inserted = inserted;
			this.updated = updated;
			this.deleted = deleted;
			this.used = used;
		}

		public void setInserted(final Boolean b) {
			inserted = b;
		}

		public void setUpdated(final Boolean b) {
			updated = b;
		}

		public void setDeleted(final Boolean b) {
			deleted = b;
		}

		public void setUsed(final Boolean b) {
			used = b;
		}

		public String getActionsString() {
			String s = " (";
			Boolean open = false;
			if (inserted == true) {
				s += "INSERT";
				open = true;
			}
			if (updated == true) {
				s += open == true ? ", UPDATE" : "UPDATE";
				open = true;
			}
			if (deleted == true) {
				s += open == true ? ", DELETE" : "DELETE";
				open = true;
			}
			if (used == true) {
				s += open == true ? ", SELECT" : "SELECT";
				open = true;
			}

			return s + ")";
		}
	}

}
