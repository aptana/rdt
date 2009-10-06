package org.rubypeople.rdt.internal.launching;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.rubypeople.rdt.internal.debug.core.RubyDebuggerProxy;
import org.rubypeople.rdt.internal.debug.core.model.IRubyDebugTarget;
import org.rubypeople.rdt.internal.debug.core.model.RubyDebugTarget;
import org.rubypeople.rdt.internal.debug.core.model.RubyProcessingException;
import org.rubypeople.rdt.launching.IVMInstall;
import org.rubypeople.rdt.launching.IVMRunner;

public class TestRubyDebugDebugger extends RDebugVMDebugger implements IVMRunner {

	public TestRubyDebugDebugger(IVMInstall vmInstance) {
		super();
		setVMInstall(vmInstance);
	}
	@Override
	protected Process exec(String[] cmdLine, File workingDirectory, String[] envp) throws CoreException {
		return new ShamProcess();
	}
	
	@Override
	protected RubyDebuggerProxy getDebugProxy(RubyDebugTarget debugTarget) {
		return new TestDebuggerProxy(debugTarget, true);
	}

	private static class TestDebuggerProxy extends RubyDebuggerProxy {

		public TestDebuggerProxy(IRubyDebugTarget debugTarget, boolean isRubyDebug) {
			super(debugTarget, isRubyDebug);
		}
		
		@Override
		public void start() throws RubyProcessingException, IOException {
			// intentionally empty
		}
		
	}
}
