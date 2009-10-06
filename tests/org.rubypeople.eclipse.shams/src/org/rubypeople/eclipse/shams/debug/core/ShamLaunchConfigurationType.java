package org.rubypeople.eclipse.shams.debug.core;

import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchDelegate;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.debug.core.sourcelookup.ISourcePathComputer;

public class ShamLaunchConfigurationType implements ILaunchConfigurationType {
	protected boolean newInstanceCreated;

	public ShamLaunchConfigurationType() {
		super();
	}

	public boolean supportsMode(String mode) {
		return false;
	}
	
	public ISourcePathComputer getSourcePathComputer() {
		return null;
	}

	public String getName() {
		return null;
	}

	public String getIdentifier() {
		return null;
	}

	public boolean isPublic() {
		return false;
	}

	public ILaunchConfigurationWorkingCopy newInstance(IContainer container, String name) throws CoreException {
		newInstanceCreated = true;
		return new ShamLaunchConfigurationWorkingCopy();
	}

	public ILaunchConfigurationDelegate getDelegate() throws CoreException {
		return null;
	}

	public boolean wasNewInstanceCreated() {
		boolean temp = newInstanceCreated;
		newInstanceCreated = false;
		return temp;
	}

	public Object getAdapter(Class adapter) {
		return null;
	}

	public String getCategory() {
		return null;
	}

	public String getAttribute(String attributeName) {
		return null;
	}

	public ILaunchConfigurationDelegate getDelegate(String mode)
		throws CoreException {
		return null;
	}

	public String getSourceLocatorId() {
		return null;
	}

	public String getPluginIdentifier() {
		return null;
	}

	public Set getSupportedModes() {
		return null;
	}

	public String getContributorName()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public ILaunchDelegate[] getDelegates(Set modes) throws CoreException
	{
		// TODO Auto-generated method stub
		return null;
	}

	public ILaunchDelegate getPreferredDelegate(Set modes) throws CoreException
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Set getSupportedModeCombinations()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public void setPreferredDelegate(Set modes, ILaunchDelegate delegate) throws CoreException
	{
		// TODO Auto-generated method stub
		
	}

	public boolean supportsModeCombination(Set modes)
	{
		// TODO Auto-generated method stub
		return false;
	}

}
