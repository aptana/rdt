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

package org.rubypeople.rdt.refactoring.core.inlinemethod;

import java.util.Collection;

import org.jruby.ast.MethodDefNode;
import org.jruby.ast.Node;
import org.rubypeople.rdt.refactoring.core.IRefactoringConfig;
import org.rubypeople.rdt.refactoring.documentprovider.DocumentProvider;
import org.rubypeople.rdt.refactoring.documentprovider.IDocumentProvider;
import org.rubypeople.rdt.refactoring.nodewrapper.MethodCallNodeWrapper;

public class InlineMethodConfig implements IRefactoringConfig {
	
	private Boolean singleReturnStatement;
	private MethodCallNodeWrapper selectedCall;
	private String className;
	private MethodDefNode methodDefinitionNode;
	private DocumentProvider methodDefDoc;
	private IDocumentProvider originalDocument;
	private int pos;
	private ITargetClassFinder targetClassFinder;
	private Collection<String> usedMembers;
	private Node callParent;

	public InlineMethodConfig(DocumentProvider doc, int pos, ITargetClassFinder targetClassFinder) {
		originalDocument = doc;
		this.pos = pos;
		this.targetClassFinder = targetClassFinder;
	}

	public Node getCallParent() {
		return callParent;
	}
	
	public void setCallParent(Node cellParent) {
		this.callParent = cellParent;
	}

	public String getClassName() {
		return className;
	}

	public DocumentProvider getMethodDefDoc() {
		return methodDefDoc;
	}

	public MethodDefNode getMethodDefinitionNode() {
		return methodDefinitionNode;
	}

	public MethodCallNodeWrapper getSelectedCall() {
		return selectedCall;
	}

	public Boolean isSingleReturnStatement() {
		return singleReturnStatement;
	}

	public int getPos() {
		return pos;
	}

	public ITargetClassFinder getTargetClassFinder() {
		return targetClassFinder;
	}

	public IDocumentProvider getDocumentProvider() {
		return originalDocument;
	}

	public void setMethodDefDoc(DocumentProvider methodDefDoc) {
		this.methodDefDoc = methodDefDoc;
	}

	public void setSelectedCall(MethodCallNodeWrapper selectedCall) {
		this.selectedCall = selectedCall;
	}

	public void setSingleReturnStatement(Boolean singleReturnStatement) {
		this.singleReturnStatement = singleReturnStatement;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public void setMethodDefinitionNode(MethodDefNode methodDefinitionNode) {
		this.methodDefinitionNode = methodDefinitionNode;
	}

	public void setUsedMembers(Collection<String> usedMembers) {
		this.usedMembers = usedMembers;
	}

	public Collection<String> getUsedMembers() {
		return usedMembers;
	}

	public void setDocumentProvider(IDocumentProvider doc) {
		this.originalDocument = doc;
	}
}
