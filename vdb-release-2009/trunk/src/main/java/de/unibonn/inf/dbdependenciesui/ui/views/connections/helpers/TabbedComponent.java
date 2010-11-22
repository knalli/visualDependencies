package de.unibonn.inf.dbdependenciesui.ui.views.connections.helpers;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicButtonUI;

import de.unibonn.inf.dbdependenciesui.Configuration;
import de.unibonn.inf.dbdependenciesui.ui.helpers.SystemTools;

public class TabbedComponent extends JPanel {

	protected static final Logger log = Logger.getLogger(Configuration.LOGGER);

	private static final long serialVersionUID = 2061145283075468994L;

	private final ConnectionTabbedPane tabbedPane;

	private final String name, connection, element;

	private final Icon icon;

	private final String title;

	private final String tooltip;

	private static MouseListener buttonMouseListener;

	private MouseListener mouseListener;

	public TabbedComponent(final ConnectionTabbedPane tabbedPane, final String name, final String connection,
			final String element, final String title, final String tooltip, final Icon icon) {

		super(new BorderLayout());

		this.tabbedPane = tabbedPane;
		this.name = name;
		this.element = element;
		this.connection = connection;

		this.icon = icon;
		this.title = title;
		this.tooltip = tooltip;

		setOpaque(false);
		initialize();
	}

	private void initialize() {
		if (buttonMouseListener == null) {
			buttonMouseListener = new MyButtonMouseListener();
		}
		mouseListener = new MyMouseListener();

		final JLabel label = new JLabel(title);
		// Use lesser font size if mac system is using small tabs.
		if (UIManager.getDefaults().getBoolean("TabbedPane.useSmallLayout")) {
			label.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
		}
		label.setToolTipText(tooltip);
		label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
		label.addMouseListener(mouseListener);

		final JButton tabButton = new TabButton();

		if (icon != null) {
			this.add(new JLabel(icon), BorderLayout.WEST);
		}
		this.add(label, BorderLayout.CENTER);
		this.add(tabButton, BorderLayout.EAST);

		setMinimumSize(new Dimension(100, 15));
		setMaximumSize(new Dimension(150, 15));
		setBorder(null);
	}

	private class MyButtonMouseListener extends MouseAdapter {
		@Override
		public void mouseEntered(final MouseEvent e) {
			final Component component = e.getComponent();
			if (component instanceof JButton) {
				final JButton button = (JButton) component;
				button.setBorderPainted(true);
			}
		}

		@Override
		public void mouseExited(final MouseEvent e) {
			final Component component = e.getComponent();
			if (component instanceof JButton) {
				final JButton button = (JButton) component;
				button.setBorderPainted(false);
			}
		}
	};

	private class MyMouseListener extends MouseAdapter {
		@Override
		public void mouseReleased(final MouseEvent e) {
			tabbedPane.setSelectedTab(connection, name, element);
		}

		@Override
		public void mousePressed(final MouseEvent e) {
			if (e.getButton() == 3) {
				final TabPanelPopupMenu menu = new TabPanelPopupMenu();
				menu.setTabTitle(tooltip);

				menu.show(TabbedComponent.this, e.getX(), e.getY());
			}
		}
	}

	/**
	 * This class creates a small icon button which gets events like click (close) and hover (impressing itself).
	 */
	private class TabButton extends JButton implements ActionListener {

		private static final long serialVersionUID = 3774683680491180844L;

		public TabButton() {
			if (SystemTools.isMac()) {
				initializeMac();
			} else {
				initialize();
			}
		}

		/**
		 * Initialize the button defined as a standard borderless rectangle w/ an icon. This will use a painted cross.
		 */
		private void initialize() {
			final int size = 15;
			setPreferredSize(new Dimension(size, size));
			setFocusable(false);
			setBorder(BorderFactory.createEtchedBorder());
			this.setUI(new BasicButtonUI());
			setBorderPainted(false);
			// Making nice rollover effect
			// we use the same listener for all buttons
			addMouseListener(TabbedComponent.buttonMouseListener);
			setRolloverEnabled(true);
			setContentAreaFilled(false);
			// Close the proper tab by clicking the button
			addActionListener(this);
		}

		/**
		 * Initialize the button defined as a standard borderless rectangle w/ an icon. This will use an internal
		 * NSImage-Icon.
		 */
		private void initializeMac() {
			final Toolkit toolkit = Toolkit.getDefaultToolkit();
			final Image image = toolkit.getImage("NSImage://NSStopProgressTemplate");
			final Image scaled = image.getScaledInstance(8, 8, Image.SCALE_SMOOTH);
			setIcon(new ImageIcon(scaled));
			setOpaque(false);
			setContentAreaFilled(false);
			setBorder(BorderFactory.createEtchedBorder());
			setBorderPainted(false);
			addActionListener(this);
		}

		@Override
		public void actionPerformed(final ActionEvent arg0) {
			tabbedPane.removeTab(connection, name, element);
		}

		/**
		 * Paint a small cross -- only used in non-mac systems.
		 */
		@Override
		protected void paintComponent(final Graphics g) {
			super.paintComponent(g);

			// Mac OS X has a local NSImage which was added via setIcon. This
			// code is not necessary.
			if (!SystemTools.isMac()) {
				final Graphics2D g2 = (Graphics2D) g.create();
				// shift the image for pressed buttons
				if (getModel().isPressed()) {
					g2.translate(1, 1);
				}
				g2.setStroke(new BasicStroke(2));
				g2.setColor(Color.BLACK);
				if (getModel().isRollover()) {
					g2.setColor(Color.RED);
				}
				final int delta = 10;
				g2.drawLine(delta, delta, getWidth() - delta - 1, getHeight() - delta - 1);
				g2.drawLine(getWidth() - delta - 1, delta, delta, getHeight() - delta - 1);
				g2.dispose();
			}
		}
	}
}
