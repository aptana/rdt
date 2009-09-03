package com.aptana.rdt.core.rspec;

import java.util.ArrayList;
import java.util.List;

import org.rubypeople.rdt.core.ISourceRange;
import org.rubypeople.rdt.core.ISourceReference;
import org.rubypeople.rdt.core.RubyModelException;
import org.rubypeople.rdt.internal.core.SourceRange;

public class Behavior implements ISourceReference
{
	private String className;
	private List<Example> examples = new ArrayList<Example>();
	private int offset;
	private int length;

	Behavior(String className, int offset, int length)
	{
		this.className = className;
		this.offset = offset;
		this.length = length;
	}

	void addExample(Example example)
	{
		example.setParent(this);
		examples.add(example);
	}

	public Object[] getExamples()
	{
		return examples.toArray(new Object[examples.size()]);
	}

	public String getClassName()
	{
		return className;
	}

	public String getSource() throws RubyModelException
	{
		return getClassName();
	}

	public ISourceRange getSourceRange() throws RubyModelException
	{
		return new SourceRange(offset, length);
	}
}
