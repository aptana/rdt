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

package org.rubypeople.rdt.refactoring.core.movemethod;

import java.util.Collection;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;
import org.jruby.ast.MethodDefNode;
import org.jruby.ast.Node;
import org.jruby.ast.SelfNode;
import org.jruby.parser.LocalStaticScope;
import org.rubypeople.rdt.core.formatter.ReWriteVisitor;
import org.rubypeople.rdt.refactoring.core.NodeFactory;
import org.rubypeople.rdt.refactoring.core.NodeProvider;
import org.rubypeople.rdt.refactoring.editprovider.InsertEditProvider;
import org.rubypeople.rdt.refactoring.nodewrapper.ArgsNodeWrapper;
import org.rubypeople.rdt.refactoring.nodewrapper.FieldNodeWrapper;
import org.rubypeople.rdt.refactoring.nodewrapper.MethodCallNodeWrapper;
import org.rubypeople.rdt.refactoring.nodewrapper.MethodNodeWrapper;
import org.rubypeople.rdt.refactoring.nodewrapper.PartialClassNodeWrapper;
import org.rubypeople.rdt.refactoring.nodewrapper.VisibilityNodeWrapper.METHOD_VISIBILITY;
import org.rubypeople.rdt.refactoring.offsetprovider.AfterLastMethodInClassOffsetProvider;
import org.rubypeople.rdt.refactoring.offsetprovider.IOffsetProvider;
import org.rubypeople.rdt.refactoring.util.NodeUtil;

public class InsertMethodEditProvider extends InsertEditProvider
{

	private MoveMethodConfig config;

	private IOffsetProvider offsetProvider;

	private PartialClassNodeWrapper classPart;

	public InsertMethodEditProvider(MoveMethodConfig config, PartialClassNodeWrapper classPart)
	{
		super(true);
		this.config = config;
		this.classPart = classPart;
	}

	@Override
	protected Node getInsertNode(int offset, String document)
	{
		boolean needsNewLineAtEndOfBlock = !isNextLineEmpty(offset, document);
		Node insertMethodNode = NodeFactory.createNewLineNode(getReferenceReplacedMethodNode(document));
		if (!config.getMethodNode().isClassMethod())
		{
			METHOD_VISIBILITY aktVisibility = classPart.getPosVisibility(offsetProvider.getOffset());
			METHOD_VISIBILITY requiredVisibility = config.getMovedMethodVisibility();
			if (!aktVisibility.equals(requiredVisibility))
			{
				Node visibilityNode = NodeFactory.createVisibilityNode(requiredVisibility, config.getMovedMethodName());
				return NodeFactory.createBlockNode(needsNewLineAtEndOfBlock, new Node[] { insertMethodNode,
						visibilityNode });
			}
		}
		return NodeFactory.createBlockNode(needsNewLineAtEndOfBlock, new Node[] { insertMethodNode });
	}

	private MethodDefNode getReferenceReplacedMethodNode(String document)
	{
		Node documentNode = NodeFactory.createNewLineNode(config.getMethodNode().getWrappedNode());
		String docStr = ReWriteVisitor.createCodeFromNode(documentNode, document, getFormatHelper());
		Document doc = new Document(docStr);
		try
		{
			getFieldInsertionEdit(docStr).apply(doc);
		}
		catch (MalformedTreeException e)
		{
			e.printStackTrace();
		}
		catch (BadLocationException e)
		{
			e.printStackTrace();
		}
		MethodDefNode methodNode = (MethodDefNode) NodeProvider.unwrap(NodeProvider
				.getRootNode("tempFile", doc.get()).getBodyNode()); //$NON-NLS-1$
		return createMethodNodeWithAdditionalArg(methodNode);
	}

	private MethodDefNode createMethodNodeWithAdditionalArg(MethodDefNode methodNode)
	{
		MethodDefNode resultMethod;
		ArgsNodeWrapper args = config.getMovedMethodArgs();
		if (config.getMethodNode().isClassMethod())
		{
			String destClassName = config.getDestinationClassNode().getName();
			resultMethod = NodeFactory.createStaticMethodNode(destClassName, config.getMovedMethodName(), args
					.getWrappedNode(), new LocalStaticScope(null), methodNode.getBodyNode());
		}
		else
		{
			resultMethod = NodeFactory.createMethodNodeWithoutNewline(config.getMovedMethodName(), args
					.getWrappedNode(), methodNode.getBodyNode());
		}

		resultMethod.addComments(methodNode.getComments());
		resultMethod.setPosition(methodNode.getPosition());
		return resultMethod;
	}

	private TextEdit getFieldInsertionEdit(String docStr)
	{
		MultiTextEdit multiEdit = new MultiTextEdit();
		Node rootNode = NodeProvider.getRootNode("tmpFile", docStr); //$NON-NLS-1$
		Collection<MethodCallNodeWrapper> callNodes = NodeProvider.getMethodCallNodes(rootNode);
		for (MethodCallNodeWrapper aktCallNode : callNodes)
		{
			addTextEditIfNeeded(multiEdit, aktCallNode);
		}
		Collection<FieldNodeWrapper> fieldNodes = NodeProvider.getFieldNodes(rootNode);
		for (FieldNodeWrapper aktFieldNode : fieldNodes)
		{
			addTextEditIfNeeded(multiEdit, aktFieldNode);
		}
		return multiEdit;
	}

	private void addTextEditIfNeeded(MultiTextEdit multiEdit, FieldNodeWrapper aktFieldNode)
	{
		if (aktFieldNode.getNodeType() == FieldNodeWrapper.SYMBOL_NODE)
		{
			return;
		}
		if (aktFieldNode.isInstVar())
		{
			int insertPos = aktFieldNode.getPosition().getStartOffset();
			int length;
			String insertText;
			if (aktFieldNode.getName().equals(config.getFieldInSourceClassOfTypeDestinationClass()))
			{
				insertText = "self"; //$NON-NLS-1$
				length = aktFieldNode.getPosition().getEndOffset() - insertPos;
			}
			else
			{
				insertText = config.getFieldInDestinationClassOfTypeSourceClass() + '.';
				length = 1; // replacing the @
			}
			try
			{
				multiEdit.addChild(new ReplaceEdit(insertPos, length, insertText));
			}
			catch (MalformedTreeException mte)
			{
				// FIXME ignore for now
			}
		}
	}

	private void addTextEditIfNeeded(MultiTextEdit multiEdit, MethodCallNodeWrapper aktCallNode)
	{
		int insertPos = aktCallNode.getPosition().getStartOffset();
		String insertText = config.getFieldInDestinationClassOfTypeSourceClass() + "."; //$NON-NLS-1$
		if (aktCallNode.isCallNode())
		{
			Node receiverNode = aktCallNode.getReceiverNode();
			if (NodeUtil.nodeAssignableFrom(receiverNode, SelfNode.class)
					&& !isCallToMovingMethod(aktCallNode.getName()))
			{
				int length = receiverNode.getPosition().getEndOffset() - insertPos;
				multiEdit.addChild(new ReplaceEdit(insertPos, length, config
						.getFieldInDestinationClassOfTypeSourceClass()));
			}
		}
		else if (isCallToSourceClass(aktCallNode.getName()))
		{

			multiEdit.addChild(new InsertEdit(insertPos, insertText));
		}
	}

	private boolean isCallToMovingMethod(String callName)
	{
		return config.getMethodNode().getName().equals(callName);
	}

	private boolean isCallToSourceClass(String callName)
	{
		if (isCallToMovingMethod(callName))
		{
			return false;
		}
		Collection<MethodNodeWrapper> methodNodes = config.getSourceClassNode().getMethods();
		for (MethodNodeWrapper aktMethod : methodNodes)
		{
			if (aktMethod.getName().equals(callName))
			{
				return true;
			}
		}
		return false;
	}

	@Override
	protected int getOffset(String document)
	{
		if (offsetProvider == null)
		{
			offsetProvider = new AfterLastMethodInClassOffsetProvider(config.getDestinationClassNode(), document);
		}
		return offsetProvider.getOffset();
	}

}
