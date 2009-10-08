package org.rubypeople.rdt.internal.core.parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.jruby.ast.CommentNode;

public class ASTTaskParser extends AbstractTaskParser
{
	public ASTTaskParser(Map<String, String> preferences)
	{
		super(preferences);
	}

	public List<TaskTag> getTasks(Collection<CommentNode> comments)
	{
		List<TaskTag> tasks = new ArrayList<TaskTag>();
		for (CommentNode commentNode : comments)
		{
			String line = commentNode.getContent();
			if (!fCaseSensitive)
				line = line.toLowerCase();
			for (int i = 0; i < fTags.length; i++)
			{
				String tag = fTags[i];
				int priority = fPriorities[i];
				if (!fCaseSensitive)
					tag = tag.toLowerCase();
				int index = line.indexOf(tag);
				if (index != -1)
				{
					String message = line.substring(index).trim();
					TaskTag task = new TaskTag(new String(message), priority, commentNode.getPosition().getStartLine(), commentNode
							.getPosition().getStartOffset(), commentNode.getPosition().getEndOffset());
					tasks.add(task);
				}
			}
		}
		return tasks;
	}

}
