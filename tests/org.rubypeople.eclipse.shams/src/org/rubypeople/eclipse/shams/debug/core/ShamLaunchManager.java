package org.rubypeople.eclipse.shams.debug.core;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationListener;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchListener;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.ILaunchMode;
import org.eclipse.debug.core.ILaunchesListener;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IPersistableSourceLocator;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.sourcelookup.ISourceContainerType;
import org.eclipse.debug.core.sourcelookup.ISourcePathComputer;

public class ShamLaunchManager implements ILaunchManager {
	protected List launches = new ArrayList();
	protected ILaunchConfigurationType launchConfigurationType = new ShamLaunchConfigurationType();

	public ShamLaunchManager() {
		super();
	}

	public void addLaunchListener(ILaunchListener listener) {
		throw new RuntimeException("Need to implement on sham.");
	}

	public void removeLaunch(ILaunch launch) {
		throw new RuntimeException("Need to implement on sham.");
	}

	public IDebugTarget[] getDebugTargets() {
		throw new RuntimeException("Need to implement on sham.");
	}

	public ILaunch[] getLaunches() {
		return (ILaunch[]) launches.toArray(new ILaunch[launches.size()]);
	}

	public IProcess[] getProcesses() {
		throw new RuntimeException("Need to implement on sham.");
	}

	public void addLaunch(ILaunch launch) {
		launches.add(launch);
	}

	public void removeLaunchListener(ILaunchListener listener) {
		throw new RuntimeException("Need to implement on sham.");
	}

	public ILaunchConfiguration[] getLaunchConfigurations() throws CoreException {
		List configurations = new ArrayList();
		for (Iterator iter = launches.iterator(); iter.hasNext();) {
			ILaunch aLaunch = (ILaunch) iter.next();
			configurations.add(aLaunch.getLaunchConfiguration());
		}
		return (ILaunchConfiguration[]) configurations.toArray(new ILaunchConfiguration[configurations.size()]);
	}

	public ILaunchConfiguration[] getLaunchConfigurations(ILaunchConfigurationType type) throws CoreException {
		return getLaunchConfigurations();
	}

	public ILaunchConfiguration getLaunchConfiguration(IFile file) {
		throw new RuntimeException("Need to implement on sham.");
	}

	public ILaunchConfiguration getLaunchConfiguration(String memento) throws CoreException {
		throw new RuntimeException("Need to implement on sham.");
	}

	public ILaunchConfigurationType[] getLaunchConfigurationTypes() {
		throw new RuntimeException("Need to implement on sham.");
	}

	public ILaunchConfigurationType getLaunchConfigurationType(String id) {
		return launchConfigurationType;
	}

	public void addLaunchConfigurationListener(ILaunchConfigurationListener listener) {
		throw new RuntimeException("Need to implement on sham.");
	}

	public void removeLaunchConfigurationListener(ILaunchConfigurationListener listener) {
		throw new RuntimeException("Need to implement on sham.");
	}

	public boolean isExistingLaunchConfigurationName(String name) throws CoreException {
		throw new RuntimeException("Need to implement on sham.");
	}

	public String generateUniqueLaunchConfigurationNameFrom(String namePrefix) {
		return namePrefix + new Date().toString();
	}

	public IPersistableSourceLocator newSourceLocator(String identifier) throws CoreException {
		throw new RuntimeException("Need to implement on sham.");
	}

	public void addLaunches(ILaunch[] launches) {
		throw new RuntimeException("Need to implement on sham.");
	}
	
	public void addLaunchListener(ILaunchesListener listener) {
		throw new RuntimeException("Need to implement on sham.");
	}

	public void removeLaunches(ILaunch[] launches) {
		throw new RuntimeException("Need to implement on sham.");
	}

	public void removeLaunchListener(ILaunchesListener listener) {
		throw new RuntimeException("Need to implement on sham.");
	}

	public ILaunchConfiguration getMovedFrom(ILaunchConfiguration addedConfiguration) {
		throw new RuntimeException("Need to implement on sham.");
	}

	public ILaunchConfiguration getMovedTo(ILaunchConfiguration removedConfiguration) {
		throw new RuntimeException("Need to implement on sham.");
	}
	public String[] getEnvironment(ILaunchConfiguration configuration)
		throws CoreException {

		return null;
	}

	public String getLaunchModeLabel(String mode) {

		return null;
	}

	public ILaunchMode[] getLaunchModes() {
		return null;
	}

	public ISourcePathComputer newSourcePathComputer(ILaunchConfiguration configuration)
		throws CoreException {
		throw new RuntimeException("Need to implement on sham.");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchManager#getNativeEnvironment()
	 */
	public Map getNativeEnvironment() {
		throw new RuntimeException("Need to implement on sham.");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchManager#getLaunchMode(java.lang.String)
	 */
	public ILaunchMode getLaunchMode(String mode) {
		throw new RuntimeException("Need to implement on sham.");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchManager#getSourcePathComputer(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public ISourcePathComputer getSourcePathComputer(ILaunchConfiguration configuration) throws CoreException {
		throw new RuntimeException("Need to implement on sham.");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchManager#getSourcePathComputer(java.lang.String)
	 */
	public ISourcePathComputer getSourcePathComputer(String id) {
		throw new RuntimeException("Need to implement on sham.");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchManager#getSourceContainerTypes()
	 */
	public ISourceContainerType[] getSourceContainerTypes() {
		throw new RuntimeException("Need to implement on sham.");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchManager#getSourceContainerType(java.lang.String)
	 */
	public ISourceContainerType getSourceContainerType(String id) {
		throw new RuntimeException("Need to implement on sham.");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchManager#isRegistered(org.eclipse.debug.core.ILaunch)
	 */
	public boolean isRegistered(ILaunch launch) {
		throw new RuntimeException("Need to implement on sham.");
	}

	public Map getNativeEnvironmentCasePreserved() {
		throw new RuntimeException("Need to implement on sham.");
	}

    public ILaunchConfiguration[] getMigrationCandidates() throws CoreException {
    	throw new RuntimeException("Need to implement on sham.");
    }

	public String getEncoding(ILaunchConfiguration configuration) throws CoreException
	{
		return null;
	}

	public String generateLaunchConfigurationName(String namePrefix)
	{
		return null;
	}

	public boolean isValidLaunchConfigurationName(String configname) throws IllegalArgumentException
	{
		return false;
	}
}
