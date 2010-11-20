package de.unibonn.inf.dbdependenciesui.ui.views.connections.helpers;

import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import de.unibonn.inf.dbdependenciesui.misc.Internationalization;
import de.unibonn.inf.dbdependenciesui.ui.controller.ViewController;
import de.unibonn.inf.dbdependenciesui.ui.views.connections.ConnectionViewMain;

public class ConnectionTreeSelectionListener implements TreeSelectionListener {

	private final JTree tree;

	/**
	 * storaged the translated string of category "Triggers".
	 */
	private final String titleTriggers;
	private final String titleProcedures;

	public ConnectionTreeSelectionListener(final JTree tree) {
		this.tree = tree;
		titleTriggers = Internationalization.getText("application.connections.tree.triggers");
		titleProcedures = Internationalization.getText("application.connections.tree.procedures");
	}

	@Override
	public void valueChanged(final TreeSelectionEvent e) {
		final TreePath path = tree.getSelectionPath();
		if (path != null) {
			final int pathLength = path.getPathCount();

			if (pathLength >= 2) {
				// One connection is selected (in tree).
				try {
					final Object[] elements = path.getPath();
					final DefaultMutableTreeNode node = (DefaultMutableTreeNode) elements[1];

					ViewController.setDatabaseConnection(node.getUserObject().toString());
				} catch (final Exception e1) {}
			} else {
				// No connection selected.
				ViewController.setDatabaseConnection(0);
			}

			// Decide if the deep of objects is 5 (tables/views) or 4 (triggers).
			if (pathLength == 5) {
				final DefaultMutableTreeNode current = (DefaultMutableTreeNode) path.getLastPathComponent();
				ConnectionViewMain.getInstance().addTab((String) current.getUserObject(),
						path.getPathComponent(1).toString(), path.getPathComponent(2).toString(),
						path.getPathComponent(3).toString());
			}
			if ((pathLength == 4)
					&& (titleTriggers.equals(path.getPathComponent(2).toString()) || titleProcedures.equals(path
							.getPathComponent(2).toString()))) {
				final DefaultMutableTreeNode current = (DefaultMutableTreeNode) path.getLastPathComponent();
				ConnectionViewMain.getInstance().addTab(current.getUserObject().toString(),
						path.getPathComponent(1).toString(), path.getPathComponent(2).toString(),
						path.getPathComponent(3).toString());
			}
		}
	}
}
