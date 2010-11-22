/**
 * $Id: Main.java 41 2009-03-01 16:21:20Z philipp $
 */
package de.unibonn.inf.dbdependenciesui.ui.factory;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import com.explodingpixels.macwidgets.IAppWidgetFactory;
import com.explodingpixels.macwidgets.MacWidgetFactory;
import com.explodingpixels.widgets.TableUtils;

import de.unibonn.inf.dbdependenciesui.Main;
import de.unibonn.inf.dbdependenciesui.controller.Controller;
import de.unibonn.inf.dbdependenciesui.misc.Internationalization;
import de.unibonn.inf.dbdependenciesui.ui.ApplicationView;
import de.unibonn.inf.dbdependenciesui.ui.controller.ViewController;
import de.unibonn.inf.dbdependenciesui.ui.views.connections.ConnectionFormEditorDialog;

/**
 * The ViewFactory is a collection of stateless and general view actions (e.g. dialogs).
 * 
 * @author Andre Kasper
 * @author Jan Philipp
 */
public class ViewFactory {

	public static final String applicationMenuPrefix = "application.menu.";
	public static final String applicationPopupMenuPrefix = "application.popup.";

	public static void openNewConnectionDialog() {
		final JDialog frame = new ConnectionFormEditorDialog();
		final ApplicationView mainFrame = ViewController.getApplicationView();

		// position mid/center
		frame.setLocationRelativeTo(mainFrame);
		frame.setModal(true);
		frame.setVisible(true);
	}

	public static void openEditConnectionDialog(final int connectionId) {
		final JDialog frame = new ConnectionFormEditorDialog(connectionId);
		final ApplicationView mainFrame = ViewController.getApplicationView();

		// position mid/center
		frame.setLocationRelativeTo(mainFrame);
		frame.setModal(true);
		frame.setVisible(true);
	}

	public static void openConfirmDeleteConnectionDialog(final int connectionId) {
		final ApplicationView mainFrame = ViewController.getApplicationView();

		final String message = Internationalization.getText("application.connection.confirmremoveconnection.message");
		final String title = Internationalization.getText("application.connection.confirmremoveconnection.title");
		final String btnYes = Internationalization.getText("application.connection.confirmremoveconnection.yes");
		final String btnNo = Internationalization.getText("application.connection.confirmremoveconnection.no");

		// the titles of buttons
		final String[] options = {
				btnYes, btnNo };

		final int returnValue = JOptionPane.showOptionDialog(mainFrame, message, title, JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE, null, options, options[0]); // default
		// button
		// title

		if (returnValue == JOptionPane.YES_OPTION) {
			Controller.removeConnection(Controller.loadConnection(connectionId, true));
		}
	}

	/**
	 * Create a new menu by a given translation key. The new menu's text will searched for
	 * <code>Internationalization.getText(applicationMenuPrefix + key)</code>.
	 * 
	 * @param key
	 * @return
	 */
	public static JMenu createMenu(final String key) {
		return createMenu(key, ViewFactory.applicationMenuPrefix);
	}

	/**
	 * Create a new menu by a given translation key. The new menu's text will searched for
	 * <code>Internationalization.getText(prefix + key)</code>.
	 * 
	 * @param key
	 * @param prefix
	 * @return
	 */
	public static JMenu createMenu(final String key, final String prefix) {
		final String text = Internationalization.getText(prefix + key);
		final JMenu menu = new JMenu(text);
		return menu;
	}

	/**
	 * Create a new menu item by a given translation key. The new menu item's text will searched for
	 * <code>Internationalization.getText(applicationMenuPrefix + key)</code> and the appropiate icon
	 * <code>Internationalization.getIcon(applicationMenuPrefix + key)</code> .
	 * 
	 * @param key
	 * @return
	 */
	public static JMenuItem createMenuItem(final String key) {
		return ViewFactory.createMenuItem(key, null);
	}

	/**
	 * Create a new menu item by a given translation key. The new menu item's text will searched for
	 * <code>Internationalization.getText(applicationMenuPrefix + key)</code> and the appropiate icon
	 * <code>Internationalization.getIcon(applicationMenuPrefix + key)</code> .
	 * 
	 * @param key
	 * @param actionListener
	 * @return
	 */
	public static JMenuItem createMenuItem(final String key, final ActionListener actionListener) {
		return ViewFactory.createMenuItem(key, actionListener, ViewFactory.applicationMenuPrefix);
	}

	/**
	 * Create a new menu item by a given translation key. The new menu item's text will searched for
	 * <code>Internationalization.getText(applicationMenuPrefix + key)</code> and the appropiate icon
	 * <code>Internationalization.getIcon(applicationMenuPrefix + key)</code> .
	 * 
	 * @param key
	 * @return
	 */
	public static JMenuItem createPopupMenuItem(final String key) {
		return ViewFactory.createPopupMenuItem(key, null);
	}

	/**
	 * Create a new menu item by a given translation key. The new menu item's text will searched for
	 * <code>Internationalization.getText(applicationMenuPrefix + key)</code> and the appropiate icon
	 * <code>Internationalization.getIcon(applicationMenuPrefix + key)</code> .
	 * 
	 * @param key
	 * @param actionListener
	 * @return
	 */
	public static JMenuItem createPopupMenuItem(final String key, final ActionListener actionListener) {
		return ViewFactory.createMenuItem(key, actionListener, ViewFactory.applicationPopupMenuPrefix);
	}

	/**
	 * Create a new menu by a given translation key. The new menu's text will searched for
	 * <code>Internationalization.getText(applicationMenuPrefix + key)</code> and the appropiate icon
	 * <code>Internationalization.getIcon(applicationMenuPrefix + key)</code> .
	 * 
	 * @param key
	 * @return
	 */
	public static JMenu createPopupMenu(final String key) {
		return ViewFactory.createPopupMenu(key, ViewFactory.applicationPopupMenuPrefix);
	}

	/**
	 * Create a new menu by a given translation key. The new menu's text will searched for
	 * <code>Internationalization.getText(prefix + key)</code> and the appropiate icon
	 * <code>Internationalization.getIcon(prefix + key)</code> .
	 * 
	 * @param key
	 * @param prefix
	 * @return
	 */
	private static JMenu createPopupMenu(final String key, final String prefix) {
		final String text = Internationalization.getText(prefix + key);
		final Icon icon = Internationalization.getScaledIcon(prefix + key, 16);
		final JMenu menu = new JMenu(text);
		if (icon != null) {
			menu.setIcon(icon);
		}
		menu.setActionCommand(key);
		ViewFactory.initializeAccelerator(menu);

		return menu;
	}

	/**
	 * Create a new menu item by a given translation key. The new menu item's text will searched for
	 * <code>Internationalization.getText(prefix + key)</code> and the appropiate icon
	 * <code>Internationalization.getIcon(prefix + key)</code> .
	 * 
	 * @param key
	 * @param actionListener
	 * @param prefix
	 * @return
	 */
	private static JMenuItem createMenuItem(final String key, final ActionListener actionListener, final String prefix) {
		final String text = Internationalization.getText(prefix + key);
		final Icon icon = Internationalization.getScaledIcon(prefix + key, 16);
		final JMenuItem item = new JMenuItem(text);
		if (icon != null) {
			item.setIcon(icon);
		}
		item.setActionCommand(key);
		ViewFactory.initializeAccelerator(item);

		if (actionListener != null) {
			item.addActionListener(actionListener);
		}

		return item;
	}

	/**
	 * Create a new checkbox menu item by a given translation key. The new menu item's text will searched for
	 * <code>Internationalization.getText(applicationMenuPrefix + key)</code> and the appropiate icon
	 * <code>Internationalization.getIcon(applicationMenuPrefix + key)</code> .
	 * 
	 * @param key
	 * @return
	 */
	public static JMenuItem createCheckBoxMenuItem(final String key) {
		return ViewFactory.createCheckBoxMenuItem(key, null);
	}

	/**
	 * Create a new checkbox menu item by a given translation key. The new menu item's text will searched for
	 * <code>Internationalization.getText(applicationMenuPrefix + key)</code> and the appropiate icon
	 * <code>Internationalization.getIcon(applicationMenuPrefix + key)</code> .
	 * 
	 * @param key
	 * @param actionListener
	 * @return
	 */
	public static JMenuItem createCheckBoxMenuItem(final String key, final ActionListener actionListener) {
		return ViewFactory.createCheckBoxMenuItem(key, actionListener, ViewFactory.applicationMenuPrefix);
	}

	/**
	 * Create a new checkbox menu item by a given translation key. The new menu item's text will searched for
	 * <code>Internationalization.getText(prefix + key)</code> and the appropiate icon
	 * <code>Internationalization.getIcon(prefix + key)</code> .
	 * 
	 * @param key
	 * @param actionListener
	 * @param prefix
	 * @return
	 */
	private static JMenuItem createCheckBoxMenuItem(final String key, final ActionListener actionListener,
			final String prefix) {
		final String text = Internationalization.getText(prefix + key);
		final Icon icon = Internationalization.getScaledIcon(prefix + key, 16);
		final JCheckBoxMenuItem item = new JCheckBoxMenuItem(text);
		if (icon != null) {
			item.setIcon(icon);
		}
		item.setActionCommand(key);
		ViewFactory.initializeAccelerator(item);

		if (actionListener != null) {
			item.addActionListener(actionListener);
		}

		return item;
	}

	/**
	 * Try to get the first occurrence of the character '&'. If it exists a new string without it will created. The
	 * position of the character in the old text string is the position of the virtual key stroke charcter in the new
	 * string. The text will replaced. MENU MASK ist the default accerlerator given by the underlying os.
	 * 
	 * @param item
	 */
	public static void initializeAccelerator(final JMenuItem item) {
		String text = item.getText();

		final int keyCodeIndex = text.indexOf('&');
		if (keyCodeIndex >= 0) {
			if (keyCodeIndex == 0) {
				text = text.substring(1);
			} else {
				text = text.substring(0, keyCodeIndex) + text.substring(keyCodeIndex + 1);
			}
			item.setText(text);
			item.setAccelerator(KeyStroke.getKeyStroke(text.toUpperCase().charAt(keyCodeIndex), Main.MENU_MASK));
		}
	}

	public static void initializeAccelerator(final JButton item) {
		String text = item.getText();

		final int keyCodeIndex = text.indexOf('&');
		if (keyCodeIndex >= 0) {
			if (keyCodeIndex == 0) {
				text = text.substring(1);
			} else {
				text = text.substring(0, keyCodeIndex) + text.substring(keyCodeIndex + 1);
			}
			item.setText(text);
			item.setMnemonic(KeyStroke.getKeyStroke(text.toUpperCase().charAt(keyCodeIndex), Main.MENU_MASK)
					.getKeyCode());
		}
	}

	/**
	 * Brings up an information-message dialog titled "Message". If the message length is greater than 300 characters,
	 * the message will displayed in a scrollpane.
	 * 
	 * @param parentComponent
	 *            determines the <code>Frame</code> in which the dialog is displayed; if <code>null</code>, or if the
	 *            <code>parentComponent</code> has no <code>Frame</code>, a default <code>Frame</code> is used
	 * @param message
	 *            the <code>Object</code> to display
	 * @exception HeadlessException
	 *                if <code>GraphicsEnvironment.isHeadless</code> returns <code>true</code>
	 * @see java.awt.GraphicsEnvironment#isHeadless
	 * @see JOptionPane#showMessageDialog(Component, Object)
	 */
	public static void showMessageDialog(Component parentComponent, final Object message) throws HeadlessException {

		if (parentComponent == null) {
			parentComponent = ViewController.getApplicationView();
		}

		if ((message instanceof String) && (((String) message).length() <= 300)) {
			final JOptionPane optionPane = new NarrowOptionPane();
			optionPane.setMessage(message);
			optionPane.setMessageType(JOptionPane.INFORMATION_MESSAGE);
			final JDialog dialog = optionPane.createDialog(parentComponent, ViewController.getApplicationView()
					.getTitle());
			dialog.setVisible(true);
		} else {
			final JEditorPane content = new JEditorPane("text/plain", message.toString());
			content.setEditable(false);
			content.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

			final JScrollPane scrollpane = new JScrollPane(content);
			scrollpane.setPreferredSize(new Dimension(400, 200));

			JOptionPane.showMessageDialog(parentComponent, scrollpane);
		}
	}

	/**
	 * Add an event pressing escape will close the <code>window</code>.
	 * 
	 * @param window
	 * @param component
	 */
	public static void registerDisposeWindowOnEscape(final Window window, final JComponent component) {
		component.registerKeyboardAction(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent e) {
				window.dispose();
			}
		}, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
	}

	/**
	 * Create a scrollpane which wrapped the given table model.
	 * 
	 * @param table
	 * @return
	 */
	public static JScrollPane createScrollableTable(final TableModel tableModel) {
		final JTable table = MacWidgetFactory.createITunesTable(tableModel);
		final JScrollPane scrollPane = new JScrollPane(table);
		IAppWidgetFactory.makeIAppScrollPane(scrollPane);
		return scrollPane;
	}

	/**
	 * Create a scrollpane which wrapped the given table.
	 * 
	 * @param table
	 * @return
	 */
	public static JScrollPane createScrollableTable(final JTable table) {
		final JScrollPane scrollPane = new JScrollPane(table);
		IAppWidgetFactory.makeIAppScrollPane(scrollPane);
		return scrollPane;
	}

	/**
	 * Create a table for the given model.
	 * 
	 * @param tableModel
	 * @return
	 */
	public static JTable createTable(final TableModel tableModel) {
		return MacWidgetFactory.createITunesTable(tableModel);
	}

	/**
	 * Create a sortable table for the given model.
	 * 
	 * @param table
	 * @return
	 */
	public static void makeSortableTable(final JTable table) {
		table.setAutoCreateRowSorter(true);
		final TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(table.getModel());

		// These four lines will create and attach an empty SortDelegate object. This step is requeried let
		// ITunesTableUI generate small arrows as sort directions.
		final TableUtils.SortDelegate sortDelegate = new TableUtils.SortDelegate() {
			public void sort(final int columnModelIndex, final TableUtils.SortDirection sortDirection) {}
		};
		TableUtils.makeSortable(table, sortDelegate);

		table.setRowSorter(sorter);
	}
}
