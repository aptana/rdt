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

package org.rubypeople.rdt.refactoring.core.overridemethod;

import java.util.Collection;

import org.jruby.ast.BlockNode;
import org.jruby.ast.DefnNode;
import org.jruby.ast.Node;
import org.rubypeople.rdt.refactoring.core.NodeFactory;
import org.rubypeople.rdt.refactoring.editprovider.InsertEditProvider;
import org.rubypeople.rdt.refactoring.nodewrapper.ClassNodeWrapper;
import org.rubypeople.rdt.refactoring.offsetprovider.ConstructorOffsetProvider;
import org.rubypeople.rdt.refactoring.offsetprovider.IOffsetProvider;
import org.rubypeople.rdt.refactoring.offsetprovider.MethodOffsetProvider;
import org.rubypeople.rdt.refactoring.signatureprovider.MethodSignature;

public class OverriddenMethod extends InsertEditProvider {
	private MethodSignature signature;

	private ClassNodeWrapper classNode;

	public OverriddenMethod(ClassNodeWrapper classNode, MethodSignature signature) {
		super(true);

		this.signature = signature;
		this.classNode = classNode;
		lastEditInGroup = false;
	}

	@Override
	protected int getOffset(String document) {
		IOffsetProvider offsetProvider;
		if (signature.isConstructor())
			offsetProvider = new ConstructorOffsetProvider(classNode, document);
		else
			offsetProvider = new MethodOffsetProvider(classNode, document);
		return offsetProvider.getOffset();
	}

	@Override
	protected BlockNode getInsertNode(int offset, String document) {
		boolean needsNewLineAtEndOfBlock = lastEditInGroup && !isNextLineEmpty(offset, document);
		BlockNode blockNode = NodeFactory.createBlockNode(needsNewLineAtEndOfBlock, getMethodNode());
		return blockNode;
	}

	private Node getMethodNode() {
		String methodName = signature.getMethodName();
		Collection<String> args = signature.getArguments();
		Node methodNode = getMethodNode(methodName, args);
		return methodNode;
	}

	private Node getMethodNode(String methodName, Collection<String> args) {
		//Node[] scopeContentNodes;
		//scopeContentNodes = new Node[] { NodeFactory.createSuperNode(args) };
		//ScopeNode scopeNode = NodeFactory.createScopeNode(args.toArray(new String[] {}), scopeContentNodes);
		//DefnNode methodNode = NodeFactory.createMethodNode(methodName, args, scopeNode);
		DefnNode methodNode = NodeFactory.createMethodNode(methodName, args.toArray(new String[args.size()]), NodeFactory.createSuperNode(args));
		return NodeFactory.createNewLineNode(methodNode);
	}
}
