package org.rubypeople.rdt.internal.debug.ui.console;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.StringTokenizer;

import org.eclipse.debug.ui.console.IConsole;
import org.eclipse.debug.ui.console.IConsoleLineTracker;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.swt.program.Program;
import org.eclipse.ui.console.IHyperlink;

public class URLConsoleLineTracker implements IConsoleLineTracker
{

	private IConsole fConsole;

	public void dispose()
	{
		fConsole = null;
	}

	public void init(IConsole console)
	{
		this.fConsole = console;
	}

	public void lineAppended(IRegion line)
	{
		String text;
		try
		{
			text = getText(line);
		}
		catch (BadLocationException e1)
		{
			return;
		}
		int index = text.indexOf("://");
		if (index == -1)
			return;
		// read backwards until we hit space to get beginning
		// TODO Handle if we hit double quote
		int start = index;
		while (true)
		{
			char c = text.charAt(start);
			if (c == ' ')
			{
				start++;
				break;
			}
			if (start == 0)
				break;
			start--;
		}
		// Read forwards until we hit space or one of \t\r\n<>
		StringTokenizer tokenizer = new StringTokenizer(text.substring(index), " \t\r\n<>");
		if (!tokenizer.hasMoreTokens())
			return;
		String url = text.substring(start, index) + tokenizer.nextToken();
		url = url.trim();
		try
		{
			new URL(url);
		}
		catch (MalformedURLException e)
		{
			return;
		}
		int offset = line.getOffset() + start;
		int length = url.length();
		IHyperlink link = new URLHyperlink(url);
		fConsole.addLink(link, offset, length);
	}

	protected String getText(IRegion line) throws BadLocationException
	{
		return fConsole.getDocument().get(line.getOffset(), line.getLength());
	}

	private static class URLHyperlink implements IHyperlink
	{

		private String fURLString;

		public URLHyperlink(String url)
		{
			this.fURLString = url;
		}

		public void linkExited()
		{
		}

		public void linkEntered()
		{
		}

		public void linkActivated()
		{
			Program.launch(fURLString);
		}
	}

}
