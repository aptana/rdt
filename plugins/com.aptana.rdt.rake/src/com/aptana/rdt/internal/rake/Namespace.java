package com.aptana.rdt.internal.rake;

import java.util.ArrayList;
import java.util.List;

import org.rubypeople.rdt.core.ISourceRange;
import org.rubypeople.rdt.core.ISourceReference;
import org.rubypeople.rdt.core.RubyModelException;
import org.rubypeople.rdt.internal.core.SourceRange;

public class Namespace implements ISourceReference {

	private List<Object> children = new ArrayList<Object>();
	private String name;
	private int offset;
	private int length;

	public Namespace(String name, int offset, int length) {
		this.name = name;
		this.offset = offset;
		this.length = length;
	}

	public void addChild(Object child) {
		children.add(child);
	}

	public Object[] getChildren() {
		return children.toArray(new Object[children.size()]);
	}

	public String toString() {
		return name;
	}

	public String getSource() throws RubyModelException {
		return name;
	}

	public ISourceRange getSourceRange() throws RubyModelException {
		return new SourceRange(offset, length);
	}

}
