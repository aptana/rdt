package com.aptana.rdt.internal.core.gems;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.aptana.rdt.core.gems.Gem;

public class GemOnePointTwoParser extends LegacyGemParser
{
	private static final Pattern NAME_AND_VERSION_PATTERN = Pattern.compile("^\\S+\\s+\\(.+\\)\\z");

	public GemOnePointTwoParser(String string)
	{
		super(string);
	}

	public GemOnePointTwoParser(boolean strict)
	{
		super(strict);
	}

	protected Set<Gem> parseOutGems(List<String> lines) throws GemParseException
	{
		Set<Gem> gems = new HashSet<Gem>();
		if (lines == null || lines.isEmpty())
			return gems;

		if (lines.get(0).startsWith("ERROR:"))
			return gems;

		String nameAndVersion = null;
		String description = null;
		while (true)
		{
			while (true)
			{
				if (lines.isEmpty())
					break;
				String nextLine = lines.remove(0);
				if (nameAndVersion == null)
				{
					nameAndVersion = nextLine;
					continue;
				}
				if (nextLine.trim().length() == 0) // empty line
				{
					// first empty line means we're now doing description
					// second empty line (with a description that is done being read) means we're done with the gem
					if (description == null)
					{
						description = nextLine;
						continue;
					}
				}
				if (description != null)
				{
					Matcher m = NAME_AND_VERSION_PATTERN.matcher(nextLine);
					if (m.find())
					{
						lines.add(0, nextLine);
						break;
					}
					description += " " + nextLine.trim();
				}

			}
			if (description == null || description.length() == 0)
			{
				if (strict)
				{
					throw new GemParseException("Doesn't appear to be RubyGems 1.2+ listing format.");
				}
				else
				{
					return gems;
				}
			}
			// add the gem!
			int openParen = nameAndVersion.indexOf('(');
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
			nameAndVersion = null;
			description = null;
			if (lines.isEmpty())
				break;
		}
		return gems;
	}
}
