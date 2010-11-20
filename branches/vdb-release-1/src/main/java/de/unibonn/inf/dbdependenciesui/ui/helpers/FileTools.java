package de.unibonn.inf.dbdependenciesui.ui.helpers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Common filesystem tools.
 * 
 * @author Jan Philipp <knallisworld@googlemail.com>
 */
public class FileTools {
	/**
	 * @param filePath
	 *            the name of the file to open. Not sure if it can accept URLs
	 *            or just filenames. Path handling could be better, and buffer
	 *            sizes are hardcoded
	 */
	public static String readFileAsString(final String filePath)
			throws IOException {
		StringBuilder fileData = new StringBuilder(1000);
		BufferedReader reader = new BufferedReader(new FileReader(filePath));
		char[] buf = new char[1024];
		int numRead = 0;
		while ((numRead = reader.read(buf)) != -1) {
			fileData.append(buf, 0, numRead);
		}
		reader.close();
		return fileData.toString();
	}

	/**
	 * @param file
	 *            the file to open.
	 */
	public static String readFileAsString(final File file) throws IOException {
		StringBuilder fileData = new StringBuilder(1000);
		BufferedReader reader = new BufferedReader(new FileReader(file));
		char[] buf = new char[1024];
		int numRead = 0;
		while ((numRead = reader.read(buf)) != -1) {
			fileData.append(buf, 0, numRead);
		}
		reader.close();
		return fileData.toString();
	}

	/**
	 * Returns the extension of the given filename.
	 * 
	 * The result is the substring after the last dot.
	 * 
	 * @param name
	 * @return
	 * @see #getExtension(String)
	 */
	public static String getExtension(File f) {
		return getExtension(f.getName());
	}

	/**
	 * Returns the extension of the given filename.
	 * 
	 * The result is the substring after the last dot.
	 * 
	 * @param name
	 * @return
	 */
	public static String getExtension(final String name) {
		String ext = null;
		String s = name;
		int i = s.lastIndexOf('.');

		if (i > 0 && i < s.length() - 1) {
			ext = s.substring(i + 1).toLowerCase();
		}
		return ext;
	}

}
