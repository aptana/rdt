package org.rubypeople.rdt.internal.ui.util;

import junit.framework.TestCase;

import org.rubypeople.rdt.internal.ui.util.StringMatcher.Position;

public class StringMatcherTest extends TestCase
{

	// TODO Find and match with offset and non-full length!
	// TODO Find with oddball patterns like "*??*" and "*abc*" to see what positions it returns!
	public void testStartAfterEnd()
	{
		String pattern = "Ch*r";
		StringMatcher matcher = new StringMatcher(pattern, true, false);
		assertNull(matcher.find("Cheerios", 3, 1));
		assertFalse(matcher.match("Cheerios", 3, 1));
	}

	public void testNullPattern()
	{
		try
		{
			new StringMatcher(null, true, false);
			fail("Expected IllegalArgumentException");
		}
		catch (IllegalArgumentException e)
		{
			assertTrue(true);
		}
	}

	public void testFindWithNullText()
	{
		StringMatcher matcher = new StringMatcher("*", true, false);
		try
		{
			matcher.find(null, 0, 0);
			fail("Expected IllegalArgumentException");
		}
		catch (IllegalArgumentException e)
		{
			assertTrue(true);
		}
	}

	public void testMatchWithNullText()
	{
		StringMatcher matcher = new StringMatcher("*", true, false);
		try
		{
			matcher.match(null);
			fail("Expected IllegalArgumentException");
		}
		catch (IllegalArgumentException e)
		{
			assertTrue(true);
		}
	}

	public void testMatchWithStartAndEndWithNullText()
	{
		StringMatcher matcher = new StringMatcher("*", true, false);
		try
		{
			matcher.match(null, 0, 0);
			fail("Expected IllegalArgumentException");
		}
		catch (IllegalArgumentException e)
		{
			assertTrue(true);
		}
	}

	public void testFindWithEmptyTextString()
	{
		String pattern = "Ch*r";
		StringMatcher matcher = new StringMatcher(pattern, true, false);
		assertNull(matcher.find("", 0, "".length()));

		pattern = "*";
		matcher = new StringMatcher(pattern, true, false);
		assertNull(matcher.find("", 0, "".length()));

		pattern = "?";
		matcher = new StringMatcher(pattern, true, false);
		assertNull(matcher.find("", 0, "".length()));
	}

	public void testFindWithEmptyPattern()
	{
		String pattern = "";
		StringMatcher matcher = new StringMatcher(pattern, false, false);
		String text = "cheerios";
		// empty string pattern is supposed to return 0's
		Position pos = matcher.find(text, 0, text.length());
		assertNotNull(pos);
		assertEquals(0, pos.getStart());
		assertEquals(0, pos.getEnd());
	}

	public void testFindIgnoringCase()
	{
		String pattern = "Ch*r";
		StringMatcher matcher = new StringMatcher(pattern, true, false);
		String text = "cheerios";
		Position pos = matcher.find(text, 0, text.length());
		assertNotNull(pos);
		assertEquals(0, pos.getStart());
		assertEquals(5, pos.getEnd());
	}

	public void testMatchIgnoringCase()
	{
		String pattern = "Ch*ios";
		StringMatcher matcher = new StringMatcher(pattern, true, false);
		assertTrue(matcher.match("cheerios"));
		assertTrue(matcher.match("Cheerios"));
		assertFalse(matcher.match("cheeris"));
	}

	public void testMatchNotIgnoringCase()
	{
		String pattern = "Ch*ios";
		StringMatcher matcher = new StringMatcher(pattern, false, false);
		assertFalse(matcher.match("cheerios"));
		assertTrue(matcher.match("Cheerios"));
		assertFalse(matcher.match("cheeris"));
	}

	public void testFindNotIgnoringCase()
	{
		String pattern = "Ch*r";
		StringMatcher matcher = new StringMatcher(pattern, false, false);
		// Finds when case matches
		String text = "Cheerios";
		Position pos = matcher.find(text, 0, text.length());
		assertNotNull(pos);
		assertEquals(0, pos.getStart());
		assertEquals(5, pos.getEnd());
		// doesn't when C case is different
		text = "cheerios";
		pos = matcher.find(text, 0, text.length());
		assertNull(pos);
	}

	public void testFindStarInPatternIgnoringWildcards()
	{
		String pattern = "Ch*r";
		StringMatcher matcher = new StringMatcher(pattern, false, true);

		String text = "Cheerios";
		Position pos = matcher.find(text, 0, text.length());
		assertNull(pos);

		text = "Ch*ris";
		pos = matcher.find(text, 0, text.length());
		assertEquals(0, pos.getStart());
		assertEquals(4, pos.getEnd());
	}

	public void testFindQuestionMarkInPatternIgnoringWildcards()
	{
		String pattern = "Ch?r";
		StringMatcher matcher = new StringMatcher(pattern, false, true);

		String text = "Cher";
		Position pos = matcher.find(text, 0, text.length());
		assertNull(pos);

		text = "Ch?r";
		pos = matcher.find(text, 0, text.length());
		assertEquals(0, pos.getStart());
		assertEquals(4, pos.getEnd());
	}

	public void testFindWithSingleCharWildcard()
	{
		String pattern = "Chr?s";
		StringMatcher matcher = new StringMatcher(pattern, false, false);

		String text = "Chris";
		Position pos = matcher.find(text, 0, text.length());
		assertNotNull(pos);
		assertEquals(0, pos.getStart());
		assertEquals(5, pos.getEnd());

		text = "Chrs";
		pos = matcher.find(text, 0, text.length());
		assertNull(pos);
	}

	public void testFindWithStarWildcard()
	{
		String pattern = "Ch*r";
		StringMatcher matcher = new StringMatcher(pattern, false, false);

		// finds when * == multiple chars
		String text = "Cheerios";
		Position pos = matcher.find(text, 0, text.length());
		assertNotNull(pos);
		assertEquals(0, pos.getStart());
		assertEquals(5, pos.getEnd());

		// finds when there's no chars picked up by *
		text = "Chris";
		pos = matcher.find(text, 0, text.length());
		assertNotNull(pos);
		assertEquals(0, pos.getStart());
		assertEquals(3, pos.getEnd());
	}

	public void testFindWithOpenEndedWildcardAtEnd()
	{
		String pattern = "Ch*";
		StringMatcher matcher = new StringMatcher(pattern, false, false);
		String text = "Cheerios";
		Position pos = matcher.find(text, 0, text.length());
		assertNotNull(pos);
		assertEquals(0, pos.getStart());
		assertEquals(2, pos.getEnd()); // only matches the concrete chars in pattern
	}

}
