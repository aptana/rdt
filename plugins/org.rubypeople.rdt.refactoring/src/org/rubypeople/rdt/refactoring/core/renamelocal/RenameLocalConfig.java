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

package org.rubypeople.rdt.refactoring.core.renamelocal;

import org.jruby.ast.MethodDefNode;
import org.jruby.ast.Node;
import org.jruby.ast.types.INameNode;
import org.rubypeople.rdt.refactoring.core.IRefactoringConfig;
import org.rubypeople.rdt.refactoring.documentprovider.IDocumentProvider;

public class RenameLocalConfig implements IRefactoringConfig {

	private IDocumentProvider docProvider;
	private int caretPosition;
	private Node selectedNode;
	private Node selectedMethod;
	private String[] localNames;
	private RenameLocalEditProvider editProvider;

	public RenameLocalConfig(IDocumentProvider docProvider, int caretPosition) {
		this.docProvider = docProvider;
		this.caretPosition = caretPosition;
	}
	
	public String getSelectedNodeName() {
		if (selectedNode instanceof INameNode) {
			return ((INameNode) selectedNode).getName();
		} else if (selectedNode == null && selectedMethod instanceof MethodDefNode) {
			return localNames[((MethodDefNode) selectedMethod).getArgsNode().getRestArg()];
		}
		return ""; //$NON-NLS-1$
	}

	public IDocumentProvider getDocumentProvider() {
		return docProvider;
	}

	public int getCaretPosition() {
		return caretPosition;
	}

	public boolean hasSelectedMethod() {
		return selectedMethod != null;
	}

	public boolean hasLocalNames() {
		return localNames.length > 2;
	}

	public Node getSelectedNode() {
		return selectedNode;
	}

	public Node getSelectedMethod() {
		return selectedMethod;
	}

	public String[] getLocalNames() {
		return localNames;
	}

	public void setLocalVariablesEditProvider(RenameLocalEditProvider editProvider) {
		this.editProvider = editProvider;
	}
	
	public RenameLocalEditProvider getRenameEditProvider() {
		return editProvider;
	}

	public void setSelectedNode(Node selectedNode) {
		this.selectedNode = selectedNode;
	}

	public void setSelectedMethod(Node selectedMethod) {
		this.selectedMethod = selectedMethod;
	}

	public void setLocalNames(String[] localNames) {
		this.localNames = localNames.clone();
	}

	public boolean hasSelectedNode() {
		return selectedNode != null;
	}

	public void setDocumentProvider(IDocumentProvider docProvider) {
		this.docProvider = docProvider;
	}

}
