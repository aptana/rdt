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

import org.jruby.ast.Node;
import org.rubypeople.rdt.refactoring.editprovider.ReplaceEditProvider;

public class ExtractedConstantCall extends ReplaceEditProvider {

	private Node selected;
	private Node editNode;

	public ExtractedConstantCall(ExtractConstantConfig config) {
		this(config.getSelectedNodes(), config.getConstantCallNode());
	}

	public ExtractedConstantCall(Node toReplace, Node replacement) {
		super(false);
		this.selected = toReplace;
		editNode = replacement;
	}
	
	protected int getOffsetLength() {
		return getEndOffset() - getStartOffset();
	}

	private int getStartOffset() {
		return selected.getPosition().getStartOffset();
	}

	private int getEndOffset() {
		return selected.getPosition().getEndOffset();
	}

	protected Node getEditNode(int offset, String document) {
		return editNode;
	}

	protected int getOffset(String document) {
		return getStartOffset();
	}
}
