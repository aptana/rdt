package org.rubypeople.rdt.internal.launching;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.service.environment.Constants;
import org.rubypeople.rdt.core.util.Util;
import org.rubypeople.rdt.launching.AbstractVMInstallType;
import org.rubypeople.rdt.launching.IVMInstall;
import org.rubypeople.rdt.launching.IVMInstallType2;

public class StandardVMType extends AbstractVMInstallType implements IVMInstallType2
{

	private static final String DEFAULT_MAJOR_MINOR_VERSION = "1.8";
	private static final String DEFAULT_VERSION = "1.8.6";
	private static final String USR = "/usr";
	private static final String USR_BIN_RUBY = USR + "/bin/ruby";
	private static final String USR_LOCAL_BIN_RUBY = "/usr/local/bin/ruby";
	private static final String OPT_LOCAL_BIN_RUBY = "/opt/local/bin/ruby";
	private static final String MAC_OSX_LEOPARD_RUBY_PATH = "/System/Library/Frameworks/Ruby.framework/Versions/Current/usr/bin/ruby";

	/**
	 * Map of the install path for which we were unable to generate the library info during this session.
	 */
	private static Map<String, LibraryInfo> fgFailedInstallPath = new HashMap<String, LibraryInfo>();

	/**
	 * Convenience handle to the system-specific file separator character
	 */
	private static final char fgSeparator = File.separatorChar;

	/**
	 * The list of locations in which to look for the ruby executable in candidate VM install locations, relative to the
	 * VM install location.
	 */
	private static final String[] fgCandidateRubyFiles = { "rubyw", "rubyw.exe", "ruby", "ruby.exe" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	private static final String[] fgCandidateRubyLocations = { "", "bin" + fgSeparator }; //$NON-NLS-1$ //$NON-NLS-2$

	@Override
	protected IVMInstall doCreateVMInstall(String id)
	{
		return new StandardVM(this, id);
	}

	public IPath[] getDefaultLibraryLocations(File installLocation)
	{
		File rubyExecutable = findRubyExecutable(installLocation);
		LibraryInfo info;
		if (rubyExecutable == null)
		{
			LaunchingPlugin.logInfo("Unable to find ruby executable under: " + installLocation);
			info = getDefaultLibraryInfo(installLocation);
		}
		else
		{
			info = getLibraryInfo(installLocation, rubyExecutable);
		}
		String[] loadpath = info.getBootpath();
		IPath[] paths = new IPath[loadpath.length];
		for (int i = 0; i < loadpath.length; i++)
		{
			paths[i] = new Path(loadpath[i]);
		}
		return paths;
	}

	public String getName()
	{
		return LaunchingMessages.StandardVMType_Standard_VM_3;
	}

	public IStatus validateInstallLocation(File rubyHome)
	{
		IStatus status = null;
		File rubyExecutable = findRubyExecutable(rubyHome);
		if (rubyExecutable == null)
		{
			status = new Status(IStatus.ERROR, LaunchingPlugin.getUniqueIdentifier(), 0,
					LaunchingMessages.StandardVMType_Not_a_JDK_Root__Java_executable_was_not_found_1, null);
		}
		else
		{
			if (canDetectDefaultSystemLibraries(rubyHome, rubyExecutable))
			{
				status = new Status(IStatus.OK, LaunchingPlugin.getUniqueIdentifier(), 0,
						LaunchingMessages.StandardVMType_ok_2, null);
			}
			else
			{
				status = new Status(IStatus.ERROR, LaunchingPlugin.getUniqueIdentifier(), 0,
						LaunchingMessages.StandardVMType_Not_a_JDK_root__System_library_was_not_found__1, null);
			}
		}
		return status;
	}

	/**
	 * Starting in the specified VM install location, attempt to find the 'ruby' executable file. If found, return the
	 * corresponding <code>File</code> object, otherwise return <code>null</code>.
	 */
	public static File findRubyExecutable(File vmInstallLocation)
	{
		// FIXME Yuck, what do we do if there's multiple VMs installed to the same base path (i.e. 1.8 and 1.9, with 1.9 installed with a suffix?)
		// Try each candidate in order. The first one found wins. Thus, the order
		// of fgCandidateRubyLocations and fgCandidateRubyFiles is significant.
		for (int i = 0; i < fgCandidateRubyFiles.length; i++)
		{
			for (int j = 0; j < fgCandidateRubyLocations.length; j++)
			{
				File rubyFile = new File(vmInstallLocation, fgCandidateRubyLocations[j] + fgCandidateRubyFiles[i]);
				rubyFile = Util.findFileWithOptionalSuffix(rubyFile.getAbsolutePath());
				if (rubyFile != null && rubyFile.isFile())
				{
					return rubyFile;
				}
			}
		}
		return null;
	}

	/**
	 * Return <code>true</code> if the appropriate system libraries can be found for the specified ruby executable,
	 * <code>false</code> otherwise.
	 */
	protected boolean canDetectDefaultSystemLibraries(File rubyHome, File rubyExecutable)
	{
		File foundExecutable = findRubyExecutable(rubyHome);
		if (foundExecutable == null || !foundExecutable.exists())
			return false;
		IPath[] locations = getDefaultLibraryLocations(rubyHome);
		return locations != null && locations.length > 0;
	}

	/**
	 * Return library information corresponding to the specified install location. If the info does not exist, create it
	 * using the given Ruby executable.
	 */
	protected synchronized LibraryInfo getLibraryInfo(File rubyHome, File rubyExecutable)
	{
		// See if we already know the info for the requested VM. If not, generate it.
		String installPath = rubyHome.getAbsolutePath();
		LibraryInfo info = LaunchingPlugin.getLibraryInfo(this, installPath);
		if (info == null)
		{
			info = fgFailedInstallPath.get(installPath);
			if (info == null)
			{
				info = generateLibraryInfo(rubyHome, rubyExecutable);
				if (info == null)
				{
					info = getDefaultLibraryInfo(rubyHome);
					fgFailedInstallPath.put(installPath, info);
				}
				else
				{
					LaunchingPlugin.setLibraryInfo(this, installPath, info);
				}
			}
		}
		return info;
	}

	/**
	 * Returns default library info for the given install location.
	 * 
	 * @param installLocation
	 * @return LibraryInfo
	 */
	protected LibraryInfo getDefaultLibraryInfo(File installLocation)
	{
		IPath[] dflts = getDefaultSystemLibrary(installLocation);
		String[] strings = new String[dflts.length];
		for (int i = 0; i < dflts.length; i++)
		{
			strings[i] = dflts[i].toOSString();
		}
		return new LibraryInfo(DEFAULT_VERSION, strings); //$NON-NLS-1$
	}

	/**
	 * Return an <code>IPath</code> corresponding to the single library file containing the standard Ruby classes for
	 * VMs version 1.8.x.
	 */
	protected IPath[] getDefaultSystemLibrary(File rubyHome)
	{
		String stdPath = rubyHome.getAbsolutePath() + fgSeparator + "lib" + fgSeparator + "ruby" + fgSeparator
				+ DEFAULT_MAJOR_MINOR_VERSION;
		String sitePath = rubyHome.getAbsolutePath() + fgSeparator + "lib" + fgSeparator + "ruby" + fgSeparator
				+ "site_ruby" + fgSeparator + DEFAULT_MAJOR_MINOR_VERSION;
		IPath[] paths = new IPath[2];
		paths[0] = new Path(sitePath);
		paths[1] = new Path(stdPath);
		return paths;
	}

	public List<File> detectInstallLocations() {
		List<File> locations = new ArrayList<File>();
		
		// Check for rvm rubies!
		if (!Platform.OS_WIN32.equals(Platform.getOS()))
		{
			String userHome = System.getProperty("user.home");
			if (userHome != null)
			{
				IPath rvm = Path.fromOSString(userHome).append(".rvm");
				if (rvm.toFile().isDirectory())
				{
					File rubiesDir = rvm.append("rubies").toFile();
					if (rubiesDir.isDirectory())
					{
						File dfRuby = new File(rubiesDir, "default");
						if (dfRuby.exists())
						{
							locations.add(dfRuby);
						}
						for (String dirName : rubiesDir.list())
						{
							if (dirName.startsWith("ruby-"))
							{
								locations.add(new File(rubiesDir, dirName));
							}
						}
					}
				}
			}
		}
		// Do normal old-school detection
		File file = detectInstallLocation();
		if (file != null)
		{
			locations.add(file);
		}
		return locations;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.rubypeople.rdt.launching.IVMInstallType#detectInstallLocation()
	 */
	public File detectInstallLocation()
	{
		// FIXME Handle detecting VMs via RVM!
		if (Platform.getOS().equals(Constants.OS_WIN32))
		{
			return tryLocation(detectInstallOnWindows());
		}
		File rubyExecutable = null;
		// Fix for ROR-179
		if (Platform.getOS().equals(Constants.OS_MACOSX))
		{ // Mac OSX Leopard
			File rubyHome = tryLocation(new File(MAC_OSX_LEOPARD_RUBY_PATH));
			if (rubyHome != null)
				return rubyHome;
		}

		File tentativeRubyHome = null;
		// Mac, Linux - so let's just run 'which ruby' and parse out the result
		try
		{
			rubyExecutable = parseRubyExecutableLocation(executeAndRead(new String[] { "which", "ruby" })); //$NON-NLS-1$ //$NON-NLS-2$
			tentativeRubyHome = tryLocation(rubyExecutable);
		}
		catch (Exception e)
		{
			LaunchingPlugin.log(e);
		}
		// If we don't find ruby, or we find one at /usr/bin/ruby
		// try to see if there's one at /usr/local or /opt/local. If so, then prefer one of those.
		if (tentativeRubyHome == null || tentativeRubyHome.getAbsolutePath().equals(USR))
		{
			File rubyHome = tryLocation(new File(USR_LOCAL_BIN_RUBY));
			if (rubyHome != null)
				return rubyHome;

			rubyHome = tryLocation(new File(OPT_LOCAL_BIN_RUBY));
			if (rubyHome != null)
				return rubyHome;
		}
		if (tentativeRubyHome != null)
			return tentativeRubyHome;
		// force trying /usr/bin/ruby if we didn't detect anything, and found nothing at /usr/local or /opt/local
		return tryLocation(new File(USR_BIN_RUBY));
	}

	private File detectInstallOnWindows()
	{
		String winPath = System.getenv("Path"); // iterate through system path and try to find ruby.exe
		String[] paths = winPath.split(";");
		for (int i = 0; i < paths.length; i++)
		{
			String possibleExecutablePath = paths[i] + File.separator + "ruby.exe";
			File possible = new File(possibleExecutablePath);
			if (possible.exists())
			{
				return possible;
			}
		}
		// TODO Where does RailsInstaller.org put it? What about Ruby installer for windows?
		// if all else fails, just try "C:/ruby"
		return new File("C:" + File.separator + "ruby" + File.separator + "bin" + File.separator + "ruby.exe");
	}

	private File tryLocation(File rubyExecutable)
	{
		if (rubyExecutable == null)
			return null;

		File bin = rubyExecutable.getParentFile();
		if (!bin.exists())
			return null;
		File rubyHome = bin.getParentFile();
		if (!rubyHome.exists())
			return null;
		if (!canDetectDefaultSystemLibraries(rubyHome, rubyExecutable))
		{
			LaunchingPlugin.logInfo("Was unable to detect default ruby system libraries at: "
					+ rubyHome.getAbsolutePath());
			return null;
		}
		return rubyHome;
	}

	public File findExecutable(File installLocation)
	{
		return findRubyExecutable(installLocation);
	}

	public String getVMPlatform(File rubyHome, File rubyExecutable)
	{
		String rubyExecutablePath = rubyExecutable.getAbsolutePath();
		String[] cmdLine = new String[] { rubyExecutablePath, "-v" }; //$NON-NLS-1$

		String platform = parsePlatform(executeAndRead(cmdLine));
		if (platform == null)
		{
			// log error that we were unable to generate library info - see bug 70011
			LaunchingPlugin
					.log(MessageFormat.format("Failed to retrieve platform for {0}", rubyHome.getAbsolutePath())); //$NON-NLS-1$
		}
		return platform;
	}

	private String parsePlatform(List<String> lines)
	{
		if (lines == null || lines.size() == 0)
			return null;
		String firstLine = lines.remove(0);
		Pattern pat = Pattern
				.compile("ruby\\s\\d\\.\\d\\.\\d\\s\\(\\d\\d\\d\\d\\-\\d\\d\\-\\d\\d\\spatchlevel\\s\\d+\\)\\s\\[.+\\-(.+)\\]");
		Matcher m = pat.matcher(firstLine);
		if (m.find())
		{
			return m.group(1);
		}
		return null;
	}
}
