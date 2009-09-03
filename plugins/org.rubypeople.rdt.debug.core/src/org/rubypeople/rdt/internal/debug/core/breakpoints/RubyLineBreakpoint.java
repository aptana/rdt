package org.rubypeople.rdt.internal.debug.core.breakpoints;

import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.rubypeople.rdt.debug.core.IRubyLineBreakpoint;
import org.rubypeople.rdt.debug.core.RdtDebugModel;

public class RubyLineBreakpoint extends RubyBreakpoint implements IRubyLineBreakpoint
{
	public static final String RUBY_BREAKPOINT_MARKER = "org.rubypeople.rdt.debug.core.rubyLineBreakpointMarker"; //$NON-NLS-1$

	private static final String EXTERNAL_FILENAME = "externalFileName";
	private int index = -1; // index of breakpoint on ruby debugger side

	public RubyLineBreakpoint()
	{
	}

	/**
	 * @param typeName2
	 * @see RDtDebugModel#createLineBreakpoint(IResource, String, int, int, int, int, boolean, Map)
	 */
	public RubyLineBreakpoint(IResource resource, String fileName, String typeName, int lineNumber, int charStart,
			int charEnd, int hitCount, boolean add, Map attributes) throws DebugException
	{
		this(resource, fileName, typeName, lineNumber, charStart, charEnd, hitCount, add, attributes,
				RUBY_BREAKPOINT_MARKER);
	}

	protected RubyLineBreakpoint(final IResource resource, final String fileName, final String typeName,
			final int lineNumber, final int charStart, final int charEnd, final int hitCount, final boolean add,
			final Map attributes, final String markerType) throws DebugException
	{

		IWorkspaceRunnable wr = new IWorkspaceRunnable()
		{
			public void run(IProgressMonitor monitor) throws CoreException
			{
				// create the marker
				setMarker(resource.createMarker(RUBY_BREAKPOINT_MARKER));
				if (resource.equals(ResourcesPlugin.getWorkspace().getRoot()))
				{
					attributes.put(EXTERNAL_FILENAME, fileName);
				}
				// add attributes
				addLineBreakpointAttributes(attributes, getModelIdentifier(), true, lineNumber, charStart, charEnd);
				 addTypeNameAndHitCount(attributes, typeName, hitCount);
				// set attributes
				// attributes.put(SUSPEND_POLICY, new Integer(getDefaultSuspendPolicy()));
				ensureMarker().setAttributes(attributes);

				// add to breakpoint manager if requested
				register(add);

			}
		};
		run(getMarkerRule(resource), wr);
	}

	/**
	 * Adds the standard attributes of a line breakpoint to the given attribute map. The standard attributes are:
	 * <ol>
	 * <li>IBreakpoint.ID</li>
	 * <li>IBreakpoint.ENABLED</li>
	 * <li>IMarker.LINE_NUMBER</li>
	 * <li>IMarker.CHAR_START</li>
	 * <li>IMarker.CHAR_END</li>
	 * </ol>
	 */
	public void addLineBreakpointAttributes(Map<String, Object> attributes, String modelIdentifier, boolean enabled,
			int lineNumber, int charStart, int charEnd)
	{
		attributes.put(IBreakpoint.ID, modelIdentifier);
		attributes.put(IBreakpoint.ENABLED, Boolean.valueOf(enabled));
		attributes.put(IMarker.LINE_NUMBER, new Integer(lineNumber));
		attributes.put(IMarker.CHAR_START, new Integer(charStart));
		attributes.put(IMarker.CHAR_END, new Integer(charEnd));
	}

	/**
	 * Adds type name and hit count attributes to the given map. If <code>hitCount > 0</code>, adds the
	 * <code>HIT_COUNT</code> attribute to the given breakpoint, and resets the <code>EXPIRED</code> attribute to false
	 * (since, if the hit count is changed, the breakpoint should no longer be expired).
	 */
	public void addTypeNameAndHitCount(Map<String, Object> attributes, String typeName, int hitCount)
	{
		attributes.put(TYPE_NAME, typeName);
		if (hitCount > 0)
		{
			attributes.put(HIT_COUNT, new Integer(hitCount));
			attributes.put(EXPIRED, Boolean.FALSE);
		}
	}

	public String getFileName() throws CoreException
	{
		IResource resource = ensureMarker().getResource();
		if (resource.equals(ResourcesPlugin.getWorkspace().getRoot()))
		{
			return ensureMarker().getAttribute(EXTERNAL_FILENAME, "");
		}
		return resource.getName();
	}

	public int getLineNumber() throws CoreException
	{
		return ensureMarker().getAttribute(IMarker.LINE_NUMBER, -1);
	}

	public int getCharStart() throws CoreException
	{
		return ensureMarker().getAttribute(IMarker.CHAR_START, -1);
	}

	public int getCharEnd() throws CoreException
	{
		return ensureMarker().getAttribute(IMarker.CHAR_END, -1);
	}

	/**
	 * Returns the type of marker associated with this type of breakpoints
	 */
	public static String getMarkerType()
	{
		return RUBY_BREAKPOINT_MARKER;
	}

	public String getModelIdentifier()
	{
		return RdtDebugModel.getModelIdentifier();
	}

	public int getIndex()
	{
		return index;
	}

	public void setIndex(int index)
	{
		this.index = index;
	}

	public String getCondition() throws CoreException
	{
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isConditionEnabled() throws CoreException
	{
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isConditionSuspendOnTrue() throws CoreException
	{
		// TODO Auto-generated method stub
		return false;
	}

	public void setCondition(String condition) throws CoreException
	{
		// TODO Auto-generated method stub

	}

	public void setConditionEnabled(boolean enabled) throws CoreException
	{
		// TODO Auto-generated method stub

	}

	public void setConditionSuspendOnTrue(boolean suspendOnTrue) throws CoreException
	{
		// TODO Auto-generated method stub

	}

	public boolean supportsCondition()
	{
		// TODO Auto-generated method stub
		return false;
	}

}
