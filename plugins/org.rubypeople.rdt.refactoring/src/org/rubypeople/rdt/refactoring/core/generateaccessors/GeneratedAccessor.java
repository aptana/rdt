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

package org.rubypeople.rdt.refactoring.core.generateaccessors;

import java.util.ArrayList;
import java.util.Collection;

import org.jruby.ast.BlockNode;
import org.jruby.ast.FCallNode;
import org.jruby.ast.Node;
import org.rubypeople.rdt.refactoring.core.NodeFactory;
import org.rubypeople.rdt.refactoring.editprovider.InsertEditProvider;
import org.rubypeople.rdt.refactoring.nodewrapper.AttrAccessorNodeWrapper;
import org.rubypeople.rdt.refactoring.nodewrapper.ClassNodeWrapper;
import org.rubypeople.rdt.refactoring.nodewrapper.VisibilityNodeWrapper;
import org.rubypeople.rdt.refactoring.offsetprovider.IOffsetProvider;

public class GeneratedAccessor extends InsertEditProvider
{

	public static final int TYPE_SIMPLE_ACCESSOR = 1;

	public static final int TYPE_METHOD_ACCESSOR = 2;

	public static final int DEFAULT_TYPE = TYPE_SIMPLE_ACCESSOR;

	public String definitionName;

	private int type;

	private String attrName;

	private ClassNodeWrapper classNode;

	public GeneratedAccessor(String definitionName, String instVarName, int type, ClassNodeWrapper classNode)
	{
		super(true);
		this.definitionName = definitionName;
		this.attrName = instVarName;
		this.type = type;
		this.classNode = classNode;
	}

	/**
	 * Visibility changed from private to public for testing.
	 */
	public boolean isWriter()
	{
		return definitionName.equals(AttrAccessorNodeWrapper.ATTR_WRITER);
	}

	/**
	 * Visibility changed from private to public for testing.
	 */
	public boolean isReader()
	{
		return definitionName.equals(AttrAccessorNodeWrapper.ATTR_READER);
	}

	/**
	 * Visibility changed from private to public for testing.
	 */
	public boolean isAccessor()
	{
		return definitionName.equals(AttrAccessorNodeWrapper.ATTR_ACCESSOR);
	}

	protected BlockNode getInsertNode(int offset, String document)
	{
		boolean needsNewLineAtEndOfBlock = lastEditInGroup && !isNextLineEmpty(offset, document);
		if (type == TYPE_SIMPLE_ACCESSOR)
		{
			return NodeFactory.createBlockNode(needsNewLineAtEndOfBlock, getSimpleInsertNode());
		}
		return NodeFactory.createBlockNode(needsNewLineAtEndOfBlock, getMethodInsertNode());
	}

	private Node getSimpleInsertNode()
	{
		FCallNode accessorNode = NodeFactory.createSimpleAccessorNode(definitionName, attrName);
		return NodeFactory.createNewLineNode(accessorNode);
	}

	private Node[] getMethodInsertNode()
	{
		Collection<Node> methodNodes = new ArrayList<Node>();
		if (isReader() || isAccessor())
			methodNodes.add(NodeFactory.createGetterSetter(attrName, false,
					VisibilityNodeWrapper.METHOD_VISIBILITY.PUBLIC));
		 if (isAccessor())
		{
			methodNodes.add(NodeFactory.createNewLineNode(null));
		}
		if (isWriter() || isAccessor())
			methodNodes.add(NodeFactory.createGetterSetter(attrName, true,
					VisibilityNodeWrapper.METHOD_VISIBILITY.PUBLIC));
		return methodNodes.toArray(new Node[methodNodes.size()]);
	}

	protected int getOffset(String document)
	{
		IOffsetProvider offsetProvider = new AccessorOffsetProvider(classNode, type, document);
		return offsetProvider.getOffset();
	}

	public String getInstVarName()
	{
		return attrName;
	}
}
