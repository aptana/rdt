package org.rubypeople.rdt.internal.launching;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.service.environment.Constants;
import org.osgi.framework.Bundle;
import org.rubypeople.rdt.launching.AbstractVMInstallType;
import org.rubypeople.rdt.launching.IVMInstall;

public class JRubyVMType extends AbstractVMInstallType {

	/**
	 * Map of the install path for which we were unable to generate
	 * the library info during this session.
	 */
	private static Map<String, LibraryInfo> fgFailedInstallPath= new HashMap<String, LibraryInfo>();

	/**
	 * Convenience handle to the system-specific file separator character
	 */															
	private static final char fgSeparator = File.separatorChar;

	/**
	 * The list of locations in which to look for the ruby executable in candidate
	 * VM install locations, relative to the VM install location.
	 */
	private static final String[] fgCandidateRubyFiles = {"jrubyw", "jrubyw.bat", "jruby", "jruby.bat"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	private static final String[] fgCandidateRubyLocations = {"", "bin" + fgSeparator}; //$NON-NLS-1$ //$NON-NLS-2$
	
	
	@Override
	protected IVMInstall doCreateVMInstall(String id) {
		return new JRubyVM(this, id);
	}

	public File detectInstallLocation() {
		File rubyExecutable = null;
		if (Platform.getOS().equals(Constants.OS_WIN32)) {
			String winPath = System.getenv("Path");  // iterate through system path and try to find jruby.bat
			String[] paths = winPath.split(";");
			for (int i = 0; i < paths.length; i++) {
				String possibleExecutablePath = paths[i] + File.separator + "jruby.bat";
				File possible = new File(possibleExecutablePath);
				if (possible.exists()) {
					rubyExecutable = possible; 
					break;
				}
			}
		} else { // Mac, Linux - so let's just run 'which jruby' and parse out the result
			String[] cmdLine = new String[] { "which", "jruby" }; //$NON-NLS-1$ //$NON-NLS-2$
			rubyExecutable = parseRubyExecutableLocation(executeAndRead(cmdLine));
		}		
		File location = tryLocation(rubyExecutable);
		if (location != null) return location;
		return tryIncludedJRuby();
	}
	
	private File tryIncludedJRuby() {
		try {
			Bundle bundle = Platform.getBundle("org.jruby");
			URL url = FileLocator.find(bundle, new Path(""), null);
			url = FileLocator.toFileURL(url);
			String fileName = url.getFile();
			String executable = fileName + "bin" + File.separator + "jruby";
			if (Platform.getOS().equals(Constants.OS_WIN32)) {
				executable += ".bat";
			}
			return tryLocation(new File(executable));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private File tryLocation(File rubyExecutable) {
		if (rubyExecutable == null) {
			return null;
		}

		File bin = rubyExecutable.getParentFile();
		if (!bin.exists()) return null;
		File rubyHome = bin.getParentFile();
		if (!rubyHome.exists()) return null;
		if (!canDetectDefaultSystemLibraries(rubyHome, rubyExecutable)) {
			return null;
		}	
	
		return rubyHome;
	}

	public IPath[] getDefaultLibraryLocations(File installLocation) {
		File rubyExecutable = findRubyExecutable(installLocation);
		LibraryInfo info;
		if (rubyExecutable == null) {
			info = getDefaultLibraryInfo(installLocation);
		} else {
			info = getLibraryInfo(installLocation, rubyExecutable);
		}
		String[] loadpath = info.getBootpath();
		IPath[] paths = new IPath[loadpath.length];
		for (int i = 0; i < loadpath.length; i++) {
			paths[i] = new Path(loadpath[i]);
		}
		return paths;
	}

	public String getName() {
		return "JRuby VM";
	}

	public IStatus validateInstallLocation(File rubyHome) {
		IStatus status = null;
		File rubyExecutable = findRubyExecutable(rubyHome);
		if (rubyExecutable == null) {
			status = new Status(IStatus.ERROR, LaunchingPlugin.getUniqueIdentifier(), 0, LaunchingMessages.StandardVMType_Not_a_JDK_Root__Java_executable_was_not_found_1, null);						
		} else {
			if (canDetectDefaultSystemLibraries(rubyHome, rubyExecutable)) {
				status = new Status(IStatus.OK, LaunchingPlugin.getUniqueIdentifier(), 0, LaunchingMessages.StandardVMType_ok_2, null); 
			} else {
				status = new Status(IStatus.ERROR, LaunchingPlugin.getUniqueIdentifier(), 0, LaunchingMessages.StandardVMType_Not_a_JDK_root__System_library_was_not_found__1, null); 
			}
		}
		return status;
	}
	
	/**
	 * Starting in the specified VM install location, attempt to find the 'jruby' executable
	 * file.  If found, return the corresponding <code>File</code> object, otherwise return
	 * <code>null</code>.
	 */
	public static File findRubyExecutable(File vmInstallLocation) {
		// Try each candidate in order.  The first one found wins.  Thus, the order
		// of fgCandidateRubyLocations and fgCandidateRubyFiles is significant.
		for (int i = 0; i < fgCandidateRubyFiles.length; i++) {
			for (int j = 0; j < fgCandidateRubyLocations.length; j++) {
				File rubyFile = new File(vmInstallLocation, fgCandidateRubyLocations[j] + fgCandidateRubyFiles[i]);
				if (rubyFile.isFile() && isPlatformProper(rubyFile)) { // Only check for .bat on win32 and others on other platforms
					return rubyFile;
				}				
			}
		}		
		return null;							
	}
	
	private static boolean isPlatformProper(File rubyFile) {
		if (Platform.getOS().equals(Platform.OS_WIN32)) {
			return rubyFile.getName().endsWith(".bat");
		} else {
			return !rubyFile.getName().endsWith(".bat");
		}
	}

	/**
	 * Return <code>true</code> if the appropriate system libraries can be found for the
	 * specified ruby executable, <code>false</code> otherwise.
	 */
	protected boolean canDetectDefaultSystemLibraries(File rubyHome, File rubyExecutable) {
		IPath[] locations = getDefaultLibraryLocations(rubyHome);
		return locations.length > 0; 
	}
	
	/**
	 * Returns default library info for the given install location.
	 * 
	 * @param installLocation
	 * @return LibraryInfo
	 */
	protected LibraryInfo getDefaultLibraryInfo(File installLocation) {
		IPath[] dflts = getDefaultSystemLibrary(installLocation);
		String[] strings = new String[dflts.length];
		for (int i = 0; i < dflts.length; i++) {
			strings[i] = dflts[i].toOSString();
		}
		return new LibraryInfo("1.8.5", strings);		 //$NON-NLS-1$
	}
	
	/**
	 * Return an <code>IPath</code> corresponding to the single library file containing the
	 * standard Ruby classes for VMs version 1.8.x.
	 */
	protected IPath[] getDefaultSystemLibrary(File rubyHome) {
		String stdPath = rubyHome.getAbsolutePath() + fgSeparator + "lib" + fgSeparator + "ruby" + fgSeparator + "1.8";
		String sitePath = rubyHome.getAbsolutePath() + fgSeparator + "lib" + fgSeparator + "ruby" + fgSeparator + "site_ruby" + fgSeparator + "1.8";
		IPath[] paths = new IPath[2];		
		paths[0] = new Path(sitePath);
		paths[1] = new Path(stdPath);
		return paths;
	}
	
	/**
	 * Return library information corresponding to the specified install
	 * location. If the info does not exist, create it using the given JRuby
	 * executable.
	 */
	protected synchronized LibraryInfo getLibraryInfo(File rubyHome, File rubyExecutable) {		
		// See if we already know the info for the requested VM.  If not, generate it.
		String installPath = rubyHome.getAbsolutePath();
		LibraryInfo info = LaunchingPlugin.getLibraryInfo(this, installPath);
		if (info == null) {
			info= fgFailedInstallPath.get(installPath);
			if (info == null) {
				info = generateLibraryInfo(rubyHome, rubyExecutable);
				if (info == null) {
					info = getDefaultLibraryInfo(rubyHome);
					fgFailedInstallPath.put(installPath, info);
				} else {
				    LaunchingPlugin.setLibraryInfo(this, installPath, info);
				}
			}
		} 
		return info;
	}	

	public File findExecutable(File installLocation) {
		return findRubyExecutable(installLocation);
	}

	public String getVMPlatform(File installLocation, File executable) {
		return "jruby";
	}
}
