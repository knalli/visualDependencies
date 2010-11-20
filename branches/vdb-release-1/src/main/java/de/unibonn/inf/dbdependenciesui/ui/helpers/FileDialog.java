package de.unibonn.inf.dbdependenciesui.ui.helpers;

import java.io.File;
import java.io.FilenameFilter;


/**
 * A wrapper class for native file dialogs. This wrapper uses the awt dialog on
 * windows or osx (default). Because of the linux implementation, linux has only
 * a "good" swing support.
 * 
 * @author Jan Philipp <knallisworld@googlemail.com>
 */
public class FileDialog {
	/**
	 * selected view mode - load (open) or save
	 * 
	 * @uml.property name="mode"
	 * @uml.associationEnd
	 */
	private Mode mode = FileDialog.Mode.LOAD;

	/**
	 * preselected framework - awt or swing
	 * 
	 * @uml.property name="framework"
	 * @uml.associationEnd
	 */
	private Framework framework = FileDialog.Framework.SWING;

	/**
	 * associated component (parent)
	 */
	private java.awt.Frame parent = null;

	/**
	 * optional custom title of dialog
	 */
	private String title = null;

	/**
	 * optional extension for simple filefilter
	 */
	private String extension = null;

	/**
	 * optional extension description
	 */
	private String extensionDescription;

	/**
	 * specify, if the selected framework can be changed in order to get the os'
	 * native one
	 */
	private boolean useOnlyDefault = false;

	/**
	 * stores the selected file - for reading or writing
	 */
	private File selectedFile;

	/**
	 * the awt FileDialog instance
	 */
	private java.awt.FileDialog awtFileDialog = null;

	/**
	 * the swing JFileChooser instance
	 */
	private javax.swing.JFileChooser swingFileDialog = null;

	public FileDialog(final String extension, final String extensionDescription) {
		this(null, "", FileDialog.Mode.LOAD, extension, extensionDescription);
	}

	public FileDialog(final java.awt.Frame parent) {
		this(parent, "", FileDialog.Mode.LOAD);
	}

	public FileDialog(final java.awt.Frame parent, final String title) {
		this(parent, title, FileDialog.Mode.LOAD);
	}

	public FileDialog(final java.awt.Frame parent, final String title,
			final FileDialog.Mode mode) {
		this.parent = parent;
		this.title = title;
		this.mode = mode;
	}

	/**
	 * Creates a FileDialog Wrapperinstance.
	 * 
	 * @param parent
	 * @param title
	 * @param mode
	 * @param extension
	 * @param extensionDescription
	 */
	public FileDialog(final java.awt.Frame parent, final String title,
			final FileDialog.Mode mode, final String extension,
			final String extensionDescription) {
		this.parent = parent;
		this.title = title;
		this.mode = mode;
		this.extension = extension;
		this.extensionDescription = extensionDescription;
	}

	/**
	 * Redefines the view mode.
	 * 
	 * @param mode
	 * @uml.property name="mode"
	 */
	public void setMode(final FileDialog.Mode mode) {
		this.mode = mode;
	}

	/**
	 * Redefines the extensions.
	 * 
	 * @param extension
	 * @param description
	 */
	public void setExtension(final String extension, final String description) {
		this.extension = extension;
		this.extensionDescription = description;
	}

	/**
	 * Removes the internal instance and forces creating a new one next time
	 * useing {@link #showFileDialog()}
	 */
	public void clearInstance() {
		this.awtFileDialog = null;
		this.swingFileDialog = null;
	}

	/**
	 * Redefines the behaviour automatically determine the best framework
	 * choose.
	 * 
	 * @param setting
	 */
	public void useOnlyDefault(final boolean setting) {
		this.useOnlyDefault = setting;
	}

	public File showOpenFileDialog() {
		this.setMode(Mode.LOAD);
		return this.showFileDialog();
	}

	public File showSaveFileDialog() {
		this.setMode(Mode.SAVE);
		return this.showFileDialog();
	}

	/**
	 * Display the file dialog and return the selected file.
	 * 
	 * The return value is null if the dialog returns no selection.
	 * 
	 * @param mode
	 * @param framework
	 * @return
	 */
	public File showFileDialog(final Mode mode, final Framework framework) {
		this.mode = mode;
		this.framework = framework;
		return this.showFileDialog();
	}

	/**
	 * Display the file dialog and return the selected file.
	 * 
	 * The return value is null if the dialog returns no selection.
	 * 
	 * @return
	 */
	public File showFileDialog() {

		// On windows or mac platforms, it can use awt's dialog for a better
		// L&F.
		if (this.useOnlyDefault == false) {
			if (SystemTools.isMac() || SystemTools.isWindows()) {
				this.framework = Framework.AWT;
			}
		}

		switch (this.framework) {
		case AWT:
			String file = null;
			if (this.awtFileDialog == null) {
				this.awtFileDialog = this.createFileDialog();
			}
			this.awtFileDialog.setFile(null);

			switch (this.mode) {
			case LOAD:
				this.awtFileDialog.setMode(java.awt.FileDialog.LOAD);
				break;
			case SAVE:
				this.awtFileDialog.setMode(java.awt.FileDialog.SAVE);
				break;
			}

			this.awtFileDialog.setVisible(true);
			file = this.awtFileDialog.getFile();
			if (file != null) {
				this.selectedFile = new File(this.awtFileDialog.getDirectory()
						+ "/" + file);
			}
			break;
		case SWING:
			int returnVal = javax.swing.JFileChooser.CANCEL_OPTION;

			if (this.swingFileDialog == null) {
				this.swingFileDialog = this.createFileChooser();
			}

			switch (this.mode) {
			case LOAD:
				returnVal = this.swingFileDialog.showOpenDialog(this.parent);
				break;
			case SAVE:
				returnVal = this.swingFileDialog.showSaveDialog(this.parent);
				break;
			}

			if (returnVal == javax.swing.JFileChooser.APPROVE_OPTION) {
				this.selectedFile = this.swingFileDialog.getSelectedFile();
			}

			break;
		}

		return this.selectedFile;
	}

	private java.awt.FileDialog createFileDialog() {
		final java.awt.FileDialog fileDialog = new java.awt.FileDialog(
				this.parent, this.title);
		fileDialog.setFilenameFilter(new FilenameFilter() {
			@Override
			public boolean accept(final File dir, final String name) {
				final String ext = FileDialog.getExtension(name);
				if ((ext != null) && ext.equals(FileDialog.this.extension)) {
					return true;
				}

				return false;
			}
		});
		return fileDialog;
	}

	private javax.swing.JFileChooser createFileChooser() {
		final javax.swing.JFileChooser filechooser = new javax.swing.JFileChooser();
		filechooser
				.addChoosableFileFilter(new javax.swing.filechooser.FileFilter() {

					@Override
					public boolean accept(final File f) {
						if (f.isDirectory()) {
							return true;
						}

						final String ext = FileDialog.getExtension(f);
						if ((ext != null)
								&& ext.equals(FileDialog.this.extension)) {
							return true;
						}

						return false;
					}

					@Override
					public String getDescription() {
						return FileDialog.this.extensionDescription;
					}
				});

		return filechooser;
	}

	/**
	 * Returns the extension of the given file.
	 * 
	 * @param f
	 * @return
	 */
	public static String getExtension(final File f) {
		String result = null;
		final String name = f.getName();

		final int i = name.lastIndexOf('.');
		if ((i > 0) && (i < name.length() - 1)) {
			result = name.substring(i + 1).toLowerCase();
		}

		return result;
	}

	/**
	 * Returns the extension of the given filename.
	 * 
	 * @param name
	 * @return
	 */
	public static String getExtension(final String name) {
		String result = null;

		final int i = name.lastIndexOf('.');
		if ((i > 0) && (i < name.length() - 1)) {
			result = name.substring(i + 1).toLowerCase();
		}

		return result;
	}

	/**
	 * Viewmodes of file dialogs
	 */
	public static enum Mode {
		/**
		 * @uml.property name="lOAD"
		 * @uml.associationEnd
		 */
		LOAD, /**
		 * @uml.property name="sAVE"
		 * @uml.associationEnd
		 */
		SAVE;
	}

	/**
	 * Frameworks
	 */
	public static enum Framework {
		/**
		 * @uml.property name="sWING"
		 * @uml.associationEnd
		 */
		SWING, /**
		 * @uml.property name="aWT"
		 * @uml.associationEnd
		 */
		AWT;
	}
}
