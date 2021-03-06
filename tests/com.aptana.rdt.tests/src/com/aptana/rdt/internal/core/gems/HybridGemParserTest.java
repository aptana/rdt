package com.aptana.rdt.internal.core.gems;

import java.util.Set;

import com.aptana.rdt.core.gems.Gem;
import com.aptana.rdt.core.gems.Version;

public class HybridGemParserTest extends GemParserTest
{

	public void testOneDotTwo() throws GemParseException
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

	protected IGemParser getParser()
	{
		return new HybridGemParser(new Version("1.2.0"));
	}
}
