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
import org.rubypeople.rdt.refactoring.core.NodeFactory;
import org.rubypeople.rdt.refactoring.editprovider.InsertEditProvider;
import org.rubypeople.rdt.refactoring.nodewrapper.ClassNodeWrapper;
import org.rubypeople.rdt.refactoring.nodewrapper.MethodNodeWrapper;
import org.rubypeople.rdt.refactoring.offsetprovider.ConstructorOffsetProvider;
import org.rubypeople.rdt.refactoring.offsetprovider.IOffsetProvider;
import org.rubypeople.rdt.refactoring.offsetprovider.MethodOffsetProvider;

public class UpPulledMethods extends InsertEditProvider {

	private Collection<MethodNodeWrapper> methodNodes;

	private boolean constructors;

	private ClassNodeWrapper classNode;

	public UpPulledMethods(Collection<MethodNodeWrapper> methodNodes, ClassNodeWrapper classNode, boolean constructors) {
		super(true);
		this.methodNodes = methodNodes;
		this.constructors = constructors;
		this.classNode = classNode;
	}

	@Override
	protected BlockNode getInsertNode(int offset, String document) {
		boolean needsNewLineAtEndOfBlock = lastEditInGroup && !isNextLineEmpty(offset, document);
		return NodeFactory.createBlockNode(needsNewLineAtEndOfBlock, getMethodNodes(methodNodes));
	}

	private Node[] getMethodNodes(Collection<MethodNodeWrapper> nodeCollection) {
		Collection<Node> nodes = new ArrayList<Node>();
		boolean first = true;
		for (MethodNodeWrapper methodNode : nodeCollection) {
			if (first) {
				first = false;
			} else {
				nodes.add(NodeFactory.createNewLineNode(null));
			}
			nodes.add(NodeFactory.createNewLineNode(methodNode.getWrappedNode()));
		}
		return nodes.toArray(new Node[nodes.size()]);
	}

	@Override
	protected int getOffset(String document) {
		IOffsetProvider offsetProvider;
		if (constructors) {
			offsetProvider = new ConstructorOffsetProvider(classNode, document);
		} else {
			offsetProvider = new MethodOffsetProvider(classNode, document);
		}
		return offsetProvider.getOffset();
	}
}
