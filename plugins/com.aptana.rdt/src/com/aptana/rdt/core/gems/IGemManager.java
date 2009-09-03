package com.aptana.rdt.core.gems;

import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.ILaunchConfiguration;

/**
 * @author cwilliams
 */
public interface IGemManager
{

	public static final String DEFAULT_GEM_HOST = "http://gems.rubyforge.org";

	/**
	 * Run a command straight up.
	 * 
	 * @param args
	 * @throws CoreException
	 */
	public ILaunchConfiguration run(String args) throws CoreException;

	/**
	 * Returns a listing of the local gems.
	 * 
	 * @return a Set<Gem> of the installed gems.
	 */
	public Set<Gem> getGems();

	/**
	 * Returns a listing of the remote gems.
	 * 
	 * @return a Set<Gem> of the remote gems available for install.
	 */
	public Set<Gem> getRemoteGems();

	/**
	 * Install the supplied gem with it's dependencies. Long-running, blocking implementation. RUN INSIDE A JOB!
	 * 
	 * @param gem
	 * @param monitor
	 * @return
	 */
	public IStatus installGem(Gem gem, IProgressMonitor monitor);

	/**
	 * Install the supplied gem. Long-running, blocking implementation. RUN INSIDE A JOB!
	 * 
	 * @param gem
	 * @param includeDependencies
	 * @param monitor
	 * @return
	 */
	public IStatus installGem(Gem gem, boolean includeDependencies, IProgressMonitor monitor);

	/**
	 * @param gem
	 * @param sourceURL
	 * @param monitor
	 * @return
	 */
	public IStatus installGem(Gem gem, String sourceURL, IProgressMonitor monitor);

	/**
	 * Removes or uninstalls the supplied gem.
	 * 
	 * @param gem
	 *            the Gem to remove
	 * @param monitor
	 * @return IStatus
	 */
	public IStatus removeGem(Gem gem, IProgressMonitor monitor);

	/**
	 * Updates the supplied gem, if a newer version is available.
	 * 
	 * @param gem
	 *            the Gem to update
	 * @param monitor
	 * @return IStatus indicating whether the operation was a success.
	 */
	public IStatus update(Gem gem, IProgressMonitor monitor);

	/**
	 * Runs a "gem update" command for all gems. For any local gems that have a newer version, the IOGemManager will
	 * install the new versions.
	 * 
	 * @return IStatus indicating whether the operation was a success.
	 */
	public IStatus updateAll(IProgressMonitor monitor);

	/**
	 * Query method to indicate if a gem using the supplied name is installed locally.
	 * 
	 * @param gemName
	 *            The unique name of the gem
	 * @return a boolean indicating whether the gem is installed
	 */
	public boolean gemInstalled(String gemName);

	/**
	 * Forces the GemManager to refresh it's listing of local gems
	 * 
	 * @param monitor
	 * @return IStatus indicating if it was a successful operation
	 */
	public IStatus refresh(IProgressMonitor monitor);

	public void addGemListener(GemListener listener);

	public void removeGemListener(GemListener listener);

	/**
	 * The base IPaths where gems are installed.
	 * 
	 * @return a List<IPath> indicating where gems get installed
	 */
	public List<IPath> getGemInstallPaths();

	/**
	 * Returns an IPath pointing to the latest version of the gem with the supplied name.
	 * 
	 * @param gemName
	 * @return an IPath pointing to the gem's location on disk for the latest version of the gem
	 */
	public IPath getGemPath(String gemName);

	/**
	 * Returns an IPath pointing to the supplied version of the gem with the supplied name.
	 * 
	 * @param gemName
	 *            The name of the gem to match
	 * @param version
	 *            The version to match
	 * @return an IPath pointing to the gem's location on disk
	 */
	public IPath getGemPath(String gemName, String version);

	/**
	 * Determine whether it appears that the RubyGems library is installed (for the current/default VM).
	 * 
	 * @return a boolean indicating if it appears that RubyGems is installed
	 */
	public boolean isRubyGemsInstalled();

	/**
	 * Forces the IGemManager to load local cached copies of the remote and local gems listings. If empty, it refreshes
	 * it's local and remote gem listing from original sources.
	 */
	public void initialize();

	/**
	 * Query to indicate whether the IGemManager has been initialized yet.
	 * 
	 * @return
	 */
	public boolean isInitialized();

	/**
	 * Cleans up (removes) older versions of gems, retaining only the latest copy.
	 */
	public IStatus cleanup(IProgressMonitor monitor);

	/**
	 * @param sourceURL
	 * @param monitor
	 * @return
	 */
	public Set<Gem> getRemoteGems(String sourceURL, IProgressMonitor monitor);

	/**
	 * Return the unique set of source urls that have been queried for gems and had actual results...
	 * 
	 * @return
	 */
	public Set<String> getSourceURLs();

	/**
	 * Return the set of gems that the passed in gem requires.
	 * 
	 * @param gem
	 *            The Gem to analyze for it's dependencies
	 * @return the Set of Gems that this gem requires.
	 */
	public Set<GemRequirement> getDependencies(Gem gem);

	/**
	 * Find the latest gem that meets the dependency requirements
	 * 
	 * @param dependency
	 * @return
	 */
	public Gem findGem(GemRequirement dependency);

	/**
	 * Gets the available versions of the gem based on the name
	 * 
	 * @param gemName
	 * @return
	 */
	public List<Version> getVersions(String gemName);

	public String getName();

	/**
	 * Returns the version of rubygems itself.
	 * 
	 * @since 1.2.0
	 * @return
	 */
	public Version getVersion();

	/**
	 * Long-running, blocking method to update the rubygems installation. RUN INSIDE A JOB!
	 * 
	 * @param monitor
	 * @return
	 */
	public IStatus updateSystem(IProgressMonitor monitor);
}