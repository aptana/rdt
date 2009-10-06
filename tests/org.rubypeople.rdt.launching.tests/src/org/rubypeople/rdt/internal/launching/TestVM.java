package org.rubypeople.rdt.internal.launching;

import org.eclipse.debug.core.ILaunchManager;
import org.rubypeople.rdt.launching.IVMInstall;
import org.rubypeople.rdt.launching.IVMInstallType;
import org.rubypeople.rdt.launching.IVMRunner;

public class TestVM extends StandardVM implements IVMInstall {

	public TestVM(IVMInstallType type, String id) {
		super(type, id);
	}
	@Override
	public IVMRunner getVMRunner(String mode) {
		if (ILaunchManager.RUN_MODE.equals(mode)) {
			return new TestVMRunner(this);
		} else if (ILaunchManager.DEBUG_MODE.equals(mode)) {
			if (useRDebug()) {
				return new TestRubyDebugDebugger(this);
			}
			return new TestVMDebugger(this);
		}
		return null;
	}
}
