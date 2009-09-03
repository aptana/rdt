package org.rubypeople.rdt.refactoring.core;

import org.eclipse.core.runtime.ListenerList;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;


public class WorkspaceTracker {

	public final static WorkspaceTracker INSTANCE= new WorkspaceTracker();
	
	public interface Listener {
		public void workspaceChanged();
	}
	
	private ListenerList fListeners;
	private ResourceListener fResourceListener;
	
	private WorkspaceTracker() {
		fListeners= new ListenerList();
	}

	private class ResourceListener implements IResourceChangeListener {
		public void resourceChanged(IResourceChangeEvent event) {
			workspaceChanged();
		}
	}
	
	private void workspaceChanged() {
		Object[] listeners= fListeners.getListeners();
		for (int i= 0; i < listeners.length; i++) {
			((Listener)listeners[i]).workspaceChanged();
		}
	}
	
	public void addListener(Listener l) {
		fListeners.add(l);
		if (fResourceListener == null) {
			fResourceListener= new ResourceListener();
			ResourcesPlugin.getWorkspace().addResourceChangeListener(fResourceListener);
		}
	}
	
	public void removeListener(Listener l) {
		if (fListeners.size() == 0)
			return;
		fListeners.remove(l);
		if (fListeners.size() == 0) {
			ResourcesPlugin.getWorkspace().removeResourceChangeListener(fResourceListener);
			fResourceListener= null;
		}
	}
}
