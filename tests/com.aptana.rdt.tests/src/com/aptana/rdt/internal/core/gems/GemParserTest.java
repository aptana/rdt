package com.aptana.rdt.internal.core.gems;

import java.util.Set;

import com.aptana.rdt.core.gems.Gem;

public class GemParserTest extends AbstractGemParserTestCase
{

	public void testParsingLocalGems() throws GemParseException
	{
		String contents = getContents("src/com/aptana/rdt/internal/core/gems/local.txt");
		Set<Gem> gems = getParser().parse(contents);
		assertEquals(77, gems.size());
	}

	public void testEndsWithTwoLineDescription() throws GemParseException
	{
		String contents = getContents("src/com/aptana/rdt/internal/core/gems/2line_description_end.txt");
		Set<Gem> gems = getParser().parse(contents);
		assertEquals(62, gems.size());
	}

	public void testMattsBrokenList() throws GemParseException
	{
		String contents = getContents("src/com/aptana/rdt/internal/core/gems/matt.txt");
		Set<Gem> gems = getParser().parse(contents);
		assertEquals(22, gems.size());
	}

	public void testJavaHomeErrorFromJRuby() throws GemParseException
	{
		String contents = "You must set JAVA_HOME to point at your Java Development Kit installation";
		Set<Gem> gems = getParser().parse(contents);
		assertEquals(0, gems.size());
	}

	public void testUpdating() throws GemParseException
	{
		String contents = getContents("src/com/aptana/rdt/internal/core/gems/updating.txt");
		Set<Gem> gems = getParser().parse(contents);
		assertEquals(120, gems.size());
	}

	protected IGemParser getParser()
	{
		return new LegacyGemParser("\n");
	}

}
