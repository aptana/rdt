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

package org.rubypeople.rdt.refactoring.core.extractmethod;

import org.jruby.ast.MethodDefNode;
import org.jruby.ast.Node;
import org.rubypeople.rdt.refactoring.core.IRefactoringConfig;
import org.rubypeople.rdt.refactoring.core.IRefactoringContext;
import org.rubypeople.rdt.refactoring.core.RefactoringContext;
import org.rubypeople.rdt.refactoring.documentprovider.IDocumentProvider;
import org.rubypeople.rdt.refactoring.nodewrapper.PartialClassNodeWrapper;

public class ExtractMethodConfig implements IRefactoringConfig {

	private IDocumentProvider docProvider;
	private IRefactoringContext selectionInfo;
	private ExtractedMethodHelper extractMethodHelper;
	private Node selectedNodes;
	private Node enclosingNode;
	private MethodDefNode enclosingMethodNode;
	private PartialClassNodeWrapper enclosingClassNode;
	private Node rootNode;

	public ExtractMethodConfig(IDocumentProvider docProvider, IRefactoringContext selectionInfo) {
		this.docProvider = docProvider;
		this.selectionInfo = optimizeSelection(selectionInfo);
	}

	private IRefactoringContext optimizeSelection(IRefactoringContext selectionInfo) {
		int start = selectionInfo.getStartOffset();
		int end = selectionInfo.getEndOffset() + 1;
		String content = docProvider.getActiveFileContent();
		if (end > content.length()) end = content.length();
		if (end == start) return selectionInfo;
		String selectedText = content.substring(start, end);
		String trimedSelectionInformation = selectedText.trim();
		start += selectedText.indexOf(trimedSelectionInformation);
		end = start + trimedSelectionInformation.length() - 1;
		return new RefactoringContext(start, end, start, selectionInfo.getSource());
	}

	public IDocumentProvider getDocumentProvider() {
		return docProvider;
	}

	public ExtractedMethodHelper getHelper() {
		return extractMethodHelper;
	}

	public IRefactoringContext getSelection() {
		return selectionInfo;
	}

	public void setEnclosingScopeNode(Node enclosingScopeNode) {
		this.enclosingNode = enclosingScopeNode;
	}

	public void setEnclosingMethodNode(MethodDefNode enclosingMethodNode) {
		this.enclosingMethodNode = enclosingMethodNode;
	}

	public void setSelectedNodes(Node selectedNodes) {
		this.selectedNodes = selectedNodes;
	}

	public void setEnclosingClassNode(PartialClassNodeWrapper classNode) {
		this.enclosingClassNode = classNode;
	}

	public Node getSelectedNodes() {
		return selectedNodes;
	}

	public void setExtractedMethodHelper(ExtractedMethodHelper extractedMethodHelper) {
		this.extractMethodHelper = extractedMethodHelper;
	}

	public boolean hasEnclosingClassNode() {
		return enclosingClassNode != null;
	}
	
	public PartialClassNodeWrapper getEnclosingClassNode() {
		return enclosingClassNode;
	}

	public Node getEnclosingScopeNode() {
		return enclosingNode;
	}

	public MethodDefNode getEnclosingMethodNode() {
		return enclosingMethodNode;
	}

	public boolean hasEnclosingMethodNode() {
		return enclosingMethodNode != null;
	}

	public void setRootNode(Node rootNode) {
		this.rootNode = rootNode;
	}

	public Node getRootNode() {
		return rootNode;
	}

	public void setDocumentProvider(IDocumentProvider doc) {
		this.docProvider = doc;
	}

	public ExtractedMethodHelper getExtractMethodHelper() {
		return extractMethodHelper;
	}
}
