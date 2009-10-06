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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.rubypeople.rdt.refactoring.util.FileHelper;

public class FileTestData extends FilePropertyData { 

	private String fileName;
	private String source;
	private String expectedResult;
	private final String sourceSuffix;
	private final String resultSuffix;

	public FileTestData(String fileName, String sourceSuffix, String resultSuffix) throws FileNotFoundException, IOException {
		super(fileName + ".test_properties");
		this.fileName = fileName;
		this.sourceSuffix = sourceSuffix;
		this.resultSuffix = resultSuffix;
		source = initSourceFile();
		expectedResult = initExpectedFile();
	}
	
	public String getFileName() {
		return fileName;
	}

	public FileTestData(String fileName) throws FileNotFoundException, IOException {
		this(fileName, ".test_source", ".test_result");
	}

	private String initSourceFile() {
		return FileHelper.getFileContent(TestsPlugin.getFile(fileName + sourceSuffix));
	}
	
	private String initExpectedFile() {
		return FileHelper.getFileContent(TestsPlugin.getFile(fileName + resultSuffix));
	}

	public String getSource() {
		return source;
	}
	
	public String getExpectedResult() {
		return expectedResult;
	}
	
	public String getActiveFileContent() {
		return getSource();
	}

	public String getActiveFileName() {
		return fileName + sourceSuffix;
	}

	public String getFileContent(String currentFileName) {
		return FileHelper.getFileContent(TestsPlugin.getFile(currentFileName));
	}

	public Collection<String> getFileNames() {
		Collection<String> names = new ArrayList<String>();
		names.add(getActiveFileName());
		return names;
	}
}
