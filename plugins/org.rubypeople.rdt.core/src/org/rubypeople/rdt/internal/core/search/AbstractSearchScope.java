package org.rubypeople.rdt.internal.core.search;

import org.rubypeople.rdt.core.IRubyElementDelta;
import org.rubypeople.rdt.core.search.IRubySearchScope;

public abstract class AbstractSearchScope implements IRubySearchScope
{

	/*
	 * (non-Javadoc) Process the given delta and refresh its internal state if needed. Returns whether the internal
	 * state was refreshed.
	 */
	public abstract void processDelta(IRubyElementDelta delta, int eventType);

}
