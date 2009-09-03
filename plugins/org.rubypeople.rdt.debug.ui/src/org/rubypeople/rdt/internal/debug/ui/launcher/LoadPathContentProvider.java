package org.rubypeople.rdt.internal.debug.ui.launcher;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.rubypeople.rdt.core.ILoadpathEntry;
import org.rubypeople.rdt.launching.IVMInstall;
import org.rubypeople.rdt.launching.RubyRuntime;

public class LoadPathContentProvider implements IStructuredContentProvider
{

	public void dispose()
	{
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
	{
	}

	public Object[] getElements(Object inputElement)
	{
		if (!(inputElement instanceof ILoadpathEntry[]))
			return null;
		List<Object> children = new ArrayList<Object>();
		IVMInstall vm = RubyRuntime.getDefaultVMInstall();
		IPath[] libraryLocations = vm.getLibraryLocations();
		ILoadpathEntry[] entries = (ILoadpathEntry[]) inputElement;
		for (ILoadpathEntry loadpathEntry : entries)
		{
			if (loadpathEntry.getEntryKind() == ILoadpathEntry.CPE_LIBRARY
					&& contains(libraryLocations, loadpathEntry.getPath()))
			{
				// Filter it out!
				continue;
			}
			children.add(loadpathEntry);
		}
		return (Object[]) children.toArray(new Object[children.size()]);
	}

	private boolean contains(IPath[] libraryLocations, IPath path)
	{
		for (IPath libraryPath : libraryLocations)
		{
			if (libraryPath.equals(path))
				return true;
		}
		return false;
	}

}
