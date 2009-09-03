package org.rubypeople.rdt.internal.debug.ui.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTargetExtension;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.jruby.ast.RootNode;
import org.jruby.parser.RubyParserResult;
import org.rubypeople.rdt.core.IMember;
import org.rubypeople.rdt.core.IMethod;
import org.rubypeople.rdt.core.IRubyElement;
import org.rubypeople.rdt.core.IRubyScript;
import org.rubypeople.rdt.core.ISourceRange;
import org.rubypeople.rdt.core.IType;
import org.rubypeople.rdt.core.RubyModelException;
import org.rubypeople.rdt.debug.core.IRubyBreakpoint;
import org.rubypeople.rdt.debug.core.IRubyLineBreakpoint;
import org.rubypeople.rdt.debug.core.IRubyMethodBreakpoint;
import org.rubypeople.rdt.debug.core.RdtDebugModel;
import org.rubypeople.rdt.internal.core.parser.RubyParser;
import org.rubypeople.rdt.internal.debug.ui.BreakpointUtils;
import org.rubypeople.rdt.internal.debug.ui.DebugWorkingCopyManager;
import org.rubypeople.rdt.internal.debug.ui.RdtDebugUiPlugin;
import org.rubypeople.rdt.internal.ui.rubyeditor.IRubyScriptEditorInput;
import org.rubypeople.rdt.ui.IWorkingCopyManager;
import org.rubypeople.rdt.ui.RubyUI;

public class ToggleBreakpointAdapter implements IToggleBreakpointsTargetExtension
{

	public boolean canToggleBreakpoints(IWorkbenchPart part, ISelection selection)
	{
		if (isRemote(part, selection))
		{
			return false;
		}
		return canToggleLineBreakpoints(part, selection);
	}

	public void toggleBreakpoints(IWorkbenchPart part, ISelection selection) throws CoreException
	{
		ISelection sel = translateToMembers(part, selection);
		if (sel instanceof IStructuredSelection)
		{
			IMember member = (IMember) ((IStructuredSelection) sel).getFirstElement();
			int mtype = member.getElementType();
			if (mtype == IRubyElement.FIELD || mtype == IRubyElement.METHOD)
			{
				// remove line breakpoint if present first
				if (selection instanceof ITextSelection)
				{
					ITextSelection ts = (ITextSelection) selection;
					IType declaringType = member.getDeclaringType();
					IResource resource = BreakpointUtils.getBreakpointResource(declaringType);
					IRubyLineBreakpoint breakpoint = RdtDebugModel.lineBreakpointExists(resource, null, ts
							.getStartLine());
					if (breakpoint != null)
					{
						breakpoint.delete();
						return;
					}
					RootNode unit = parseRubyScript(getTextEditor(part));
					ValidBreakpointLocationLocator loc = new ValidBreakpointLocationLocator(unit, ts.getStartLine(),
							true);
					unit.accept(loc);
					if (loc.getLocationType() == ValidBreakpointLocationLocator.LOCATION_METHOD)
					{
						toggleMethodBreakpoints(part, sel);
					}
					else if (loc.getLocationType() == ValidBreakpointLocationLocator.LOCATION_FIELD)
					{
						toggleWatchpoints(part, ts);
					}
					else if (loc.getLocationType() == ValidBreakpointLocationLocator.LOCATION_LINE)
					{
						toggleLineBreakpoints(part, ts);
					}
					else
					{
						// fall back to old behavior, always create a line breakpoint
						toggleLineBreakpoints(part, selection, true);
					}
				}
			}
			// else if(member.getElementType() == IRubyElement.TYPE) {
			// toggleClassBreakpoints(part, sel);
			// }
			else
			{
				// fall back to old behavior, always create a line breakpoint
				toggleLineBreakpoints(part, selection, true);
			}
		}
		else
		{
			toggleLineBreakpoints(part, selection, true);
		}
	}

	private RootNode parseRubyScript(ITextEditor textEditor) throws RubyModelException
	{
		IRubyScript script = getTypeRoot(textEditor.getEditorInput());
		RubyParserResult result = new RubyParser().parse(script.getElementName(), script.getSource());
		return (RootNode) result.getAST();
	}

	public boolean canToggleLineBreakpoints(IWorkbenchPart part, ISelection selection)
	{
		if (isRemote(part, selection))
		{
			return false;
		}
		return selection instanceof ITextSelection;
	}

	public boolean canToggleMethodBreakpoints(IWorkbenchPart part, ISelection selection)
	{
		if (isRemote(part, selection))
		{
			return false;
		}
		if (selection instanceof IStructuredSelection)
		{
			IStructuredSelection ss = (IStructuredSelection) selection;
			return getMethods(ss).length > 0;
		}
		return (selection instanceof ITextSelection) && isMethod((ITextSelection) selection, part);
	}

	public boolean canToggleWatchpoints(IWorkbenchPart part, ISelection selection)
	{
		// TODO Auto-generated method stub
		return false;
	}

	public void toggleLineBreakpoints(IWorkbenchPart part, ISelection selection) throws CoreException
	{
		toggleLineBreakpoints(part, selection, false);
	}

	/**
	 * Toggles a line breakpoint.
	 * 
	 * @param part
	 *            the currently active workbench part
	 * @param selection
	 *            the current selection
	 * @param bestMatch
	 *            if we should make a best match or not
	 */
	public void toggleLineBreakpoints(final IWorkbenchPart part, final ISelection selection, final boolean bestMatch)
	{
		Job job = new Job("Toggle Line Breakpoint") { //$NON-NLS-1$
			protected IStatus run(IProgressMonitor monitor)
			{
				ITextEditor editor = getTextEditor(part);
				if (editor != null && selection instanceof ITextSelection)
				{
					if (monitor.isCanceled())
					{
						return Status.CANCEL_STATUS;
					}
					try
					{
						ISelection sel = selection;
						if (!(selection instanceof IStructuredSelection))
						{
							sel = translateToMembers(part, selection);
						}
						String tname = null;
						IRubyElement element = null;
						if (sel instanceof IStructuredSelection)
						{
							IMember member = (IMember) ((IStructuredSelection) sel).getFirstElement();
							IType type = null;
							if (member.getElementType() == IRubyElement.TYPE)
							{
								type = (IType) member;
							}
							else
							{
								type = member.getDeclaringType();
							}
							tname = createQualifiedTypeName(type);
							element = type;
						}
						// free form code not inside some method or class.
						if (tname == null)
							tname = "Object"; // toplevel
						if (element == null)
							element = getTypeRoot(editor.getEditorInput());
						IResource resource = BreakpointUtils.getBreakpointResource(element);
						int lnumber = ((ITextSelection) selection).getStartLine();
						IRubyLineBreakpoint existingBreakpoint = RdtDebugModel.lineBreakpointExists(resource, tname,
								lnumber + 1);
						if (existingBreakpoint != null)
						{
							DebugPlugin.getDefault().getBreakpointManager().removeBreakpoint(existingBreakpoint, true);
							return Status.OK_STATUS;
						}

						Map attributes = new HashMap(10);
						IDocumentProvider documentProvider = editor.getDocumentProvider();
						if (documentProvider == null)
						{
							return Status.CANCEL_STATUS;
						}
						IDocument document = documentProvider.getDocument(editor.getEditorInput());
						try
						{
							IRegion line = document.getLineInformation(lnumber - 1);
							int start = line.getOffset();
							int end = start + line.getLength() - 1;
							BreakpointUtils.addRubyBreakpointAttributesWithMemberDetails(attributes, element, start,
									end);
						}
						catch (BadLocationException ble)
						{
							RdtDebugUiPlugin.log(ble);
						}
						RdtDebugModel.createLineBreakpoint(resource, getFileName(editor.getEditorInput()), tname,
								lnumber + 1, true, attributes);
					}
					catch (CoreException x)
					{
						System.out.println(x.getMessage());
					}

				}
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.schedule();
	}

	private String getFileName(IEditorInput editorInput)
	{
		if (editorInput instanceof IRubyScriptEditorInput)
			return ((IRubyScriptEditorInput) editorInput).getRubyScript().getPath().makeAbsolute().toOSString();
		return null;
	}

	public void toggleMethodBreakpoints(final IWorkbenchPart part, final ISelection finalSelection)
			throws CoreException
	{
		Job job = new Job("Toggle Method Breakpoints") { //$NON-NLS-1$
			protected IStatus run(IProgressMonitor monitor)
			{
				if (monitor.isCanceled())
				{
					return Status.CANCEL_STATUS;
				}
				try
				{

					ISelection selection = finalSelection;
					if (!(selection instanceof IStructuredSelection))
					{
						selection = translateToMembers(part, selection);
					}
					if (selection instanceof IStructuredSelection)
					{
						IMethod[] members = getMethods((IStructuredSelection) selection);
						if (members.length == 0)
						{
							// report(ActionMessages.ToggleBreakpointAdapter_9, part);
							return Status.OK_STATUS;
						}
						IRubyBreakpoint breakpoint = null;
						ISourceRange range = null;
						Map attributes = null;
						IType type = null;
						String mname = null;
						for (int i = 0, length = members.length; i < length; i++)
						{
							breakpoint = getMethodBreakpoint(members[i]);
							if (breakpoint == null)
							{
								int start = -1;
								int end = -1;
								range = members[i].getNameRange();
								if (range != null)
								{
									start = range.getOffset();
									end = start + range.getLength();
								}
								attributes = new HashMap(10);
								BreakpointUtils.addRubyBreakpointAttributes(attributes, members[i]);
								type = members[i].getDeclaringType();

								mname = members[i].getElementName();
								if (members[i].isConstructor())
								{
									mname = "<init>"; //$NON-NLS-1$
								}
								RdtDebugModel.createMethodBreakpoint(BreakpointUtils.getBreakpointResource(members[i]),
										createQualifiedTypeName(type), mname, true, false, -1, start, end, 0, true,
										attributes);
							}
							else
							{
								DebugPlugin.getDefault().getBreakpointManager().removeBreakpoint(breakpoint, true);
							}
						}
					}
					else
					{
						// report(ActionMessages.ToggleBreakpointAdapter_4, part);
						return Status.OK_STATUS;
					}
				}
				catch (CoreException e)
				{
					return e.getStatus();
				}
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.schedule();
	}

	protected String createQualifiedTypeName(IType type)
	{
		if (type == null)
			return null;
		return type.getFullyQualifiedName();
	}

	/**
	 * Returns the <code>IRubyBreakpoint</code> from the specified <code>IMember</code>
	 * 
	 * @param element
	 *            the element to get the breakpoint from
	 * @return the current breakpoint from the element or <code>null</code>
	 */
	protected IRubyBreakpoint getMethodBreakpoint(IMember element)
	{
		IBreakpointManager breakpointManager = DebugPlugin.getDefault().getBreakpointManager();
		IBreakpoint[] breakpoints = breakpointManager.getBreakpoints(RdtDebugModel.getModelIdentifier());
		if (element instanceof IMethod)
		{
			IMethod method = (IMethod) element;
			for (int i = 0; i < breakpoints.length; i++)
			{
				IBreakpoint breakpoint = breakpoints[i];
				if (breakpoint instanceof IRubyMethodBreakpoint)
				{
					IRubyMethodBreakpoint methodBreakpoint = (IRubyMethodBreakpoint) breakpoint;
					IMember container = null;
					try
					{
						container = BreakpointUtils.getMember(methodBreakpoint);
					}
					catch (CoreException e)
					{
						RdtDebugUiPlugin.log(e);
						return null;
					}
					if (container == null)
					{
						try
						{
							if (method.getDeclaringType().getFullyQualifiedName()
									.equals(methodBreakpoint.getTypeName())
									&& method.getElementName().equals(methodBreakpoint.getMethodName()))
							{
								return methodBreakpoint;
							}
						}
						catch (CoreException e)
						{
							RdtDebugUiPlugin.log(e);
						}
					}
					else
					{
						if (container instanceof IMethod)
						{
							if (method.getDeclaringType().getFullyQualifiedName().equals(
									container.getDeclaringType().getFullyQualifiedName()))
							{
								if (method.isSimilar((IMethod) container))
								{
									return methodBreakpoint;
								}
							}
						}
					}
				}
			}
		}
		return null;
	}

	public void toggleWatchpoints(IWorkbenchPart part, ISelection selection) throws CoreException
	{
		// TODO Auto-generated method stub

	}

	/**
	 * Returns whether the given part/selection is remote (viewing a repository)
	 * 
	 * @param part
	 * @param selection
	 * @return
	 */
	protected boolean isRemote(IWorkbenchPart part, ISelection selection)
	{
		if (selection instanceof IStructuredSelection)
		{
			IStructuredSelection ss = (IStructuredSelection) selection;
			Object element = ss.getFirstElement();
			if (element instanceof IMember)
			{
				IMember member = (IMember) element;
				return !member.getRubyProject().getProject().exists();
			}
		}
		ITextEditor editor = getTextEditor(part);
		if (editor != null)
		{
			IEditorInput input = editor.getEditorInput();
			Object adapter = Platform.getAdapterManager().getAdapter(input,
					"org.eclipse.team.core.history.IFileRevision"); //$NON-NLS-1$
			return adapter != null;
		}
		return false;
	}

	/**
	 * Returns the text editor associated with the given part or <code>null</code> if none. In case of a multi-page
	 * editor, this method should be used to retrieve the correct editor to perform the breakpoint operation on.
	 * 
	 * @param part
	 *            workbench part
	 * @return text editor part or <code>null</code>
	 */
	protected ITextEditor getTextEditor(IWorkbenchPart part)
	{
		if (part instanceof ITextEditor)
		{
			return (ITextEditor) part;
		}
		return (ITextEditor) part.getAdapter(ITextEditor.class);
	}

	/**
	 * Returns the methods from the selection, or an empty array
	 * 
	 * @param selection
	 *            the selection to get the methods from
	 * @return an array of the methods from the selection or an empty array
	 */
	protected IMethod[] getMethods(IStructuredSelection selection)
	{
		if (selection.isEmpty())
		{
			return new IMethod[0];
		}
		List<IMethod> methods = new ArrayList<IMethod>(selection.size());
		Iterator iterator = selection.iterator();
		while (iterator.hasNext())
		{
			Object thing = iterator.next();
			if (thing instanceof IMethod)
			{
				methods.add((IMethod) thing);
			}
		}
		return (IMethod[]) methods.toArray(new IMethod[methods.size()]);
	}

	/**
	 * Returns if the text selection is a valid method or not
	 * 
	 * @param selection
	 *            the text selection
	 * @param part
	 *            the associated workbench part
	 * @return true if the selection is a valid method, false otherwise
	 */
	private boolean isMethod(ITextSelection selection, IWorkbenchPart part)
	{
		ITextEditor editor = getTextEditor(part);
		if (editor != null)
		{
			IRubyElement element = getRubyElement(editor.getEditorInput());
			if (element != null)
			{
				try
				{
					if (element instanceof IRubyScript)
					{
						element = ((IRubyScript) element).getElementAt(selection.getOffset());
					}
					return element != null && element.getElementType() == IRubyElement.METHOD;
				}
				catch (RubyModelException e)
				{
					return false;
				}
			}
		}
		return false;
	}

	/**
	 * gets the <code>IRubyElement</code> from the editor input
	 * 
	 * @param input
	 *            the current editor input
	 * @return the corresponding <code>IRubyElement</code>
	 * @since 1.3
	 */
	private IRubyElement getRubyElement(IEditorInput input)
	{
		IRubyElement re = RubyUI.getEditorInputRubyElement(input);
		if (re != null)
		{
			return re;
		}
		// try to get from the working copy manager
		return DebugWorkingCopyManager.getWorkingCopy(input, false);
	}

	/**
	 * Returns a selection of the member in the given text selection, or the original selection if none.
	 * 
	 * @param part
	 * @param selection
	 * @return a structured selection of the member in the given text selection, or the original selection if none
	 * @exception CoreException
	 *                if an exception occurs
	 */
	protected ISelection translateToMembers(IWorkbenchPart part, ISelection selection) throws CoreException
	{
		ITextEditor textEditor = getTextEditor(part);
		if (textEditor != null && selection instanceof ITextSelection)
		{
			ITextSelection textSelection = (ITextSelection) selection;
			IEditorInput editorInput = textEditor.getEditorInput();
			IDocumentProvider documentProvider = textEditor.getDocumentProvider();
			if (documentProvider == null)
			{
				throw new CoreException(Status.CANCEL_STATUS);
			}
			IDocument document = documentProvider.getDocument(editorInput);
			int offset = textSelection.getOffset();
			if (document != null)
			{
				try
				{
					IRegion region = document.getLineInformationOfOffset(offset);
					int end = region.getOffset() + region.getLength();
					while (Character.isWhitespace(document.getChar(offset)) && offset < end)
					{
						offset++;
					}
				}
				catch (BadLocationException e)
				{
				}
			}
			IMember m = null;
			IRubyScript root = getTypeRoot(editorInput);
			if (root != null)
			{
				synchronized (root)
				{
					root.reconcile(false, null, null);
				}
				IRubyElement e = root.getElementAt(offset);
				if (e instanceof IMember)
				{
					m = (IMember) e;
				}
			}
			if (m != null)
			{
				return new StructuredSelection(m);
			}
		}
		return selection;
	}

	/**
	 * Returns the {@link IRubyScript} for the given {@link IEditorInput}
	 * 
	 * @param input
	 * @return the type root or <code>null</code> if one cannot be derived
	 * @since 1.3.0
	 */
	private IRubyScript getTypeRoot(IEditorInput input)
	{
		IWorkingCopyManager manager = RubyUI.getWorkingCopyManager();
		IRubyScript root = manager.getWorkingCopy(input);
		if (root == null)
		{
			root = DebugWorkingCopyManager.getWorkingCopy(input, false);
		}
		return root;
	}
}
