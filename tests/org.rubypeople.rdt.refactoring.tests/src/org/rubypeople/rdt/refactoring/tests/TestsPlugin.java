/***** BEGIN LICENSE BLOCK *****
 * Version: CPL 1.0/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Common Public
 * License Version 1.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * Copyright (C) 2006 Lukas Felber <lfelber@hsr.ch>
 * 
 * Alternatively, the contents of this file may be used under the terms of
 * either of the GNU General Public License Version 2 or later (the "GPL"),
 * or the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the CPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the CPL, the GPL or the LGPL.
 ***** END LICENSE BLOCK *****/

package org.rubypeople.rdt.refactoring.tests;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class TestsPlugin extends AbstractUIPlugin {

	// The shared instance.
	private static TestsPlugin plugin;

	/**
	 * The constructor.
	 */
	public TestsPlugin() {
		plugin = this;
	}

	/**
	 * This method is called upon plug-in activation
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		plugin = null;
	}

	/**
	 * Returns the shared instance.
	 */
	public static TestsPlugin getDefault() {
		return plugin;
	}
	
	private static HashMap<String, String> files;
	
	private static void initializeFiles() {
		files = new HashMap<String, String>();
		
		Enumeration enumeration = getDefault().getBundle().findEntries("/resources", null, true);
		while(enumeration != null && enumeration.hasMoreElements()) {
			URL file = (URL) enumeration.nextElement();
			if(file.getFile().matches(".*\\.svn.*")) {
				continue;
			}
			String[] segments = file.getPath().split("[/|\\\\]");
			String fileName = segments[segments.length - 1];
			try {
				files.put(fileName, FileLocator.resolve(file).getFile());
			} catch (IOException e) {
				e.printStackTrace();
				continue;
			}
		}
	}
	
	public static String getFile(String name) {
		if(files == null) {
			initializeFiles();
		}
		return files.get(name);
	}
	
	protected static Collection<File> getFiles(final String filter) throws IOException {
		ArrayList<File> files = new ArrayList<File>();

		Enumeration enumeration = TestsPlugin.getDefault().getBundle().findEntries("/resources", filter, true);
		while(enumeration.hasMoreElements()) {
			URL url = FileLocator.resolve((URL) enumeration.nextElement());
			files.add(new File( url.getFile()));
		}

		return files;
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in
	 * relative path.
	 * 
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return AbstractUIPlugin.imageDescriptorFromPlugin("org.rubypeople.rdt.refactoring.tests", path);
	}
}
