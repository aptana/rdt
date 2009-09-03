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

package org.rubypeople.rdt.refactoring.nodewrapper;

import java.util.ArrayList;
import java.util.Collection;

import org.jruby.ast.DefsNode;
import org.jruby.ast.MethodDefNode;
import org.jruby.ast.Node;
import org.jruby.ast.SymbolNode;
import org.jruby.lexer.yacc.ISourcePosition;
import org.rubypeople.rdt.refactoring.core.NodeProvider;
import org.rubypeople.rdt.refactoring.signatureprovider.MethodSignature;
import org.rubypeople.rdt.refactoring.util.Constants;

public class MethodNodeWrapper implements INodeWrapper {

	protected MethodDefNode methodNode;
	private final ClassNodeWrapper containingClass;
	
	public MethodNodeWrapper(MethodDefNode methodDef, ClassNodeWrapper containingClass) {
		this.methodNode = methodDef;
		this.containingClass = containingClass;
	}

	public String getName() {
		return methodNode.getName();
	}

	public MethodSignature getSignature() {
		return new MethodSignature(methodNode.getName(), getArgsNode());
	}

	public ArgsNodeWrapper getArgsNode() {
		return new ArgsNodeWrapper(methodNode.getArgsNode());
	}

	public MethodDefNode getWrappedNode() {
		return methodNode;
	}
	
	public boolean isClassMethod() {
		return methodNode instanceof DefsNode;
	}
	
	public Collection<MethodCallNodeWrapper> getMethodCallNodes() {
		return NodeProvider.getMethodCallNodes(methodNode);
	}

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + (isClassMethod() ? 1231 : 1237);
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof MethodNodeWrapper) {
			MethodNodeWrapper otherMethodNode = (MethodNodeWrapper) obj;
			return getSignature().equals(otherMethodNode.getSignature());
		}
		return false;
	}

	public boolean isConstructor() {
		return getName().equals(Constants.CONSTRUCTOR_NAME);
	}
	
	public ISourcePosition getPosition() {
		return methodNode.getPosition();
	}

	public String[] getLocalNames() {
		return methodNode.getScope().getVariables();
	}

	public Node getBodyNode() {
		return methodNode.getBodyNode();
	}
	
	public Collection<MethodCallNodeWrapper> getCallCandidatesInClass(ClassNodeWrapper classNode) {
		if(classNode == null){
			return new ArrayList<MethodCallNodeWrapper>();
		}	
		return classNode.getMethodCalls(methodNode);
	}

	public Collection<SymbolNode> getSymbolCandidatesInClass(ClassNodeWrapper classNode) {
		if(classNode == null){
			return new ArrayList<SymbolNode>();
		}
		return classNode.getMethodSymbols(methodNode);
	}
	
	public boolean isAccessor() {
		return isWriter() || isReader();
	}	
	
	public boolean isWriter() {
		if(containingClass == null) {
			return false;
		}
		for (FieldNodeWrapper field : containingClass.getFields()) {
			if ((field.getNameWithoutAts() + "=").equals(getName()) && getSignature().getArguments().size() == 1) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isReader() {
		if(containingClass == null) {
			return false;
		}
		for (FieldNodeWrapper field : containingClass.getFields()) {
			if(field.getNameWithoutAts().equals(getName()) && getSignature().getArguments().isEmpty()) {
				return true;
			}
		}
		return false;
	}
}
