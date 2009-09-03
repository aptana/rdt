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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jruby.ast.Node;
import org.jruby.ast.RootNode;
import org.rubypeople.rdt.refactoring.classnodeprovider.AllFilesClassNodeProvider;
import org.rubypeople.rdt.refactoring.classnodeprovider.ClassNodeProvider;
import org.rubypeople.rdt.refactoring.classnodeprovider.IncludedClassesProvider;
import org.rubypeople.rdt.refactoring.core.NodeProvider;


public abstract class DocumentProvider implements IDocumentProvider {

	private Map<String, RootNode> cachedRootNodes;
	
	public DocumentProvider() {
		cachedRootNodes = new LinkedHashMap<String, RootNode>();
	}

	public ClassNodeProvider getClassNodeProvider() {
		return new ClassNodeProvider(this);
	}
	
	public ClassNodeProvider getProjectClassNodeProvider() {
		return new AllFilesClassNodeProvider(this);
	}
	
	public ClassNodeProvider getIncludedClassNodeProvider() {
		return new IncludedClassesProvider(this);
	}
	
	public RootNode getActiveFileRootNode() {
		return getRootNode(getActiveFileName());
	}

	public Collection<Node> getAllNodes() {
		ArrayList<Node> allNodes = new ArrayList<Node>();
	
		for(String currentFileName : getFileNames()){
			allNodes.addAll(getAllNodes(currentFileName));
		}
		return allNodes;
	}
	
	public Collection<Node> getAllNodes(String fileName){
		return NodeProvider.getAllNodes(getRootNode(fileName));
	}
	
	public RootNode getRootNode(String fileName) {
		if(!cachedRootNodes.containsKey(fileName)) {
			cachedRootNodes.put(fileName, NodeProvider.getRootNode(fileName, getFileContent(fileName))); 
		}
		return cachedRootNodes.get(fileName);
	}
}
