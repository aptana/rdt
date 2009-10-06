package org.rubypeople.rdt.internal.launching;

import org.rubypeople.rdt.launching.IVMInstall;

public class TestVMType extends StandardVMType {

	@Override
	protected IVMInstall doCreateVMInstall(String id) {
		return new TestVM(this, id);
	}
}
