package com.aptana.rdt.internal.parser.warnings;

import java.util.HashSet;
import java.util.Set;

import org.jruby.ast.HashNode;
import org.jruby.ast.ListNode;
import org.jruby.ast.Node;
import org.rubypeople.rdt.core.parser.warnings.RubyLintVisitor;
import org.rubypeople.rdt.internal.core.util.ASTUtil;

import com.aptana.rdt.AptanaRDTPlugin;

public class DuplicateHashKeyVisitor extends RubyLintVisitor
{

	public DuplicateHashKeyVisitor(String code)
	{
		super(AptanaRDTPlugin.getDefault().getOptions(), code);
	}

	@Override
	protected String getOptionKey()
	{
		return AptanaRDTPlugin.COMPILER_PB_DUPLICATE_HASH_KEY;
	}

	@Override
	public Object visitHashNode(HashNode visited)
	{
		ListNode list = visited.getListNode();
		Set<String> keys = new HashSet<String>();
		for (int i = 0; i < list.size(); i++)
		{
			if (i % 2 != 0)
				continue;

			Node node = list.get(i);
			String name = ASTUtil.stringValue(node);
			if (name == null)
				continue;
			if (keys.contains(name))
			{
				// FIXME Include the value in position too, need to merge start of key with end of value to generate
				// proper position.
				createProblem(node.getPosition(), "Duplicate hash key definition: '" + name + "'");
			}
			keys.add(name);
		}
		return super.visitHashNode(visited);
	}

}
