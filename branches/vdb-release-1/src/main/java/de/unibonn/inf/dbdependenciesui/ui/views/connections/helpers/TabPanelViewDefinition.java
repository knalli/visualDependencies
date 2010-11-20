package de.unibonn.inf.dbdependenciesui.ui.views.connections.helpers;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.text.JTextComponent;

import org.javabuilders.BuildResult;
import org.javabuilders.swing.SwingJavaBuilder;

import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseTable;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseView;

public class TabPanelViewDefinition extends JPanel {

	private static final long serialVersionUID = 1540576067694355415L;

	protected DatabaseView view = null;

	protected final BuildResult result;

	protected JTextArea ctnBody;

	public TabPanelViewDefinition(final DatabaseTable view) {
		super();
		this.view = (DatabaseView) view;
		result = SwingJavaBuilder.build(this);
		postInitialize();
		makeElementsNotEditable();
	}

	private void postInitialize() {
		if (view != null) {
			ctnBody.setRows(16);
			ctnBody.setText(view.getSelectStatement());
		}
	}

	protected void makeElementsNotEditable() {
		final ActionListener actionListener = new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				if (e.getSource() instanceof JCheckBox) {
					final JCheckBox checkbox = (JCheckBox) e.getSource();
					checkbox.setSelected(!checkbox.isSelected());
				}
			}
		};

		for (final String key : new String[] {
			"ctnBody" }) {
			final Object component = result.get(key);
			if ((component != null)) {
				if (component instanceof JCheckBox) {
					((JCheckBox) component).addActionListener(actionListener);
				} else if (component instanceof JToggleButton) {
					((JToggleButton) component).setEnabled(false);
				} else if (component instanceof JButton) {
					((JButton) component).setEnabled(false);
				} else if (component instanceof JTextComponent) {
					((JTextComponent) component).setEditable(false);
				}
			}
		}
	}
}
