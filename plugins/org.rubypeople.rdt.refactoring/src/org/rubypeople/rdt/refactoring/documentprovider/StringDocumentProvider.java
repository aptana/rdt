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

package org.rubypeople.rdt.refactoring.documentprovider;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class StringDocumentProvider extends DocumentProvider {

	protected String document;
	protected String docName;
	
	protected Map<String, String> files;

	public StringDocumentProvider(String name, String document) {
		this.document = document;
		this.docName = name;
		files = new LinkedHashMap<String, String>();
		files.put(name, document);
	}
	
	public StringDocumentProvider(IDocumentProvider other) {
		this(other.getActiveFileName(), other.getActiveFileContent());
	}
	
	public String getActiveFileContent() {
		return document;
	}

	public String getActiveFileName() {
		return docName;
	}

	public String getFileContent(String currentFileName) {
		return files.get(currentFileName);
	}

	public Collection<String> getFileNames() {
		return files.keySet();
	}
	
	public void addFile(String fileName, String fileContent) {
		files.put(fileName, fileContent);
	}
}
