/**
 * Copyright (c) 2008 Aptana, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl -v10.html. If redistributing this code,
 * this entire header must remain intact.
 *
 * This file is based on a JDT equivalent:
 *
 *******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.rubypeople.rdt.internal.ui.wizards;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;
import org.rubypeople.rdt.core.ILoadpathEntry;
import org.rubypeople.rdt.core.IRubyElement;
import org.rubypeople.rdt.core.IRubyProject;
import org.rubypeople.rdt.core.RubyCore;
import org.rubypeople.rdt.internal.ui.RubyPlugin;
import org.rubypeople.rdt.internal.ui.RubyPluginImages;
import org.rubypeople.rdt.internal.ui.util.CoreUtility;
import org.rubypeople.rdt.internal.ui.util.ExceptionHandler;
import org.rubypeople.rdt.internal.ui.wizards.buildpaths.BuildPathsBlock;
import org.rubypeople.rdt.internal.ui.wizards.buildpaths.CPListElement;
import org.rubypeople.rdt.launching.IVMInstall;
import org.rubypeople.rdt.launching.RubyRuntime;
import org.rubypeople.rdt.ui.PreferenceConstants;

public class RubyProjectWizard extends NewElementWizard implements IExecutableExtension {
    
    private RubyProjectWizardFirstPage fFirstPage;
//    private RubyProjectWizardSecondPage fSecondPage;
    
    private IConfigurationElement fConfigElement;
    private URI fCurrProjectLocation;
    
    public RubyProjectWizard() {
        setDefaultPageImageDescriptor(RubyPluginImages.DESC_WIZBAN_NEWJPRJ);
        setDialogSettings(RubyPlugin.getDefault().getDialogSettings());
        setWindowTitle(NewWizardMessages.RubyProjectWizard_title); 
    }

    /*
     * @see Wizard#addPages
     */	
    public void addPages() {
        super.addPages();
        fFirstPage= new RubyProjectWizardFirstPage();
        addPage(fFirstPage);
//        fSecondPage= new RubyProjectWizardSecondPage(fFirstPage);
//        addPage(fSecondPage);
    }		
    
    /* (non-Javadoc)
     * @see org.eclipse.jdt.internal.ui.wizards.NewElementWizard#finishPage(org.eclipse.core.runtime.IProgressMonitor)
     */
    protected void finishPage(IProgressMonitor monitor) throws InterruptedException, CoreException {    	
    	try {
			monitor.beginTask(NewWizardMessages.RubyProjectWizardSecondPage_operation_create, 3); 
			configureRubyProject(new SubProgressMonitor(monitor, 2));
		} finally {
			monitor.done();
		}    	
    }
    
	/**
	 * Adds the Ruby nature to the project (if not set yet) and configures the build loadpath.
	 * 
	 * @param monitor a progress monitor to report progress or <code>null</code> if
	 * progress reporting is not desired
	 * @throws CoreException Thrown when the configuring the Ruby project failed.
	 * @throws InterruptedException Thrown when the operation has been canceled.
	 */
	public void configureRubyProject(IProgressMonitor monitor) throws CoreException, InterruptedException {
		if (monitor == null) {
			monitor= new NullProgressMonitor();
		}
		
		int nSteps= 8;			
		monitor.beginTask(NewWizardMessages.RubyCapabilityConfigurationPage_op_desc_ruby, nSteps); 
		
		try {
			IProject project= fFirstPage.getProjectHandle();
			fCurrProjectLocation= getProjectLocationURI();
			
			URI realLocation= fCurrProjectLocation;
			if (fCurrProjectLocation == null) {  // inside workspace
				try {
					URI rootLocation= ResourcesPlugin.getWorkspace().getRoot().getLocationURI();
					realLocation= new URI(rootLocation.getScheme(), null,
						Path.fromPortableString(rootLocation.getPath()).append(project.getName()).toString(),
						null);
				} catch (URISyntaxException e) {
					Assert.isTrue(false, "Can't happen"); //$NON-NLS-1$
				}
			}
			
			BuildPathsBlock.createProject(project, fCurrProjectLocation, monitor);
			IRubyProject rubyProject = RubyCore.create(project);
			List<ILoadpathEntry> cpEntries= new ArrayList<ILoadpathEntry>();
			IPath projectPath= project.getFullPath();
			cpEntries.add(RubyCore.newSourceEntry(projectPath));
			cpEntries.addAll(Arrays.asList(getDefaultLoadpathEntry()));
			
			List<CPListElement> newClassPath= new ArrayList<CPListElement>();
			for (ILoadpathEntry entry : cpEntries) {
				newClassPath.add(CPListElement.createFromExisting(entry, rubyProject));
			}			
			
			monitor.worked(2);			
			BuildPathsBlock.addRubyNature(project, new SubProgressMonitor(monitor, 1));
			BuildPathsBlock.flush(newClassPath, rubyProject, new SubProgressMonitor(monitor, 5));
		} catch (OperationCanceledException e) {
			throw new InterruptedException();
		} finally {
			monitor.done();
		}			
	}
       
	private ILoadpathEntry[] getDefaultLoadpathEntry() {
		ILoadpathEntry[] defaultJRELibrary= PreferenceConstants.getDefaultRubyVMLibrary();
		String compliance= fFirstPage.getCompilerCompliance();
		IPath jreContainerPath= new Path(RubyRuntime.RUBY_CONTAINER);
		if (compliance == null || defaultJRELibrary.length > 1 || !jreContainerPath.isPrefixOf(defaultJRELibrary[0].getPath())) {
			// use default
			return defaultJRELibrary;
		}
		IVMInstall inst= fFirstPage.getJVM();
		if (inst != null) {
			IPath newPath= jreContainerPath.append(inst.getVMInstallType().getId()).append(inst.getName());
			return new ILoadpathEntry[] { RubyCore.newContainerEntry(newPath) };
		}
		return defaultJRELibrary;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizard#performFinish()
	 */
	public boolean performFinish() {
		boolean res= super.performFinish();
		if (res) {
			BasicNewProjectResourceWizard.updatePerspective(fConfigElement);
	 		selectAndReveal(fFirstPage.getProjectHandle());
		}
		return res;
	}
    
    protected void handleFinishException(Shell shell, InvocationTargetException e) {
        String title= NewWizardMessages.RubyProjectWizard_op_error_title; 
        String message= NewWizardMessages.RubyProjectWizard_op_error_create_message;			 
        ExceptionHandler.handle(e, getShell(), title, message);
    }	
    
    /*
     * Stores the configuration element for the wizard.  The config element will be used
     * in <code>performFinish</code> to set the result perspective.
     */
    public void setInitializationData(IConfigurationElement cfig, String propertyName, Object data) {
        fConfigElement= cfig;
    }
    
    /* (non-Javadoc)
     * @see IWizard#performCancel()
     */
    public boolean performCancel() {
        return super.performCancel();
    }
    
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.ui.wizards.NewElementWizard#getCreatedElement()
	 */
	public IRubyElement getCreatedElement() {
		return RubyCore.create(fFirstPage.getProjectHandle());
	}
	
	private URI getProjectLocationURI() throws CoreException {
		if (fFirstPage.isInWorkspace()) {
			return null;
		}
		return URIUtil.toURI(fFirstPage.getLocationPath());
	}
        
}
