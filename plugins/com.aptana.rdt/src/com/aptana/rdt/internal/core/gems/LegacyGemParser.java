package com.aptana.rdt.internal.core.gems;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.aptana.rdt.AptanaRDTPlugin;
import com.aptana.rdt.core.gems.Gem;

public class LegacyGemParser implements IGemParser
{

	private String lineDelimeter;
	protected boolean strict;

	LegacyGemParser()
	{
		this(System.getProperty("line.separator"), false);
	}

	public LegacyGemParser(String lineDelimeter)
	{
		this(lineDelimeter, false);
	}

	LegacyGemParser(boolean strict)
	{
		this(System.getProperty("line.separator"), strict);
	}

	LegacyGemParser(String lineDelimeter, boolean strict)
	{
		this.lineDelimeter = lineDelimeter;
		this.strict = strict;
	}

	public Set<Gem> parse(String string) throws GemParseException
	{
		if (string == null || string.trim().length() == 0)
			return new HashSet<Gem>();
		String[] raw = string.split(lineDelimeter);
		if (raw.length == 1)
		{
			raw = string.split("\n");
		}
		if (raw.length == 1)
		{
			raw = string.split("\r");
		}
		List<String> lines = new ArrayList<String>(Arrays.asList(raw));
		if (lines.size() < 2)
		{
			return new HashSet<Gem>();
		}

		if (lines.get(0).startsWith("Updating metadata"))
		{
			lines.remove(0); // remove first line
			for (int i = 0; i < lines.size(); i++)
			{
				String line = lines.remove(0);
				if (line.trim().equals("complete"))
				{ // remove until we hit "complete"
					break;
				}
			}
		}
		else
		{
			String line = null;
			while (true)
			{
				if (lines.isEmpty())
					break;
				line = lines.get(0);
				if (line.trim().length() == 0 || line.trim().equals("*** LOCAL GEMS ***") || line.trim().equals("*** REMOTE GEMS ***"))
				{
					lines.remove(0);
				}
				else
				{
					break;
				}
			}
		}
		// TODO Trim off "\r" at end of each line!
		return parseOutGems(trimTrailingCRs(lines));
	}

	private List<String> trimTrailingCRs(List<String> lines)
	{
		List<String> trimmed = new ArrayList<String>();
		for (String line : lines)
		{
			if (line.endsWith("\r"))
				line = line.substring(0, line.length() - 1);
			trimmed.add(line);
		}
		return trimmed;
	}

	protected Set<Gem> parseOutGems(List<String> lines) throws GemParseException
	{
		Set<Gem> gems = new HashSet<Gem>();
		if (lines == null || lines.isEmpty())
			return gems;

		String line = lines.get(0);
		if (line.startsWith("ERROR:"))
			return gems;

		for (int i = 0; i < lines.size();)
		{
			String nameAndVersion = lines.get(i);
			String description = "";
			if ((i + 1) < lines.size())
			{
				description = lines.get(i + 1);
			}
			int j = 2;
			while (true)
			{
				if ((i + j) >= lines.size())
					break; // if there is no next line, break out
				String nextLine = lines.get(i + j);
				if (nextLine.trim().length() == 0)
					break; // if line is empty, break out
				description += " " + nextLine.trim(); // add line to
				// description
				j++; // move to next line
			}
			int openParen = nameAndVersion.indexOf('(');
			if (openParen == -1)
			{
				if (strict)
				{
					throw new GemParseException(
							"Parsing using legacy Gem Parser (< 1.2.0). Bad gems output format, no opening parenthesis for version: "
									+ lines);
				}
				else
				{
					AptanaRDTPlugin
							.log("Parsing using legacy Gem Parser (< 1.2.0). Bad gems output format, no opening parenthesis for version: "
									+ lines);
					return gems;
				}
			}
			int closeParen = nameAndVersion.indexOf(')');
			String name = nameAndVersion.substring(0, openParen);
			String version = nameAndVersion.substring(openParen + 1, closeParen);
			if (version.indexOf(",") != -1)
			{
				String[] versions = version.split(", ");
				for (int y = 0; y < versions.length; y++)
					gems.add(new Gem(name.trim(), versions[y], description.trim()));
			}
			else
			{
				gems.add(new Gem(name.trim(), version, description.trim()));
			}
			i += (j + 1);
		}
		return gems;
	}

}
