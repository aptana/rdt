package com.aptana.rdt;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Plugin;


public class AptanaRDTTests extends Plugin {

	// The shared instance
	private static AptanaRDTTests plugin;
	
	/**
	 * The constructor
	 */
	public AptanaRDTTests() {
		super();
		plugin = this;
	}
	
	public static File getFileInPlugin(IPath path) {
		try {
			URL installURL = new URL(
					getDefault().getBundle().getEntry("/"), path.toString()); //$NON-NLS-1$
			URL localURL = FileLocator.toFileURL(installURL);
			return new File(localURL.getFile());
		} catch (IOException ioe) {
			return null;
		}
	}
	
	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static AptanaRDTTests getDefault() {
		return plugin;
	}

	
}
