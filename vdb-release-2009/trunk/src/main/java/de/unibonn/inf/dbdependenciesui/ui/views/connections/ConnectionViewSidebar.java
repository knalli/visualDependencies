/**
 * $Id: Main.java 41 2009-03-01 16:21:20Z philipp $
 */
package de.unibonn.inf.dbdependenciesui.ui.views.connections;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Logger;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import com.explodingpixels.macwidgets.IAppWidgetFactory;

import de.unibonn.inf.dbdependenciesui.Configuration;
import de.unibonn.inf.dbdependenciesui.controller.Controller;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseConnection;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseObject;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseProcedure;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseTable;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseTrigger;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseView;
import de.unibonn.inf.dbdependenciesui.metadata.MetaDataFactory.Vendor;
import de.unibonn.inf.dbdependenciesui.misc.Internationalization;
import de.unibonn.inf.dbdependenciesui.ui.controller.ViewController;
import de.unibonn.inf.dbdependenciesui.ui.views.connections.helpers.ConnectionTreeSelectionListener;

/**
 * The sidebar of the connection view displays all available connections and their contents.
 * 
 * @author Andre Kasper
 * @author Jan Philipp
 */
public class ConnectionViewSidebar extends JPanel implements Observer {

	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(Configuration.LOGGER);

	private static final long serialVersionUID = -134888844327839924L;

	private JScrollPane jScrollPane;
	private JTree tree;
	private DefaultTreeModel treeModel;
	private ConnectionViewSidebarPopup popup;

	/**
	 * map with created nodes (used for backrefs, see {@link #update(Observable, Object)}).
	 */
	private final Map<Object, DefaultMutableTreeNode> cachedTreeNodes = new HashMap<Object, DefaultMutableTreeNode>();

	private final String titleConnections = Internationalization.getText("application.connections");
	private final String titleTables = Internationalization.getText("application.connections.tree.tables");
	private final String titleViews = Internationalization.getText("application.connections.tree.views");
	private final String titleTrigger = Internationalization.getText("application.connections.tree.triggers");
	private final String titleProcedure = Internationalization.getText("application.connections.tree.procedures");
	private final String titleData = Internationalization.getText("application.connections.tree.data");
	private final String titleColumns = Internationalization.getText("application.connections.tree.columns");
	private final String titleConstraints = Internationalization.getText("application.connections.tree.constraints");
	private final String titleViewDefinition = Internationalization
			.getText("application.connections.tree.viewdefinition");

	public ConnectionViewSidebar() {
		super(new BorderLayout());
		initialize();
	}

	private void initialize() {
		this.add(getJScrollPane(), BorderLayout.CENTER);
		setPreferredSize(new Dimension(220, 500));
		setBackground(UIManager.getColor("Tree.textBackground"));

		// Add myself to controller.
		Controller.addObserverObject(this);
		ViewController.addObserverObject(this);
	}

	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane(getJConnectionTree());
			jScrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
			IAppWidgetFactory.makeIAppScrollPane(jScrollPane);
		}
		return jScrollPane;
	}

	private JTree getJConnectionTree() {
		if (tree == null) {

			// Build the jtree
			this.update(null, "connections");

			tree.setBorder(new EmptyBorder(5, 5, 0, 0));
			popup = new ConnectionViewSidebarPopup();
			tree.addMouseListener(new MousePopupListener());
			tree.addTreeSelectionListener(new ConnectionTreeSelectionListener(tree));
		}
		return tree;
	}

	// An inner class to check whether mouse events are the popup trigger
	public class MousePopupListener extends MouseAdapter {
		@Override
		public void mousePressed(final MouseEvent e) {
			checkPopup(e);
		}

		@Override
		public void mouseReleased(final MouseEvent e) {
			checkPopup(e);
		}

		@Override
		public void mouseClicked(final MouseEvent e) {
			checkPopup(e);
		}

		/**
		 * Check if the popup is available.
		 * 
		 * @param e
		 */
		private void checkPopup(final MouseEvent e) {
			final int row = tree.getRowForLocation(e.getX(), e.getY());
			// If row is -1, the click was not on a tree's element/row, but
			// outside in the white and deep space.
			if (row == -1) { return; }
			// Reset the current selected element/row.
			tree.setSelectionRow(row);
			// Show the popup if event-source's trigger.
			if (e.isPopupTrigger()) {
				// Get the complete path (root to current selected node)
				final TreePath path = tree.getSelectionPath();
				// If path length greater than 2, ignore it.
				if (path.getPathCount() > 2) { return; }
				final DefaultMutableTreeNode current = (DefaultMutableTreeNode) path.getLastPathComponent();
				popup
						.show(ConnectionViewSidebar.this, e.getX(), e.getY(), path.getPathCount(), current
								.getUserObject());
			}
		}
	}

	@Override
	public void update(final Observable o, final Object arg) {
		if (arg == null) { return; }

		// If the argument is an object of the type DatabaseObject it could force a selection of that userobject.
		// If the argument is an object of the type String it could be an update of the model.

		if (arg instanceof DatabaseObject) {
			final DatabaseConnection connection = ((DatabaseObject) arg).getConnection();
			if (arg instanceof DatabaseTrigger) {
				final Object node1 = getTreeNode(titleConnections);
				final Object node2 = getTreeNode(connection, titleConnections);
				final Object node3 = getTreeNode(titleTrigger, connection);
				final Object node4 = getTreeNode(arg, connection);

				tree.clearSelection();
				tree.setExpandsSelectedPaths(true);
				tree.addSelectionPath(new TreePath(new Object[] {
						node1, node2, node3, node4 }));
				tree.updateUI();
			} else if (arg instanceof DatabaseProcedure) {
				final Object node1 = getTreeNode(titleConnections);
				final Object node2 = getTreeNode(connection, titleConnections);
				final Object node3 = getTreeNode(titleProcedure, connection);
				final Object node4 = getTreeNode(arg, connection);

				tree.clearSelection();
				tree.setExpandsSelectedPaths(true);
				tree.addSelectionPath(new TreePath(new Object[] {
						node1, node2, node3, node4 }));
				tree.updateUI();
			} else if (arg instanceof DatabaseTable) {
				final Object node1 = getTreeNode(titleConnections);
				final Object node2 = getTreeNode(connection, titleConnections);
				final Object node3 = getTreeNode((arg instanceof DatabaseView) ? titleViews : titleTables, connection);
				final Object node4 = getTreeNode(arg, connection);
				final Object node5 = getTreeNode(titleColumns, arg.toString() + connection.toString());

				tree.clearSelection();
				tree.setExpandsSelectedPaths(true);
				tree.addSelectionPath(new TreePath(new Object[] {
						node1, node2, node3, node4, node5 }));
				tree.updateUI();
			}
		} else if (arg instanceof String) {
			if ("connections".equals(arg)) {
				clearTreeNodes();

				final List<String> connections = Controller.getConnections();
				final DefaultMutableTreeNode top = createTreeNode(titleConnections);

				treeModel = new DefaultTreeModel(top);
				for (final String connection : connections) {
					treeModel.insertNodeInto(buildNodes(connection), top, top.getChildCount());
				}

				// Create tree and renderer if tree not exists.
				if (tree == null) {
					tree = new JTree(treeModel);
					tree.setCellRenderer(new ConnectionTreeCellRenderer());
					tree.setExpandsSelectedPaths(true);
				} else {
					tree.setModel(treeModel);
				}

				tree.repaint();
				tree.validate();
			}
		}
	}

	private DefaultMutableTreeNode buildNodes(final String title) {
		final DatabaseConnection connection = Controller.loadConnection(title, true);
		final DefaultMutableTreeNode treeNode = createTreeNode(connection, titleConnections);

		treeNode.add(buildTableNodes(connection));
		treeNode.add(buildViewNodes(connection));
		treeNode.add(buildTriggerNodes(connection));
		final Vendor vendor = connection.getVendor();
		if (Vendor.ORACLE.equals(vendor) || Vendor.ORACLE10.equals(vendor)) {
			treeNode.add(buildProcedureNodes(connection));
		}

		return treeNode;
	}

	private DefaultMutableTreeNode buildTableNodes(final DatabaseConnection connection) {
		final DefaultMutableTreeNode treeNode = createTreeNode(titleTables, connection);
		for (final DatabaseTable table : connection.getTables()) {
			treeNode.add(buildTableSubNodes(table));
		}
		return treeNode;
	}

	private DefaultMutableTreeNode buildViewNodes(final DatabaseConnection connection) {
		final DefaultMutableTreeNode treeNode = createTreeNode(titleViews, connection);
		for (final DatabaseView view : connection.getViews()) {
			treeNode.add(buildTableSubNodes(view));
		}
		return treeNode;
	}

	private DefaultMutableTreeNode buildTriggerNodes(final DatabaseConnection connection) {
		final DefaultMutableTreeNode treeNode = createTreeNode(titleTrigger, connection);
		for (final DatabaseTrigger trigger : connection.getTriggers()) {
			treeNode.add(createTreeNode(trigger, connection));
		}
		return treeNode;
	}

	private DefaultMutableTreeNode buildProcedureNodes(final DatabaseConnection connection) {
		final DefaultMutableTreeNode treeNode = createTreeNode(titleProcedure, connection);
		for (final DatabaseProcedure procedure : connection.getProcedures()) {
			treeNode.add(createTreeNode(procedure, connection));
		}
		return treeNode;
	}

	private DefaultMutableTreeNode buildTableSubNodes(final DatabaseTable table) {
		final DefaultMutableTreeNode treeNode = createTreeNode(table, table.getConnection());
		treeNode.add(createTreeNode(titleColumns, table.toString() + table.getConnection().toString()));
		treeNode.add(createTreeNode(titleData, table + table.getConnection().toString()));
		treeNode.add(createTreeNode(titleConstraints, table + table.getConnection().toString()));

		if (table instanceof DatabaseView) {
			treeNode.add(createTreeNode(titleViewDefinition, table + table.getConnection().toString()));
		}

		return treeNode;
	}

	private DefaultMutableTreeNode createTreeNode(final Object userObject) {
		final DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(userObject);
		final String key = userObject.getClass().toString() + userObject.toString();
		cachedTreeNodes.put(key, treeNode);
		return treeNode;
	}

	private DefaultMutableTreeNode createTreeNode(final Object userObject, final Object parentObject) {
		final DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(userObject);
		final String key = userObject.getClass().toString() + parentObject.toString() + userObject.toString();
		cachedTreeNodes.put(key, treeNode);
		return treeNode;
	}

	private DefaultMutableTreeNode getTreeNode(final Object userObject) {
		final String key = userObject.getClass().toString() + userObject.toString();
		return cachedTreeNodes.get(key);
	}

	private DefaultMutableTreeNode getTreeNode(final Object userObject, final Object detailObject) {
		final String key = userObject.getClass().toString() + detailObject.toString() + userObject.toString();
		return cachedTreeNodes.get(key);
	}

	private void clearTreeNodes() {
		cachedTreeNodes.clear();
	}
}
