/**
 * $Id: Main.java 41 2009-03-01 16:21:20Z philipp $
 */
package de.unibonn.inf.dbdependenciesui;

import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Level;

import de.unibonn.inf.dbdependenciesui.misc.Internationalization;
import de.unibonn.inf.dbdependenciesui.ui.helpers.SystemTools;

/**
 * This is the global configuration object. Initially, the configuration will loaded with {@link #configure()}. The
 * configuration file is searched in application directory and in the user directory.
 * 
 * @author Andre Kasper
 * @author Jan Philipp
 */
final public class Configuration {
	/**
	 * global logger identification name
	 */
	public static String LOGGER = "dbvis";

	/**
	 * global used Locale object
	 */
	public static Locale LOCALE;

	/**
	 * global internal name representation (files, paths)
	 */
	public static String INTERNAL_NAME;

	/**
	 * application's settingsfolder in user home
	 */
	public static String SETTINGS_PATH;

	/**
	 * current developer revision
	 */
	public static int REVISION = 0;

	/**
	 * current application version
	 */
	public static String VERSION = null;

	/**
	 * current built date
	 */
	public static Date build;

	/**
	 * Merged properties from: system defaults, configuration and user
	 */
	private static Properties props;

	/**
	 * current logger level
	 */
	public static Level LOGGER_LEVEL = Level.SEVERE;

	/**
	 * application's height
	 */
	public static int APPLICATION_HEIGHT = 650;

	/**
	 * application's width
	 */
	public static int APPLICATION_WIDTH = 900;

	/**
	 * private constructor, each method is static, so you don't need a constructor anyway
	 */
	private Configuration() {}

	/**
	 * Read the configuration properties.
	 */
	public static void configure() {
		props = new Properties();

		configureDefaults();
		configureByProperties();
		initializeLocalUserSettings();

		// Load internationalization strings
		ResourceBundle res = ResourceBundle.getBundle("strings", LOCALE);
		Internationalization.setResource(res);

		ResourceBundle resource;

		// Load system
		try {
			final String filename = "configuration";
			resource = ResourceBundle.getBundle(filename);
			loadResourceKeys(resource);
			configureByProperties();
			initializeLocalUserSettings();
		} catch (final Exception e) {
			// System.err.printf("Invalid resource bundle: %s%n", "configuration");
		}

		// Load user
		try {
			final String filename = SETTINGS_PATH + "/configuration.properties";
			final Properties props2 = new Properties();
			props2.load(new FileReader(filename));
			loadResourceKeys(props2);
			configureByProperties();
			initializeLocalUserSettings();
		} catch (final Exception e) {
			System.out.printf("No configuration found at: %s%n", SETTINGS_PATH + "/configuration");
		}
	}

	/**
	 * Configure the properties with default settings.
	 */
	private static void configureDefaults() {
		props.setProperty("loggername", "dbvis");
		props.setProperty("loggerlevel", "info");
		props.setProperty("internalname", "dbvis");
		props.setProperty("buildRevision", "0");
		props.setProperty("buildDatetime", "");
		props.setProperty("buildVersion", "");
		props.setProperty("application.height", "0");
		props.setProperty("application.width", "0");

		LOCALE = Locale.getDefault();
	}

	/**
	 * Configure the properties with values of the given resource.
	 * 
	 * @param resource
	 */
	private static void configureByProperties() {
		LOGGER = props.getProperty("loggername");
		INTERNAL_NAME = props.getProperty("internalname");

		final String buildRevision = props.getProperty("buildRevision");
		try {
			REVISION = Integer.parseInt(buildRevision);
		} catch (final Exception e) {
			REVISION = 0;
		}

		final String buildVersion = props.getProperty("buildVersion");
		if (buildVersion != null) {
			VERSION = buildVersion;
		}

		Level level = null;
		try {
			level = Level.parse(props.getProperty("loggerlevel").toUpperCase());
		} catch (final Exception e) {} finally {
			if (level == null) {
				level = Level.SEVERE;
			}
		}
		LOGGER_LEVEL = level;

		final String buildDatetime = props.getProperty("buildDatetime");
		try {
			final SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:MM:SS z");
			build = df.parse(buildDatetime);
		} catch (final Exception e) {
			build = new Date();
		}

		try {
			APPLICATION_HEIGHT = Integer.parseInt(props.getProperty("application.height"));
		} catch (final NumberFormatException e) {}
		try {
			APPLICATION_WIDTH = Integer.parseInt(props.getProperty("application.width"));
		} catch (final NumberFormatException e) {}

	}

	/**
	 * Load all known values from the resource bundle.
	 * 
	 * @param resource
	 */
	private static void loadResourceKeys(final ResourceBundle resource) {
		if (resource != null) {
			tryGetProperty(resource, "loggername");
			tryGetProperty(resource, "loggerlevel");
			tryGetProperty(resource, "internalname");
			tryGetProperty(resource, "buildVersion");
			tryGetProperty(resource, "buildRevision");
			tryGetProperty(resource, "buildDatetime");
			tryGetProperty(resource, "application.height");
			tryGetProperty(resource, "application.width");
			tryGetProperty(resource, "debugconnection.enabled");
			tryGetProperty(resource, "debugconnection.host");
			tryGetProperty(resource, "debugconnection.port");
			tryGetProperty(resource, "debugconnection.username");
			tryGetProperty(resource, "debugconnection.password");
			tryGetProperty(resource, "debugconnection.schema");
		}
	}

	/**
	 * Load all known values from the properties.
	 * 
	 * @param resource
	 */
	private static void loadResourceKeys(final Properties resource) {
		if (resource != null) {
			tryGetProperty(resource, "loggername");
			tryGetProperty(resource, "loggerlevel");
			tryGetProperty(resource, "internalname");
			tryGetProperty(resource, "buildVersion");
			tryGetProperty(resource, "buildRevision");
			tryGetProperty(resource, "buildDatetime");
			tryGetProperty(resource, "application.height");
			tryGetProperty(resource, "application.width");
			tryGetProperty(resource, "debugconnection.enabled");
			tryGetProperty(resource, "debugconnection.host");
			tryGetProperty(resource, "debugconnection.port");
			tryGetProperty(resource, "debugconnection.username");
			tryGetProperty(resource, "debugconnection.password");
			tryGetProperty(resource, "debugconnection.schema");
		}
	}

	/**
	 * Try getting a value of the given resource and key. If the resource, the key or the represented object not exist
	 * it return null.
	 * 
	 * @param resource
	 * @param key
	 * @return
	 */
	private static String tryGetProperty(final ResourceBundle resource, final String key) {

		String value = null;

		// Ignoring all exceptions (e.g. MissingResourceException) because it is
		// irrelevant.
		try {
			if ((resource != null) && (key != null)) {
				value = resource.getString(key);
				if (value != null) {
					props.setProperty(key, value);
				}
			}
		} catch (final Exception irrelevant) {}

		return value;
	}

	/**
	 * Try getting a value of the given resource and key. If the resource, the key or the represented object not exist
	 * it return null.
	 * 
	 * @param resource
	 * @param key
	 * @return
	 */
	private static String tryGetProperty(final Properties resource, final String key) {

		String value = null;

		// Ignoring all exceptions (e.g. MissingResourceException) because it is
		// irrelevant.
		try {
			if ((resource != null) && (key != null)) {
				value = resource.getProperty(key);
				if (value != null) {
					props.setProperty(key, value);
				}
			}
		} catch (final Exception irrelevant) {}

		return value;
	}

	/**
	 * Initialize special local user settings, e.g. user home, application preferences etc.
	 */
	private static void initializeLocalUserSettings() {
		final String userHome = System.getProperty("user.home");
		final char ds = '/';

		String path = null;
		if (SystemTools.isMac()) {
			path = userHome + ds + "Library" + ds + INTERNAL_NAME + ds;
		} else {
			path = userHome + ds + INTERNAL_NAME + ds;
		}

		SETTINGS_PATH = new File(path).toString();
	}
	
	public static String getSetting(final String key) {
		return props.getProperty(key);
	}
	
	public static boolean getBoolean(String key) {
		try {
			return Boolean.valueOf(getSetting(key));
		} catch (Exception e) {
			System.err.println(e.getLocalizedMessage());
			return false;
		}
	}
}
