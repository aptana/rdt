package org.rubypeople.rdt.internal.debug.ui;

import java.io.File;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.IValueDetailListener;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.rubypeople.rdt.debug.core.IRubyBreakpoint;
import org.rubypeople.rdt.debug.core.IRubyLineBreakpoint;
import org.rubypeople.rdt.debug.core.IRubyMethodBreakpoint;
import org.rubypeople.rdt.debug.core.model.IEvaluationResult;
import org.rubypeople.rdt.debug.core.model.IRubyExceptionBreakpoint;
import org.rubypeople.rdt.debug.core.model.IRubyStackFrame;
import org.rubypeople.rdt.debug.core.model.IRubyThread;
import org.rubypeople.rdt.debug.core.model.IRubyValue;
import org.rubypeople.rdt.debug.core.model.IRubyVariable;
import org.rubypeople.rdt.internal.ui.rubyeditor.ExternalRubyFileEditorInput;
import org.rubypeople.rdt.ui.RubyUI;
import org.rubypeople.rdt.ui.viewsupport.ImageDescriptorRegistry;

public class DebugModelPresentation extends LabelProvider implements IDebugModelPresentation
{
	private static Map<ImageDescriptor, Image> imageCache = new Hashtable<ImageDescriptor, Image>();
	private static ImageDescriptorRegistry fgDebugImageRegistry;
	private Boolean isShowTypes;

	public String getText(Object item)
	{
		if (item instanceof IRubyMethodBreakpoint)
		{
			IRubyMethodBreakpoint breakpoint = (IRubyMethodBreakpoint) item;
			try
			{
				return breakpoint.getTypeName() + "." + breakpoint.getMethodName();
			}
			catch (CoreException e)
			{
				DebugUIPlugin.log(e);
				return "--";
			}
		}
		if (item instanceof IRubyLineBreakpoint)
		{
			IRubyLineBreakpoint breakpoint = (IRubyLineBreakpoint) item;
			try
			{
				return breakpoint.getFileName() + ":" + breakpoint.getLineNumber();
			}
			catch (CoreException e)
			{
				DebugUIPlugin.log(e);
				return "--";
			}
		}
		if (item instanceof IRubyExceptionBreakpoint)
		{
			IRubyExceptionBreakpoint exceptionBreakpoint = (IRubyExceptionBreakpoint) item;
			try
			{
				String exceptionType = exceptionBreakpoint.getTypeName();
				if (exceptionType == null || exceptionType.length() == 0)
				{
					return "No Catchpoint defined";
				}
				else
				{
					return "Suspend on exception: " + exceptionType;
				}
			}
			catch (CoreException e)
			{
				RdtDebugUiPlugin.log(e);
				return "Unable to grab exception type name";
			}
		}
		if (item instanceof IRubyVariable)
		{
			IRubyVariable variable = (IRubyVariable) item;
			try
			{
				if (isShowTypes == Boolean.TRUE)
				{
					return ((IRubyValue) variable.getValue()).getReferenceTypeName() + " " + variable.toString();
				}
			}
			catch (DebugException e)
			{
				RdtDebugUiPlugin.log(e);
			}
			return variable.toString();
		}
		return DebugUIPlugin.getDefaultLabelProvider().getText(item);
	}

	protected IBreakpoint getBreakpoint(IMarker marker)
	{
		return DebugPlugin.getDefault().getBreakpointManager().getBreakpoint(marker);
	}

	public void computeDetail(IValue value, IValueDetailListener listener)
	{
		String string = "";
		try
		{
			IRubyStackFrame frame = RdtDebugUiPlugin.getEvaluationContextManager().getEvaluationContext(
					(IWorkbenchWindow) null);
			IRubyValue rubyValue = (IRubyValue) value;
			IRubyVariable var = rubyValue.getOwner();
			String snippet = var.getName() + ".inspect";

			IEvaluationResult result = frame.evaluate(snippet);
			try
			{
				if (result != null && result.getValue() != null)
					string = result.getValue().getValueString();
				if (string != null && string.startsWith("\"") && string.endsWith("\""))
				{
					string = string.substring(1, string.length() - 1);
				}
			}
			catch (DebugException e)
			{
				// ignore
			}
		}
		catch (Throwable t)
		{
			// ignore
		}
		listener.detailComputed(value, string);
	}

	public void setAttribute(String attribute, Object value)
	{
		if (attribute.equals(IDebugModelPresentation.DISPLAY_VARIABLE_TYPE_NAMES))
		{
			this.isShowTypes = (Boolean) value;
		}
	}

	public String getEditorId(IEditorInput input, Object element)
	{
		if (input instanceof ExternalRubyFileEditorInput)
		{
			return RubyUI.ID_EXTERNAL_EDITOR;
		}
		else if (input instanceof IFileEditorInput)
		{
			IFileEditorInput fileInput = (IFileEditorInput) input;
			return IDE.getDefaultEditor(fileInput.getFile()).getId();
		}
		return null;
	}

	public IEditorInput getEditorInput(Object element)
	{
		if (element instanceof IRubyLineBreakpoint)
		{
			IRubyLineBreakpoint bp = (IRubyLineBreakpoint) element;
			IResource resource = bp.getMarker().getResource();
			// If resource is workspace root, that means its external. Try to resolve via filename
			if (resource.equals(ResourcesPlugin.getWorkspace().getRoot()))
			{
				try
				{
					String filename = bp.getFileName();
					if (filename == null)
						return null;
					return new ExternalRubyFileEditorInput(new File(filename));
				}
				catch (CoreException e)
				{
					RdtDebugUiPlugin.log(e);
				}
			}
			else
			{
				if (resource instanceof IFile) // resource should be a file, stick it in a FileEditorInput
					return new FileEditorInput((IFile) resource);
			}
		}
		return null;
	}

	private Image getImage(ImageDescriptor imageDescriptor)
	{
		Image image = (Image) imageCache.get(imageDescriptor);
		if (image == null)
		{
			image = imageDescriptor.createImage();
			imageCache.put(imageDescriptor, image);
		}
		return image;
	}

	public Image getImage(Object item)
	{
		try
		{
			ImageDescriptor descriptor;
			if (item instanceof IMarker)
			{
				IBreakpoint bp = getBreakpoint((IMarker) item);
				if (bp != null && bp instanceof IRubyBreakpoint)
				{
					return getBreakpointImage((IRubyBreakpoint) bp);
				}
			}
			if (item instanceof IRubyBreakpoint)
			{
				return getBreakpointImage((IRubyBreakpoint) item);
			}
			else if (item instanceof IRubyThread)
			{
				IRubyThread thread = (IRubyThread) item;
				if (thread.isSuspended())
				{
					descriptor = DebugUITools.getImageDescriptor(IDebugUIConstants.IMG_OBJS_THREAD_SUSPENDED);
				}
				else if (thread.isTerminated())
				{
					descriptor = DebugUITools.getImageDescriptor(IDebugUIConstants.IMG_OBJS_THREAD_TERMINATED);
				}
				else
				{
					descriptor = DebugUITools.getImageDescriptor(IDebugUIConstants.IMG_OBJS_THREAD_RUNNING);
				}
			}
			else
			{
				descriptor = DebugUITools.getDefaultImageDescriptor(item);
			}
			return getImage(descriptor);
		}
		catch (Exception e)
		{
			// ignore
		}
		return null;
	}

	protected Image getBreakpointImage(IRubyBreakpoint breakpoint) throws CoreException
	{
		if (breakpoint instanceof IRubyExceptionBreakpoint)
		{
			return getExceptionBreakpointImage((IRubyExceptionBreakpoint) breakpoint);
		}

		// if (breakpoint instanceof IRubyLineBreakpoint
		// && BreakpointUtils.isRunToLineBreakpoint((IRubyLineBreakpoint) breakpoint))
		// {
		// return null;
		// }
		return getRubyBreakpointImage(breakpoint);
	}

	protected Image getExceptionBreakpointImage(IRubyExceptionBreakpoint exception) throws CoreException
	{
		int flags = computeBreakpointAdornmentFlags(exception);
		RubyDebugImageDescriptor descriptor = null;
		if ((flags & RubyDebugImageDescriptor.ENABLED) == 0)
		{
			descriptor = new RubyDebugImageDescriptor(getImageDescriptor(RubyDebugImages.IMG_OBJS_EXCEPTION_DISABLED),
					flags);
			// } else if (exception.isChecked()) {
			// descriptor= new RubyDebugImageDescriptor(getImageDescriptor(RubyDebugImages.IMG_OBJS_EXCEPTION), flags);
		}
		else
		{
			descriptor = new RubyDebugImageDescriptor(getImageDescriptor(RubyDebugImages.IMG_OBJS_ERROR), flags);
		}
		return getDebugImageRegistry().get(descriptor);
	}

	protected Image getRubyBreakpointImage(IRubyBreakpoint breakpoint) throws CoreException
	{
		if (breakpoint instanceof IRubyMethodBreakpoint)
		{
			IRubyMethodBreakpoint mBreakpoint = (IRubyMethodBreakpoint) breakpoint;
			return getRubyMethodBreakpointImage(mBreakpoint);
			// } else if (breakpoint instanceof IJavaWatchpoint) {
			// IJavaWatchpoint watchpoint= (IJavaWatchpoint)breakpoint;
			// return getJavaWatchpointImage(watchpoint);
			// } else if (breakpoint instanceof IJavaMethodEntryBreakpoint) {
			// IJavaMethodEntryBreakpoint meBreakpoint = (IJavaMethodEntryBreakpoint)breakpoint;
			// return getJavaMethodEntryBreakpointImage(meBreakpoint);
		}
		else
		{
			int flags = computeBreakpointAdornmentFlags(breakpoint);
			RubyDebugImageDescriptor descriptor = null;
			if (breakpoint.isEnabled())
			{
				descriptor = new RubyDebugImageDescriptor(DebugUITools
						.getImageDescriptor(IDebugUIConstants.IMG_OBJS_BREAKPOINT), flags);
			}
			else
			{
				descriptor = new RubyDebugImageDescriptor(DebugUITools
						.getImageDescriptor(IDebugUIConstants.IMG_OBJS_BREAKPOINT_DISABLED), flags);
			}
			return getDebugImageRegistry().get(descriptor);
		}
	}

	protected Image getRubyMethodBreakpointImage(IRubyMethodBreakpoint mBreakpoint) throws CoreException
	{
		int flags = computeBreakpointAdornmentFlags(mBreakpoint);
		RubyDebugImageDescriptor descriptor = null;
		if (mBreakpoint.isEnabled())
		{
			descriptor = new RubyDebugImageDescriptor(DebugUITools
					.getImageDescriptor(IDebugUIConstants.IMG_OBJS_BREAKPOINT), flags);
		}
		else
		{
			descriptor = new RubyDebugImageDescriptor(DebugUITools
					.getImageDescriptor(IDebugUIConstants.IMG_OBJS_BREAKPOINT_DISABLED), flags);
		}

		return getDebugImageRegistry().get(descriptor);
	}

	protected static ImageDescriptorRegistry getDebugImageRegistry()
	{
		if (fgDebugImageRegistry == null)
		{
			fgDebugImageRegistry = RdtDebugUiPlugin.getImageDescriptorRegistry();
		}
		return fgDebugImageRegistry;
	}

	/**
	 * Returns the adornment flags for the given breakpoint. These flags are used to render appropriate overlay icons
	 * for the breakpoint.
	 */
	private int computeBreakpointAdornmentFlags(IRubyBreakpoint breakpoint)
	{
		int flags = 0;
		try
		{
			if (breakpoint.isEnabled())
			{
				flags |= RubyDebugImageDescriptor.ENABLED;
			}
			if (breakpoint.isInstalled())
			{
				flags |= RubyDebugImageDescriptor.INSTALLED;
			}
			if (breakpoint instanceof IRubyLineBreakpoint)
			{
				if (((IRubyLineBreakpoint) breakpoint).isConditionEnabled())
				{
					flags |= RubyDebugImageDescriptor.CONDITIONAL;
				}
				if (breakpoint instanceof IRubyMethodBreakpoint)
				{
					IRubyMethodBreakpoint mBreakpoint = (IRubyMethodBreakpoint) breakpoint;
					// if (mBreakpoint.isEntry()) {
					flags |= RubyDebugImageDescriptor.ENTRY;
					// }
					// if (mBreakpoint.isExit()) {
					// flags |= JDIImageDescriptor.EXIT;
					// }
				}
				// if (breakpoint instanceof IRubyMethodEntryBreakpoint) {
				// flags |= JDIImageDescriptor.ENTRY;
				// }
			}
			else if (breakpoint instanceof IRubyExceptionBreakpoint)
			{
				IRubyExceptionBreakpoint eBreakpoint = (IRubyExceptionBreakpoint) breakpoint;
				// if (eBreakpoint.isCaught()) {
				// flags |= JDIImageDescriptor.CAUGHT;
				// }
				// if (eBreakpoint.isUncaught()) {
				flags |= RubyDebugImageDescriptor.UNCAUGHT;
				// }
				// if (eBreakpoint.getExclusionFilters().length > 0 || eBreakpoint.getInclusionFilters().length > 0) {
				// flags |= JDIImageDescriptor.SCOPED;
				// }
			}
		}
		catch (CoreException e)
		{
		}
		return flags;
	}

	private ImageDescriptor getImageDescriptor(String key)
	{
		return RubyDebugImages.getImageDescriptor(key);
	}

}