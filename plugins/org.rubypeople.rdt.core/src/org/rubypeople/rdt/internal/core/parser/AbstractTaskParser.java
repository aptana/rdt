package org.rubypeople.rdt.internal.core.parser;

import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IMarker;
import org.rubypeople.rdt.core.RubyCore;

public class AbstractTaskParser
{

	protected boolean fCaseSensitive = false;
	protected String[] fTags;
	protected int[] fPriorities;

	public AbstractTaskParser(Map<String, String> preferences)
	{
		super();
		String caseSensitive = getString(preferences, RubyCore.COMPILER_TASK_CASE_SENSITIVE, RubyCore.ENABLED);
		if (caseSensitive.equals(RubyCore.ENABLED))
			fCaseSensitive = true;
		String tags = getString(preferences, RubyCore.COMPILER_TASK_TAGS, RubyCore.DEFAULT_TASK_TAGS);
		String priorities = getString(preferences, RubyCore.COMPILER_TASK_PRIORITIES, RubyCore.DEFAULT_TASK_PRIORITIES);
		fTags = tokenize(tags, ",");
		fPriorities = convertPriorities(tokenize(priorities, ","));
	}

	protected String getString(Map<String, String> preferences, String key, String def)
	{
		if (preferences == null)
			return def;
		String answer = preferences.get(key);
		if (answer == null)
			return def;
		return answer;
	}

	protected int[] convertPriorities(String[] stringPriorities)
	{
		int priorities[] = new int[stringPriorities.length];
		for (int i = 0; i < stringPriorities.length; i++)
		{
			String priority = stringPriorities[i];
			if (priority.equals(RubyCore.COMPILER_TASK_PRIORITY_LOW))
			{
				priorities[i] = IMarker.PRIORITY_LOW;
			}
			else if (priority.equals(RubyCore.COMPILER_TASK_PRIORITY_HIGH))
			{
				priorities[i] = IMarker.PRIORITY_HIGH;
			}
			else
			{
				priorities[i] = IMarker.PRIORITY_NORMAL;
			}
		}
		return priorities;
	}

	protected String[] tokenize(String tags, String delim)
	{
		String[] tokens;
		StringTokenizer tokenizer = new StringTokenizer(tags, delim);
		tokens = new String[tokenizer.countTokens()];
		int i = 0;
		while (tokenizer.hasMoreTokens())
		{
			tokens[i++] = tokenizer.nextToken();
		}
		return tokens;
	}

}