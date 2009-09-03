/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.rubypeople.rdt.internal.debug.ui.rubyvms;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.rubypeople.rdt.internal.debug.ui.RdtDebugUiPlugin;
import org.rubypeople.rdt.internal.launching.VMDefinitionsContainer;
import org.rubypeople.rdt.launching.IVMInstall;
import org.rubypeople.rdt.launching.IVMInstallType;
import org.rubypeople.rdt.launching.RubyRuntime;

/**
 * Processes add/removed/changed VMs.
 */
public class RubyVMsUpdater {
	
	// the VMs defined when this updated is instantiated
	private VMDefinitionsContainer fOriginalVMs;	
	
	/**
	 * Contstructs a new VM updater to update VM install settings.
	 */
	public RubyVMsUpdater() {
		fOriginalVMs = new VMDefinitionsContainer();
		IVMInstall def = RubyRuntime.getDefaultVMInstall();
		if (def != null) {
			fOriginalVMs.setDefaultVMInstallCompositeID(RubyRuntime.getCompositeIdFromVM(def));
		}
	
		IVMInstallType[] types = RubyRuntime.getVMInstallTypes();
		for (int i = 0; i < types.length; i++) {
			IVMInstall[] vms = types[i].getVMInstalls();
			for (int j = 0; j < vms.length; j++) {
				fOriginalVMs.addVM(vms[j]);
			}
		}
	}
	
	/**
	 * Updates VM settings and returns whether the update was successful.
	 * 
	 * @param rubyVMs new installed JREs
	 * @param defaultRubyVM new default VM
	 * @return whether the update was successful
	 */
	public boolean updateRubyVMSettings(IVMInstall[] rubyVMs, IVMInstall defaultRubyVM) {
		
		// Create a VM definition container
		VMDefinitionsContainer vmContainer = new VMDefinitionsContainer();
		
		// Set the default VM Id on the container
		String defaultVMId = RubyRuntime.getCompositeIdFromVM(defaultRubyVM);
		vmContainer.setDefaultVMInstallCompositeID(defaultVMId);
		
		// Set the VMs on the container
		for (int i = 0; i < rubyVMs.length; i++) {
			vmContainer.addVM(rubyVMs[i]);
		}
		
		
		// Generate XML for the VM defs and save it as the new value of the VM preference
		saveVMDefinitions(vmContainer);
		
		return true;
	}
	
	private void saveVMDefinitions(final VMDefinitionsContainer container) {
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				try {
					monitor.beginTask(RubyVMMessages.JREsUpdater_0, 100); 
					String vmDefXML = container.getAsXML();
					monitor.worked(40);
					RubyRuntime.getPreferences().setValue(RubyRuntime.PREF_VM_XML, vmDefXML);
					monitor.worked(30);
					RubyRuntime.savePreferences();
					monitor.worked(30);
				} catch (IOException ioe) {
					RdtDebugUiPlugin.log(ioe);
				} catch (ParserConfigurationException e) {
					RdtDebugUiPlugin.log(e);
				} catch (TransformerException e) {
					RdtDebugUiPlugin.log(e);
				} finally {
					monitor.done();
				}
				
			}
		};
		try {
			RdtDebugUiPlugin.getDefault().getWorkbench().getProgressService().busyCursorWhile(runnable);
		} catch (InvocationTargetException e) {
			RdtDebugUiPlugin.log(e);
		} catch (InterruptedException e) {
			RdtDebugUiPlugin.log(e);
		}
	}
}
