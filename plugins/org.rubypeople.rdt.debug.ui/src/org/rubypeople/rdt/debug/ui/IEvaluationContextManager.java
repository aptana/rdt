package org.rubypeople.rdt.debug.ui;

import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.rubypeople.rdt.debug.core.model.IRubyStackFrame;

public interface IEvaluationContextManager {

	public IRubyStackFrame getEvaluationContext(IWorkbenchPart part);

	public IRubyStackFrame getEvaluationContext(IWorkbenchWindow window);
	
	public void startup();
}
