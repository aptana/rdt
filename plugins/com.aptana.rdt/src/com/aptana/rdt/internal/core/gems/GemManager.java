package com.aptana.rdt.internal.core.gems;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.core.net.proxy.IProxyService;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.rubypeople.rdt.core.RubyCore;
import org.rubypeople.rdt.core.util.Util;
import org.rubypeople.rdt.launching.IRubyLaunchConfigurationConstants;
import org.rubypeople.rdt.launching.IVMInstall;
import org.rubypeople.rdt.launching.IVMInstallChangedListener;
import org.rubypeople.rdt.launching.PropertyChangeEvent;
import org.rubypeople.rdt.launching.RubyRuntime;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.aptana.rdt.AptanaRDTPlugin;
import com.aptana.rdt.core.gems.AbstractGemManager;
import com.aptana.rdt.core.gems.Gem;
import com.aptana.rdt.core.gems.GemListener;
import com.aptana.rdt.core.gems.GemRequirement;
import com.aptana.rdt.core.gems.IGemManager;
import com.aptana.rdt.core.gems.LogicalGem;
import com.aptana.rdt.core.gems.Version;
import com.aptana.rdt.core.preferences.IPreferenceConstants;

// XXX If user tries to install a gem that someone has contributed a local copy of, try using the local copy! (Need to worry about dependencies then!)
public class GemManager extends AbstractGemManager implements IGemManager, IVMInstallChangedListener
{

	private static final String DETAIL_SWITCH = "-d";
	private static final String SOURCE_SWITCH = "--source";
	private static final String INCLUDE_DEPENDENCIES_SWITCH = "-y";
	private static final String LOCAL_SWITCH = "-l";
	private static final String VERSION_SWITCH = "-v";
	private static final String REMOTE_SWITCH = "-r";

	private static final String LIST_COMMAND = "list";
	private static final String INSTALL_COMMAND = "install";
	private static final String UNINSTALL_COMMAND = "uninstall";
	private static final String UPDATE_COMMAND = "update";
	private static final String CLEANUP_COMMAND = "cleanup";
	private static final String EXECUTABLE = "ruby";

	private static final String LOCAL_GEMS_CACHE_FILE = "local_gems.xml";

	private static final String RUBYGEMS_UPDATE_GEM_NAME = "rubygems-update";
	private static final String UPDATE_RUBYGEMS_COMMAND = "update_rubygems";

	private static GemManager fgInstance;

	private Set<Gem> gems;
	private Set<String> urls;
	private List<IPath> fGemInstallPaths;
	private Map<String, Set<Gem>> fRemoteGems = new HashMap<String, Set<Gem>>();

	protected boolean isInitialized;
	private Version fVersion;

	private static int seed = 0; // A number we append to the launch configs' name to ensure uniqueness (because the

	// method which is supposed to generate unique names in the LaunchManager doesn't
	// actually do it).

	protected GemManager()
	{
		super();
		urls = new HashSet<String>();
		gems = new HashSet<Gem>();
	}

	public boolean isInitialized()
	{
		return isInitialized;
	}

	protected Set<Gem> loadLocalCache(File file)
	{
		FileReader fileReader = null;
		try
		{
			fileReader = new FileReader(file);
			XMLReader reader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
			GemManagerContentHandler handler = new GemManagerContentHandler();
			reader.setContentHandler(handler);
			reader.parse(new InputSource(fileReader));

			return handler.getGems();
		}
		catch (FileNotFoundException e)
		{
			// This is okay, will get thrown if no config exists yet
		}
		catch (SAXException e)
		{
			AptanaRDTPlugin.log(e);
		}
		catch (ParserConfigurationException e)
		{
			AptanaRDTPlugin.log(e);
		}
		catch (FactoryConfigurationError e)
		{
			AptanaRDTPlugin.log(e);
		}
		catch (IOException e)
		{
			AptanaRDTPlugin.log(e);
		}
		finally
		{
			try
			{
				if (fileReader != null)
					fileReader.close();
			}
			catch (IOException e)
			{
				// ignore
			}
		}
		return new HashSet<Gem>();
	}

	protected void storeGemCache(Set<Gem> gems, File file)
	{
		XMLWriter out = null;
		try
		{
			out = new XMLWriter(new FileOutputStream(file));
			writeXML(gems, out);
		}
		catch (FileNotFoundException e)
		{
			AptanaRDTPlugin.log(e);
		}
		catch (IOException e)
		{
			AptanaRDTPlugin.log(e);
		}
		finally
		{
			if (out != null)
				out.close();
		}
	}

	protected File getConfigFile(String fileName)
	{
		return AptanaRDTPlugin.getDefault().getStateLocation().append(fileName).toFile();
	}

	/**
	 * Writes each server configuration to file in XML format.
	 * 
	 * @param gems
	 * @param out
	 *            the writer to use
	 */
	private void writeXML(Set<Gem> gems, XMLWriter out)
	{
		out.startTag("gems", null);
		for (Gem gem : gems)
		{
			out.startTag("gem", null);
			out.printSimpleTag("name", gem.getName());
			out.printSimpleTag("version", gem.getVersion());
			out.printSimpleTag("description", gem.getDescription());
			out.printSimpleTag("platform", gem.getPlatform());
			out.endTag("gem");
		}
		out.endTag("gems");
		out.flush();
	}

	protected Set<Gem> loadRemoteGems(String gemIndexUrl, IProgressMonitor monitor)
	{
		if (!isRubyGemsInstalled())
			return new HashSet<Gem>();

		IGemParser parser;
		String command = LIST_COMMAND + " " + DETAIL_SWITCH + " " + REMOTE_SWITCH + " " + SOURCE_SWITCH + " "
				+ gemIndexUrl;
		String output = launchInBackgroundAndRead(command, getStateFile("remote_listing.txt"));
		if (output != null && output.contains("ERROR:"))
		{
			// If the text returned contains "ERROR:", then we should try the short listing command
			command = LIST_COMMAND + " " + REMOTE_SWITCH + " " + SOURCE_SWITCH + " " + gemIndexUrl;
			output = launchInBackgroundAndRead(command, getStateFile("remote_listing.txt"));
			parser = getGemParser(false);
		}
		else
		{
			parser = getGemParser(true);
		}

		try
		{
			return parser.parse(output);
		}
		catch (GemParseException e)
		{
			return Collections.emptySet();
		}
	}

	protected IGemParser getGemParser()
	{
		return getGemParser(true);
	}

	protected IGemParser getGemParser(boolean detailed)
	{
		if (detailed)
			return new HybridGemParser(getVersion());
		return new ShortListingGemParser();
	}

	private Set<Gem> loadLocalGems(IProgressMonitor monitor)
	{
		if (!isRubyGemsInstalled())
			return new HashSet<Gem>();

		IGemParser parser = getGemParser();
		String output = getLocalGemsListing();
		try
		{
			return parser.parse(output);
		}
		catch (GemParseException e)
		{
			return Collections.emptySet();
		}
	}

	private String launchInBackgroundAndRead(final ILaunchConfiguration config, final File file)
	{
		return RubyRuntime.launchInBackgroundAndRead(config, file);
	}

	private String launchInBackgroundAndRead(String command, File file)
	{
		return launchInBackgroundAndRead(createGemLaunchConfiguration(command, false), file);
	}

	public Version getVersion()
	{
		if (fVersion != null)
			return fVersion;
		int tries = 0;
		while (fVersion == null && tries < 3)
		{
			String version = launchInBackgroundAndRead("-v", getStateFile("version.txt"));
			try
			{
				if (version != null && version.trim().length() > 0)
					fVersion = new Version(version.trim());
			}
			catch (RuntimeException e)
			{
				AptanaRDTPlugin.log(e);
				fVersion = null;
			}
			tries++;
		}
		return fVersion;
	}

	private String getLocalGemsListing()
	{
		String command = "query -d";
		// If we're using RubyGems older than 0.9.3, we need to do a "gem list -l" to get the equivalent of query -d
		if (getVersion() != null && getVersion().isLessThanOrEqualTo("0.9.3"))
		{
			command = LIST_COMMAND + " " + LOCAL_SWITCH;
		}
		return launchInBackgroundAndRead(command, getGemListingFile());
	}

	private File getGemListingFile()
	{
		return getStateFile("local_listing.txt");
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.rdt.internal.gems.IGemManager#update(com.aptana.rdt.internal.gems.Gem)
	 */
	public IStatus update(final Gem gem, IProgressMonitor monitor)
	{
		if (monitor == null)
			monitor = new NullProgressMonitor();

		if (!isRubyGemsInstalled())
			return new Status(IStatus.ERROR, AptanaRDTPlugin.PLUGIN_ID, -1, "RubyGems not installed", null);
		try
		{
			String command = UPDATE_COMMAND + " " + gem.getName();
			command = addProxy(IGemManager.DEFAULT_GEM_HOST, command);
			ILaunchConfiguration config = createGemLaunchConfiguration(command, true);

			if (monitor.isCanceled())
				return Status.CANCEL_STATUS;

			final ILaunch launch = config.launch(ILaunchManager.RUN_MODE, monitor);
			while (!launch.isTerminated())
			{
				if (monitor.isCanceled())
				{
					try
					{
						launch.terminate();
					}
					catch (DebugException e)
					{
						// ignore
					}
					return Status.CANCEL_STATUS;
				}
				Thread.yield();
			}
			refresh(monitor);
			for (GemListener listener : new ArrayList<GemListener>(listeners))
			{
				listener.gemUpdated(gem);
			}
			return Status.OK_STATUS;
		}
		catch (CoreException e)
		{
			return e.getStatus();
		}
	}

	private ILaunchConfigurationType getRubyApplicationConfigType()
	{
		return getLaunchManager().getLaunchConfigurationType(IRubyLaunchConfigurationConstants.ID_RUBY_APPLICATION);
	}

	private ILaunchManager getLaunchManager()
	{
		return DebugPlugin.getDefault().getLaunchManager();
	}

	private ILaunchConfiguration createGemLaunchConfiguration(String arguments, boolean isSudo)
	{
		String gemPath = getGemScriptPath();
		ILaunchConfiguration config = null;
		try
		{
			ILaunchConfigurationType configType = getRubyApplicationConfigType();
			ILaunchConfigurationWorkingCopy wc = configType.newInstance(null, getUniqueName("gem"));
			wc.setAttribute(IRubyLaunchConfigurationConstants.ATTR_FILE_NAME, gemPath);
			wc.setAttribute(IRubyLaunchConfigurationConstants.ATTR_VM_INSTALL_NAME, RubyRuntime.getDefaultVMInstall()
					.getName());
			wc.setAttribute(IRubyLaunchConfigurationConstants.ATTR_VM_INSTALL_TYPE, RubyRuntime.getDefaultVMInstall()
					.getVMInstallType().getId());
			wc.setAttribute(IRubyLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, arguments);
			wc.setAttribute(IRubyLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, "");
			wc.setAttribute(IRubyLaunchConfigurationConstants.ATTR_IS_SUDO, isSudo);
			if (isSudo)
			{
				wc.setAttribute(IRubyLaunchConfigurationConstants.ATTR_TERMINAL_COMMAND, "gem " + arguments);
				wc.setAttribute(IRubyLaunchConfigurationConstants.ATTR_USE_TERMINAL, "org.radrails.rails.shell"); // use
				// rails
				// shell
				// if
				// it's
				// available
			}
			Map<String, String> map = new HashMap<String, String>();
			map.put(IRubyLaunchConfigurationConstants.ATTR_RUBY_COMMAND, EXECUTABLE);
			wc.setAttribute(IRubyLaunchConfigurationConstants.ATTR_VM_INSTALL_TYPE_SPECIFIC_ATTRS_MAP, map);
			wc.setAttribute(IDebugUIConstants.ATTR_PRIVATE, true);
			wc.setAttribute(IDebugUIConstants.ATTR_LAUNCH_IN_BACKGROUND, false);
			config = wc.doSave();
		}
		catch (CoreException ce)
		{
			AptanaRDTPlugin.log(ce);
		}
		return config;
	}

	private synchronized String getUniqueName(String name)
	{
		return RubyRuntime.generateUniqueLaunchConfigurationNameFrom(name) + seed++;
	}

	public ILaunchConfiguration run(String args) throws CoreException
	{
		boolean useSudo = false;
		if ((args.contains("install ") || args.contains("update") || args.contains("uninstall ") || args
				.contains("cleanup"))
				&& !args.contains("help "))
		{
			useSudo = true;
		}
		return createGemLaunchConfiguration(args, useSudo);
	}

	private static String getGemScriptPath()
	{
		String path = Platform.getPreferencesService().getString(AptanaRDTPlugin.PLUGIN_ID,
				IPreferenceConstants.GEM_SCRIPT_PATH, "", null);
		if (path != null && path.trim().length() > 0)
			return path;
		// TODO Cache this result until the VM changes?
		IVMInstall vm = RubyRuntime.getDefaultVMInstall();
		if (vm == null)
			return null;
		path = vm.getInstallLocation().getAbsolutePath() + File.separator + "bin" + File.separator + "gem";
		// FIXME What if it picks up bad file like gemtopbm on cygwin?! Need to prioritize somehow!
		File gemScript = Util.findFileWithOptionalSuffix(path);
		if (gemScript == null)
			return null;
		return gemScript.getAbsolutePath();
	}

	public boolean isRubyGemsInstalled()
	{
		String path = getGemScriptPath();
		if (path == null)
			return false;
		File file = new File(path);
		return file.exists();
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.rdt.core.gems.IGemManager#installGem(com.aptana.rdt.core.gems.Gem,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IStatus installGem(final Gem gem, IProgressMonitor monitor)
	{
		return installGem(gem, true, monitor);
	}

	public IStatus installGem(final Gem gem, boolean includeDependencies, IProgressMonitor monitor)
	{
		if (gem.isLocal())
		{
			return doLocalInstallGem(gem, monitor);
		}
		return installGem(gem, DEFAULT_GEM_HOST, includeDependencies, monitor);
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.rdt.internal.gems.IGemManager#removeGem(com.aptana.rdt.internal.gems.Gem)
	 */
	public IStatus removeGem(final Gem gem, IProgressMonitor monitor)
	{
		if (!isRubyGemsInstalled())
			return new Status(IStatus.ERROR, AptanaRDTPlugin.PLUGIN_ID, -1, "RubyGems not installed", null);
		try
		{
			String command = UNINSTALL_COMMAND + " " + gem.getName();
			if (gem.getVersion() != null && gem.getVersion().trim().length() > 0)
			{
				command += " " + VERSION_SWITCH + " " + gem.getVersion();
			}
			ILaunchConfiguration config = createGemLaunchConfiguration(command, true);

			if (monitor.isCanceled())
				return Status.CANCEL_STATUS;

			final ILaunch launch = config.launch(ILaunchManager.RUN_MODE, monitor);
			while (!launch.isTerminated())
			{
				if (monitor.isCanceled())
				{
					launch.terminate();
					return Status.CANCEL_STATUS;
				}
				Thread.yield();
			}
			refresh(monitor);
			// Need to wait until uninstall is finished
			for (GemListener listener : new ArrayList<GemListener>(listeners))
			{
				listener.gemRemoved(gem);
			}
			return Status.OK_STATUS;
		}
		catch (CoreException e)
		{
			return e.getStatus();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.rdt.internal.gems.IGemManager#getGems()
	 */
	public Set<Gem> getGems()
	{
		return Collections.unmodifiableSortedSet(new TreeSet<Gem>(gems));
	}

	public static GemManager getInstance()
	{
		if (fgInstance == null)
			fgInstance = new GemManager();
		return fgInstance;
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.rdt.core.gems.IGemManager#refresh(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IStatus refresh(IProgressMonitor monitor)
	{
		SubMonitor progress = SubMonitor.convert(monitor, 100);
		Set<Gem> newGems = loadLocalGems(progress.newChild(95));
		gems = newGems;
		storeGemCache(gems, getConfigFile(LOCAL_GEMS_CACHE_FILE));
		progress.worked(4);
		Job job = new Job("notifying Gem Listeners of refresh")
		{

			@Override
			protected IStatus run(IProgressMonitor monitor)
			{
				for (GemListener listener : new ArrayList<GemListener>(listeners))
				{
					listener.gemsRefreshed();
				}
				return Status.OK_STATUS;
			}

		};
		job.setSystem(true);
		job.schedule();
		progress.done();
		return Status.OK_STATUS;
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.rdt.internal.gems.IGemManager#getRemoteGems()
	 */
	public Set<Gem> getRemoteGems()
	{
		return getRemoteGems(DEFAULT_GEM_HOST, new NullProgressMonitor());
	}

	public Set<Gem> getRemoteGems(String sourceURL, IProgressMonitor monitor)
	{
		Set<Gem> remoteGems = new HashSet<Gem>();
		if (fRemoteGems.containsKey(sourceURL))
		{
			// FIXME How long should we be caching this?
			remoteGems = fRemoteGems.get(sourceURL);
		}
		else
		{
			remoteGems = makeLogical(loadRemoteGems(sourceURL, monitor));
			if (!remoteGems.isEmpty())
			{
				addSourceURL(sourceURL);
				fRemoteGems.put(sourceURL, remoteGems);
			}
		}
		return Collections.unmodifiableSortedSet(new TreeSet<Gem>(remoteGems));
	}

	protected void addSourceURL(String sourceURL)
	{
		if (urls.contains(sourceURL))
			return;
		launchInBackgroundAndRead("sources -a " + sourceURL, getConfigFile("add_source.txt"));
		urls.add(sourceURL);
	}

	public Set<String> getSourceURLs()
	{
		return Collections.unmodifiableSet(new TreeSet<String>(urls));
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.rdt.internal.gems.IGemManager#gemInstalled(java.lang.String)
	 */
	public boolean gemInstalled(String gemName)
	{
		Set<Gem> gems = getGems();
		for (Gem gem : gems)
		{
			if (gem.getName().equalsIgnoreCase(gemName))
				return true;
		}
		return false;
	}

	public synchronized List<IPath> getGemInstallPaths()
	{
		if (fGemInstallPaths == null)
		{
			if (!isRubyGemsInstalled())
				return null;
			ILaunchConfiguration config = createGemLaunchConfiguration("", false);
			if (config == null)
				return null;
			try
			{
				ILaunchConfigurationWorkingCopy wc = config.getWorkingCopy();
				if (wc == null)
					return null;
				wc.setAttribute(IRubyLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, "-r rubygems -e p(Gem.path)");
				config = wc.doSave();
			}
			catch (CoreException e)
			{
				AptanaRDTPlugin.log(e);
			}

			try
			{
				String output = launchInBackgroundAndRead(config, getGemInstallPathFile());
				fGemInstallPaths = parseInstallPaths(output);
			}
			catch (IllegalArgumentException e)
			{
				fGemInstallPaths = null;
				return null;
			}
		}
		return fGemInstallPaths;
	}

	private List<IPath> parseInstallPaths(String output)
	{
		try
		{
			if (output == null || output.trim().length() == 0)
				throw new IllegalArgumentException("Got empty output for gem install paths");
			output = output.trim();
			if (!output.startsWith("[") || !output.endsWith("]"))
				throw new IllegalArgumentException("Expected an array for gem install paths, but was: " + output);
			// toss the array brackets
			output = new String(output.substring(1, output.length() - 1));

			String[] paths = output.split(",");
			if (paths == null || paths.length < 1)
				return null;
			List<IPath> installPaths = new ArrayList<IPath>();
			for (int i = 0; i < paths.length; i++)
			{
				String path = paths[i].trim();
				// toss out the quotes
				path = new String(path.substring(1, path.length() - 1));
				installPaths.add(new Path(path.trim()));
			}
			return installPaths;
		}
		catch (Exception e)
		{
			AptanaRDTPlugin.log(e);
		}
		return null;
	}

	private File getGemInstallPathFile()
	{
		return getStateFile("install_path.txt");
	}

	private File getStateFile(String name)
	{
		String currentVMId = RubyRuntime.getDefaultVMInstall().getId();
		File file = AptanaRDTPlugin.getDefault().getStateLocation().append("gems").append(currentVMId).append(name)
				.toFile();
		try
		{
			file.getParentFile().mkdirs();
			file.createNewFile();
		}
		catch (IOException e)
		{
			// ignore
		}
		return file;
	}

	public IPath getGemPath(String gemName)
	{
		List<IPath> paths = getGemInstallPaths();
		if (paths == null)
			return null;
		List<IPath> matches = new ArrayList<IPath>();
		for (IPath path : paths)
		{
			path = path.append("gems");
			File gemFolder = path.toFile();
			File[] gems = gemFolder.listFiles();
			if (gems == null)
				continue;
			for (int i = 0; i < gems.length; i++)
			{
				File gem = gems[i];
				String name = gem.getName();
				if (name.startsWith(gemName))
					matches.add(new Path(gem.getAbsolutePath()));
			}
		}

		if (matches.isEmpty())
			return null;
		if (matches.size() == 1)
			return matches.get(0).append("lib");
		// otherwise, find latest version
		List<Version> versions = new ArrayList<Version>();
		for (IPath match : matches)
		{
			String name = match.lastSegment();
			String[] parts = name.split("-");
			for (int i = parts.length - 1; i >= 0; i--)
			{
				String version = parts[i];
				if (!Version.correctFormat(version))
					continue;
				try
				{
					Version duh = new Version(version);
					versions.add(duh);
					break;
				}
				catch (IllegalArgumentException e)
				{
					// ignore, that part may not be version for gem
				}
			}
		}
		Collections.sort(versions);
		Version latest = versions.get(versions.size() - 1);
		for (IPath match : matches)
		{
			String name = match.lastSegment();
			String[] parts = name.split("-");
			String version = null;
			for (int i = parts.length - 1; i >= 0; i--)
			{
				if (!Version.correctFormat(parts[i]))
					continue;
				version = parts[i];
				try
				{
					Version duh = new Version(version);
					versions.add(duh);
					break;
				}
				catch (IllegalArgumentException e)
				{
					// ignore, that part may not be version for gem
				}
			}
			if (version != null && version.equals(latest.toString()))
				return match.append("lib");
		}
		return null;
	}

	public IPath getGemPath(String gemName, String version)
	{
		return getGemPath(gemName + "-" + version);
	}

	public IStatus updateAll(IProgressMonitor monitor)
	{
		if (monitor == null)
			monitor = new NullProgressMonitor();

		if (!isRubyGemsInstalled())
			return new Status(IStatus.ERROR, AptanaRDTPlugin.PLUGIN_ID, "RubyGems not installed", null);

		// TODO create sub progress monitor?
		IStatus result = updateSystem(monitor);
		if (result != null && !result.isOK())
		{
			return result;
		}

		try
		{
			ILaunchConfiguration config = createGemLaunchConfiguration(addProxy(IGemManager.DEFAULT_GEM_HOST,
					UPDATE_COMMAND + " " + INCLUDE_DEPENDENCIES_SWITCH), true);
			if (monitor.isCanceled())
				return Status.CANCEL_STATUS;
			final ILaunch launch = config.launch(ILaunchManager.RUN_MODE, monitor);
			while (!launch.isTerminated())
			{
				if (monitor.isCanceled())
				{
					launch.terminate();
					return Status.CANCEL_STATUS;
				}
				Thread.yield();
			}
			refresh(monitor);
			return Status.OK_STATUS;
		}
		catch (CoreException e)
		{
			return e.getStatus();
		}
	}

	public void initialize()
	{
		RubyRuntime.addVMInstallChangedListener(this);
		scheduleLoadingSources();
		scheduleLoadingLocalGems();
	}

	private void scheduleLoadingSources()
	{
		Job job = new Job("Loading Remote Gem Sources")
		{

			@Override
			protected IStatus run(IProgressMonitor monitor)
			{
				urls = loadSourceURLs();
				// if (urls.size() < 2) {
				// addSourceURL(RAILS_GEM_HOST);
				// }
				return Status.OK_STATUS;
			}

		};
		job.setPriority(Job.LONG);
		job.setSystem(true);
		job.schedule();
	}

	protected Set<String> loadSourceURLs()
	{
		Set<String> sources = new HashSet<String>();
		String output = launchInBackgroundAndRead("sources -l", getConfigFile("sources_list.txt"));
		if (output == null)
			return sources;
		String[] lines = output.split("\n");
		if (lines == null)
			return sources;
		for (int i = 2; i < lines.length; i++)
		{
			sources.add(lines[i].trim());
		}
		return sources;
	}

	private void scheduleLoadingLocalGems()
	{
		Job job = new Job(GemsMessages.GemManager_loading_local_gems)
		{

			@Override
			protected IStatus run(IProgressMonitor monitor)
			{
				try
				{
					gems = loadLocalCache(getConfigFile(LOCAL_GEMS_CACHE_FILE));
					for (GemListener listener : new ArrayList<GemListener>(listeners))
					{
						listener.gemsRefreshed();
					}
					gems = loadLocalGems(monitor);
					int tries = 0;
					while (gems.isEmpty() && tries < 3)
					{ // if we get back an empty list retry up to 3 times
						tries++;
						gems = loadLocalGems(monitor);
					}
					storeGemCache(gems, getConfigFile(LOCAL_GEMS_CACHE_FILE));
					isInitialized = true;
					for (GemListener listener : new ArrayList<GemListener>(listeners))
					{
						listener.managerInitialized();
					}
					for (GemListener listener : new ArrayList<GemListener>(listeners))
					{
						listener.gemsRefreshed();
					}
				}
				catch (Exception e)
				{
					AptanaRDTPlugin.log(e);
					return Status.CANCEL_STATUS;
				}
				return Status.OK_STATUS;
			}

		};
		job.setPriority(Job.LONG);
		job.setSystem(true);
		job.schedule();
	}

	protected Set<Gem> makeLogical(Set<Gem> remoteGems)
	{
		SortedSet<Gem> sorted = new TreeSet<Gem>(remoteGems);
		SortedSet<Gem> logical = new TreeSet<Gem>();
		String name = null;
		Collection<Gem> temp = new HashSet<Gem>();
		for (Gem gem : sorted)
		{
			if (name != null && !gem.getName().equals(name))
			{
				logical.add(LogicalGem.create(temp));
				temp.clear();
			}
			name = gem.getName();
			temp.add(gem);
		}
		if (name != null && !temp.isEmpty())
		{
			logical.add(LogicalGem.create(temp));
			temp.clear();
		}
		return Collections.unmodifiableSortedSet(logical);
	}

	public IStatus cleanup(IProgressMonitor monitor)
	{
		if (monitor == null)
			monitor = new NullProgressMonitor();

		if (!isRubyGemsInstalled())
			return new Status(IStatus.ERROR, AptanaRDTPlugin.PLUGIN_ID, "RubyGems not installed", null);
		try
		{
			String command = CLEANUP_COMMAND;
			ILaunchConfiguration config = createGemLaunchConfiguration(command, true);
			if (monitor.isCanceled())
				return Status.CANCEL_STATUS;
			final ILaunch launch = config.launch(ILaunchManager.RUN_MODE, monitor);
			while (!launch.isTerminated())
			{
				if (monitor.isCanceled())
				{
					launch.terminate();
					return Status.CANCEL_STATUS;
				}
				Thread.yield();
			}
			refresh(monitor);
			return Status.OK_STATUS;
		}
		catch (CoreException e)
		{
			return e.getStatus();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.rdt.core.gems.IGemManager#installGem(com.aptana.rdt.core.gems.Gem, java.lang.String,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IStatus installGem(Gem gem, String sourceURL, IProgressMonitor monitor)
	{
		return installGem(gem, sourceURL, true, new NullProgressMonitor());
	}

	private IStatus doInstallGem(final Gem gem, String command, IProgressMonitor monitor)
	{
		SubMonitor progress = SubMonitor.convert(monitor, 100);
		if (!isRubyGemsInstalled())
			return new Status(IStatus.ERROR, AptanaRDTPlugin.PLUGIN_ID, -1, "RubyGems not installed", null);
		try
		{
			ILaunchConfiguration config = createGemLaunchConfiguration(command, true);
			final ILaunch launch = config.launch(ILaunchManager.RUN_MODE, null);
			progress.worked(2);
			while (!launch.isTerminated())
			{
				if (progress.isCanceled())
				{
					try
					{
						launch.terminate();
					}
					catch (DebugException e)
					{
						// ignore
					}
					return Status.CANCEL_STATUS;
				}
				Thread.yield();
			}
			progress.worked(88);
			refresh(progress.newChild(7));
			// Need to wait until install is finished
			for (GemListener listener : listeners)
			{
				listener.gemAdded(gem);
			}
			progress.worked(3);
			return Status.OK_STATUS;
		}
		catch (CoreException e)
		{
			return e.getStatus();
		}
		finally 
		{
			progress.done();
		}
	}

	private IStatus doLocalInstallGem(final Gem gem, IProgressMonitor monitor)
	{
		SubMonitor progress = SubMonitor.convert(monitor, 100);
		progress.setTaskName("Installing " + gem.getName());
		if (!isRubyGemsInstalled())
			return new Status(IStatus.ERROR, AptanaRDTPlugin.PLUGIN_ID, -1, "RubyGems not installed", null);
		try
		{
			// force working directory to that containing the gem
			String command = INSTALL_COMMAND + " -l " + new File(gem.getAbsolutePath()).getName() + "";
			ILaunchConfiguration config = createGemLaunchConfiguration(command, true);
			ILaunchConfigurationWorkingCopy wc = config.getWorkingCopy();
			wc.setAttribute(IRubyLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, new File(gem.getAbsolutePath())
					.getParent());
			config = wc.doSave();
			final ILaunch launch = config.launch(ILaunchManager.RUN_MODE, null);
			while (!launch.isTerminated())
			{
				if (monitor.isCanceled())
				{
					try
					{
						launch.terminate();
					}
					catch (DebugException e)
					{
						// ignore
					}
					return Status.CANCEL_STATUS;
				}
				Thread.yield();
			}
			progress.worked(90);
			refresh(progress.newChild(7));
			// Need to wait until uninstall is finished
			for (GemListener listener : new ArrayList<GemListener>(listeners))
			{
				listener.gemAdded(gem);
			}
			progress.worked(3);
			return Status.OK_STATUS;
		}
		catch (CoreException e)
		{
			return e.getStatus();
		}
		finally {
			progress.done();
		}
	}

	private IStatus installGem(final Gem gem, String sourceURL, boolean includeDependencies, IProgressMonitor monitor)
	{
		SubMonitor progress = SubMonitor.convert(monitor, 100);
		if (!gem.isInstallable())
			return new Status(IStatus.ERROR, AptanaRDTPlugin.getPluginId(), "Gem is uninstallable: " + gem.getName());

		if (gem.getName() == null || gem.getName().trim().length() == 0)
			return new Status(IStatus.ERROR, AptanaRDTPlugin.getPluginId(), "Can't install gem with empty name");

		if (progress.isCanceled())
			return Status.CANCEL_STATUS;

		String command = INSTALL_COMMAND + " " + gem.getName();
		if (gem.getVersion() != null && gem.getVersion().trim().length() > 0)
		{
			command += " " + VERSION_SWITCH + " " + gem.getVersion();
		}
		if (getVersion() == null || getVersion().isGreaterThanOrEqualTo("0.9.5")) // assume it's greater than 0.9.5 if
		// we can't determine version
		{
			if (!includeDependencies)
			{
				// dependencies included by default in 0.9.5+
				command += " --ignore-dependencies";
			}
		}
		else
		{
			if (includeDependencies)
			{ // need to to add switch if on older than 0.9.5
				command += " " + INCLUDE_DEPENDENCIES_SWITCH;
			}
		}
		if (sourceURL != null && !sourceURL.equals(DEFAULT_GEM_HOST))
		{
			command += " " + SOURCE_SWITCH + " " + sourceURL;
		}
		command = addProxy(sourceURL, command);
		progress.worked(5);
		return doInstallGem(gem, command, progress.newChild(95));
	}

	private String addProxy(String host, String command)
	{
		IProxyService service = getProxyService();
		if (service == null || !service.isProxiesEnabled())
			return command;
		IProxyData proxyData = service.getProxyDataForHost(host, IProxyData.HTTP_PROXY_TYPE);
		if (proxyData == null)
			return command;
		StringBuilder proxyLine = new StringBuilder(" -p http://");
		if (proxyData.isRequiresAuthentication())
		{
			proxyLine.append(proxyData.getUserId());
			proxyLine.append(":");
			proxyLine.append(proxyData.getPassword());
			proxyLine.append("@");
		}
		proxyLine.append(proxyData.getHost());
		proxyLine.append(":");
		proxyLine.append(proxyData.getPort());
		return command + proxyLine;
	}

	private IProxyService getProxyService()
	{
		return AptanaRDTPlugin.getDefault().getProxyService();
	}

	public Set<GemRequirement> getDependencies(Gem gem)
	{
		if (!isRubyGemsInstalled())
			return Collections.emptySet();
		String command = "dependency " + gem.getName() + " -v " + gem.getVersion();
		File file = getStateFile("dependencies_" + gem.getName() + "_" + gem.getVersion() + ".txt");
		String output = launchInBackgroundAndRead(command, file);
		Set<GemRequirement> requirements = parseDependencies(output);
		if (requirements.isEmpty() && gem.getName().equals("rails"))
		{
			AptanaRDTPlugin.log("Was unable to find out dependencies for rails gem!");
		}
		return requirements;
	}

	private Set<GemRequirement> parseDependencies(String output)
	{
		if (output == null)
			return Collections.emptySet();
		Set<GemRequirement> dependencies = new HashSet<GemRequirement>();
		Pattern pat = Pattern.compile("\\s+(\\w+)\\s+\\((.+?)\\)");
		String[] lines = output.split("[\\r|\\n]");
		for (int i = 1; i < lines.length; i++)
		{ // skip first line
			String line = lines[i];
			Matcher matcher = pat.matcher(line);
			if (!matcher.find())
				continue;
			String name = matcher.group(1);
			String version = matcher.group(2);
			dependencies.add(new GemRequirement(name, version));
		}
		return dependencies;
	}

	public Gem findGem(GemRequirement dependency)
	{
		// There's probably a more efficient way to do this, but oh well.
		// FIXME Should grab latest version of gem that meets the requirements
		// FIXME Break logical gems up!
		for (Gem gem : gems)

		{
			if (gem instanceof LogicalGem)
			{
				LogicalGem logical = (LogicalGem) gem;
				Collection<Gem> logicalsGems = logical.getGems();
				for (Gem gem2 : logicalsGems)
				{
					if (gem2.meetsRequirements(dependency))
						return gem2;
				}
			}
			if (gem.meetsRequirements(dependency))
				return gem;
		}
		return null;
	}

	public void defaultVMInstallChanged(IVMInstall previous, IVMInstall current)
	{
		fVersion = null; // invalidate the cached version
		fGemInstallPaths = null; // invalidate cached install path(s)
		Job job = new Job("Refreshing local gem listing")
		{

			@Override
			protected IStatus run(IProgressMonitor monitor)
			{
				return refresh(monitor);
			}

		};
		job.schedule();
	}

	public void vmAdded(IVMInstall newVm)
	{
	}

	public void vmChanged(PropertyChangeEvent event)
	{
	}

	public void vmRemoved(IVMInstall removedVm)
	{
	}

	public List<Version> getVersions(String gemName)
	{
		List<Version> versions = new ArrayList<Version>();
		for (Gem gem : gems)
		{
			if (gem.getName().equals(gemName))
			{
				versions.add(gem.getVersionObject());
			}
		}
		return versions;
	}

	public String getName()
	{
		return "Local Gem Manager";
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.rdt.core.gems.IGemManager#updateSystem(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IStatus updateSystem(IProgressMonitor monitor)
	{
		if (monitor == null)
			monitor = new NullProgressMonitor();

		monitor.beginTask("Updating RubyGems", 100);
		if (monitor.isCanceled())
			return Status.CANCEL_STATUS;

		IStatus status = null;
		if (getVersion() != null && getVersion().isLessThan("1.3.4"))
		{
			// TODO Create a sub monitor for this portion?
			status = updateRubygems(monitor, "1.3.4");
			if (status != null && !status.isOK())
				return status;
		}
		return updateRubygems(monitor, Gem.ANY_VERSION);
	}

	private IStatus updateRubygems(IProgressMonitor monitor, String version)
	{
		IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 33);
		IStatus status = installGem(new Gem(RUBYGEMS_UPDATE_GEM_NAME, version, null), subMonitor);
		subMonitor.done();
		if (!status.isOK())
			return status;

		try
		{
			// Run "update_rubygems"
			ILaunchConfiguration config = createGemLaunchConfiguration("", true);
			ILaunchConfigurationWorkingCopy wc = config.getWorkingCopy();
			String fileName = getFileIfExists(UPDATE_RUBYGEMS_COMMAND);
			wc.setAttribute(IRubyLaunchConfigurationConstants.ATTR_FILE_NAME, fileName);
			config = wc.doSave();
			ILaunch launch = config.launch(ILaunchManager.RUN_MODE, monitor);
			while (!launch.isTerminated())
			{
				if (monitor.isCanceled())
				{
					launch.terminate();
					return Status.CANCEL_STATUS;
				}

				Thread.yield();
			}
			monitor.done();
			// Check exit status
			if (launch.getProcesses() != null && launch.getProcesses()[0] != null
					&& launch.getProcesses()[0].getExitValue() != 0)
				return new Status(IStatus.ERROR, AptanaRDTPlugin.PLUGIN_ID, -1, "Updating rubygems failed", null);

			// success. Wipe the cached version
			fVersion = null;
		}
		catch (CoreException e)
		{
			return e.getStatus();
		}
		return Status.OK_STATUS;

	}

	/**
	 * Try to find bin script in interpreter bin path and then system PATH.
	 * 
	 * @param command
	 * @return
	 */
	private String getFileIfExists(String command)
	{
		IPath path = RubyRuntime.checkInterpreterBin(command);
		if (path != null && path.toFile().exists())
		{
			return path.toOSString();
		}
		path = RubyCore.checkSystemPath(command);
		if (path != null && path.toFile().exists())
			return path.toOSString();
		return null;
	}
}
