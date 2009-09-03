package com.aptana.rdt.internal.core.gems;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import com.aptana.rdt.core.gems.Gem;

public class ShortListingGemParser extends LegacyGemParser
{

	@Override
	protected Set<Gem> parseOutGems(List<String> lines) throws GemParseException
	{
		Set<Gem> gems = new HashSet<Gem>();
		for (String line : lines)
		{
			int openParen = line.indexOf("(");
			String name = line.substring(0, openParen).trim();
			int closeParen = line.indexOf(")", openParen);
			String versions = line.substring(openParen + 1, closeParen);
			StringTokenizer tokenizer = new StringTokenizer(versions, ",");
			while (tokenizer.hasMoreTokens())
			{
				String version = tokenizer.nextToken();
				gems.add(new Gem(name, version.trim(), null));
			}
		}
		return gems;
	}
}
