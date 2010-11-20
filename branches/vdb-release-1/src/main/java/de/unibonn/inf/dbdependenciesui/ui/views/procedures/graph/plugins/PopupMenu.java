package de.unibonn.inf.dbdependenciesui.ui.views.procedures.graph.plugins;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingWorker;

import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseObject;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseProcedure;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseTable;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseTrigger;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.Column;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.ForeignKey;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.PrimaryKey;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.Relation;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.UniqueKey;
import de.unibonn.inf.dbdependenciesui.ui.controller.ViewController;
import de.unibonn.inf.dbdependenciesui.ui.factory.ViewFactory;
import de.unibonn.inf.dbdependenciesui.ui.views.procedures.ProcViewData;
import edu.uci.ics.jung.visualization.VisualizationViewer;

public class PopupMenu extends JPopupMenu {

	protected static final long serialVersionUID = 8118370265197014992L;
	protected final VisualizationViewer<DatabaseTable, Relation> viewer;
	protected ActionListener actionListener;
	protected final DatabaseObject selectedObject;
	protected final ProcViewData data;

	public PopupMenu(final VisualizationViewer<DatabaseTable, Relation> vv, final DatabaseObject selectedObject,
			final ProcViewData data) {
		super();
		viewer = vv;
		this.selectedObject = selectedObject;
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
					// data.getSelectedTables().put(selectedTable, false);
					// data.actionUpdateSelections();
					break;
				case SHOW_DATA:
					new SwingWorker<Void, Void>() {
						@Override
						protected Void doInBackground() throws Exception {
							ViewController.changeViewAndShowObject(ViewController.ViewMode.CONNECTIONS, selectedObject);
							return null;
						}
					}.run();
					break;
				case SHOW_SOURCECODE:
					ViewFactory.showMessageDialog(null, ((DatabaseTrigger) selectedObject).getTriggerSchemaObject()
							.getBody());
					break;
				case SHOW_PROC_SOURCECODE:
					ViewFactory.showMessageDialog(null, ((DatabaseProcedure) selectedObject).getProcedureSchemaObject()
							.getBody());
					break;
				}

			}
		};
	}

	protected void initialize() {
		final String keyData = "entityrelationsview.vertex.data";
		final String keyColumns = "entityrelationsview.vertex.columns";
		final String keyConstraints = "entityrelationsview.vertex.constraints";
		final String keyTriggers = "entityrelationsview.vertex.triggers";

		final String keySourcecode = "triggerview.vertex.sourcode";
		final String keyAffectedTables = "triggerview.vertex.tables";

		JMenuItem item;

		item = ViewFactory.createPopupMenuItem(keyData, actionListener);
		item.setActionCommand(Command.SHOW_DATA.toString());
		add(item);

		if (selectedObject instanceof DatabaseTable) {
			final DatabaseTable selectedTable = (DatabaseTable) selectedObject;
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
		} else if (selectedObject instanceof DatabaseTrigger) {
			final DatabaseTrigger selectedTrigger = (DatabaseTrigger) selectedObject;
			// Show source code item and command.
			item = ViewFactory.createPopupMenuItem(keySourcecode, actionListener);
			item.setActionCommand(Command.SHOW_SOURCECODE.toString());
			add(item);

			// Show affected tables submenu if exist at least one table.
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
			// Show affected tables submenu if exist at least one table.
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
		} else if (selectedObject instanceof DatabaseProcedure) {
			final DatabaseProcedure selectedProcedure = (DatabaseProcedure) selectedObject;
			// Show source code item and command.
			item = ViewFactory.createPopupMenuItem(keySourcecode, actionListener);
			item.setActionCommand(Command.SHOW_PROC_SOURCECODE.toString());
			add(item);

			// Show affected tables submenu if exist at least one table.
			final List<String> insertedTables = selectedProcedure.getProcedureSchemaObject().getAffectedInsertedTable();
			final List<String> updatedTables = selectedProcedure.getProcedureSchemaObject().getAffectedUpdatedTable();
			final List<String> deletedTables = selectedProcedure.getProcedureSchemaObject().getAffectedDeletedTable();
			final List<String> usedTables = selectedProcedure.getProcedureSchemaObject().getUsedTables();
			if (!insertedTables.isEmpty() || !updatedTables.isEmpty() || !deletedTables.isEmpty()
					|| !usedTables.isEmpty()) {
				final JMenu menu = ViewFactory.createPopupMenu(keyAffectedTables);
				add(menu);

				final Map<String, UsedTable> containsList = new TreeMap<String, UsedTable>();
				UsedTable usedTable;

				for (final String table : insertedTables) {
					if (!containsList.containsKey(table)) {
						containsList.put(table, new UsedTable(true, false, false, false));
					} else {
						usedTable = containsList.get(table);
						usedTable.setInserted(true);
						containsList.put(table, usedTable);
					}
				}
				for (final String table : updatedTables) {
					if (!containsList.containsKey(table)) {
						containsList.put(table, new UsedTable(false, true, false, false));
					} else {
						usedTable = containsList.get(table);
						usedTable.setUpdated(true);
						containsList.put(table, usedTable);
					}
				}
				for (final String table : deletedTables) {
					if (!containsList.containsKey(table)) {
						containsList.put(table, new UsedTable(false, false, true, false));
					} else {
						usedTable = containsList.get(table);
						usedTable.setDeleted(true);
						containsList.put(table, usedTable);
					}
				}
				for (final String table : usedTables) {
					if (!containsList.containsKey(table)) {
						containsList.put(table, new UsedTable(false, false, false, true));
					} else {
						usedTable = containsList.get(table);
						usedTable.setUsed(true);
						containsList.put(table, usedTable);
					}
				}

				for (final String key : containsList.keySet()) {
					usedTable = containsList.get(key);
					item = new JMenuItem(key + usedTable.getActionsString());
					item.setActionCommand(Command.SHOW_TABLE.toString());
					item.addActionListener(actionListener);

					menu.add(item);
				}

				// for (final String table : insertedTables) {
				// item = new JMenuItem(table);
				// item.setActionCommand(Command.SHOW_TABLE.toString());
				// item.addActionListener(actionListener);
				// if (!containsList.contains(table)) {
				// menu.add(item);
				// containsList.add(table);
				// }
				// }

			}
		}
	}

	public static enum Command {
		SHOW_DATA, CLOSE, SHOW_COLUMN, SHOW_FOREIGNKEY, SHOW_UNIQUEKEY, SHOW_PRIMARYKEY, SHOW_SOURCECODE, SHOW_TABLE, SHOW_PROC_SOURCECODE
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
