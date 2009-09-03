package org.rubypeople.rdt.internal.debug.ui;

import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.rubypeople.rdt.core.IMember;
import org.rubypeople.rdt.core.IMethod;
import org.rubypeople.rdt.core.IRubyElement;
import org.rubypeople.rdt.core.IRubyScript;
import org.rubypeople.rdt.core.IType;
import org.rubypeople.rdt.core.RubyCore;
import org.rubypeople.rdt.core.RubyModelException;
import org.rubypeople.rdt.debug.core.IRubyBreakpoint;
import org.rubypeople.rdt.debug.core.IRubyLineBreakpoint;
import org.rubypeople.rdt.debug.core.IRubyMethodBreakpoint;

public class BreakpointUtils
{

	/**
	 * Marker attribute storing the handle id of the Ruby element associated with a Ruby breakpoint
	 */
	private static final String HANDLE_ID = RdtDebugUiPlugin.getUniqueIdentifier() + ".RUBY_ELEMENT_HANDLE_ID"; //$NON-NLS-1$

	/**
	 * Marker attribute used to denote a run to line breakpoint
	 */
	private static final String RUN_TO_LINE = RdtDebugUiPlugin.getUniqueIdentifier() + ".run_to_line"; //$NON-NLS-1$

	/**
	 * Marker attribute used to denote the start of the region within a Ruby member that the breakpoint is located
	 * within.
	 */
	private static final String MEMBER_START = RdtDebugUiPlugin.getUniqueIdentifier() + ".member_start"; //$NON-NLS-1$

	/**
	 * Marker attribute used to denote the end of the region within a Ruby member that the breakpoint is located within.
	 */
	private static final String MEMBER_END = RdtDebugUiPlugin.getUniqueIdentifier() + ".member_end"; //$NON-NLS-1$

	/**
	 * Adds attributes to the given attribute map:
	 * <ul>
	 * <li>Ruby element handle id</li>
	 * <li>Attributes defined by <code>RubyCore</code></li>
	 * </ul>
	 * 
	 * @param attributes
	 *            the attribute map to use
	 * @param element
	 *            the Ruby element associated with the breakpoint
	 * @exception CoreException
	 *                if an exception occurs configuring the marker
	 */
	public static void addRubyBreakpointAttributes(Map attributes, IRubyElement element)
	{
		String handleId = element.getHandleIdentifier();
		attributes.put(HANDLE_ID, handleId);
		// RubyCore.addRubyElementMarkerAttributes(attributes, element);
	}

	/**
	 * Adds attributes to the given attribute map:
	 * <ul>
	 * <li>Ruby element handle id</li>
	 * <li>Member start position</li>
	 * <li>Member end position</li>
	 * <li>Attributes defined by <code>RubyCore</code></li>
	 * </ul>
	 * 
	 * @param attributes
	 *            the attribute map to use
	 * @param element
	 *            the Ruby element associated with the breakpoint
	 * @param memberStart
	 *            the start position of the Ruby member that the breakpoint is positioned within
	 * @param memberEnd
	 *            the end position of the Ruby member that the breakpoint is positioned within
	 * @exception CoreException
	 *                if an exception occurs configuring the marker
	 */
	public static void addRubyBreakpointAttributesWithMemberDetails(Map attributes, IRubyElement element,
			int memberStart, int memberEnd)
	{
		addRubyBreakpointAttributes(attributes, element);
		attributes.put(MEMBER_START, new Integer(memberStart));
		attributes.put(MEMBER_END, new Integer(memberEnd));
	}

	/**
	 * Returns the resource on which a breakpoint marker should be created for the given member. The resource returned
	 * is the associated file, or workspace root in the case of a binary in an external archive.
	 * 
	 * @param member
	 *            member in which a breakpoint is being created
	 * @return resource the resource on which a breakpoint marker should be created
	 */
	public static IResource getBreakpointResource(IRubyElement member)
	{
		if (member instanceof IMember)
		{
			IRubyScript script = ((IMember)member).getRubyScript();
			if (script != null && script.isWorkingCopy())
			{
				member = (IMember) member.getPrimaryElement();
			}
		}
		IResource res = member.getResource();
		if (res == null)
		{
			res = ResourcesPlugin.getWorkspace().getRoot();
		}
		else if (!res.getProject().exists())
		{
			res = ResourcesPlugin.getWorkspace().getRoot();
		}
		return res;
	}

	/**
	 * Returns the member associated with the line number of the given breakpoint.
	 * 
	 * @param breakpoint
	 *            Java line breakpoint
	 * @return member at the given line number in the type associated with the breakpoint
	 * @exception CoreException
	 *                if an exception occurs accessing the breakpoint
	 */
	public static IMember getMember(IRubyLineBreakpoint breakpoint) throws CoreException
	{
		if (breakpoint instanceof IRubyMethodBreakpoint)
		{
			return getMethod((IRubyMethodBreakpoint) breakpoint);
		}
		// if (breakpoint instanceof IRubyWatchpoint)
		// {
		// return getField((IRubyWatchpoint) breakpoint);
		// }

		int start = breakpoint.getCharStart();
		int end = breakpoint.getCharEnd();

		IType type = getType(breakpoint);

		if (start == -1 && end == -1)
		{
			start = breakpoint.getMarker().getAttribute(MEMBER_START, -1);
			end = breakpoint.getMarker().getAttribute(MEMBER_END, -1);
		}

		IMember member = null;
		if ((type != null && type.exists()) && (end >= start) && (start >= 0))
		{
			member = binSearch(type, start, end);
		}
		if (member == null)
		{
			member = type;
		}
		return member;
	}

	/**
	 * Returns the method associated with the method entry breakpoint.
	 * 
	 * @param breakpoint
	 *            Ruby method entry breakpoint
	 * @return method
	 */
	public static IMethod getMethod(IRubyMethodBreakpoint breakpoint)
	{
		String handle = breakpoint.getMarker().getAttribute(HANDLE_ID, null);
		if (handle != null)
		{
			IRubyElement je = RubyCore.create(handle);
			if (je != null)
			{
				if (je instanceof IMethod)
				{
					return (IMethod) je;
				}
			}
		}
		return null;
	}

	/**
	 * Returns the type that the given Ruby breakpoint refers to
	 * 
	 * @param breakpoint
	 *            Ruby breakpoint
	 * @return the type the breakpoint is associated with
	 */
	public static IType getType(IRubyBreakpoint breakpoint)
	{
		String handle = breakpoint.getMarker().getAttribute(HANDLE_ID, null);
		if (handle != null)
		{
			IRubyElement je = RubyCore.create(handle);
			if (je != null)
			{
				if (je instanceof IType)
				{
					return (IType) je;
				}
				if (je instanceof IMember)
				{
					return ((IMember) je).getDeclaringType();
				}
			}
		}
		return null;
	}

	/**
	 * Searches the given source range of the container for a member that is not the same as the given type.
	 */
	protected static IMember binSearch(IType type, int start, int end) throws RubyModelException
	{
		IRubyElement je = getElementAt(type, start);
		if (je != null && !je.equals(type))
		{
			return asMember(je);
		}
		if (end > start)
		{
			je = getElementAt(type, end);
			if (je != null && !je.equals(type))
			{
				return asMember(je);
			}
			int mid = ((end - start) / 2) + start;
			if (mid > start)
			{
				je = binSearch(type, start + 1, mid);
				if (je == null)
				{
					je = binSearch(type, mid + 1, end - 1);
				}
				return asMember(je);
			}
		}
		return null;
	}

	/**
	 * Returns the given Ruby element if it is an <code>IMember</code>, otherwise <code>null</code>.
	 * 
	 * @param element
	 *            Ruby element
	 * @return the given element if it is a type member, otherwise <code>null</code>
	 */
	private static IMember asMember(IRubyElement element)
	{
		if (element instanceof IMember)
		{
			return (IMember) element;
		}
		return null;
	}

	/**
	 * Returns the element at the given position in the given type
	 */
	protected static IRubyElement getElementAt(IType type, int pos) throws RubyModelException
	{
		return type.getRubyScript().getElementAt(pos);
	}
}
