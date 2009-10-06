package org.rubypeople.rdt.internal.launching;

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.rubypeople.rdt.launching.IVMInstall;
import org.rubypeople.rdt.launching.IVMRunner;

public class TestVMRunner extends StandardVMRunner implements IVMRunner {

	public TestVMRunner(IVMInstall vmInstance) {
		super();
		setVMInstall(vmInstance);
	}
	
	@Override
	protected Process exec(String[] cmdLine, File workingDirectory, String[] envp) throws CoreException {
		return new ShamProcess();
	}

}
