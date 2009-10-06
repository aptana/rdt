package org.rubypeople.rdt.internal.debug.ui.launcher;

import junit.framework.TestCase;

import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.rubypeople.eclipse.shams.debug.core.ShamLaunchConfigurationWorkingCopy;
import org.rubypeople.rdt.internal.debug.ui.RdtDebugUiMessages;
import org.rubypeople.rdt.launching.IRubyLaunchConfigurationConstants;

public class TC_RubyArgumentsTab extends TestCase {

	public TC_RubyArgumentsTab(String name) {
		super(name);
	}

	public void testIsValid() {
		RubyArgumentsTab tab = new RubyArgumentsTab();
		
		ILaunchConfigurationWorkingCopy configuration = new ShamLaunchConfigurationWorkingCopy();
		String errorMessage = tab.getErrorMessage();
		assertNull("There should be no error message.", errorMessage);
		assertTrue("The tab is not valid when the configuration is completely empty.", !tab.isValid(configuration));
		errorMessage = RdtDebugUiMessages.LaunchConfigurationTab_RubyArguments_working_dir_error_message;
		assertEquals("The tab should set the error message for invalid working directory.", errorMessage, tab.getErrorMessage());

		configuration.setAttribute(IRubyLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, "aValidDirectory");
		assertTrue("The tab is valid when the configuration has a working directory.", tab.isValid(configuration));
		assertNull("The tab should set the error message to null when there is a working directory.", tab.getErrorMessage());
	}
}
