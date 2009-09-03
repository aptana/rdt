package com.aptana.rdt.internal.rake;

import org.rubypeople.rdt.core.ISourceRange;
import org.rubypeople.rdt.core.ISourceReference;
import org.rubypeople.rdt.core.RubyModelException;
import org.rubypeople.rdt.internal.core.SourceRange;

public class Task implements ISourceReference {
	
	private String className;
	private int offset;
	private int length;

	Task(String className, int offset, int length) {
		this.className = className;
		this.offset = offset;
		this.length = length;
	}

	public String getName() {
		return className;
	}

	public String getSource() throws RubyModelException {
		return getName();
	}

	public ISourceRange getSourceRange() throws RubyModelException {
		return new SourceRange(offset, length);
	}
}
