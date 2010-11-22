package de.unibonn.inf.dbdependenciesui.ui.helpers;

import java.awt.Color;
import java.awt.Window;
import java.lang.reflect.Method;

import javax.swing.JFrame;

public class AWTUtilities {
	@SuppressWarnings("unchecked")
	public static void setAlpha(final Window window, float alpha) {

		// Force alpha in range [0f..1f].
		if (alpha < 0) {
			alpha = 0f;
		} else if (alpha > 1) {
			alpha = 1f;
		}

		try {
			// invoke AWTUtilities.setWindowOpacity(win, 0.0f);
			final Class awtutil = Class.forName("com.sun.awt.AWTUtilities");
			final Method setWindowOpaque = awtutil.getMethod("setWindowOpacity", Window.class, float.class);
			setWindowOpaque.invoke(null, window, alpha);
		} catch (final Throwable anything) {
			if (AWTUtilities.isMac()) {
				if (window instanceof JFrame) {
					((JFrame) window).getRootPane().putClientProperty("Window.alpha", new Float(alpha));
				} else {
					final Color oldBg = window.getBackground();
					final Color newBg = new Color(oldBg.getRed(), oldBg.getGreen(), oldBg.getBlue(),
							(int) (alpha * 255));
					window.setBackground(newBg);
				}
			}
		}
	}

	/**
	 * Checks if the running system is a mac.
	 * 
	 * @link http://www.joachim-uhl.de/2008/02/01/browser-aus-java-applikation- starten
	 * @return is a mac based platform
	 */
	public static boolean isMac() {
		final String os = System.getProperty("os.name");
		if ((os != null) && os.startsWith("Mac")) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean hasAlpha() {
		try {
			Class.forName("com.sun.awt.AWTUtilities");
			return true;
		} catch (final ClassNotFoundException e) {
			if (AWTUtilities.isMac()) { return true; }
		}
		return false;
	}
}
