package com.aptana.rdt.internal.ui.text.correction;

import org.eclipse.core.runtime.CoreException;
import org.rubypeople.rdt.ui.text.ruby.IInvocationContext;
import org.rubypeople.rdt.ui.text.ruby.IProblemLocation;
import org.rubypeople.rdt.ui.text.ruby.IQuickAssistProcessor;
import org.rubypeople.rdt.ui.text.ruby.IRubyCompletionProposal;

public class QuickAssistProcessor implements IQuickAssistProcessor {

	public IRubyCompletionProposal[] getAssists(IInvocationContext context, IProblemLocation[] locations) throws CoreException {
		if (!hasAssists(context)) return new IRubyCompletionProposal[0];
		IRubyCompletionProposal modifier = new StatementModifierAssist(context);
		return new IRubyCompletionProposal[] { modifier };
	}

	public boolean hasAssists(IInvocationContext context) throws CoreException {
		return StatementModifierAssist.enabled(context);
	}

}
