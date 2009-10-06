package org.rubypeople.rdt.debug.core.tests;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.HashMap;

import junit.framework.TestCase;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.model.IProcess;
import org.rubypeople.eclipse.testutils.ResourceTools;
import org.rubypeople.rdt.debug.core.RdtDebugModel;
import org.rubypeople.rdt.internal.launching.RubyLaunchConfigurationAttribute;
import org.rubypeople.rdt.launching.IRubyLaunchConfigurationConstants;
import org.rubypeople.rdt.launching.IVMInstallType;
import org.rubypeople.rdt.launching.RubyRuntime;
import org.rubypeople.rdt.launching.VMStandin;

/*
 * 
 */
public class FTC_DebuggerLaunch extends TestCase {

	private static final String RUBY_INTERPRETER_ID = "RubyInterpreter";
	private static final String VM_TYPE_ID = "org.rubypeople.rdt.launching.StandardVMType";
	private static final boolean VERBOSE = false;
	private IVMInstallType vmType;


	public void setUp() {
		vmType = RubyRuntime.getVMInstallType(VM_TYPE_ID);
		// We rely on the RUBY_INTERPRETER to be a full path to a valid ruby executable, therefore the property rdt.rubyInterpreter has
		// to be set accordingly
		String rubyInterpreterPath = FTC_ClassicDebuggerCommunicationTest.RUBY_INTERPRETER;
		log("Using interpreter: " + rubyInterpreterPath);
		VMStandin standin = new VMStandin(vmType, RUBY_INTERPRETER_ID);
		standin.setInstallLocation(new File(rubyInterpreterPath));
		standin.convertToRealVM();
	}
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		vmType.disposeVMInstall(RUBY_INTERPRETER_ID);
	}
	
	protected void log(String label, ILaunch launch) throws Exception {
		log("Infos about " + label + ":");
		IProcess process = launch.getProcesses()[0];
		if (process.isTerminated()) {
			log("Process has finished with exit-value: " + process.getExitValue());
		} else {
			log("Process still running.");
		}
		String error = process.getStreamsProxy().getErrorStreamMonitor().getContents();
		if (error != null && error.length() > 0) {
			log("Process stderr: " + error);
		}	
		String stdout = process.getStreamsProxy().getOutputStreamMonitor().getContents();
		if (stdout != null && stdout.length() > 0) {
			log("Process stdout: " + stdout);
		}		
	}
	
	private void log(String message) {
		if (VERBOSE) System.out.println(message);		
	}

	
	public void testTwoSessions() throws Exception {
		ILaunchConfigurationType lcT = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurationType(IRubyLaunchConfigurationConstants.ID_RUBY_APPLICATION);
		
		ILaunchConfigurationWorkingCopy wc = lcT.newInstance(null, "TestLaunchConfiguration") ;
		IProject project = ResourceTools.createProject("FTCDebuggerLaunchMultipleSessions") ;
		IFile rubyFile = project.getFile("run.rb");
		
		rubyFile.create(new ByteArrayInputStream("puts 'a'\nputs 'b'".getBytes()), true, new NullProgressMonitor()) ;
		wc.setAttribute(IRubyLaunchConfigurationConstants.ATTR_PROJECT_NAME, rubyFile.getProject().getName());
		wc.setAttribute(IRubyLaunchConfigurationConstants.ATTR_FILE_NAME, rubyFile.getProjectRelativePath().toString());
		//wc.setAttribute(RubyLaunchConfigurationAttribute.WORKING_DIRECTORY, RubyApplicationShortcut.getDefaultWorkingDirectory(rubyFile.getProject()));
		wc.setAttribute(RubyLaunchConfigurationAttribute.SELECTED_INTERPRETER, RUBY_INTERPRETER_ID);
		ILaunchConfiguration lc = wc.doSave() ;
		
		RdtDebugModel.createLineBreakpoint(rubyFile, rubyFile.getName(), "Object", 1, true, new HashMap());
		
		ILaunch launch = lc.launch("debug", new NullProgressMonitor()) ;
		Thread.sleep(5000)  ;
		this.log("1. launch", launch) ;
		// getDebugTarget returns null if connection between ruby debugger and RubyDebuggerProxy (RubyLoop) could not
		// be established
		assertNotNull("1. debug target not null", launch.getDebugTarget()) ;
        assertNotNull("1. debug target has threads", launch.getDebugTarget().getThreads());
        assertTrue("1. debug target has at least one thread", launch.getDebugTarget().getThreads().length > 0);
		assertTrue("1. debug target's first thread is suspended ", launch.getDebugTarget().getThreads()[0].isSuspended()) ;
		
		// the breakpoint we have set for the first launch has disappeard at this point through
		// a ResourceChanged Event
		RdtDebugModel.createLineBreakpoint(rubyFile, rubyFile.getName(), "Object", 1, true, new HashMap());
		
		ILaunch secondlaunch = lc.launch("debug", new NullProgressMonitor()) ;
		Thread.sleep(5000)  ;
		this.log("2. launch", secondlaunch) ;
		assertNotNull("2. debug target not null", secondlaunch.getDebugTarget()) ;
        assertNotNull("2. debug target has threads", secondlaunch.getDebugTarget().getThreads());
        assertTrue("2. debug target has at least one thread", secondlaunch.getDebugTarget().getThreads().length > 0);
		assertFalse("2. debug target's first prozess is not terminated", secondlaunch.getProcesses()[0].isTerminated()) ;
		assertTrue("2. debug target's first thread is suspended ", secondlaunch.getDebugTarget().getThreads()[0].isSuspended()) ;
	}
}
