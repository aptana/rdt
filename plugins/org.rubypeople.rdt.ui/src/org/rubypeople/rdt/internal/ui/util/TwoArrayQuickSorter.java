package org.rubypeople.rdt.internal.ui.util;

import java.util.Comparator;

import org.eclipse.core.runtime.Assert;

/**
 * Quick sort to sort key-value pairs. The keys and values are specified in separate arrays.
 */
public class TwoArrayQuickSorter
{

	private Comparator fComparator;

	/**
	 * Default comparator.
	 */
	public static final class StringComparator implements Comparator<String>
	{
		private boolean fIgnoreCase;

		StringComparator(boolean ignoreCase)
		{
			fIgnoreCase = ignoreCase;
		}

		public int compare(String left, String right)
		{
			return fIgnoreCase ? left.compareToIgnoreCase(right) : left.compareTo(right);
		}
	}

	/**
	 * Creates a sorter with default string comparator. The keys are assumed to be strings.
	 * 
	 * @param ignoreCase
	 *            specifies whether sorting is case sensitive or not.
	 */
	public TwoArrayQuickSorter(boolean ignoreCase)
	{
		fComparator = new StringComparator(ignoreCase);
	}

	/**
	 * Creates a sorter with a comparator.
	 * 
	 * @param comparator
	 *            the comparator to order the elements. The comparator must not be <code>null</code>.
	 */
	public TwoArrayQuickSorter(Comparator<? extends Object> comparator)
	{
		fComparator = comparator;
	}

	/**
	 * Sorts keys and values in parallel. Values stay "attached" to the keys, which are the ones getting sorted.
	 * 
	 * @param keys
	 *            the keys to use for sorting.
	 * @param values
	 *            the values associated with the keys.
	 */
	public void sort(Object[] keys, Object[] values)
	{
		if ((keys == null) || (values == null))
		{
			Assert.isTrue(false, "Either keys or values is null"); //$NON-NLS-1$
			return;
		}

		if (keys.length <= 1)
			return;

		internalSort(keys, values, 0, keys.length - 1);
	}

	private void internalSort(Object[] keys, Object[] values, int left, int right)
	{
		int original_left = left;
		int original_right = right;

		Object mid = keys[(left + right) / 2];
		do
		{
			while (fComparator.compare(keys[left], mid) < 0)
				left++;

			while (fComparator.compare(mid, keys[right]) < 0)
				right--;

			if (left <= right)
			{
				swap(keys, left, right);
				swap(values, left, right);
				left++;
				right--;
			}
		}
		while (left <= right);

		if (original_left < right)
			internalSort(keys, values, original_left, right);

		if (left < original_right)
			internalSort(keys, values, left, original_right);
	}

	/*
	 * Swaps x[a] with x[b].
	 */
	private static final void swap(Object x[], int a, int b)
	{
		Object t = x[a];
		x[a] = x[b];
		x[b] = t;
	}

}