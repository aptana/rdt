package com.aptana.rdt.core.rspec;

import org.rubypeople.rdt.core.ISourceRange;
import org.rubypeople.rdt.core.ISourceReference;
import org.rubypeople.rdt.core.RubyModelException;
import org.rubypeople.rdt.internal.core.SourceRange;

public class Example implements ISourceReference
{
	private String description;
	private Behavior parent;
	private int offset;
	private int length;

	Example(String description, int offset, int length)
	{
		this.description = description;
		this.offset = offset;
		this.length = length;
	}

	void setParent(Behavior parent)
	{
		this.parent = parent;
	}

	public Behavior getBehavior()
	{
		return parent;
	}

	public String getDescription()
	{
		return description;
	}

	public String getSource() throws RubyModelException
	{
		return getDescription();
	}

	public ISourceRange getSourceRange() throws RubyModelException
	{
		return new SourceRange(offset, length);
	}
}
