package de.unibonn.inf.dbdependenciesui.ui.factory;

import javax.swing.JOptionPane;

/**
 * This is special narrow wide {@link JOptionPane}. In opposite to the standard pane, this one will break lines if they
 * reach the charsPerLine size.
 */
public class NarrowOptionPane extends JOptionPane {
	private static final long serialVersionUID = -5271821084143664642L;

	private final int charsPerLine;

	public NarrowOptionPane() {
		this(80);
	}

	public NarrowOptionPane(final int charsPerLine) {
		super();
		this.charsPerLine = charsPerLine;
	}

	@Override
	public int getMaxCharactersPerLineCount() {
		return charsPerLine;
	}
}
