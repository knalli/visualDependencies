package de.unibonn.inf.dbdependenciesui.ui.views.common.graph.renderer;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Observable;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseObject;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseProcedure;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseTable;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseTrigger;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseView;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseObject.ViewState;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.ProcedureSchema;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.TriggerSchema;
import de.unibonn.inf.dbdependenciesui.misc.Icons;
import de.unibonn.inf.dbdependenciesui.misc.Internationalization;
import de.unibonn.inf.dbdependenciesui.ui.factory.ViewFactory;

/**
 * This is the panel compontent for a jung graph visualization. The visual component is a rectangle displaying any type
 * of the database models: {@link DatabaseTable}, {@link DatabaseTrigger} and {@link DatabaseView}. The renderer uses a
 * slight gradient, the object's title and a type specific main color. Additionally, the component is available to show
 * additional information. That is disabled because jung does not support this right now (and in ways of more
 * efficiency).
 * 
 * @author Andre Kasper
 * @author Jan Philipp
 */
@SuppressWarnings("serial")
public class DatabaseObjectComponent extends JLayeredPane {

	protected DatabaseObject userObject = null;

	protected JPanel header = null;

	protected JPanel headerControls = null;

	protected JPanel body = null;

	protected JLabel lblTitle;

	protected JLabel lblType;

	protected String keyIconTable;

	protected String keyIconTrigger;

	protected String keyIconProcedure;

	protected String keyIconView;

	protected String keyIconBack;

	protected String keyIconInformation;

	protected String keyIconData;

	protected String keyIconTriggers;

	protected String keyIconClose;

	protected TranslucentButton btnBack;

	protected TranslucentButton btnInformation;

	protected TranslucentButton btnData;

	protected TranslucentButton btnTriggers;

	protected TranslucentButton btnClose;

	protected int iconTypeSize;

	protected int iconControlSize;

	protected int titleSize;

	protected int width;

	protected int height;

	protected int iconPadding;

	protected int iconControlsPadding;

	protected String tooltipBack;

	protected String tooltipInformation;

	protected String tooltipData;

	protected String tooltipTriggers;

	protected String tooltipClose;

	protected MyActionListener actionListener;

	protected JComponent tableData;

	protected JLabel frontLabel;

	protected JLabel tableDataPreview;

	protected ButtonGroup group;

	protected List<String> recursiveTriggers = new ArrayList<String>();

	protected final String appKey = "application.graph.hierarchicalview.";

	public DatabaseObjectComponent() {
		loadIcons();
		loadStrings();
		initialize();
	}

	protected void loadIcons() {
		keyIconTable = "icons/table.png";
		keyIconView = "icons/view.png";
		keyIconTrigger = "icons/trigger.png";
		keyIconProcedure = "icons/procedure.png";
		keyIconBack = "icons/back.png";
		keyIconInformation = "icons/info.png";
		keyIconData = "icons/txt.png";
		keyIconTriggers = "icons/trigger.png";
		keyIconClose = "icons/button_cancel.png";
	}

	protected void loadStrings() {
		tooltipBack = "Back";
		tooltipInformation = "Information";
		tooltipData = "Data";
		tooltipTriggers = "Triggers";
		tooltipClose = "Close";
	}

	protected void initialize() {
		actionListener = new MyActionListener();

		setPreferredSize(new Dimension(100, 30));
		this.setSize(new Dimension(100, 30));

		setOpaque(true);
		setBackground(Color.gray);

		this.add(getHeader(), 0);
		this.add(getBody(), 0);
		// this.add(getHeaderControls(), 1);
	}

	public DatabaseObject getUserObject() {
		return userObject;
	}

	public void resetUserObject() {
		setUserObject(userObject);
	}

	public void setUserObject(final DatabaseObject obj) {
		setUserObject(obj, false);
	}

	public void setUserObject(final DatabaseObject obj, final boolean picked) {

		userObject = obj;

		width = (int) getPreferredSize().getWidth();
		height = (int) getPreferredSize().getHeight();

		// Compute the icon type size which should be 30% of the component's
		// height.
		iconTypeSize = (int) (height * 0.3);

		// Compute the icon controls size which should be 20& of the component's
		// height;
		iconControlSize = (int) (height * 0.2);

		// Compute the font size as fonzt size which should be 40% of the
		// component's height.
		titleSize = (int) (height * 0.2);

		// Compute the icon's border (left/right padding) which should be 10% of
		// the component's height.
		iconPadding = (int) (height * 0.1);

		// Compute the icon's border (left/right padding) which should be 10% of
		// the component's height.
		iconControlsPadding = (int) (height * 0.05);

		Color backgroundColor = null;
		String keyIcon = null;

		if (isView(obj)) {
			backgroundColor = Color.orange;
			keyIcon = keyIconView;
			if ((headerControls != null) && !btnTriggers.isVisible()) {
				group.add(btnTriggers);
				btnTriggers.setVisible(true);
			}
		} else if (isTrigger(obj)) {
			if (checkMutatingTable((DatabaseTrigger) obj)) {
				backgroundColor = new Color(233, 139, 52); // dark red-orange
			} else if (recursiveTriggers.contains(obj.getTitle())) {
				backgroundColor = Color.red.brighter().brighter(); // red
			} else {
				backgroundColor = new Color(108, 197, 69); // green (default trigger)
			}

			if ((headerControls != null) && btnTriggers.isVisible()) {
				group.remove(btnTriggers);
				btnTriggers.setVisible(false);
			}
			keyIcon = keyIconTrigger;
		} else if (isTable(obj)) {
			backgroundColor = new Color(75, 170, 228); // blue
			keyIcon = keyIconTable;
			if ((headerControls != null) && !btnTriggers.isVisible()) {
				group.add(btnTriggers);
				btnTriggers.setVisible(true);
			}
		} else if (isProcedure(obj)) {
			backgroundColor = new Color(180, 20, 180); // violett
			keyIcon = keyIconProcedure;
			if ((headerControls != null) && !btnTriggers.isVisible()) {
				group.add(btnTriggers);
				btnTriggers.setVisible(true);
			}
		} else {
			backgroundColor = Color.gray;
			keyIcon = null;
			if ((headerControls != null) && !btnTriggers.isVisible()) {
				group.add(btnTriggers);
				btnTriggers.setVisible(true);
			}
		}

		// Make colors brighter if selected/picked.
		if (picked) {
			backgroundColor = backgroundColor.brighter().brighter();
		}

		setBackground(backgroundColor);

		lblType.setIcon(Icons.getScaledIcon(keyIcon, iconTypeSize));
		lblType.setBorder(BorderFactory.createEmptyBorder(0, iconPadding, 0, iconPadding));

		if (headerControls != null) {
			headerControls.setBorder(BorderFactory.createEmptyBorder(iconControlsPadding, iconControlsPadding,
					iconControlsPadding, iconControlsPadding));

			final Border border = BorderFactory.createEmptyBorder(0, 0, 0, iconControlsPadding);
			btnBack.setIcon(Icons.getScaledIcon(keyIconBack, iconControlSize));
			btnBack.setBorder(border);
			btnInformation.setIcon(Icons.getScaledIcon(keyIconInformation, iconControlSize));
			btnInformation.setBorder(border);
			btnData.setIcon(Icons.getScaledIcon(keyIconData, iconControlSize));
			btnData.setBorder(border);
			btnTriggers.setIcon(Icons.getScaledIcon(keyIconTriggers, iconControlSize));
			btnTriggers.setBorder(border);
			btnClose.setIcon(Icons.getScaledIcon(keyIconClose, iconControlSize));
		}

		lblTitle.setText("<html><b>" + obj.getTitle());

		if (headerControls != null) {
			getBody().removeAll();
			switch (obj.getState()) {
			case INFORMATION:
				if (!btnBack.isVisible()) {
					group.add(btnBack);
					btnBack.setVisible(true);
				}
				break;
			case DATA:
				if (height >= 80) {
					getBody().add(this.getTableData(), BorderLayout.CENTER);
				} else {
					getBody().add(this.getTableDataPreview(), BorderLayout.CENTER);
					this.getTableDataPreview().setText(
							"<html><center>Please zoom in for a<br/> preview of this object's data.");
				}
				if (!btnBack.isVisible()) {
					group.add(btnBack);
					btnBack.setVisible(true);
				}
				break;
			case TRIGGERS:
				if (!btnBack.isVisible()) {
					group.add(btnBack);
					btnBack.setVisible(true);
				}
				break;
			case NORMAL:
				getBody().add(getFront(), BorderLayout.CENTER);
				if (btnBack.isVisible()) {
					group.remove(btnBack);
					btnBack.setVisible(false);
				}
			default:
			}
			try {
				// getBody().updateUI();
			} catch (final Throwable e) {}
		} else {
			if (!Arrays.asList(getComponents()).contains(getFront())) {
				getBody().add(getFront(), BorderLayout.CENTER);
			}
		}

		String text = null;
		if (isView(userObject)) {
			if (((DatabaseView) userObject).isMaterialized()) {
				text = Internationalization.getTextFormatted(appKey + "vertex.info.mview", ((DatabaseView) userObject)
						.getDdlSchemaObject().getColumns().size());
			} else {
				text = Internationalization.getTextFormatted(appKey + "vertex.info.view", ((DatabaseView) userObject)
						.getDdlSchemaObject().getColumns().size());
			}
		} else if (isTable(userObject)) {
			text = Internationalization.getTextFormatted(appKey + "vertex.info.table", ((DatabaseTable) userObject)
					.getDdlSchemaObject().getColumns().size());
		} else if (userObject instanceof DatabaseTrigger) {
			final TriggerSchema schema = ((DatabaseTrigger) userObject).getTriggerSchemaObject();
			if (checkMutatingTable((DatabaseTrigger) userObject)) {
				text = Internationalization.getTextFormatted(appKey + "vertex.info.triggermutated", schema
						.getTableName());
			} else {
				text = Internationalization.getTextFormatted(appKey + "vertex.info.trigger", schema.getTableName());
			}
		} else if (userObject instanceof DatabaseProcedure) {
			final ProcedureSchema schema = ((DatabaseProcedure) userObject).getProcedureSchemaObject();
			text = schema.getType().equalsIgnoreCase("PROCEDURE") ? Internationalization.getText(appKey
					+ "vertex.info.procedure") : Internationalization.getText(appKey + "vertex.info.function");
		}
		getFront().setText("<html><center>" + text);

		recalibrate();
	}

	protected boolean checkMutatingTable(final DatabaseTrigger trigger) {
		final TriggerSchema schema = trigger.getTriggerSchemaObject();
		if (schema.isAfterRow()) {
			if (schema.getTableName() != null) {
				final List<String> affectedTables = schema.getAffectedTables();
				final List<String> usedTables = schema.getUsedTables();
				if (affectedTables.contains(schema.getTableName()) || usedTables.contains(schema.getTableName())) { return true; }
			}
		}
		return false;
	}

	public void setRecursiveTrigger(final List<String> triggerName) {
		recursiveTriggers = triggerName;
	}

	protected JLabel getFront() {
		if (frontLabel == null) {
			frontLabel = new JLabel();
		}

		return frontLabel;
	}

	protected JLabel getTableDataPreview() {
		return this.getTableDataPreview(true);
	}

	protected JLabel getTableDataPreview(final boolean force) {
		if (tableDataPreview == null) {
			tableDataPreview = new JLabel();
		}
		return tableDataPreview;
	}

	protected JComponent getTableData() {
		return this.getTableData(true);
	}

	/**
	 * Create and return a swing component display the table data of this object.
	 * 
	 * @param force
	 *            if true and already the table exists the swing component will rebuild
	 * @return
	 */
	protected JComponent getTableData(final boolean force) {
		if (force && (tableData == null)) {
			final TableModel model = new DefaultTableModel(new Object[][] {
					new Object[] {
							1, 2, 3, 4 }, new Object[] {
							1, 2, 3, 4 }, new Object[] {
							1, 2, 3, 4 }, new Object[] {
							1, 2, 3, 4 }, new Object[] {
							1, 2, 3, 4 } }, new Object[] {
					"Spalte A", "Spalte B", "Spalte 3", "Spalte 4" }) {

				@Override
				public boolean isCellEditable(final int row, final int column) {
					return false;
				}

			};
			final JTable table = ViewFactory.createTable(model);
			ViewFactory.makeSortableTable(table);
			table.setOpaque(false);
			tableData = ViewFactory.createScrollableTable(table);
			tableData.setOpaque(false);
		}
		return tableData;
	}

	/**
	 * Create and return a header component.
	 * 
	 * @return
	 */
	protected JPanel getHeader() {
		if (header == null) {
			header = new JPanel();
			header.setLayout(new BorderLayout());
			header.setOpaque(false);

			lblType = new JLabel((Icon) null);
			lblTitle = new JLabel("Unknown table name");

			header.add(lblType, BorderLayout.WEST);
			header.add(lblTitle, BorderLayout.CENTER);
		}
		return header;
	}

	/**
	 * Create and return a body component.
	 * 
	 * @return
	 */
	protected JPanel getBody() {
		if (body == null) {
			body = new JPanel();
			body.setLayout(new BorderLayout());
			body.setOpaque(false);
		}
		return body;
	}

	/**
	 * Create and return a header controls section component.
	 * 
	 * @return
	 */
	protected JPanel getHeaderControls() {
		if (headerControls == null) {
			headerControls = new JPanel() {
				@Override
				public void paintComponent(final Graphics g) {

					// Set rendering mode using antialiase.
					final Graphics2D g2 = (Graphics2D) g;
					g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

					// Paint a semitransparent (70%) white base "layer". The
					// height and width is doubled because the
					// rect has round corners.
					final Color ppColor = new Color(255, 255, 255, 70);
					g2.setColor(ppColor);
					final int width = (int) getPreferredSize().getWidth();
					final int height = (int) getPreferredSize().getHeight();
					final int arc = 4 * iconControlsPadding;
					g2.fillRoundRect(0, -height, 2 * width, 2 * height, arc, arc);

					// Delegate painting the actual components of the panel.
					super.paintComponent(g2);
				}
			};
			headerControls.setLayout(new GridBagLayout());
			headerControls.setOpaque(false);

			final Border border = BorderFactory.createEmptyBorder();

			group = new ButtonGroup();

			btnBack = new TranslucentButton((Icon) null);
			btnBack.setToolTipText(tooltipBack);
			btnBack.setBorder(border);
			btnBack.setActionCommand(Command.BACK);
			btnBack.addActionListener(actionListener);
			group.add(btnBack);

			btnInformation = new TranslucentButton((Icon) null);
			btnInformation.setToolTipText(tooltipInformation);
			btnInformation.setBorder(border);
			btnInformation.setActionCommand(Command.INFORMATION);
			btnInformation.addActionListener(actionListener);
			group.add(btnInformation);

			btnData = new TranslucentButton((Icon) null);
			btnData.setToolTipText(tooltipData);
			btnData.setBorder(border);
			btnData.setActionCommand(Command.DATA);
			btnData.addActionListener(actionListener);
			group.add(btnData);

			btnTriggers = new TranslucentButton((Icon) null);
			btnTriggers.setToolTipText(tooltipTriggers);
			btnTriggers.setBorder(border);
			btnTriggers.setActionCommand(Command.TRIGGERS);
			btnTriggers.addActionListener(actionListener);
			group.add(btnTriggers);

			btnClose = new TranslucentButton((Icon) null);
			btnClose.setToolTipText(tooltipClose);
			btnClose.setBorder(border);
			btnClose.setActionCommand(Command.CLOSE);
			btnClose.addActionListener(actionListener);
			group.add(btnClose);

			final GridBagConstraints column1 = new GridBagConstraints();
			final GridBagConstraints column2 = new GridBagConstraints();
			final GridBagConstraints column3 = new GridBagConstraints();
			final GridBagConstraints column4 = new GridBagConstraints();
			final GridBagConstraints column5 = new GridBagConstraints();

			column1.gridx = 0;
			column1.gridy = 0;
			column2.gridx = 1;
			column2.gridy = 0;
			column3.gridx = 2;
			column3.gridy = 0;
			column4.gridx = 3;
			column4.gridy = 0;
			column5.gridx = 4;
			column5.gridy = 0;

			headerControls.add(btnBack, column1);
			headerControls.add(btnInformation, column2);
			headerControls.add(btnData, column3);
			headerControls.add(btnTriggers, column4);
			headerControls.add(btnClose, column5);
		}
		return headerControls;
	}

	/**
	 * Recalibrate the swing elements' sizes, locations and orders for the new layout/setup and size of the main window.
	 */
	protected void recalibrate() {
		final int headerHeight = (int) (height * 0.3);
		final int bodyHeight = height - headerHeight;
		final int bodyX = getBody().getX();

		getHeader().setSize(new Dimension(width, headerHeight));
		getHeader().setPreferredSize(new Dimension(width, headerHeight));

		getBody().setLocation(bodyX, headerHeight);
		getBody().setSize(new Dimension(width, bodyHeight));
		getBody().setPreferredSize(new Dimension(width, bodyHeight));

		if (headerControls != null) {
			final int cntIcons = group.getButtonCount();
			final int controlsWidth = cntIcons * iconControlSize + (cntIcons + 1) * iconControlsPadding;
			final int controlsHeight = iconControlSize + 2 * iconControlsPadding;
			getHeaderControls().setSize(new Dimension(controlsWidth, controlsHeight));
			final int controlsX = width - controlsWidth;
			final int controlsY = getHeaderControls().getY();
			getHeaderControls().setLocation(controlsX, controlsY);

			switch (userObject.getState()) {
			case INFORMATION:
				btnInformation.setSelected(true);
				break;
			case DATA:
				btnData.setSelected(true);
				break;
			case TRIGGERS:
				btnTriggers.setSelected(true);
				break;
			default:
				btnInformation.setSelected(false);
				btnData.setSelected(false);
				btnTriggers.setSelected(false);
				btnClose.setSelected(false);
				break;
			}
		}

		lblTitle.setFont(new Font("Sans-Serif", Font.PLAIN, titleSize));

		if (getFront() != null) {
			getFront().setPreferredSize(new Dimension(width, bodyHeight));
			getFront().setFont(new Font("Sans-Serif", Font.PLAIN, (int) (titleSize * 0.9)));
			getFront().setVerticalAlignment(SwingConstants.TOP);
			getFront().setHorizontalAlignment(SwingConstants.CENTER);
			getFront().setHorizontalTextPosition(SwingConstants.CENTER);
		}

		if (headerControls != null) {
			if (this.getTableDataPreview(false) != null) {
				this.getTableDataPreview().setPreferredSize(new Dimension(width, bodyHeight));
				this.getTableDataPreview().setFont(new Font("Sans-Serif", Font.PLAIN, (int) (titleSize * 0.7)));
				this.getTableDataPreview().setHorizontalAlignment(SwingConstants.CENTER);
				this.getTableDataPreview().setHorizontalTextPosition(SwingConstants.CENTER);
			}
		}
	}

	@Override
	public void paintComponent(final Graphics g) {
		try {
			super.paintComponent(g);
			final Graphics2D g2d = (Graphics2D) g;
			final int w = getWidth();
			final int h = getHeight();

			final Color color1 = getBackground();
			final Color color2 = color1.darker();

			// Paint a gradient from top to bottom
			final GradientPaint gp = new GradientPaint(0, 0, color1, 0, h, color2);
			g2d.setPaint(gp);
			g2d.fillRect(0, 0, w, h);
		} catch (final Throwable e) {
			// Print out the exception, but forget it then.
			System.err.println("Just catch an exception: " + e.getLocalizedMessage());
		}
	}

	public String getTitleText() {
		return lblTitle.getText();
	}

	public Observable getObservable() {
		return actionListener;
	}

	/**
	 * This class is a special variant of a standard Swing {@link JToggleButton}. This button is complete
	 * transparent/translucent for the given alpha value. Please note the three different possible values for selected,
	 * normal and disabled.
	 */
	final public static class TranslucentButton extends JToggleButton {

		protected float alpha = 0.7f;

		protected float disabledAlpha = 0.2f;

		protected float selectedAlpha = 1f;

		public TranslucentButton(final Icon image) {
			super(image);
		}

		public void setActionCommand(final Command mode) {
			this.setActionCommand(mode.toString());
		}

		@Override
		public void setIcon(final Icon defaultIcon) {
			super.setIcon(defaultIcon);
		}

		public void setAlpha(final float alpha) {
			this.alpha = alpha;
		}

		public void setDisabledAlpha(final float alpha) {
			disabledAlpha = alpha;
		}

		public void setSelectedAlpha(final float alpha) {
			selectedAlpha = alpha;
		}

		@Override
		public void paint(final Graphics g) {

			try {
				float alpha = this.alpha;
				if (!isEnabled()) {
					alpha = disabledAlpha;
				} else if (isSelected()) {
					alpha = selectedAlpha;
				}

				final Graphics2D g2 = (Graphics2D) g;
				final Composite oldComposite = g2.getComposite();
				g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
				super.paint(g);
				g2.setComposite(oldComposite);
			} catch (final Throwable e) {}
		}
	}

	/**
	 * This class is a special variant of a standard Swing {@link JLabel}. This button is complete
	 * transparent/translucent for the given alpha value.
	 */
	final public static class TranslucentLabel extends JLabel {

		protected float alpha = 1f;

		public TranslucentLabel(final Icon image) {
			super(image);
		}

		public void setAlpha(final float alpha) {
			this.alpha = alpha;
		}

		@Override
		public void paint(final Graphics g) {
			try {
				final Graphics2D g2 = (Graphics2D) g;
				final Composite oldComposite = g2.getComposite();
				g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
				super.paint(g);
				g2.setComposite(oldComposite);
			} catch (final Throwable e) {}
		}
	}

	/**
	 * Internal {@link ActionListener} used by DatabaseObjectComponent.
	 */
	final public static class MyActionListener extends Observable implements ActionListener {
		@Override
		public void actionPerformed(final ActionEvent e) {
			Object component = e.getSource();
			if (component instanceof JComponent) {
				component = ((JComponent) component).getParent();
				if (component instanceof JComponent) {
					component = ((JComponent) component).getParent();
					if (component instanceof DatabaseObjectComponent) {
						final String actionCommand = e.getActionCommand();
						final DatabaseObject userObject = ((DatabaseObjectComponent) component).getUserObject();
						final Command command = Command.valueOf(actionCommand);

						switch (command) {
						case INFORMATION:
							if (userObject.getState().equals(ViewState.INFORMATION)) {
								userObject.setState(ViewState.NORMAL);
							} else {
								userObject.setState(ViewState.INFORMATION);
							}
							break;
						case DATA:
							if (userObject.getState().equals(ViewState.DATA)) {
								userObject.setState(ViewState.NORMAL);
							} else {
								userObject.setState(ViewState.DATA);
							}
							break;
						case TRIGGERS:
							if (userObject.getState().equals(ViewState.TRIGGERS)) {
								userObject.setState(ViewState.NORMAL);
							} else {
								userObject.setState(ViewState.TRIGGERS);
							}
							break;
						case CLOSE:
							if (userObject.getState().equals(ViewState.CLOSED)) {
								userObject.setState(ViewState.NORMAL);
							} else {
								userObject.setState(ViewState.CLOSED);
							}
							break;
						case BACK:
							userObject.setState(ViewState.NORMAL);
							break;
						}
						setChanged();
						this.notifyObservers(component);
					} else {
						System.err.println("No DatabaseObjectComponent (3)");
					}
				} else {
					System.err.println("No JComponent (2)");
				}
			} else {
				System.err.println("No JComponent (1)");
			}
		}
	}

	public boolean isTable(final DatabaseObject object) {
		return (object instanceof DatabaseTable);
	}

	public boolean isView(final DatabaseObject object) {
		return (object instanceof DatabaseView);
	}

	public boolean isTrigger(final DatabaseObject object) {
		return (object instanceof DatabaseTrigger);
	}

	public boolean isProcedure(final DatabaseObject object) {
		return (object instanceof DatabaseProcedure);
	}
}
