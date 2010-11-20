/**
 * $Id: Main.java 41 2009-03-01 16:21:20Z philipp $
 */
package de.unibonn.inf.dbdependenciesui.misc;

import java.util.ResourceBundle;

import javax.swing.Icon;

/**
 * Application internationalization object.
 * 
 * @author Andre Kasper
 * @author Jan Philipp
 */
public class Internationalization {

	private static ResourceBundle res;

	/**
	 * Get the localized text for the key.
	 * 
	 * @param key
	 * @return
	 */
	public static String getText(final String key) {
		try {
			return Internationalization.res.getString(key);
		} catch (final Exception e) {
			return null;
		}
	}

	/**
	 * Get the localized text for the key and substitute with patterns. See {@link String#format(String, Object...)} for
	 * more details.
	 * 
	 * @param key
	 * @param args
	 * @return
	 */
	public static String getTextFormatted(final String key, final Object... args) {
		return String.format(Internationalization.res.getString(key), args);
	}

	/**
	 * Get the localized setting for the key.
	 * 
	 * @param key
	 * @return
	 */
	public static String getSetting(final String key) {
		return Internationalization.res.getString(key);
	}
	
	/**
	 * Get the current bundle resource.
	 * 
	 * @return
	 */
	public static ResourceBundle getResource() {
		return Internationalization.res;
	}

	/**
	 * Get the specified icon.
	 * 
	 * @param key
	 * @return
	 */
	public static Icon getIcon(final String key) {
		return Icons.getIcon(Internationalization.getText(key + ".icon"));
	}

	public static Icon getScaledIcon(final String key, final int scale) {
		return Icons.getScaledIcon(Internationalization.getText(key + ".icon"), scale);
	}

	public static void setResource(final ResourceBundle bundle) {
		res = bundle;
	}
}
