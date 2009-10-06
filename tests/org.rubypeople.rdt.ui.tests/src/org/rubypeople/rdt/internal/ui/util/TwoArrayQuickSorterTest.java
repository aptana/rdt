package org.rubypeople.rdt.internal.ui.util;

import java.util.Comparator;

import junit.framework.TestCase;

import org.eclipse.core.runtime.AssertionFailedException;

public class TwoArrayQuickSorterTest extends TestCase
{

	public void testSortStringIgnoringCase() throws Exception
	{
		// TODO Test String compare actually testing case ignoring and with case
		String[] keys = new String[] { "zebra", "apple", "pear", "baby" };
		String[] values = new String[] { "stripes", "fuji", "bartlett", "diaper" };
		TwoArrayQuickSorter sorter = new TwoArrayQuickSorter(true);
		sorter.sort(keys, values);

		// Keys get sorted
		int i = 0;
		assertEquals("apple", keys[i++]);
		assertEquals("baby", keys[i++]);
		assertEquals("pear", keys[i++]);
		assertEquals("zebra", keys[i++]);

		// Values stayed with the keys, rather than get sorted themselves
		i = 0;
		assertEquals("fuji", values[i++]);
		assertEquals("diaper", values[i++]);
		assertEquals("bartlett", values[i++]);
		assertEquals("stripes", values[i++]);
	}

	public void testSortIntegerKeysStringValues() throws Exception
	{
		Integer[] keys = new Integer[] { 9, 3, 7, 1 };
		String[] values = new String[] { "nine", "three", "seven", "one" };
		Comparator comparator = new Comparator()
		{
			public int compare(Object o1, Object o2)
			{
				if (o1 instanceof Integer)
				{
					return ((Integer) o1).compareTo((Integer) o2);
				}
				if (o1 instanceof String)
				{
					return ((String) o1).compareTo((String) o2);
				}
				return 0;
			}
		};
		TwoArrayQuickSorter sorter = new TwoArrayQuickSorter(comparator);
		sorter.sort(keys, values);

		// Keys get sorted
		int i = 0;
		assertEquals(new Integer(1), keys[i++]);
		assertEquals(new Integer(3), keys[i++]);
		assertEquals(new Integer(7), keys[i++]);
		assertEquals(new Integer(9), keys[i++]);

		// Values stayed with the keys, rather than get sorted themselves
		i = 0;
		assertEquals("one", values[i++]);
		assertEquals("three", values[i++]);
		assertEquals("seven", values[i++]);
		assertEquals("nine", values[i++]);
	}

	public void testSortNullKeysThrowsException() throws Exception
	{
		try
		{
			TwoArrayQuickSorter sorter = new TwoArrayQuickSorter(true);
			sorter.sort(null, new String[0]);
			fail("Expected an AssertionFailedException");
		}
		catch (AssertionFailedException e)
		{
			assertTrue(true);
		}
	}

	public void testSortNullValuesThrowsException() throws Exception
	{
		try
		{
			TwoArrayQuickSorter sorter = new TwoArrayQuickSorter(true);
			sorter.sort(new String[0], null);
			fail("Expected an AssertionFailedException");
		}
		catch (AssertionFailedException e)
		{
			assertTrue(true);
		}
	}

	// TODO Test two arrays with different lengths
	
	public void testSortEmptyArrays() throws Exception
	{
		String[] keys = new String[0];
		String[] values = new String[0];
		TwoArrayQuickSorter sorter = new TwoArrayQuickSorter(true);
		sorter.sort(new String[0], new String[0]);

		assertEquals(0, keys.length);
		assertEquals(0, values.length);
	}
}
