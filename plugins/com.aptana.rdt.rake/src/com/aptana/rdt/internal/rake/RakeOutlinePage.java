package com.aptana.rdt.internal.rake;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.jruby.ast.RootNode;
import org.rubypeople.rdt.core.IImportContainer;
import org.rubypeople.rdt.core.ILocalVariable;
import org.rubypeople.rdt.core.IParent;
import org.rubypeople.rdt.core.IRubyElement;
import org.rubypeople.rdt.core.IRubyScript;
import org.rubypeople.rdt.core.RubyModelException;
import org.rubypeople.rdt.internal.core.RubyBlock;
import org.rubypeople.rdt.internal.ui.RubyPlugin;
import org.rubypeople.rdt.internal.ui.rubyeditor.ASTProvider;
import org.rubypeople.rdt.internal.ui.rubyeditor.RubyOutlinePage;
import org.rubypeople.rdt.internal.ui.viewsupport.RubyElementImageProvider;
import org.rubypeople.rdt.ui.RubyElementLabels;
import org.rubypeople.rdt.ui.rubyeditor.ICustomRubyOutlinePage;
import org.rubypeople.rdt.ui.viewsupport.ImageDescriptorRegistry;

import com.aptana.rdt.rake.RakePlugin;

public class RakeOutlinePage extends RubyOutlinePage implements ICustomRubyOutlinePage
{

	protected ITreeContentProvider getContentProvider()
	{
		return new RakeChildrenProvider();
	}

	@Override
	protected IBaseLabelProvider getLabelProvider()
	{
		return new LabelProvider()
		{

			public String getText(Object element)
			{
				if (element instanceof Task)
				{
					return ((Task) element).getName();
				}
				if (element instanceof Namespace)
				{
					return ((Namespace) element).toString();
				}
				if (element instanceof IRubyElement)
				{
					return RubyElementLabels.getTextLabel(element, 0);
				}
				return super.getText(element);
			}

			public Image getImage(Object element)
			{
				ImageDescriptorRegistry registry = RubyPlugin.getImageDescriptorRegistry();
				ImageDescriptor descriptor = null;
				if (element instanceof Task)
				{
					descriptor = RakePlugin.imageDescriptorFromPlugin(RakePlugin.PLUGIN_ID,
							"icons/targetpublic_obj.gif");
				}
				else if (element instanceof Namespace)
				{
					descriptor = RakePlugin.imageDescriptorFromPlugin(RakePlugin.PLUGIN_ID, "icons/task_obj.gif");
				}
				else if (element instanceof IRubyElement)
				{
					descriptor = new RubyElementImageProvider().getRubyImageDescriptor((IRubyElement) element, 0);
				}

				if (descriptor != null)
				{
					return registry.get(descriptor);
				}
				return super.getImage(element);
			}
		};
	}

	class RakeChildrenProvider extends ChildrenProvider
	{
		public Object[] getElements(Object inputElement)
		{
			IRubyScript script = (IRubyScript) inputElement;
			RootNode root = ASTProvider.getASTProvider()
					.getAST(script, ASTProvider.WAIT_YES, new NullProgressMonitor());
			RakeStructureCreator rakeCreator = new RakeStructureCreator();
			rakeCreator.acceptNode(root);
			try
			{
				Object[] tasks = rakeCreator.getTasks();
				Object[] scriptChildren = filter(script.getChildren());
				Object[] all = new Object[tasks.length + scriptChildren.length];

				System.arraycopy(scriptChildren, 0, all, 0, scriptChildren.length);
				System.arraycopy(tasks, 0, all, scriptChildren.length, tasks.length);
				return all;
			}
			catch (RubyModelException e)
			{
				RakePlugin.log(e);
			}
			return rakeCreator.getTasks();
		}

		public boolean hasChildren(Object element)
		{
			if (element instanceof IRubyScript)
				return true;
			if (element instanceof Namespace)
				return true;
			if (element instanceof Task)
				return false;
			if (element instanceof IImportContainer)
				return true;
			return false;
		}

		public Object getParent(Object element)
		{
			if (element instanceof IRubyScript)
				return null;
			return null;
		}

		/**
		 * Remove local variables and blocks.
		 * 
		 * @param original
		 * @return
		 */
		private Object[] filter(Object[] original)
		{
			List<Object> filtered = new ArrayList<Object>();
			for (int i = 0; i < original.length; i++)
			{
				if ((original[i] instanceof ILocalVariable) || (original[i] instanceof RubyBlock))
				{
					continue;
				}
				filtered.add(original[i]);
			}
			return filtered.toArray(new Object[filtered.size()]);
		}

		public Object[] getChildren(Object parentElement)
		{
			if (parentElement instanceof Namespace)
			{
				return ((Namespace) parentElement).getChildren();
			}
			try
			{
				if (parentElement instanceof IParent)
					return filter(((IParent) parentElement).getChildren());
			}
			catch (RubyModelException e)
			{
				RakePlugin.log(e);
			}
			return new Object[0];
		}
	}

	public boolean isEnabled(IRubyElement inputElement)
	{
		if (inputElement == null)
			return false;
		IPath path = inputElement.getPath();
		if (path == null)
			return false;
		String name = path.lastSegment();
		return name.endsWith(".rake") || name.equals("Rakefile");
	}
}
