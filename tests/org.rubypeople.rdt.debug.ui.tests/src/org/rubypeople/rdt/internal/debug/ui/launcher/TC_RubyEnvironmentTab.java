package org.rubypeople.rdt.internal.debug.ui.launcher;

import junit.framework.TestCase;

import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.rubypeople.eclipse.shams.debug.core.ShamLaunchConfigurationWorkingCopy;
import org.rubypeople.rdt.internal.debug.ui.RdtDebugUiMessages;
import org.rubypeople.rdt.internal.debug.ui.launcher.RubyEnvironmentTab;
import org.rubypeople.rdt.internal.launching.RubyLaunchConfigurationAttribute;

public class TC_RubyEnvironmentTab extends TestCase {

	public TC_RubyEnvironmentTab(String name) {
		super(name);
	}

	public void testIsValid() {
		RubyEnvironmentTab tab = new RubyEnvironmentTab();
		
		ILaunchConfigurationWorkingCopy configuration = new ShamLaunchConfigurationWorkingCopy();
		String errorMessage = tab.getErrorMessage();
		assertNull("There should be no error message.", errorMessage);
		assertTrue("The tab is not valid when the configuration is completely empty.", !tab.isValid(configuration));
		errorMessage = RdtDebugUiMessages.LaunchConfigurationTab_RubyEnvironment_interpreter_not_selected_error_message;
		assertEquals("The tab should set the error message for no interpreter selected.", errorMessage, tab.getErrorMessage());

		configuration.setAttribute(RubyLaunchConfigurationAttribute.SELECTED_INTERPRETER, "anInterpreter");
		assertTrue("The tab is valid when the configuration has a selected interpreter.", tab.isValid(configuration));
		assertNull("The tab should set the error message to null when there is a selected interpreter.", tab.getErrorMessage());
	}
}
