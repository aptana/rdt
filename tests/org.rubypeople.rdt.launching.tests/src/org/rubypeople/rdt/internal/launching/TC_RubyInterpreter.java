package org.rubypeople.rdt.internal.launching;

import java.io.File;

import junit.framework.TestCase;

import org.rubypeople.rdt.launching.IVMInstall;
import org.rubypeople.rdt.launching.IVMInstallType;
import org.rubypeople.rdt.launching.RubyRuntime;

public class TC_RubyInterpreter extends TestCase {

    private static final String VM_TYPE_ID = "org.rubypeople.rdt.launching.StandardVMType";
	private IVMInstallType vmType;

    @Override
    protected void setUp() throws Exception {
    	super.setUp();
    	vmType = RubyRuntime.getVMInstallType(VM_TYPE_ID);
    }
    
    public void testEquals() {
		IVMInstall interpreterOne = new StandardVM(vmType, "InterpreterOne");
		interpreterOne.setInstallLocation(new File("/InterpreterOnePath"));
		IVMInstall similarInterpreterOne = new StandardVM(vmType, "InterpreterOne");
		similarInterpreterOne.setInstallLocation(new File("/InterpreterOnePath"));
		assertTrue("Interpreters should be equal.", interpreterOne.equals(similarInterpreterOne));
		
		IVMInstall interpreterTwo = new StandardVM(vmType, "InterpreterTwo");
		interpreterTwo.setInstallLocation(new File("/InterpreterTwoPath"));
		assertTrue("Interpreters should not be equal.", !interpreterOne.equals(interpreterTwo));
	}
}
