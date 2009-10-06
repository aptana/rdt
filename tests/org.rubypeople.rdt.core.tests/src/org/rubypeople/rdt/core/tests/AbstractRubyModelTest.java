package org.rubypeople.rdt.core.tests;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

import junit.framework.TestCase;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.Job;
import org.rubypeople.rdt.core.ILoadpathEntry;
import org.rubypeople.rdt.core.IRubyProject;
import org.rubypeople.rdt.core.IRubyScript;
import org.rubypeople.rdt.core.ISourceFolder;
import org.rubypeople.rdt.core.ISourceFolderRoot;
import org.rubypeople.rdt.core.RubyCore;
import org.rubypeople.rdt.core.RubyModelException;
import org.rubypeople.rdt.internal.core.LoadpathEntry;
import org.rubypeople.rdt.internal.core.util.CharOperation;
import org.rubypeople.rdt.internal.core.util.Util;

public abstract class AbstractRubyModelTest extends TestCase {
	
	protected IRubyProject currentProject;
	protected String endChar = ",";
	
	public AbstractRubyModelTest(String name) {
		super(name);
	}
	
	@Override
	protected void setUp() throws Exception {
		// TODO Make it so this stuff is only run once per suite, not before every method
		super.setUp();
		
		// ensure autobuilding is turned off
		IWorkspaceDescription description = getWorkspace().getDescription();
		if (description.isAutoBuilding()) {
			description.setAutoBuilding(false);
			getWorkspace().setDescription(description);
		}
	}
	
	protected IRubyProject setUpRubyProject(final String projectName) throws CoreException, IOException {
		this.currentProject = setUpRubyProject(projectName, "1.8.4");
		return this.currentProject;
	}
	
	protected String getPluginDirectoryPath() {
		try {
			URL platformURL = Platform.getBundle("org.rubypeople.rdt.core.tests").getEntry("/");
			return new File(FileLocator.toFileURL(platformURL).getFile()).getAbsolutePath();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public String getSourceWorkspacePath() {
		return getPluginDirectoryPath() +  java.io.File.separator + "workspace";
	}
	
	/**
	 * Returns the IWorkspace this test suite is running on.
	 */
	public IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}
	public IWorkspaceRoot getWorkspaceRoot() {
		return getWorkspace().getRoot();
	}
	
	/**
	 * Copy file from src (path to the original file) to dest (path to the destination file).
	 */
	public void copy(File src, File dest) throws IOException {
		// read source bytes
		byte[] srcBytes = this.read(src);
		
		if (convertToIndependantLineDelimiter(src)) {
			String contents = new String(srcBytes);
			contents = org.rubypeople.rdt.core.tests.util.Util.convertToIndependantLineDelimiter(contents);
			srcBytes = contents.getBytes();
		}
	
		// write bytes to dest
		FileOutputStream out = new FileOutputStream(dest);
		out.write(srcBytes);
		out.close();
	}
	
	public byte[] read(java.io.File file) throws java.io.IOException {
		int fileLength;
		byte[] fileBytes = new byte[fileLength = (int) file.length()];
		java.io.FileInputStream stream = new java.io.FileInputStream(file);
		int bytesRead = 0;
		int lastReadSize = 0;
		while ((lastReadSize != -1) && (bytesRead != fileLength)) {
			lastReadSize = stream.read(fileBytes, bytesRead, fileLength - bytesRead);
			bytesRead += lastReadSize;
		}
		stream.close();
		return fileBytes;
	}
	
	public boolean convertToIndependantLineDelimiter(File file) {
		return file.getName().endsWith(".rb") || file.getName().endsWith(".rbw");
	}
	
	/**
	 * Copy the given source directory (and all its contents) to the given target directory.
	 */
	protected void copyDirectory(File source, File target) throws IOException {
		if (!target.exists()) {
			target.mkdirs();
		}
		File[] files = source.listFiles();
		if (files == null) return;
		for (int i = 0; i < files.length; i++) {
			File sourceChild = files[i];
			String name =  sourceChild.getName();
			if (name.equals("CVS")) continue;
			File targetChild = new File(target, name);
			if (sourceChild.isDirectory()) {
				copyDirectory(sourceChild, targetChild);
			} else {
				copy(sourceChild, targetChild);
			}
		}
	}
	
	protected IRubyProject setUpRubyProject(final String projectName, String compliance) throws CoreException, IOException {
		// copy files in project from source workspace to target workspace
		String sourceWorkspacePath = getSourceWorkspacePath();
		String targetWorkspacePath = getWorkspaceRoot().getLocation().toFile().getCanonicalPath();
		copyDirectory(new File(sourceWorkspacePath, projectName), new File(targetWorkspacePath, projectName));

		// ensure variables are set
//		setUpJCLClasspathVariables(compliance);

		// create project
		final IProject project = getWorkspaceRoot().getProject(projectName);
		IWorkspaceRunnable populate = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				project.create(null);
				project.open(null);
			}
		};
		getWorkspace().run(populate, null);
		IRubyProject rubyProject = RubyCore.create(project);
		return rubyProject;
	}
	
	/**
	 * Batch deletion of projects
	 */
	protected void deleteProjects(final String[] projectNames) throws CoreException {
		ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				if (projectNames != null){
					for (int i = 0, max = projectNames.length; i < max; i++){
						if (projectNames[i] != null)
							deleteProject(projectNames[i]);
					}
				}
			}
		},
		null);
	}
	
	protected void deleteProject(String projectName) throws CoreException {
		IProject project = this.getProject(projectName);
		if (project.exists() && !project.isOpen()) { // force opening so that project can be deleted without logging (see bug 23629)
			project.open(null);
		}
		deleteResource(project);
	}
	
	/**
	 * Delete this resource.
	 */
	public void deleteResource(IResource resource) throws CoreException {
		int retryCount = 0; // wait 1 minute at most
		while (++retryCount <= 60) {
			if (!org.rubypeople.rdt.core.tests.util.Util.delete(resource)) {
				System.gc();
			}
		}
	}
	
	protected IProject getProject(String project) {
		return getWorkspaceRoot().getProject(project);
	}
	
	/**
	 * Wait for autobuild notification to occur
	 */
	public static void waitForAutoBuild() {
		boolean wasInterrupted = false;
		do {
			try {
				Job.getJobManager().join(ResourcesPlugin.FAMILY_AUTO_BUILD, null);
				wasInterrupted = false;
			} catch (OperationCanceledException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				wasInterrupted = true;
			}
		} while (wasInterrupted);
	}
	
	protected void assertResourcesEqual(String message, String expected, Object[] resources) {
		sortResources(resources);
		StringBuffer buffer = new StringBuffer();
		for (int i = 0, length = resources.length; i < length; i++) {
			if (resources[i] instanceof IResource) {
				buffer.append(((IResource) resources[i]).getFullPath().toString());
			} else if (resources[i] instanceof IStorage) {
				buffer.append(((IStorage) resources[i]).getFullPath().toString());
			} else if (resources[i] == null) {
				buffer.append("<null>");
			}
			if (i != length-1)buffer.append("\n");
		}
		if (!expected.equals(buffer.toString())) {
			System.out.print(org.rubypeople.rdt.core.tests.util.Util.displayString(buffer.toString(), 2));
			System.out.println(this.endChar);
		}
		assertEquals(
			message,
			expected,
			buffer.toString()
		);
	}
	
	protected void sortResources(Object[] resources) {
		Util.Comparer comparer = new Util.Comparer() {
			public int compare(Object a, Object b) {
				IResource resourceA = (IResource)a;
				IResource resourceB = (IResource)b;
				return resourceA.getFullPath().toString().compareTo(resourceB.getFullPath().toString());			}
		};
		Util.sort(resources, comparer);
	}

	protected IFile getFile(String path) {
		return getWorkspaceRoot().getFile(new Path(path));
	}
	
	
	protected IRubyProject createRubyProject(String projectName) throws CoreException {
		return this.createRubyProject(projectName, new String[] {""}, new String[] {"RUBY_LIB", "org.rubypeople.rdt.launching.RUBY_CONTAINER"});
	}
	
	/*
	 * Creates a Java project with the given source folders. 
	 * Add those on the project's loadpath.
	 */
	protected IRubyProject createRubyProject(String projectName, String[] sourceFolders) throws CoreException {
		return 
			this.createRubyProject(
				projectName, 
				sourceFolders, 
				null/*no lib*/, 
				null/*no inclusion pattern*/,
				null/*no exclusion pattern*/,
				null/*no project*/, 
				null/*no inclusion pattern*/,
				null/*no exclusion pattern*/,
				null/*no exported project*/, 
				null/*no inclusion pattern*/,
				null/*no exclusion pattern*/
			);
	}

	protected IRubyProject createRubyProject(String projectName, String[] sourceFolders, String[] libraries) throws CoreException {
		return 
			this.createRubyProject(
				projectName, 
				sourceFolders, 
				libraries, 
				null/*no inclusion pattern*/,
				null/*no exclusion pattern*/,
				null/*no project*/, 
				null/*no inclusion pattern*/,
				null/*no exclusion pattern*/,
				null/*no exported project*/, 
				null/*no inclusion pattern*/,
				null/*no exclusion pattern*/
			);
	}
	
	protected IRubyProject createRubyProject(String projectName, String[] sourceFolders, String[] libraries, String[] projects, boolean[] exportedProject) throws CoreException {
		return
			this.createRubyProject(
				projectName,
				sourceFolders,
				libraries,
				null/*no inclusion pattern*/,
				null/*no exclusion pattern*/,
				projects,
				null/*no inclusion pattern*/,
				null/*no exclusion pattern*/,
				exportedProject, 
				null/*no inclusion pattern*/,
				null/*no exclusion pattern*/
			);
	}
	protected IRubyProject createRubyProject(String projectName, String[] sourceFolders, String[] libraries, String[] projects) throws CoreException {
		return 
			createRubyProject(
				projectName, 
				sourceFolders, 
				libraries, 
				null/*no inclusion pattern*/,
				null/*no exclusion pattern*/,
				projects, 
				null/*no inclusion pattern*/,
				null/*no exclusion pattern*/,
				null/*no exported project*/, 
				null/*no inclusion pattern*/,
				null/*no exclusion pattern*/
			);
		}
	protected IRubyProject createRubyProject(final String projectName, final String[] sourceFolders, final String[] libraries, final String[] projects, final boolean[] exportedProjects, final String[][] inclusionPatterns, final String[][] exclusionPatterns) throws CoreException {
		return
		this.createRubyProject(
			projectName,
			sourceFolders,
			libraries,
			null/*no inclusion pattern*/,
			null/*no exclusion pattern*/,
			projects,
			null/*no inclusion pattern*/,
			null/*no exclusion pattern*/,
			exportedProjects,
			inclusionPatterns,
			exclusionPatterns
		);
	}

	protected IRubyProject createRubyProject(
			final String projectName,
			final String[] sourceFolders,
			final String[] libraries,
			final String[][] librariesInclusionPatterns,
			final String[][] librariesExclusionPatterns,
			final String[] projects,
			final String[][] projectsInclusionPatterns,
			final String[][] projectsExclusionPatterns,
			final boolean[] exportedProjects,
			final String[][] inclusionPatterns,
			final String[][] exclusionPatterns) throws CoreException {
		final IRubyProject[] result = new IRubyProject[1];
		IWorkspaceRunnable create = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				// create project
				createProject(projectName);
				
				// set ruby nature
				addRubyNature(projectName);
				
				// create loadpath entries 
				IProject project = getWorkspaceRoot().getProject(projectName);
				IPath projectPath = project.getFullPath();
				int sourceLength = sourceFolders == null ? 0 : sourceFolders.length;
				int libLength = libraries == null ? 0 : libraries.length;
				int projectLength = projects == null ? 0 : projects.length;
				ILoadpathEntry[] entries = new ILoadpathEntry[sourceLength+libLength+projectLength];
				for (int i= 0; i < sourceLength; i++) {
					IPath sourcePath = new Path(sourceFolders[i]);
					int segmentCount = sourcePath.segmentCount();
					if (segmentCount > 0) {
						// create folder and its parents
						IContainer container = project;
						for (int j = 0; j < segmentCount; j++) {
							IFolder folder = container.getFolder(new Path(sourcePath.segment(j)));
							if (!folder.exists()) {
								folder.create(true, true, null);
							}
							container = folder;
						}
					}
					// inclusion patterns
					IPath[] inclusionPaths;
					if (inclusionPatterns == null) {
						inclusionPaths = new IPath[0];
					} else {
						String[] patterns = inclusionPatterns[i];
						int length = patterns.length;
						inclusionPaths = new IPath[length];
						for (int j = 0; j < length; j++) {
							String inclusionPattern = patterns[j];
							inclusionPaths[j] = new Path(inclusionPattern);
						}
					}
					// exclusion patterns
					IPath[] exclusionPaths;
					if (exclusionPatterns == null) {
						exclusionPaths = new IPath[0];
					} else {
						String[] patterns = exclusionPatterns[i];
						int length = patterns.length;
						exclusionPaths = new IPath[length];
						for (int j = 0; j < length; j++) {
							String exclusionPattern = patterns[j];
							exclusionPaths[j] = new Path(exclusionPattern);
						}
					}
					// create source entry
					entries[i] = 
						RubyCore.newSourceEntry(
							projectPath.append(sourcePath), 
							inclusionPaths,
							exclusionPaths,
							LoadpathEntry.NO_EXTRA_ATTRIBUTES);
				}
				for (int i= 0; i < libLength; i++) {
					String lib = libraries[i];
//					if (lib.startsWith("JCL")) {
//						try {
//							// ensure JCL variables are set
//							setUpJCLClasspathVariables(compliance);
//						} catch (IOException e) {
//							e.printStackTrace();
//						}
//					}
//					
					// accessible files
					IPath[] accessibleFiles;
					if (librariesInclusionPatterns == null) {
						accessibleFiles = new IPath[0];
					} else {
						String[] patterns = librariesInclusionPatterns[i];
						int length = patterns.length;
						accessibleFiles = new IPath[length];
						for (int j = 0; j < length; j++) {
							String inclusionPattern = patterns[j];
							accessibleFiles[j] = new Path(inclusionPattern);
						}
					}
					// non accessible files
					IPath[] nonAccessibleFiles;
					if (librariesExclusionPatterns == null) {
						nonAccessibleFiles = new IPath[0];
					} else {
						String[] patterns = librariesExclusionPatterns[i];
						int length = patterns.length;
						nonAccessibleFiles = new IPath[length];
						for (int j = 0; j < length; j++) {
							String exclusionPattern = patterns[j];
							nonAccessibleFiles[j] = new Path(exclusionPattern);
						}
					}
					if (lib.indexOf(File.separatorChar) == -1 && lib.charAt(0) != '/' && lib.equals(lib.toUpperCase())) { // all upper case is a var 
						char[][] vars = CharOperation.splitOn(',', lib.toCharArray());
						entries[sourceLength+i] = RubyCore.newVariableEntry(new Path(new String(vars[0])), false);
					} else if (lib.startsWith("org.rubypeople.rdt")) { // container
						entries[sourceLength+i] = RubyCore.newContainerEntry(new Path(lib), false);
					} else {
						IPath libPath = new Path(lib);
						if (!libPath.isAbsolute() && libPath.segmentCount() > 0 && libPath.getFileExtension() == null) {
							project.getFolder(libPath).create(true, true, null);
							libPath = projectPath.append(libPath);
						}
						entries[sourceLength+i] = RubyCore.newLibraryEntry(libPath, false);
					}
				}
				for  (int i= 0; i < projectLength; i++) {
					boolean isExported = exportedProjects != null && exportedProjects.length > i && exportedProjects[i];
					
					// accessible files
					IPath[] accessibleFiles;
					if (projectsInclusionPatterns == null) {
						accessibleFiles = new IPath[0];
					} else {
						String[] patterns = projectsInclusionPatterns[i];
						int length = patterns.length;
						accessibleFiles = new IPath[length];
						for (int j = 0; j < length; j++) {
							String inclusionPattern = patterns[j];
							accessibleFiles[j] = new Path(inclusionPattern);
						}
					}
					// non accessible files
					IPath[] nonAccessibleFiles;
					if (projectsExclusionPatterns == null) {
						nonAccessibleFiles = new IPath[0];
					} else {
						String[] patterns = projectsExclusionPatterns[i];
						int length = patterns.length;
						nonAccessibleFiles = new IPath[length];
						for (int j = 0; j < length; j++) {
							String exclusionPattern = patterns[j];
							nonAccessibleFiles[j] = new Path(exclusionPattern);
						}
					}
					
					entries[sourceLength+libLength+i] =
						RubyCore.newProjectEntry(new Path(projects[i]), isExported);
				}
								
				// set classpath and output location
				IRubyProject javaProject = RubyCore.create(project);
				javaProject.setRawLoadpath(entries, null, null);
								
				result[0] = javaProject;
			}
		};
		getWorkspace().run(create, null);	
		return result[0];
	}
	
	/*
	 * Create simple project.
	 */
	protected IProject createProject(final String projectName) throws CoreException {
		final IProject project = getProject(projectName);
		IWorkspaceRunnable create = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				project.create(null);
				project.open(null);
			}
		};
		getWorkspace().run(create, null);	
		return project;
	}
	
	protected void addRubyNature(String projectName) throws CoreException {
		IProject project = getWorkspaceRoot().getProject(projectName);
		IProjectDescription description = project.getDescription();
		description.setNatureIds(new String[] {RubyCore.NATURE_ID});
		project.setDescription(description, null);
	}
	
	/**
	 * Returns the specified ruby script in the given project, root, and
	 * source folder or <code>null</code> if it does not exist.
	 */
	public IRubyScript getRubyScript(String projectName, String rootPath, String packageName, String cuName) throws RubyModelException {
		ISourceFolder pkg= getSourceFolder(projectName, rootPath, packageName);
		if (pkg == null) {
			return null;
		}
		return pkg.getRubyScript(cuName);
	}
	
	/**
	 * Returns the specified package fragment in the given project and root, or
	 * <code>null</code> if it does not exist.
	 * The rootPath must be specified as a project relative path. The empty
	 * path refers to the default package fragment.
	 */
	public ISourceFolder getSourceFolder(String projectName, String rootPath, String packageName) throws RubyModelException {
		ISourceFolderRoot root= getSourceFolderRoot(projectName, rootPath);
		if (root == null) {
			return null;
		}
		return root.getSourceFolder(packageName);
	}
	
	/**
	 * Returns the specified package fragment root in the given project, or
	 * <code>null</code> if it does not exist.
	 * If relative, the rootPath must be specified as a project relative path. 
	 * The empty path refers to the package fragment root that is the project
	 * folder iteslf.
	 * If absolute, the rootPath refers to either an external jar, or a resource 
	 * internal to the workspace
	 */
	public ISourceFolderRoot getSourceFolderRoot(
		String projectName, 
		String rootPath)
		throws RubyModelException {
			
		IRubyProject project = getRubyProject(projectName);
		if (project == null) {
			return null;
		}
		IPath path = new Path(rootPath);
		if (path.isAbsolute()) {
			IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
			IResource resource = workspaceRoot.findMember(path);
			ISourceFolderRoot root;
			if (resource == null) {
				// external jar
				root = project.getSourceFolderRoot(rootPath);
			} else {
				// resource in the workspace
				root = project.getSourceFolderRoot(resource);
			}
			return root;
		} else {
			ISourceFolderRoot[] roots = project.getSourceFolderRoots();
			if (roots == null || roots.length == 0) {
				return null;
			}
			for (int i = 0; i < roots.length; i++) {
				ISourceFolderRoot root = roots[i];
				if (!root.isExternal()
					&& root.getUnderlyingResource().getProjectRelativePath().equals(path)) {
					return root;
				}
			}
		}
		return null;
	}
	
	/**
	 * Returns the Ruby Project with the given name in this test
	 * suite's model. This is a convenience method.
	 */
	public IRubyProject getRubyProject(String name) {
		IProject project = getProject(name);
		return RubyCore.create(project);
	}
	
	/*
	 * Asserts that the given actual source (usually coming from a file content) is equal to the expected one.
	 * Note that 'expected' is assumed to have the '\n' line separator. 
	 * The line separators in 'actual' are converted to '\n' before the comparison.
	 */
	protected void assertSourceEquals(String message, String expected, String actual) {
		if (actual == null) {
			assertEquals(message, expected, null);
			return;
		}
		actual = org.rubypeople.rdt.core.tests.util.Util.convertToIndependantLineDelimiter(actual);
		if (!actual.equals(expected)) {
			System.out.print(org.rubypeople.rdt.core.tests.util.Util.displayString(actual.toString(), 2));
			System.out.println(this.endChar);
		}
		assertEquals(message, expected, actual);
	}
	
	protected IFolder createFolder(IPath path) throws CoreException {
		final IFolder folder = getWorkspaceRoot().getFolder(path);
		getWorkspace().run(new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				IContainer parent = folder.getParent();
				if (parent instanceof IFolder && !parent.exists()) {
					createFolder(parent.getFullPath());
				} 
				folder.create(true, true, null);
			}
		},
		null);
	
		return folder;
	}
	
	protected IRubyScript getRubyScript(String path) {
		return RubyCore.create(getFile(path));
	}
	
	public static void waitUntilIndexesReady() {
		
		// TODO Find some way to wait until the indexes are ready from SymbolIndex/build process
		
	}
	
	public void deleteFile(File file) {
		int retryCount = 0;
		while (++retryCount <= 60) { // wait 1 minute at most
			if (org.rubypeople.rdt.core.tests.util.Util.delete(file)) {
				break;
			}
		}
	}
	protected void deleteFolder(IPath folderPath) throws CoreException {
		deleteResource(getFolder(folderPath));
	}
	
	protected IFolder getFolder(IPath path) {
		return getWorkspaceRoot().getFolder(path);
	}
}
