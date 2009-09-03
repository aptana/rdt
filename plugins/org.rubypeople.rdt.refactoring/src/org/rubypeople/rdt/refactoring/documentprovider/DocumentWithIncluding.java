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

package org.rubypeople.rdt.refactoring.documentprovider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.jruby.ast.FCallNode;
import org.jruby.ast.RootNode;
import org.jruby.ast.StrNode;
import org.jruby.lexer.yacc.SyntaxException;
import org.rubypeople.rdt.refactoring.classnodeprovider.ClassNodeProvider;
import org.rubypeople.rdt.refactoring.core.NodeProvider;
import org.rubypeople.rdt.refactoring.nodewrapper.ClassNodeWrapper;
import org.rubypeople.rdt.refactoring.nodewrapper.PartialClassNodeWrapper;

public class DocumentWithIncluding extends StringDocumentProvider {
	
	protected final IDocumentProvider docProvider;

	public DocumentWithIncluding(IDocumentProvider docProvider) {
		super(docProvider.getActiveFileName(), docProvider.getActiveFileContent());
		this.docProvider = docProvider;
		searchForRelatedFiles();
	}
	
	private void searchForRelatedFiles() {
		Collection<String> candidates = new ArrayList<String>(docProvider.getFileNames());
		ArrayList<String> markedForRemoval = new ArrayList<String>();
		HashSet<String> includedFiles = findAllIncludedFiles();
		
		do {
			markedForRemoval.clear();
			
			for(String actFileName : candidates) {
				String fileName = getFileNameWithoutPath(actFileName);
				if(includedFiles.contains(fileName)) {
					addAndRemove(markedForRemoval, actFileName, fileName);
					continue;
				}
				for (FCallNode node : getRequires(actFileName)) {
					if(nodeRequiresMe(node) && !getFileNames().contains(actFileName)) {
						addAndRemove(markedForRemoval, actFileName, fileName);
					}
				}
			}
			removeMarkedFromCandidates(markedForRemoval, candidates);
			
		} while(markedForRemoval.size() > 0);
	}

	private void addAndRemove(ArrayList<String> markedForRemoval, String actFileName, String fileName) {
		addFile(fileName, docProvider.getFileContent(actFileName));
		markedForRemoval.add(actFileName);
	}

	private String cutProjectPath(String fileName) {
		
		return fileName.substring(fileName.lastIndexOf('/') + 1);
	}

	private HashSet<String> findAllIncludedFiles() {
		HashSet<String> includedFiles = new HashSet<String>();
		ClassNodeProvider includedProvider = docProvider.getIncludedClassNodeProvider();
		for(ClassNodeWrapper classNode : includedProvider.getAllClassNodes()) {
			for (PartialClassNodeWrapper partialClassNode : classNode.getPartialClassNodes()) {
				String file = partialClassNode.getWrappedNode().getPosition().getFile();
				if(!file.equals(docName)) {
					includedFiles.add(file);
				}
			}
		}
		return includedFiles;
	}

	private String getFileNameWithoutPath(String actFileName) {
		if(actFileName.contains("/")) { //$NON-NLS-1$
			return actFileName.substring(actFileName.lastIndexOf("/") + 1); //$NON-NLS-1$
		}
		return actFileName;
	}

	private boolean nodeRequiresMe(FCallNode node) {
		return isStrNode(node) && fileIsInResultSet(getRequiredFilename(node));
	}

	private void removeMarkedFromCandidates(ArrayList<String> markedForRemoval, Collection<String> candidates) {
		for (String actName : markedForRemoval) {
			candidates.remove(actName);
		}
	}

	private String getRequiredFilename(FCallNode node) {
		return ((StrNode) node.getArgsNode().childNodes().iterator().next()).getValue().toString();
	}

	private Collection<FCallNode> getRequires(String fileName) {
		try {
			RootNode rootNode = docProvider.getRootNode(fileName);
			return NodeProvider.getLoadAndRequireNodes(rootNode);
		} catch(SyntaxException e) {
			return new ArrayList<FCallNode>();
		} 
	}

	private boolean fileIsInResultSet(String fileName) {
		String pathlessName = cutProjectPath(fileName);
		if(!pathlessName.matches(".*\\.rb$")) { //$NON-NLS-1$
			pathlessName += ".rb"; //$NON-NLS-1$
		}

		if(fileName.equals(cutProjectPath(docName))) {
			return true;
		}
		
		for (String name: files.keySet()) {
			if(cutProjectPath(name).equals(pathlessName)) {
				return true;
			}
		}
		return false;
	}
	
	private boolean isStrNode(FCallNode node) {
		return node.getArgsNode().childNodes().iterator().next() instanceof StrNode;
	}
}
