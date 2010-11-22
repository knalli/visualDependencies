package de.unibonn.inf.dbdependenciesui.ui.misc;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import de.unibonn.inf.dbdependenciesui.ui.helpers.AWTUtilities;

/**
 * Fade effects.
 * 
 * @author Andre Kasper
 * @author Jan Philipp
 */
public class FadeEffectHelper {

	/**
	 * Show the frame with a fade-in effect. This feature works with java >=1.6.10 or Mac OS X 10.5+Java. Otherwise, it
	 * will called {@link #setVisible(true)}. The fade operation is performed in a separated thread.
	 * 
	 * @uses AWTUtilities wrapper class for java's AWTUtilities
	 */
	public static void fadeIn(final JFrame frame) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				final float step = 0.1f;
				frame.validate();
				if (AWTUtilities.hasAlpha()) {
					AWTUtilities.setAlpha(frame, 0);
					frame.setVisible(true);
					float alpha = 0;
					while (alpha <= 1 - step) {
						alpha += step;
						AWTUtilities.setAlpha(frame, alpha);
						try {
							Thread.sleep(50);
						} catch (final InterruptedException e) {}
					}
					AWTUtilities.setAlpha(frame, 1f);
				} else {
					frame.setVisible(true);
				}
			}
		});
	}

	/**
	 * Show the frame with a fade-out effect. This feature works with java >=1.6.10 or Mac OS X 10.5+Java. Otherwise, it
	 * will called {@link #setVisible(false)}. The fade operation is performed in a separated thread.
	 * 
	 * @uses AWTUtilities wrapper class for java's AWTUtilities
	 */
	public static void fadeOut(final JFrame frame) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				final float step = 0.1f;
				if (AWTUtilities.hasAlpha()) {
					float alpha = 1f;
					while (alpha >= step) {
						alpha -= step;
						AWTUtilities.setAlpha(frame, alpha);
						try {
							Thread.sleep(50);
						} catch (final InterruptedException e) {}
					}
					AWTUtilities.setAlpha(frame, 0f);
				} else {
					frame.setVisible(false);
				}
			}
		});
	}
}
