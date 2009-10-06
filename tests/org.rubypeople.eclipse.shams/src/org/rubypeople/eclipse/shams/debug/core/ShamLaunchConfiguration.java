package org.rubypeople.eclipse.shams.debug.core;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchDelegate;

public class ShamLaunchConfiguration implements ILaunchConfiguration {

	public ShamLaunchConfiguration() {
		super();
	}

	public ILaunch launch(String mode, IProgressMonitor monitor) throws CoreException {
		return null;
	}

	public boolean supportsMode(String mode) throws CoreException {
		throw new RuntimeException("Need to implement on sham.");
	}

	public String getName() {
		throw new RuntimeException("Need to implement on sham.");
	}

	public IPath getLocation() {
		throw new RuntimeException("Need to implement on sham.");
	}

	public boolean exists() {
		throw new RuntimeException("Need to implement on sham.");
	}

	public int getAttribute(String attributeName, int defaultValue) throws CoreException {
		throw new RuntimeException("Need to implement on sham.");
	}

	public String getAttribute(String attributeName, String defaultValue) throws CoreException {
		return defaultValue;
	}

	public boolean getAttribute(String attributeName, boolean defaultValue) throws CoreException {
		throw new RuntimeException("Need to implement on sham.");
	}

	public List getAttribute(String attributeName, List defaultValue) throws CoreException {
		throw new RuntimeException("Need to implement on sham.");
	}

	public Map getAttribute(String attributeName, Map defaultValue) throws CoreException {
		throw new RuntimeException("Need to implement on sham.");
	}

	public IFile getFile() {
		throw new RuntimeException("Need to implement on sham.");
	}

	public ILaunchConfigurationType getType() throws CoreException {
		throw new RuntimeException("Need to implement on sham.");
	}

	public boolean isLocal() {
		throw new RuntimeException("Need to implement on sham.");
	}

	public ILaunchConfigurationWorkingCopy getWorkingCopy() throws CoreException {
		throw new RuntimeException("Need to implement on sham.");
	}

	public ILaunchConfigurationWorkingCopy copy(String name) throws CoreException {
		throw new RuntimeException("Need to implement on sham.");
	}

	public boolean isWorkingCopy() {
		throw new RuntimeException("Need to implement on sham.");
	}

	public void delete() throws CoreException {
		throw new RuntimeException("Need to implement on sham.");
	}

	public String getMemento() throws CoreException {
		throw new RuntimeException("Need to implement on sham.");
	}

	public boolean contentsEqual(ILaunchConfiguration configuration) {
		throw new RuntimeException("Need to implement on sham.");
	}

	public Object getAdapter(Class adapter) {
		throw new RuntimeException("Need to implement on sham.");
	}

	public String getCategory() throws CoreException {
		throw new RuntimeException("Need to implement on sham.");
	}

	public Map getAttributes() throws CoreException {
		throw new RuntimeException("Need to implement on sham.");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchConfiguration#launch(java.lang.String, org.eclipse.core.runtime.IProgressMonitor, boolean)
	 */
	public ILaunch launch(String mode, IProgressMonitor monitor, boolean build) throws CoreException {
		throw new RuntimeException("Need to implement on sham.");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchConfiguration#launch(java.lang.String, org.eclipse.core.runtime.IProgressMonitor, boolean, boolean)
	 */
	public ILaunch launch(String mode, IProgressMonitor monitor, boolean build, boolean register) throws CoreException {
		throw new RuntimeException("Need to implement on sham.");
	}

    public IResource[] getMappedResources() throws CoreException {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean isMigrationCandidate() throws CoreException {
        // TODO Auto-generated method stub
        return false;
    }

    public void migrate() throws CoreException {
        // TODO Auto-generated method stub
        
    }

	public Set getAttribute(String attributeName, Set defaultValue) throws CoreException
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Set getModes() throws CoreException
	{
		// TODO Auto-generated method stub
		return null;
	}

	public ILaunchDelegate getPreferredDelegate(Set modes) throws CoreException
	{
		// TODO Auto-generated method stub
		return null;
	}

	public boolean hasAttribute(String attributeName) throws CoreException
	{
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isReadOnly()
	{
		// TODO Auto-generated method stub
		return false;
	}
}
