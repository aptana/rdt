package com.aptana.rdt.internal.core.gems;

import java.util.Set;

import com.aptana.rdt.core.gems.Gem;

public class GemOnePointTwoParserTest extends AbstractGemParserTestCase
{

	public void testParsingLocalGems() throws GemParseException
	{
		String contents = getContents("src/com/aptana/rdt/internal/core/gems/1.2.txt");
		Set<Gem> gems = getParser().parse(contents);
		assertEquals(7, gems.size());
	}

	public void testRubygems1dot3() throws GemParseException
	{
		String contents = getContents("src/com/aptana/rdt/internal/core/gems/1.3.txt");
		Set<Gem> gems = getParser().parse(contents);
		assertEquals(110, gems.size());
	}
	
	public void testRubygems1dot3Remote() throws GemParseException
	{
		String contents = getContents("src/com/aptana/rdt/internal/core/gems/1.3_remote.txt");
		Set<Gem> gems = getParser().parse(contents);
		assertEquals(3758, gems.size());
	}

	protected LegacyGemParser getParser()
	{
		return new GemOnePointTwoParser("\n");
	}
}
