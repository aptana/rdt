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

package org.rubypeople.rdt.refactoring.core.encapsulatefield;

import java.util.ArrayList;
import java.util.Collection;

import org.jruby.ast.BlockNode;
import org.jruby.ast.CommentNode;
import org.jruby.ast.FCallNode;
import org.jruby.ast.Node;
import org.jruby.lexer.yacc.IDESourcePosition;
import org.rubypeople.rdt.refactoring.core.NodeFactory;
import org.rubypeople.rdt.refactoring.editprovider.DeleteEditProvider;
import org.rubypeople.rdt.refactoring.editprovider.EditProvider;
import org.rubypeople.rdt.refactoring.editprovider.InsertEditProvider;
import org.rubypeople.rdt.refactoring.editprovider.MultiEditProvider;
import org.rubypeople.rdt.refactoring.nodewrapper.AttrAccessorNodeWrapper;
import org.rubypeople.rdt.refactoring.nodewrapper.VisibilityNodeWrapper;
import org.rubypeople.rdt.refactoring.offsetprovider.AfterLastMethodInClassOffsetProvider;
import org.rubypeople.rdt.refactoring.offsetprovider.IOffsetProvider;

public class FieldEncapsulator extends MultiEditProvider
{

	public static enum ACCESSOR
	{
		ATTR_ACCESSOR, PROTECTED, PUBLIC
	}

	private VisibilityNodeWrapper.METHOD_VISIBILITY readerVisibility;

	private VisibilityNodeWrapper.METHOD_VISIBILITY writerVisibility;

	private EncapsulateFieldConfig config;

	public FieldEncapsulator(EncapsulateFieldConfig config)
	{
		this.config = config;
		initVisibilities();
		initGenerationFields();
	}

	private void initVisibilities()
	{
		writerVisibility = (!config.hasSelectedAccessor() || !config.getSelectedAccessor().isWriter() ? VisibilityNodeWrapper.METHOD_VISIBILITY.PRIVATE
				: VisibilityNodeWrapper.METHOD_VISIBILITY.PUBLIC);
		readerVisibility = (!config.hasSelectedAccessor() || !config.getSelectedAccessor().isReader() ? VisibilityNodeWrapper.METHOD_VISIBILITY.PRIVATE
				: VisibilityNodeWrapper.METHOD_VISIBILITY.PUBLIC);
	}

	private void initGenerationFields()
	{
		config.setReaderGenerationDisabled(isReaderGenerationOptional());
		config.setWriterGenerationDisabled(isWriterGenerationOptional());
	}

	@Override
	public Collection<EditProvider> getEditProviders()
	{
		Collection<EditProvider> providers = new ArrayList<EditProvider>();
		if (!config.isReaderGenerationDisabled())
		{
			providers.add(getInsertEditProvider(false, readerVisibility));
		}
		if (!config.isWriterGenerationDisabled())
		{
			providers.add(getInsertEditProvider(true, writerVisibility));
		}
		if (config.hasSelectedAccessor())
		{
			for (FCallNode aktAccessorNode : config.getSelectedAccessor().getAccessorNodes())
			{
				providers.add(new DeleteEditProvider(aktAccessorNode));
			}
		}
		return providers;
	}

	private InsertEditProvider getInsertEditProvider(final boolean writer,
			final VisibilityNodeWrapper.METHOD_VISIBILITY visibility)
	{
		return new InsertEditProvider(true)
		{

			@Override
			protected Node getInsertNode(int offset, String document)
			{
				boolean needsNewLineAtEndOfBlock = lastEditInGroup && !isNextLineEmpty(offset, document);
				Node contentNode = createGetterOrSetter(writer, visibility);
				return NodeFactory.createBlockNode(needsNewLineAtEndOfBlock, contentNode);
			}

			@Override
			protected int getOffset(String document)
			{
				IOffsetProvider offsetProvider = new AfterLastMethodInClassOffsetProvider(config
						.getEnclosingClassNode(), document);
				return offsetProvider.getOffset();
			}
		};
	}

	public String getSelectedFieldName()
	{
		return (config.hasSelectedAccessor()) ? '@' + config.getSelectedAccessor().getAttrName() : config
				.getSelectedInstNode().getName();
	}

	public String getExistingAccessorName()
	{

		return (config.hasSelectedAccessor()) ? config.getSelectedAccessor().getAccessorTypeName() : "none"; //$NON-NLS-1$
	}

	public boolean isWriterGenerationOptional()
	{
		return writerVisibility.equals(VisibilityNodeWrapper.METHOD_VISIBILITY.PRIVATE);
	}

	public VisibilityNodeWrapper.METHOD_VISIBILITY getWriterVisibility()
	{
		return writerVisibility;
	}

	public boolean isReaderGenerationOptional()
	{
		return readerVisibility.equals(VisibilityNodeWrapper.METHOD_VISIBILITY.PRIVATE);
	}

	public VisibilityNodeWrapper.METHOD_VISIBILITY getReaderVisibility()
	{
		return readerVisibility;
	}

	public void setWriterDisabled(boolean writerDisabled)
	{
		config.setWriterGenerationDisabled(writerDisabled);
	}

	public void setWriterVisibility(VisibilityNodeWrapper.METHOD_VISIBILITY visibility)
	{
		writerVisibility = visibility;
	}

	public void setReaderVisibility(VisibilityNodeWrapper.METHOD_VISIBILITY visibility)
	{
		readerVisibility = visibility;
	}

	public void setReaderDisabled(boolean readerDisabled)
	{
		config.setReaderGenerationDisabled(readerDisabled);
	}

	protected BlockNode createGetterOrSetter(final boolean writer,
			final VisibilityNodeWrapper.METHOD_VISIBILITY visibility)
	{
		return NodeFactory.createGetterSetter(config.getFieldName(), writer, visibility, getAccessorComments(writer));
	}

	private Collection<CommentNode> getAccessorComments(boolean isSetter)
	{

		ArrayList<CommentNode> matchingComments = new ArrayList<CommentNode>();
		AttrAccessorNodeWrapper accessor = config.getSelectedAccessor();
		if (accessor != null)
		{
			Collection<FCallNode> accessors = accessor.getAccessorNodes();

			for (FCallNode currentAccessor : accessors)
			{
				if (isSetter && currentAccessor.getName().equals("attr_writer"))
				{
					matchingComments.addAll(currentAccessor.getComments());
				}
				else if (!isSetter && currentAccessor.getName().equals("attr_reader"))
				{
					matchingComments.addAll(currentAccessor.getComments());
				}
				else if (currentAccessor.getName().equals("attr_accessor"))
				{
					matchingComments.addAll(currentAccessor.getComments());
				}
			}

		}
		return resetCommentPositions(matchingComments);
	}

	private Collection<CommentNode> resetCommentPositions(ArrayList<CommentNode> comments)
	{
		ArrayList<CommentNode> resettedComments = new ArrayList<CommentNode>();
		for (CommentNode currentComment : comments)
		{
			resettedComments
					.add(new CommentNode(new IDESourcePosition("", -1, -1, -1, -1), currentComment.getContent()));
		}
		return resettedComments;
	}
}
