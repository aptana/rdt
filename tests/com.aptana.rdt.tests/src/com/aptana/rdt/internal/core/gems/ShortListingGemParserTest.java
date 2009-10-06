package com.aptana.rdt.internal.core.gems;

import java.util.Set;

import com.aptana.rdt.core.gems.Gem;

public class ShortListingGemParserTest extends AbstractGemParserTestCase
{

	@Override
	protected IGemParser getParser()
	{
		return new ShortListingGemParser();
	}
	
	public void testBlah() throws GemParseException
	{
		String contents = getContents("src/com/aptana/rdt/internal/core/gems/short.txt");
		Set<Gem> gems = getParser().parse(contents);
		assertEquals(5323, gems.size());
	}

}
