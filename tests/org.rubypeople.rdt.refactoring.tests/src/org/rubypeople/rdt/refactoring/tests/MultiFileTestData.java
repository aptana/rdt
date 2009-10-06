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
import java.util.Arrays;
import java.util.Collection;
import java.util.StringTokenizer;

import org.jruby.ast.Node;
import org.rubypeople.rdt.refactoring.util.FileHelper;

public class MultiFileTestData extends FilePropertyData {

	private String testName;
	private String sourceSuffix;
	private String resultSuffix;
	
	public MultiFileTestData(String testName) throws FileNotFoundException, IOException{
		this(testName + "." , ".source", ".result", testName + ".test_properties");
	}	
	
	public MultiFileTestData(String testName, String sourceSuffix, String resultSuffix, String propertyFile) throws FileNotFoundException, IOException{
		super(propertyFile);
		this.testName = testName;
		this.sourceSuffix = sourceSuffix;
		this.resultSuffix = resultSuffix;
	}

	public String getFileContent(String fileName){
		String sourceName = getSourceName(fileName);
		return FileHelper.getFileContent(TestsPlugin.getFile(sourceName));
	}
	
	public String getSource(String fileName) {
		
		return getFileContent(fileName);
	}
	
	public String getSourceName(String fileName){
		return testName + fileName + sourceSuffix;
	}
	
	public String getResult(String fileName) {
		return FileHelper.getFileContent(TestsPlugin.getFile(getResultName(fileName)));
	}
	
	public String getResultName(String fileName) {
		return testName + fileName + resultSuffix;
	}
	
	public String getActiveFileName(){
		
		return properties.getProperty("activeFile");
	}
	
	public String[] getIncludedFileNames(){
		ArrayList<String> destFileNames = new ArrayList<String>();
		if(!hasProperty("destinationFiles")) {
			return new String[0];
		}
		String destFilesProperty = properties.getProperty("destinationFiles");
		StringTokenizer tokenizer = new StringTokenizer(destFilesProperty, ",",false);
		
		while(tokenizer.hasMoreElements()){
			String destFile = tokenizer.nextToken();
			destFileNames.add(destFile.trim());
		}
		
		return destFileNames.toArray(new String[0]);
	}
	

	public String getActiveFileContent() {
		return getFileContent(getActiveFileName());
	}

	public Collection<String> getFileNames() {
		ArrayList<String> files = new ArrayList<String>();
		files.add(properties.getProperty("activeFile"));
		files.addAll(Arrays.asList(getIncludedFileNames()));
		
		return files;
	}

	@Override
	public Collection<Node> getAllNodes() {
		ArrayList<Node> allNodes = new ArrayList<Node>();
		
		for(String currentFileName : getFileNames()){
			allNodes.addAll(getAllNodes(currentFileName));
		}
		
		return allNodes;
	}
	
}
