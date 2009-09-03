package com.aptana.rdt.internal.core.gems;

import java.util.Collections;
import java.util.Set;

import com.aptana.rdt.AptanaRDTPlugin;
import com.aptana.rdt.core.gems.Gem;
import com.aptana.rdt.core.gems.Version;

/**
 * Special Gem Parser which tries both old and new parsers so if one faisl we at least try to pasre with the other.
 * Version of RubyGems determines order we try.
 * 
 * @author cwilliams
 */
public class HybridGemParser implements IGemParser
{

	private IGemParser[] parsers;

	public HybridGemParser(Version version)
	{
		if (version != null && version.isLessThan("1.2.0"))
		{ // prefer the old parser
			parsers = new IGemParser[] { new LegacyGemParser(true), new GemOnePointTwoParser(true) };
		}
		else
		{ // prefer the new parser
			parsers = new IGemParser[] { new GemOnePointTwoParser(true), new LegacyGemParser(true) };
		}
	}

	/**
	 * Tries parsers in order until it gets a non-empty list of gems and returns that. Otherwise it returns empty set.
	 */
	public Set<Gem> parse(String string)
	{
		for (int i = 0; i < parsers.length; i++)
		{
			try
			{
				Set<Gem> gems = parsers[i].parse(string);
				if (gems != null && !gems.isEmpty())
					return gems;
			}
			catch (GemParseException e)
			{
				AptanaRDTPlugin.log(e);
			}
		}
		return Collections.emptySet();
	}

}
