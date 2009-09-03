/**
 * Copyright (c) 2008 Aptana, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl -v10.html. If redistributing this code,
 * this entire header must remain intact.
 */
package org.rubypeople.rdt.refactoring.core.pullup;

import java.util.ArrayList;
import java.util.Collection;

import org.jruby.ast.BlockNode;
import org.jruby.ast.Node;
import org.jruby.lexer.yacc.IDESourcePosition;
import org.rubypeople.rdt.refactoring.core.NodeFactory;
import org.rubypeople.rdt.refactoring.core.pushdown.NewClassOffsetProvier;
import org.rubypeople.rdt.refactoring.editprovider.InsertEditProvider;
import org.rubypeople.rdt.refactoring.nodewrapper.MethodNodeWrapper;
import org.rubypeople.rdt.refactoring.offsetprovider.IOffsetProvider;

public class UpPulledMethodsClass extends InsertEditProvider {
	private Collection<MethodNodeWrapper> methodNodes;

	private Collection<MethodNodeWrapper> constructorNodes;

	private String className;

	public UpPulledMethodsClass(String className, Collection<MethodNodeWrapper> allMethodNodes) {
		super(true);
		this.className = className;
		initConstrucorAndMethodNodes(allMethodNodes);
	}

	private void initConstrucorAndMethodNodes(Collection<MethodNodeWrapper> allMethodNodes) {
		methodNodes = new ArrayList<MethodNodeWrapper>();
		constructorNodes = new ArrayList<MethodNodeWrapper>();
		for (MethodNodeWrapper node : allMethodNodes) {
			if (node.getSignature().isConstructor())
				constructorNodes.add(node);
			else
				methodNodes.add(node);
		}
	}

	@Override
	protected BlockNode getInsertNode(int offset, String document) {
		if (firstEditInGroup) {
			setInsertType(INSERT_AT_BEGIN_OF_LINE);
		}
		boolean needsNewLineAtEndOfBlock = lastEditInGroup && !isNextLineEmpty(offset, document);
		Node classNode = getClassNode();

		BlockNode blockNode = NodeFactory.createBlockNode();
		blockNode.add(classNode);
		if (!firstEditInGroup)
			blockNode.add(NodeFactory.createNewLineNode(null));
		if (needsNewLineAtEndOfBlock)
			blockNode.add(NodeFactory.createNewLineNode(null));
		return blockNode;
	}

	private Node getClassNode() {
		return NodeFactory.createNewLineNode(NodeFactory.createClassNode(className, getBody()));
	}

	private Node getBody() {
		BlockNode body = new BlockNode(new IDESourcePosition());
		body.add(NodeFactory.createNewLineNode(null));
		for (MethodNodeWrapper constructor : constructorNodes) {
			body.add(NodeFactory.createNewLineNode(constructor.getWrappedNode()));
		}
		for (MethodNodeWrapper method : methodNodes) {
			body.add(NodeFactory.createNewLineNode(method.getWrappedNode()));
		}
		body.add(NodeFactory.createNewLineNode(null));
		return body;
	}

	@Override
	protected int getOffset(String document) {
		IOffsetProvider offsetProvider = new NewClassOffsetProvier();
		return offsetProvider.getOffset();
	}
}
