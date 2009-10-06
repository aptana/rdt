package org.rubypeople.rdt.internal.launching;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.rubypeople.rdt.core.tests.ModifyingResourceTest;
import org.rubypeople.rdt.launching.IVMInstall;
import org.rubypeople.rdt.launching.IVMInstallChangedListener;
import org.rubypeople.rdt.launching.IVMInstallType;
import org.rubypeople.rdt.launching.PropertyChangeEvent;
import org.rubypeople.rdt.launching.RubyRuntime;
import org.rubypeople.rdt.launching.VMStandin;

public class TC_RubyRuntime extends ModifyingResourceTest
{

	private static final String VM_TYPE_ID = "org.rubypeople.rdt.launching.StandardVMType";

	private IVMInstallType vmType;
	private IFolder folderOne;
	private IFolder folderTwo;

	public TC_RubyRuntime(String name)
	{
		super(name);
	}

	// TODO Need to create a test vm type or something

	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		vmType = RubyRuntime.getVMInstallType(VM_TYPE_ID);
		// RubyRuntime.setDefaultVMInstall(null, null, true);
		IVMInstall[] installs = vmType.getVMInstalls();
		for (int i = 0; i < installs.length; i++)
		{
			vmType.disposeVMInstall(installs[i].getId());
		}
		LaunchingPlugin.getDefault().setIgnoreVMDefPropertyChangeEvents(true);
		createProject("/rubyRuntime");
		folderOne = createFolder("/rubyRuntime/interpreterOne");
		createFolder("/rubyRuntime/interpreterOne/lib");
		createFolder("/rubyRuntime/interpreterOne/bin");
		createFile("/rubyRuntime/interpreterOne/bin/ruby", "");
		folderTwo = createFolder("/rubyRuntime/interpreterTwo");
		createFolder("/rubyRuntime/interpreterTwo/lib");
		createFolder("/rubyRuntime/interpreterTwo/bin");
		createFile("/rubyRuntime/interpreterTwo/bin/ruby", "");
	}

	@Override
	protected void tearDown() throws Exception
	{
		RubyRuntime.setDefaultVMInstall(null, null, true);
		IVMInstall[] installs = vmType.getVMInstalls();
		for (int i = 0; i < installs.length; i++)
		{
			vmType.disposeVMInstall(installs[i].getId());
		}
		vmType = null;
		RubyRuntime.getPreferences().setValue(RubyRuntime.PREF_VM_XML, "");
		deleteProject("/rubyRuntime");
		super.tearDown();
	}

	public void testGetInstalledInterpreters()
	{
		String vmOneName = "InterpreterOne";
		String vmOneId = vmOneName;
		String vmTwoName = "InterpreterTwo";
		String vmTwoId = vmTwoName;
		try
		{
			VMStandin standin = new VMStandin(vmType, vmOneId);
			standin.setInstallLocation(folderOne.getLocation().toFile());
			standin.setName(vmOneName);
			standin.convertToRealVM();

			VMStandin standin2 = new VMStandin(vmType, vmTwoId);
			standin2.setInstallLocation(folderTwo.getLocation().toFile());
			standin2.setName(vmTwoName);
			standin2.convertToRealVM();

			IVMInstall[] installs = vmType.getVMInstalls();
			assertEquals(2, installs.length);
			assertEquals(vmOneName, installs[0].getName());
			assertEquals(vmTwoName, installs[1].getName());
		}
		finally
		{
			vmType.disposeVMInstall(vmOneId);
			vmType.disposeVMInstall(vmTwoId);
		}
	}

	public void testSetDefaultVM() throws Exception
	{
		String vmOneName = "InterpreterOne";
		String vmOneId = vmOneName;
		try
		{
			VMStandin standin = new VMStandin(vmType, vmOneId);
			standin.setInstallLocation(folderOne.getLocation().toFile());
			standin.setName(vmOneName);
			IVMInstall vm = standin.convertToRealVM();

			final boolean[] receivedDefaultVMInstallChangedEvent = new boolean[1];
			RubyRuntime.addVMInstallChangedListener(new IVMInstallChangedListener()
			{

				public void defaultVMInstallChanged(IVMInstall previous, IVMInstall current)
				{
					receivedDefaultVMInstallChangedEvent[0] = true;
				}

				public void vmAdded(IVMInstall newVm)
				{
				}

				public void vmChanged(PropertyChangeEvent event)
				{
				}

				public void vmRemoved(IVMInstall removedVm)
				{
				}

			});
			RubyRuntime.setDefaultVMInstall(vm, new NullProgressMonitor(), false);
			assertEquals(vm, RubyRuntime.getDefaultVMInstall());
			assertTrue(receivedDefaultVMInstallChangedEvent[0]);
		}
		finally
		{
			vmType.disposeVMInstall(vmOneId);
		}
	}

	public void testCheckInterpreterBin() throws Exception
	{
		String vmOneName = "InterpreterOne";
		String vmOneId = vmOneName;
		try
		{
			VMStandin standin = new VMStandin(vmType, vmOneId);
			standin.setInstallLocation(folderOne.getLocation().toFile());
			standin.setName(vmOneName);
			IVMInstall vm = standin.convertToRealVM();

			RubyRuntime.setDefaultVMInstall(vm, new NullProgressMonitor(), false);

			IPath path = RubyRuntime.checkInterpreterBin("ruby");
			assertNotNull(path);
			path = RubyRuntime.checkInterpreterBin("doesnt_exist");
			assertNull(path);
		}
		finally
		{
			vmType.disposeVMInstall(vmOneId);
		}
	}
}
