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
 * Copyright (C) 2006 Mirko Stocker <me@misto.ch>
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

package org.rubypeople.rdt.refactoring.tests.core;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.regex.Pattern;

import org.rubypeople.rdt.refactoring.documentprovider.DocumentProvider;
import org.rubypeople.rdt.refactoring.tests.FileTestData;

public class MultipleDocumentsInOneProvider extends DocumentProvider {

	private StringBuffer activeSection;
	private HashMap<String, StringBuffer> sections;
	private String fileName;
	private String partName;

	public MultipleDocumentsInOneProvider(String baseFile, Class<?> resource) {
		this.fileName = baseFile;
		FileTestData doc = null;
		
		try {
			doc = new FileTestData(baseFile, "", "");
		} catch (FileNotFoundException e) {
			assert false : "File not found! " + e;
			return; 
		} catch (IOException e) {
			assert false : "IOException " + e;
			return;
		}
		
		String[] strings = doc.getActiveFileContent().split("\n");
		sections = new HashMap<String, StringBuffer>();
		StringBuffer active = new StringBuffer();
		for (String string : strings) {
			if(Pattern.compile("^##!(.*)\\s*(\\w*)*$").matcher(string).matches()) {
				active = new StringBuffer();
				sections.put(string.replaceFirst("^##!", ""), active);
			} else {
				if (active.length() > 0)
					active.append("\n");
				active.append(string);
			}
		}
	}
	
	public DocumentProvider setActive(String part) {
		this.partName = part;
		activeSection = sections.get(part);
		return this;
	}
	
	public String getActiveFileContent() {
		return activeSection.toString();
	}

	public String getActiveFileName() {
		return fileName + "_" + partName;
	}

	public String getFileContent(String currentFileName) {
		return getActiveFileContent();
	}

	public Collection<String> getFileNames() {
		return new ArrayList<String>();
	}
}
