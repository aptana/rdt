package com.aptana.rdt.internal.ui.rspec;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.jruby.ast.RootNode;
import org.rubypeople.rdt.core.IImportContainer;
import org.rubypeople.rdt.core.IParent;
import org.rubypeople.rdt.core.IRubyElement;
import org.rubypeople.rdt.core.IRubyScript;
import org.rubypeople.rdt.core.RubyModelException;
import org.rubypeople.rdt.internal.ui.RubyPlugin;
import org.rubypeople.rdt.internal.ui.rubyeditor.ASTProvider;
import org.rubypeople.rdt.internal.ui.rubyeditor.RubyOutlinePage;
import org.rubypeople.rdt.internal.ui.viewsupport.RubyElementImageProvider;
import org.rubypeople.rdt.ui.RubyElementLabels;
import org.rubypeople.rdt.ui.rubyeditor.ICustomRubyOutlinePage;
import org.rubypeople.rdt.ui.viewsupport.ImageDescriptorRegistry;

import com.aptana.rdt.core.rspec.Behavior;
import com.aptana.rdt.core.rspec.Example;
import com.aptana.rdt.core.rspec.RSpecStructureCreator;
import com.aptana.rdt.ui.AptanaRDTUIPlugin;

public class RSpecOutlinePage extends RubyOutlinePage implements ICustomRubyOutlinePage
{

	private static final String SPEC_FILENAME_SUFFIX = "_spec.rb";

	protected ITreeContentProvider getContentProvider()
	{
		return new RSpecChildrenProvider();
	}

	@Override
	protected IBaseLabelProvider getLabelProvider()
	{
		return new LabelProvider()
		{

			public String getText(Object element)
			{
				if (element instanceof Example)
				{
					return ((Example) element).getDescription();
				}
				if (element instanceof Behavior)
				{
					return ((Behavior) element).getClassName();
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
				if (element instanceof Example)
				{
					descriptor = RubyElementImageProvider.getMethodImageDescriptor(0);
				}
				else if (element instanceof Behavior)
				{
					descriptor = RubyElementImageProvider.getTypeImageDescriptor(false, false, false);
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

	class RSpecChildrenProvider extends ChildrenProvider
	{
		public Object[] getElements(Object inputElement)
		{
			IRubyScript script = (IRubyScript) inputElement;
			RootNode root = ASTProvider.getASTProvider()
					.getAST(script, ASTProvider.WAIT_YES, new NullProgressMonitor());
			RSpecStructureCreator rspecCreator = new RSpecStructureCreator();
			rspecCreator.acceptNode(root);

			Object[] behaviors = rspecCreator.getBehaviors();
			Object[] all = new Object[behaviors.length + 1];
			all[0] = script.getImportContainer();
			System.arraycopy(behaviors, 0, all, 1, behaviors.length);
			return all;
		}

		public boolean hasChildren(Object element)
		{
			if (element instanceof IRubyScript)
				return true;
			if (element instanceof Behavior)
				return true;
			if (element instanceof Example)
				return false;
			if (element instanceof IImportContainer)
				return true;
			return false;
		}

		public Object getParent(Object element)
		{
			if (element instanceof IRubyScript)
				return null;
			if (element instanceof Example)
				return ((Example) element).getBehavior();

			return null;
		}

		public Object[] getChildren(Object parentElement)
		{
			if (parentElement instanceof Behavior)
				return ((Behavior) parentElement).getExamples();
			try
			{
				if (parentElement instanceof IParent)
					return ((IParent) parentElement).getChildren();
			}
			catch (RubyModelException e)
			{
				AptanaRDTUIPlugin.log(e);
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
		return name.endsWith(SPEC_FILENAME_SUFFIX);
	}
}
