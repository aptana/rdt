package org.rubypeople.rdt.internal.debug.ui.launcher;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.rubypeople.rdt.core.ILoadpathEntry;
import org.rubypeople.rdt.internal.debug.ui.RdtDebugUiPlugin;

public class LoadPathEntryLabelProvider implements ILabelProvider {

	/**
	 * @see ILabelProvider#getImage(Object)
	 */
	public Image getImage(Object element) {
		return null;
	}

	/**
	 * @see ILabelProvider#getText(Object)
	 */
	public String getText(Object element) {
		if (element != null && element instanceof ILoadpathEntry) {
			ILoadpathEntry entry = (ILoadpathEntry) element;
			return entry.getPath().toOSString();
		}
		RdtDebugUiPlugin.log(new RuntimeException("Unable to render load path."));
		return null;
	}

	/**
	 * @see IBaseLabelProvider#addListener(ILabelProviderListener)
	 */
	public void addListener(ILabelProviderListener listener) {
	}

	/**
	 * @see IBaseLabelProvider#dispose()
	 */
	public void dispose() {
	}

	/**
	 * @see IBaseLabelProvider#isLabelProperty(Object, String)
	 */
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	/**
	 * @see IBaseLabelProvider#removeVMInstallChangedListener(ILabelProviderListener)
	 */
	public void removeListener(ILabelProviderListener listener) {
	}

}
