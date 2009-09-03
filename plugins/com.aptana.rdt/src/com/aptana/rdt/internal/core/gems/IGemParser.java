package com.aptana.rdt.internal.core.gems;

import java.util.Set;

import com.aptana.rdt.core.gems.Gem;

public interface IGemParser
{
	/**
	 * Throws a GemParseException if the parser is set to strict and format is not as expected.
	 * 
	 * @param string
	 * @return
	 * @throws GemParseException
	 */
	public Set<Gem> parse(String string) throws GemParseException;
}
