/**
 * Copyright (c) 2008 Aptana, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl -v10.html. If redistributing this code,
 * this entire header must remain intact.
 */
package org.rubypeople.rdt.refactoring.core.extractconstant;

import org.jruby.ast.BlockNode;
import org.jruby.ast.ClassNode;
import org.jruby.ast.ModuleNode;
import org.jruby.ast.Node;
import org.jruby.ast.SClassNode;
import org.rubypeople.rdt.refactoring.core.IRefactoringContext;
import org.rubypeople.rdt.refactoring.core.NodeProvider;
import org.rubypeople.rdt.refactoring.core.SelectionNodeProvider;
import org.rubypeople.rdt.refactoring.editprovider.InsertEditProvider;
import org.rubypeople.rdt.refactoring.nodewrapper.RealClassNodeWrapper;
import org.rubypeople.rdt.refactoring.nodewrapper.SClassNodeWrapper;
import org.rubypeople.rdt.refactoring.offsetprovider.AfterNodeOffsetProvider;
import org.rubypeople.rdt.refactoring.offsetprovider.BeforeFirstMethodInClassOffsetProvider;

public class ExtractedConstantDef extends InsertEditProvider {

	private Node insertAfterNode;
	private ExtractConstantConfig config;

	public ExtractedConstantDef(ExtractConstantConfig config) {
		super(true);
		this.config = config;
		insertAfterNode = getNodeToInsertAfter(config.getRootNode(), config.getSelection());
		if (insertAfterNode == null) {
			setInsertType(INSERT_AT_BEGIN_OF_LINE);
		}
	}

	private Node getNodeToInsertAfter(Node rootNode, IRefactoringContext selection) {
		Node enclosingClassNode = SelectionNodeProvider.getEnclosingNode(rootNode, selection, ClassNode.class);
		if (enclosingClassNode != null) {
			return enclosingClassNode;
		}
		Node enclosingModuleNode = SelectionNodeProvider.getEnclosingNode(rootNode, selection, ModuleNode.class);
		if (enclosingModuleNode != null) {
			return enclosingModuleNode;
		}
		Node enclosingBlockNode = SelectionNodeProvider.getEnclosingNode(config.getRootNode(), config.getSelection(), BlockNode.class);
		if (enclosingBlockNode != null) {
			Node firstSelectedNode = (Node) config.getSelectedNodes();
			if (!firstSelectedNode.childNodes().isEmpty()) {
				firstSelectedNode = (Node) firstSelectedNode.childNodes().toArray()[0];
			}
			return NodeProvider.getNodeBefore(enclosingBlockNode, firstSelectedNode);
		}
		return null;
	}

	@Override
	protected Node getInsertNode(int offset, String document) {
		return config.getConstantDeclNode();
	}

	@Override
	protected int getOffset(String document) {
		if (insertAfterNode == null) {
			return 0;
		}
		if (insertAfterNode instanceof ClassNode) {
			return new BeforeFirstMethodInClassOffsetProvider(new RealClassNodeWrapper((ClassNode)insertAfterNode), config.getDocumentProvider().getActiveFileContent()).getOffset();
		}
		if (insertAfterNode instanceof SClassNode) {
			return new BeforeFirstMethodInClassOffsetProvider(new SClassNodeWrapper((SClassNode)insertAfterNode, config.getRootNode()), config.getDocumentProvider().getActiveFileContent()).getOffset();
		}
		if (insertAfterNode instanceof ModuleNode) {
			return new BeforeFirstMethodInClassOffsetProvider((ModuleNode) insertAfterNode, config.getDocumentProvider().getActiveFileContent()).getOffset();
		}
		return new AfterNodeOffsetProvider(insertAfterNode, document).getOffset();
	}

}
