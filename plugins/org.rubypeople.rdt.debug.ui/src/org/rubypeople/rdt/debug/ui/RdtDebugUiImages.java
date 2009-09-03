package org.rubypeople.rdt.debug.ui;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.rubypeople.rdt.internal.debug.ui.RdtDebugUiPlugin;

public class RdtDebugUiImages {

	protected static final String NAME_PREFIX = "org.rubypeople.rdt.debug.ui.";
	protected static final int NAME_PREFIX_LENGTH = NAME_PREFIX.length();
	protected static URL iconBaseURL;

	static {
		iconBaseURL= RdtDebugUiPlugin.getDefault().getBundle().getEntry("/icons/full/"); //$NON-NLS-1$
	}

	protected static final ImageRegistry IMAGE_REGISTRY = new ImageRegistry();

	protected static final String CTOOL_PREFIX = "ctool16";
	protected static final String EVIEW_PREFIX = "eview16";

	public static final String IMG_EVIEW_ARGUMENTS_TAB= NAME_PREFIX + "arguments_tab.gif";

	public static final ImageDescriptor DESC_EVIEW_ARGUMENTS_TAB = createManaged(EVIEW_PREFIX, IMG_EVIEW_ARGUMENTS_TAB);

	/**
	 * Returns the image managed under the given key in this registry.
	 * 
	 * @param key the image's key
	 * @return the image managed under the given key
	 */
	public static Image get(String key) {
		return IMAGE_REGISTRY.get(key);
	}

	/**
	 * Sets the three image descriptors for enabled, disabled, and hovered to an action. The actions
	 * are retrieved from the *tool16 folders.
	 */
	public static void setToolImageDescriptors(IAction action, String iconName) {
		setImageDescriptors(action, "tool16", iconName);
	}

	/**
	 * Sets the three image descriptors for enabled, disabled, and hovered to an action. The actions
	 * are retrieved from the *lcl16 folders.
	 */
	public static void setLocalImageDescriptors(IAction action, String iconName) {
		setImageDescriptors(action, "lcl16", iconName);
	}

	public static ImageRegistry getImageRegistry() {
		return IMAGE_REGISTRY;
	}

	//---- Helper methods to access icons on the file system --------------------------------------

	protected static void setImageDescriptors(IAction action, String type, String relPath) {

		try {
			ImageDescriptor id = ImageDescriptor.createFromURL(makeIconFileURL("d" + type, relPath));
			if (id != null)
				action.setDisabledImageDescriptor(id);
		} catch (MalformedURLException e) {}

		try {
			ImageDescriptor id = ImageDescriptor.createFromURL(makeIconFileURL("c" + type, relPath));
			if (id != null)
				action.setHoverImageDescriptor(id);
		} catch (MalformedURLException e) {}

		action.setImageDescriptor(create("e" + type, relPath));
	}

	protected static ImageDescriptor createManaged(String prefix, String name) {
		try {
			ImageDescriptor result = ImageDescriptor.createFromURL(makeIconFileURL(prefix, name.substring(NAME_PREFIX_LENGTH)));
			IMAGE_REGISTRY.put(name, result);
			return result;
		} catch (MalformedURLException e) {
			return ImageDescriptor.getMissingImageDescriptor();
		}
	}

	protected static ImageDescriptor create(String prefix, String name) {
		try {
			return ImageDescriptor.createFromURL(makeIconFileURL(prefix, name));
		} catch (MalformedURLException e) {
			return ImageDescriptor.getMissingImageDescriptor();
		}
	}

	protected static URL makeIconFileURL(String prefix, String name) throws MalformedURLException {
		if (iconBaseURL == null)
			throw new MalformedURLException();

		StringBuffer buffer = new StringBuffer(prefix);
		buffer.append('/');
		buffer.append(name);
		return new URL(iconBaseURL, buffer.toString());
	}
}