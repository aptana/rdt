/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.rubypeople.rdt.launching;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamsProxy;
import org.rubypeople.rdt.internal.launching.LaunchingMessages;
import org.rubypeople.rdt.internal.launching.LaunchingPlugin;
import org.rubypeople.rdt.internal.launching.LibraryInfo;

/**
 * Abstract implementation of a VM install type. Subclasses should implement
 * <ul>
 * <li><code>IVMInstall doCreateVMInstall(String id)</code></li>
 * <li><code>String getName()</code></li>
 * <li><code>IStatus validateInstallLocation(File installLocation)</code></li>
 * </ul>
 * <p>
 * Clients implementing VM install types should subclass this class.
 * </p>
 */

public abstract class AbstractVMInstallType implements IVMInstallType,
		IExecutableExtension {
	private List <IVMInstall> fVMs;
	private String fId;

	/**
	 * Constructs a new VM install type.
	 */
	protected AbstractVMInstallType() {
		fVMs = new ArrayList<IVMInstall>(10);
	}

	/*
	 * (non-Javadoc) Subclasses should not override this method.
	 * 
	 * @see IVMType#getVMs()
	 */
	public IVMInstall[] getVMInstalls() {
		IVMInstall[] vms = new IVMInstall[fVMs.size()];
		return (IVMInstall[]) fVMs.toArray(vms);
	}

	/*
	 * (non-Javadoc) Subclasses should not override this method.
	 * 
	 * @see IVMType#disposeVM(String)
	 */
	public void disposeVMInstall(String id) {
		for (int i = 0; i < fVMs.size(); i++) {
			IVMInstall vm = (IVMInstall) fVMs.get(i);
			if (vm.getId().equals(id)) {
				fVMs.remove(i);
				RubyRuntime.fireVMRemoved(vm);
				return;
			}
		}
	}

	/*
	 * (non-Javadoc) Subclasses should not override this method.
	 * 
	 * @see IVMType#getVM(String)
	 */
	public IVMInstall findVMInstall(String id) {
		for (int i = 0; i < fVMs.size(); i++) {
			IVMInstall vm = (IVMInstall) fVMs.get(i);
			if (vm.getId().equals(id)) {
				return vm;
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc) Subclasses should not override this method.
	 * 
	 * @see IVMType#createVM(String)
	 */
	public IVMInstall createVMInstall(String id)
			throws IllegalArgumentException {
		if (findVMInstall(id) != null) {
			String format = LaunchingMessages.vmInstallType_duplicateVM;
			throw new IllegalArgumentException(MessageFormat.format(format, id));
		}
		IVMInstall vm = doCreateVMInstall(id);
		fVMs.add(vm);
		return vm;
	}

	/**
	 * Subclasses should return a new instance of the appropriate
	 * <code>IVMInstall</code> subclass from this method.
	 * 
	 * @param id
	 *            The vm's id. The <code>IVMInstall</code> instance that is
	 *            created must return <code>id</code> from its
	 *            <code>getId()</code> method. Must not be <code>null</code>.
	 * @return the newly created IVMInstall instance. Must not return
	 *         <code>null</code>.
	 */
	protected abstract IVMInstall doCreateVMInstall(String id);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org
	 * .eclipse.core.runtime.IConfigurationElement, java.lang.String,
	 * java.lang.Object)
	 */
	/**
	 * Initializes the id parameter from the "id" attribute in the configuration
	 * markup. Subclasses should not override this method.
	 * 
	 * @param config
	 *            the configuration element used to trigger this execution. It
	 *            can be queried by the executable extension for specific
	 *            configuration properties
	 * @param propertyName
	 *            the name of an attribute of the configuration element used on
	 *            the <code>createExecutableExtension(String)</code> call. This
	 *            argument can be used in the cases where a single configuration
	 *            element is used to define multiple executable extensions.
	 * @param data
	 *            adapter data in the form of a <code>String</code>, a
	 *            <code>Hashtable</code>, or <code>null</code>.
	 * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement,
	 *      java.lang.String, java.lang.Object)
	 */
	public void setInitializationData(IConfigurationElement config,
			String propertyName, Object data) {
		fId = config.getAttribute("id"); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc) Subclasses should not override this method.
	 * 
	 * @see IVMType#getId()
	 */
	public String getId() {
		return fId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jdt.launching.IVMInstallType#findVMInstallByName(java.lang
	 * .String)
	 */
	public IVMInstall findVMInstallByName(String name) {
		for (int i = 0; i < fVMs.size(); i++) {
			IVMInstall vm = (IVMInstall) fVMs.get(i);
			if (vm.getName().equals(name)) {
				return vm;
			}
		}
		return null;
	}

	/**
	 * Parses the output from 'LibraryDetector'.
	 */
	protected LibraryInfo parseLibraryInfo(IProcess process) {
		IStreamsProxy streamsProxy = process.getStreamsProxy();
		if (streamsProxy == null)
			return null;
		String text = streamsProxy.getOutputStreamMonitor().getContents();
		BufferedReader reader = new BufferedReader(new StringReader(text));
		List<String> lines = new ArrayList<String>();
		try {
			String line = null;
			while ((line = reader.readLine()) != null) {
				lines.add(line);
			}
		} catch (IOException e) {
			LaunchingPlugin.log(e);
		}
		if (lines.size() > 0) {
			String version = lines.remove(0);
			removeNotExistingLibs(lines);
			if (lines.size() > 0) {
				String[] loadpath = lines.toArray(new String[lines.size()]);
				return new LibraryInfo(version, loadpath);
			}
		}
		return null;
	}

	protected LibraryInfo generateLibraryInfo(File rubyHome, File rubyExecutable) {
		LibraryInfo info = null;
		// locate the script to grab us our loadpaths
		File file = getLibraryInfoGeneratorPath(); //$NON-NLS-1$
		if (file.exists()) {
			String rubyExecutablePath = rubyExecutable.getAbsolutePath();
			String[] cmdLine = new String[] { rubyExecutablePath,
					file.getAbsolutePath() }; //$NON-NLS-1$
			Process p = null;
			try {
				p = Runtime.getRuntime().exec(cmdLine);
				IProcess process = DebugPlugin.newProcess(new Launch(null,
						ILaunchManager.RUN_MODE, null), p, "Library Detection"); //$NON-NLS-1$
				for (int i = 0; i < 200; i++) {
					// Wait no more than 10 seconds (200 * 50 mils)
					if (process.isTerminated()) {
						break;
					}
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
					}
				}
				info = parseLibraryInfo(process);
			} catch (IOException ioe) {
				LaunchingPlugin.log(ioe);
			} finally {
				if (p != null) {
					p.destroy();
				}
			}
		}
		if (info == null) {
			// log error that we were unable to generate library info - see bug
			// 70011
			LaunchingPlugin
					.log(MessageFormat
							.format(
									"Failed to retrieve default libraries for {0}", rubyHome.getAbsolutePath())); //$NON-NLS-1$
		}
		return info;
	}

	protected File getLibraryInfoGeneratorPath() {
		return LaunchingPlugin.getFileInPlugin(new Path("ruby").append(
				"standard_vm_type").append("loadpath.rb"));
	}

	/**
	 * Do not consider libraries which does not exist.
	 * 
	 * @param libraries
	 */
	private void removeNotExistingLibs(List<String> libraries) {
		List<String> toRemove = new ArrayList<String>();
		for (String path : libraries) {
			File file = new File(path);
			if (!file.exists())
				toRemove.add(path);
		}
		libraries.removeAll(toRemove);
	}

	private List<String> readOutput(IProcess process) {
		IStreamsProxy streamsProxy = process.getStreamsProxy();
		if (streamsProxy == null) return Collections.emptyList();
		String text = streamsProxy.getOutputStreamMonitor().getContents();
		BufferedReader reader = new BufferedReader(new StringReader(text));
		List<String> lines = new ArrayList<String>();
		try {
			String line = null;
			while ((line = reader.readLine()) != null) {
				lines.add(line);
			}
		} catch (IOException e) {
			LaunchingPlugin.log(e);
		}
		return lines;
	}
	
	protected List<String> executeAndRead(String[] cmdLine) {
		Process p = null;
		try {
			p = Runtime.getRuntime().exec(cmdLine);
			IProcess process = DebugPlugin.newProcess(new Launch(null, ILaunchManager.RUN_MODE, null), p, "Library Detection"); //$NON-NLS-1$
			for (int i= 0; i < 200; i++) {
				// Wait no more than 10 seconds (200 * 50 mils)
				if (process.isTerminated()) {
					break;
				}
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
				}
			}
			return readOutput(process);
		} catch (IOException ioe) {
			LaunchingPlugin.log(ioe);
		} finally {
			if (p != null) {
				p.destroy();
			}
		}
		return null;
	}
	
	protected File parseRubyExecutableLocation(List<String> lines) {
		if (lines == null || lines.isEmpty()) {
			return null;
		}
		String location = lines.remove(0);
		File executable = new File(location);
		if (executable.isFile() && executable.exists())
			return executable;
		return null;
	}
	
	protected abstract LibraryInfo getLibraryInfo(File rubyHome, File rubyExecutable);

	public String getVMVersion(File installLocation, File executable) {
		LibraryInfo info = getLibraryInfo(installLocation, executable);
		return info.getVersion();
	}
}
