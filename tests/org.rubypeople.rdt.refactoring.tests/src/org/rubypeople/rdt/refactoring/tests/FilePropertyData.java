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
 * Copyright (C) 2006 Mirko Stocker <me@misto.ch>
 * Copyright (C) 2006 Thomas Corbat <tcorbat@hsr.ch>
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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

import org.rubypeople.rdt.refactoring.documentprovider.DocumentProvider;

public abstract class FilePropertyData extends DocumentProvider {

	protected Properties properties;
	
	public FilePropertyData(String fileName) throws FileNotFoundException, IOException {
		if(TestsPlugin.getFile(fileName) != null) {
			properties = initProperties(fileName);
		}
	}
	
	public static Properties initProperties(String propertyFileName) throws FileNotFoundException, IOException {
		Properties properties = new Properties();
		FileInputStream fileInputStream = null;
		try {
			fileInputStream = new FileInputStream(TestsPlugin.getFile(propertyFileName));
			properties.load(fileInputStream);
		} finally {
			if(fileInputStream != null)	fileInputStream.close();
		}
		return properties;
	}

	public static boolean getBoolValue(String value) {
		return "true".equalsIgnoreCase(value) || "yes".equalsIgnoreCase(value);
	}

	public boolean hasProperty(String propertyName) {
		return properties.containsKey(propertyName);
	}

	public String getProperty(String propertyName) {
		return properties.getProperty(propertyName);
	}

	public boolean getBoolProperty(String propertyName) {
		return getBoolValue(properties.getProperty(propertyName));
	}

	public static String[] seperateString(String value) {
		return value.split(",\\s?");
	}

	public int getIntProperty(String propertyName) {
		if(hasProperty(propertyName)){
			return Integer.parseInt(properties.getProperty(propertyName));
		}
		return 0;
	}

	public String[] getCommaSeparatedStringArray(String propertyName) {
		return seperateString(properties.getProperty(propertyName));
	}

	public int[] getCommaSeparatedIntArray(String propertyName) {
		String[] strings = properties.getProperty(propertyName).split(",\\s?");
		int[] ints = new int[strings.length];
		for(int i = 0; i < strings.length; i++) {
			ints[i] = Integer.parseInt(strings[i]);
		}
		return ints;
	}

	public Collection<String> getNumberedProperty(String propertyName) {
		Collection<String> properties = new ArrayList<String>();
		int propCount = 0;
		while(hasProperty(propertyName + propCount)) {
			properties.add(getProperty(propertyName + propCount));
			propCount++;
		}
		return properties;
	}

}