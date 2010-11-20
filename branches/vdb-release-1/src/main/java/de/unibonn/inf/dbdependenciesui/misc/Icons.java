package de.unibonn.inf.dbdependenciesui.misc;

import java.awt.Image;
import java.awt.Toolkit;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;

public class Icons {

	private static final Map<String, Icon> cache = new HashMap<String, Icon>();

	public static Icon getIcon(final String filename) {
		if ((filename == null) || filename.isEmpty()) {
			return null;
		}

		if (Icons.existInCache(filename)) {
			return Icons.readFromCache(filename);
		} else {
			final Icon icon = Icons.loadIcon(filename);
			Icons.writeIntoCache(filename, icon);
			return icon;
		}
	}

	public static Icon getScaledIcon(final String filename, final int scale) {
		if ((filename == null) || filename.isEmpty()) {
			return null;
		}

		if (Icons.existInCache(filename + scale)) {
			return Icons.readFromCache(filename + scale);
		} else {
			final Icon icon = Icons.loadScaledIcon(filename, scale);
			Icons.writeIntoCache(filename + scale, icon);
			return icon;
		}
	}

	private static boolean existInCache(final String key) {
		return Icons.cache.containsKey(key);
	}

	private static void writeIntoCache(final String key, final Icon icon) {
		Icons.cache.put(key, icon);
	}

	private static Icon readFromCache(final String key) {
		return Icons.cache.get(key);
	}

	public static Image createImage(final String filename) {
		try {
			return Toolkit.getDefaultToolkit().getImage(
					Icons.class.getClassLoader().getResource(filename));
		} catch (final Exception e) {
		}

		return null;
	}

	private static Icon loadIcon(final String filename) {
		if ((filename == null) || filename.isEmpty()) {
			return null;
		}

		try {
			return new ImageIcon(Icons.class.getClassLoader().getResource(
					filename));
		} catch (final Throwable e) {
			return null;
		}
	}

	private static Icon loadScaledIcon(final String filename, final int scale) {
		if ((filename == null) || filename.isEmpty()) {
			return null;
		}

		try {
			final Image image = Icons.createImage(filename);
			final Image scaled = image.getScaledInstance(scale, scale,
					Image.SCALE_SMOOTH);
			return new ImageIcon(scaled);
		} catch (final Throwable e) {
			return null;
		}
	}
}
