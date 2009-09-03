package org.rubypeople.rdt.internal.debug.ui.launcher;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;

public class ExecutionArguments {
	protected static final QualifiedName EXECUTION_ARGUMENTS_PROPERTY = new QualifiedName("org.rubypeople.rdt", "executionArguments");
	protected static final String ARGUMENT_SEPARATOR = "**<ArgBreak>**";
	
	protected String interpreterArguments, rubyFileArguments;

	public static ExecutionArguments getExecutionArguments(IFile rubyScriptFile) {
		try {
			String executionArgumentsPersistableFormat = rubyScriptFile.getPersistentProperty(EXECUTION_ARGUMENTS_PROPERTY);
			ExecutionArguments executionArguments = new ExecutionArguments();
			
			if (executionArgumentsPersistableFormat != null) {				
				int argBreakIndex = executionArgumentsPersistableFormat.indexOf(ARGUMENT_SEPARATOR);
				executionArguments.setInterpreterArguments(executionArgumentsPersistableFormat.substring(0, argBreakIndex));
				executionArguments.setRubyFileArguments(executionArgumentsPersistableFormat.substring(argBreakIndex + ARGUMENT_SEPARATOR.length()));
			}
			
			return executionArguments;
		} catch (CoreException e) {}
		
		return null;
	}

	public static void setExecutionArguments(IFile rubyScriptFile, ExecutionArguments arguments) {
		try {
			rubyScriptFile.setPersistentProperty(EXECUTION_ARGUMENTS_PROPERTY, arguments.toPersistableFormat());
		} catch (CoreException e) {}
	}

	public void setInterpreterArguments(String theArguments) {
		interpreterArguments = theArguments;
	}

	public void setRubyFileArguments(String theArguments) {
		rubyFileArguments = theArguments;
	}

	public String toPersistableFormat() {
		return interpreterArguments + ARGUMENT_SEPARATOR + rubyFileArguments;
	}
}