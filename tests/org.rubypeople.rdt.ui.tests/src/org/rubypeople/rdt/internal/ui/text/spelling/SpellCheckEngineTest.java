package org.rubypeople.rdt.internal.ui.text.spelling;

import java.util.Locale;
import java.util.Set;

import junit.framework.TestCase;

public class SpellCheckEngineTest extends TestCase
{

	public void testUSDictionaryIncluded() throws Exception
	{
		Set<Locale> locales = SpellCheckEngine.getAvailableLocales();
		assertNotNull(locales);
		assertTrue(locales.contains(Locale.US));
	}
}
