/*
 * Created on Feb 18, 2005
 *
 */
package org.rubypeople.rdt.internal.core.parser;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.rubypeople.rdt.core.RubyCore;

/**
 * @author Chris
 */
public class TaskParser extends AbstractTaskParser
{
	// FIXME This implementation is really, really slow!
	public TaskParser(Map<String, String> preferences)
	{
		super(preferences);
	}

	public List<TaskTag> getTasks(Reader reader) throws IOException
	{
		return getTasks(loadFromReader(reader));
	}

	public List<TaskTag> getTasks(String contents)
	{
		List<TaskTag> tasks = new ArrayList<TaskTag>();
		try
		{
			if (fTags.length <= 0)
				return Collections.emptyList();
			int offset = 0;
			int lineNum = 0;
			String line = null;

			while ((line = findNextLine(contents, offset)) != null)
			{
				tasks.addAll(processLine(line, offset, lineNum));
				lineNum++;
				offset += line.length();
			}
		}
		catch (CoreException e)
		{
			RubyCore.log(e);
		}
		return tasks;
	}

	private String findNextLine(String contents, int offset)
	{
		if (offset >= contents.length())
			return null;

		int crPos = contents.indexOf('\r', offset);
		int nlPos = contents.indexOf('\n', offset);
		int eolPos = crPos;
		if (crPos == -1)
			eolPos = nlPos;
		if (nlPos == -1 && crPos == -1)
			return contents.substring(offset);
		if (crPos + 1 == nlPos && crPos >= 0)
		{
			eolPos++;
		}
		return contents.substring(offset, eolPos + 1);
	}

	private List<TaskTag> processLine(String line, int offset, int lineNum) throws CoreException
	{
		List<TaskTag> tasks = new ArrayList<TaskTag>();
		if (!fCaseSensitive)
			line = line.toLowerCase();
		for (int i = 0; i < fTags.length; i++)
		{
			String tag = fTags[i];
			int priority = fPriorities[i];
			if (!fCaseSensitive)
				tag = tag.toLowerCase();
			if (line.matches(".*#.*" + tag + ".*[\\n\\r]*"))
			{
				int index = line.indexOf(tag);
				String message = line.substring(index).trim();
				tasks.add(createTaskTag(priority, message, lineNum + 1, offset + index, offset + index
						+ message.length()));
			}
		}
		return tasks;
	}

	private TaskTag createTaskTag(int priority, String message, int lineNumber, int start, int end)
			throws CoreException
	{
		return new TaskTag(message, priority, lineNumber, start, end);
	}

	private String loadFromReader(Reader reader) throws IOException
	{
		StringBuffer contents = new StringBuffer();
		char[] buffer = new char[4096];
		while (true)
		{
			int bytesRead = reader.read(buffer);
			if (bytesRead == -1)
				return contents.toString();
			contents.append(buffer, 0, bytesRead);
		}
	}
}
