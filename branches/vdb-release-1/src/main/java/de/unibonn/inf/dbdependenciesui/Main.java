/**
 * $Id: Main.java 702 2009-07-16 21:14:11Z philipp $
 */
package de.unibonn.inf.dbdependenciesui;

import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.UIDefaults;
import javax.swing.UIManager;

import org.javabuilders.swing.SwingJavaBuilder;

import de.unibonn.inf.dbdependenciesui.apple.OSXAdapter;
import de.unibonn.inf.dbdependenciesui.controller.Controller;
import de.unibonn.inf.dbdependenciesui.hibernate.HibernateDAOFactory;
import de.unibonn.inf.dbdependenciesui.hibernate.HibernateUtil;
import de.unibonn.inf.dbdependenciesui.misc.Internationalization;
import de.unibonn.inf.dbdependenciesui.ui.ApplicationView;
import de.unibonn.inf.dbdependenciesui.ui.LogView;
import de.unibonn.inf.dbdependenciesui.ui.controller.ViewController;
import de.unibonn.inf.dbdependenciesui.ui.helpers.SystemTools;
import de.unibonn.inf.dbdependenciesui.ui.misc.SplashScreenFrame;

/**
 * This is the application bootstrap (main). This main class boots the application performing all needed initialization.
 * The main issues are: setting l & f, loading splash screen while initialization, loading ui.
 * 
 * @author Andre Kasper
 * @author Jan Philipp
 */
public class Main {

	private static Logger log;

	private static SplashScreenFrame loadScreen;

	/**
	 * appropriate accelerator key for menu shortcuts
	 */
	public static int MENU_MASK;

	private static ApplicationView app;

	private static Main instance;

	public static void main(final String[] args) {
		instance = new Main();
		setPlatformIndependentSettings();
		setPlatformDependentSettings();
		setLookAndFeel();

		SwingJavaBuilder.getConfig().addResourceBundle(Internationalization.getResource());

		final List<String> messages = new ArrayList<String>();
		messages.add(Internationalization.getText("application.splash.loading1"));
		messages.add(Internationalization.getText("application.splash.loading2"));
		messages.add(Internationalization.getText("application.splash.loading3"));
		loadScreen = new SplashScreenFrame(messages);
		loadScreen.setFinalStatus(Internationalization.getText("application.splash.final"));
		loadScreen.fadeIn();

		try {
			Thread.sleep(500);
		} catch (final InterruptedException irrelevantException) {}

		loadScreen.showNextStatus();
		// Small workaround notifying hibernate to load: This will
		// create a hibernate session and close it immediately.
		HibernateUtil.getSession().close();

		loadScreen.showNextStatus();
		LogView.getInstance();
		app = new ApplicationView();

		// Show the main frame
		app.setVisible(true);

		// Show the final status ("ready").
		loadScreen.showFinalStatus();

		try {
			Thread.sleep(500);
		} catch (final InterruptedException irrelevantException) {}

		// Fade out splash screen
		loadScreen.fadeOut();
		app.repaint(); // Windows FIX
		loadScreen.dispose();
	}

	/**
	 * Set up platform independent settings. This method should called before platform dependent settings (
	 * {@link #setPlatformDependentSettings()}).
	 */
	private static void setPlatformIndependentSettings() {

		Configuration.configure();
		log = Logger.getLogger(Configuration.LOGGER);
		log.setLevel(Configuration.LOGGER_LEVEL);

		log.info("Configuration was loaded.");

		MENU_MASK = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

			@Override
			public void run() {
				instance.destruct();
			}
		}));
	}

	/**
	 * Set up a general destruction for the whole application. This method will close all open resources like the
	 * current hibernate session.
	 */
	public void destruct() {
		log.info("Closing hibernate session..");
		HibernateDAOFactory.closeSession();
		HibernateUtil.destroySessionFactory();
		log.info("done.");
	}

	/**
	 * Show the aboutMe window.
	 */
	public void showAboutMe() {
		ViewController.showAboutView();
	}

	/**
	 * Set up special platform dependent settings. This method should called after platform independent setings (
	 * {@link #setPlatformIndependentSettings()}).
	 */
	private static void setPlatformDependentSettings() {
		if (SystemTools.isMac()) {
			// Enable the menu bar on top of the screen (osx human guide line)
			System.setProperty("apple.laf.useScreenMenuBar", "true");
			// Redefine the application name (using the title)
			System.setProperty("com.apple.mrj.application.apple.menu.about.name", Internationalization
					.getText("application.title"));
			// Setup mac using small tabs.
			final UIDefaults defaults = UIManager.getDefaults();
			defaults.put("TabbedPane.useSmallLayout", Boolean.TRUE);
			try {
				// Add a special quit handler; in addition to standard shutdown
				// hook.
				OSXAdapter.setQuitHandler(new Main(), Main.class.getDeclaredMethod("destruct", (Class[]) null));
				// Add a special aboutme handler.
				OSXAdapter.setAboutHandler(new Main(), Main.class.getDeclaredMethod("showAboutMe", (Class[]) null));
			} catch (final Exception e) {}
		}
	}

	/**
	 * Set the default system look & feel. This method should called before the first ui object is created.
	 */
	private static void setLookAndFeel() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (final Exception e) {
			final String message = Internationalization.getText("error.nolookandfeel");
			Controller.showErrorMessage(message);
			System.exit(1);
		}
	}
}