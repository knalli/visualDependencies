package de.unibonn.inf.dbdependenciesui.ui.helpers;

/**
 * Common system and platform tools.
 * 
 * @author Jan Philipp <knallisworld@googlemail.com>
 * 
 */
public class SystemTools {
	/**
	 * Checks if the running system is a windows.
	 * 
	 * @link http://www.joachim-uhl.de/2008/02/01/browser-aus-java-applikation-
	 *       starten
	 * @return is a windows based plattform
	 */
	public static boolean isWindows() {
		final String os = System.getProperty("os.name");
		if (os != null && os.startsWith("Windows")) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Checks if the running system is a mac.
	 * 
	 * @link http://www.joachim-uhl.de/2008/02/01/browser-aus-java-applikation-
	 *       starten
	 * @return is a mac based plattform
	 */
	public static boolean isMac() {
		final String os = System.getProperty("os.name");
		if (os != null && os.startsWith("Mac")) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Checks if the running system is an osx.
	 * 
	 * @link http://www.joachim-uhl.de/2008/02/01/browser-aus-java-applikation-
	 *       starten
	 * @return is an osx based plattform
	 */
	public static boolean isOSX() {
		final String os = System.getProperty("os.name");
		if (os != null && os.indexOf("OS X") > -1) {
			return true;
		} else {
			return false;
		}
	}
}
