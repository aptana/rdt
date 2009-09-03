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

package org.rubypeople.rdt.astviewer.views;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.jruby.ast.CommentNode;
import org.jruby.ast.NewlineNode;
import org.jruby.ast.Node;
import org.jruby.common.NullWarnings;
import org.jruby.parser.DefaultRubyParser;
import org.jruby.parser.RubyParserPool;
import org.rubypeople.rdt.astviewer.Activator;
import org.rubypeople.rdt.internal.core.parser.RubyParser;
import org.rubypeople.rdt.internal.core.util.Util;
import org.rubypeople.rdt.internal.ui.rubyeditor.RubyEditor;

class ViewContentProvider implements IStructuredContentProvider, ITreeContentProvider
{
	private TreeParent invisibleRoot;

	private IViewSite viewSite;

	private RubyEditor editor;

	private DefaultRubyParser parser;

	private boolean showNewline;

	public ViewContentProvider(IViewSite viewSite)
	{
		this.viewSite = viewSite;
		parser = RubyParserPool.getInstance().borrowParser();
		parser.setWarnings(new NullWarnings());
	}

	public void inputChanged(@SuppressWarnings("unused") Viewer v, @SuppressWarnings("unused") Object oldInput,
			@SuppressWarnings("unused") Object newInput)
	{
	}

	public void dispose()
	{
		RubyParserPool.getInstance().returnParser(parser);
	}

	protected IFile getFile()
	{
		IEditorInput input = editor.getEditorInput();
		if (input instanceof IFileEditorInput)
		{
			return ((IFileEditorInput) input).getFile();
		}
		return null;
	}

	public Object[] getElements(Object parent)
	{
		if (parent.equals(viewSite))
		{
			if (invisibleRoot == null)
				initialize();
			return getChildren(invisibleRoot);
		}
		return getChildren(parent);
	}

	public Object getParent(Object child)
	{
		if (child instanceof TreeObject)
		{
			return ((TreeObject) child).getParent();
		}
		return null;
	}

	public Object[] getChildren(Object parent)
	{
		if (parent instanceof TreeParent)
		{
			return ((TreeParent) parent).getChildren();
		}
		return new Object[0];
	}

	public boolean hasChildren(Object parent)
	{
		if (parent instanceof TreeParent)
			return ((TreeParent) parent).hasChildren();
		return false;
	}

	public Node getRootNode()
	{
		try
		{
			return new RubyParser().parse(getName(), new String(Util.getInputStreamAsCharArray(getContents(), -1, null)))
					.getAST();
		}
		catch (CoreException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private String getName() throws CoreException
	{
		IFile file = getFile();
		if (file != null)
			return file.getName();
		IEditorInput input = editor.getEditorInput();
		if (input instanceof IStorageEditorInput)
		{
			IStorageEditorInput storageInput = (IStorageEditorInput) input;
			return storageInput.getStorage().getName();
		}
		return "";
	}

	private InputStream getContents() throws CoreException
	{
		IFile file = getFile();
		if (file != null)
			return file.getContents();
		IEditorInput input = editor.getEditorInput();
		if (input instanceof IStorageEditorInput)
		{
			IStorageEditorInput storageInput = (IStorageEditorInput) input;
			return storageInput.getStorage().getContents();
		}
		return null;
	}

	private void initialize()
	{
		updateContent();
	}

	private void buildTree(TreeParent parent, Node n)
	{

		if (n == null || n.isInvisible())
			return;

		if (!showNewline && n instanceof NewlineNode)
			n = ((NewlineNode) n).getNextNode();

		List<Node> children = new ArrayList<Node>(n.childNodes());
		children.addAll(n.getComments());
		if ((n instanceof CommentNode) || (children.size() <= 0))
		{
			parent.addChild(new TreeObject(n));
		}
		else
		{
			TreeParent newParent = new TreeParent(n);
			parent.addChild(newParent);
			for (Node childNode : children)
			{
				if (childNode.isInvisible())
					continue;
				buildTree(newParent, childNode);
			}
		}
	}

	private IWorkbenchPage getActiveWorkbenchPage()
	{
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
	}

	private boolean editorChanged()
	{
		if (getActiveWorkbenchPage() == null || !(getActiveWorkbenchPage().getActiveEditor() instanceof RubyEditor))
			return false;

		return getEditor() != (RubyEditor) getActiveWorkbenchPage().getActiveEditor();
	}

	public boolean forceUpdateContent()
	{
		if (getActiveWorkbenchPage() == null)
			return false;

		setEditor((RubyEditor) getActiveWorkbenchPage().getActiveEditor());

		invisibleRoot = new TreeParent(null);
		showNewline = Activator.getDefault().getPreferenceStore().getBoolean("showNewline");
		buildTree(invisibleRoot, getRootNode());

		return true;
	}

	public boolean updateContent()
	{

		if (editorChanged())
			return forceUpdateContent();
		else
			return false;
	}

	private void setEditor(RubyEditor editor)
	{
		this.editor = editor;
	}

	private RubyEditor getEditor()
	{
		return editor;
	}
}
