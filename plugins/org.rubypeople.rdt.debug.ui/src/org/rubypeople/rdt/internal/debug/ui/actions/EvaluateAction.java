/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.rubypeople.rdt.internal.debug.ui.actions;

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.Iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.texteditor.ITextEditor;
import org.rubypeople.rdt.debug.core.RdtDebugCorePlugin;
import org.rubypeople.rdt.debug.core.model.IEvaluationResult;
import org.rubypeople.rdt.debug.core.model.IRubyStackFrame;
import org.rubypeople.rdt.debug.core.model.IRubyValue;
import org.rubypeople.rdt.debug.core.model.IRubyVariable;
import org.rubypeople.rdt.debug.ui.RdtDebugUiConstants;
import org.rubypeople.rdt.internal.debug.ui.RdtDebugUiPlugin;
import org.rubypeople.rdt.internal.debug.ui.display.IDataDisplay;
import org.rubypeople.rdt.internal.debug.ui.display.RubyInspectExpression;
import org.rubypeople.rdt.internal.ui.text.RubyWordFinder;

/**
 * Action to do simple code evaluation. The evaluation is done in the UI thread and the expression and result are
 * displayed using the IDataDisplay.
 */
public abstract class EvaluateAction implements IWorkbenchWindowActionDelegate, IObjectActionDelegate,
		IEditorActionDelegate, IPartListener, IViewActionDelegate
{

	private IAction fAction;
	private IWorkbenchPart fTargetPart;
	private IWorkbenchWindow fWindow;
	private Object fSelection;
	private IRegion fRegion;

	/**
	 * Is the action waiting for an evaluation.
	 */
	private boolean fEvaluating;

	/**
	 * The new target part to use with the evaluation completes.
	 */
	private IWorkbenchPart fNewTargetPart = null;

	/**
	 * Used to resolve editor input for selected stack frame
	 */
	private IDebugModelPresentation fPresentation;

	public EvaluateAction()
	{
		super();
	}

	/**
	 * Returns the 'object' context for this evaluation, or <code>null</code> if none. If the evaluation is being
	 * performed in the context of the variables view/inspector. Then perform the evaluation in the context of the
	 * selected value.
	 * 
	 * @return Ruby object or <code>null</code>
	 */
	protected IRubyValue getObjectContext()
	{
		IWorkbenchPage page = RdtDebugUiPlugin.getActivePage();
		if (page == null)
			return null;
		IWorkbenchPart activePart = page.getActivePart();
		if (activePart == null)
			return null;
		IDebugView a = (IDebugView) activePart.getAdapter(IDebugView.class);
		if (a == null || a.getViewer() == null)
			return null;
		ISelection s = a.getViewer().getSelection();
		if (!(s instanceof IStructuredSelection))
			return null;
		IStructuredSelection structuredSelection = (IStructuredSelection) s;
		if (structuredSelection.size() != 1)
			return null;
		Object selection = structuredSelection.getFirstElement();
		if (selection instanceof IRubyVariable)
		{
			IRubyVariable var = (IRubyVariable) selection;
			// if 'this' is selected, use stack frame context
			try
			{
				if (!var.getName().equals("this")) { //$NON-NLS-1$
					IValue value = var.getValue();
					if (value instanceof IRubyValue)
					{
						return (IRubyValue) value;
					}
				}
			}
			catch (DebugException e)
			{
				RdtDebugUiPlugin.log(e);
			}
		}
		else if (selection instanceof RubyInspectExpression)
		{
			IValue value = ((RubyInspectExpression) selection).getValue();
			if (value instanceof IRubyValue)
			{
				return (IRubyValue) value;
			}
		}
		return null;
	}

	/**
	 * Finds the currently selected stack frame in the UI. Stack frames from a scrapbook launch are ignored.
	 */
	protected IRubyStackFrame getStackFrameContext()
	{
		try
		{
			IWorkbenchPart part = getTargetPart();
			if (part == null)
			{
				return RdtDebugUiPlugin.getEvaluationContextManager().getEvaluationContext(getWindow());
			}
			return RdtDebugUiPlugin.getEvaluationContextManager().getEvaluationContext(part);
		}
		catch (Throwable e)
		{
			RdtDebugUiPlugin.log(e);
			return null;
		}
	}

	/**
	 * @see IEvaluationListener#evaluationComplete(IEvaluationResult)
	 */
	public void evaluationComplete(final IEvaluationResult result)
	{
		// if plug-in has shutdown, ignore - see bug# 8693
		if (RdtDebugUiPlugin.getDefault() == null)
		{
			return;
		}

		final IValue value = result.getValue();
		if (result.hasErrors() || value != null)
		{
			final Display display = RdtDebugUiPlugin.getStandardDisplay();
			if (display.isDisposed())
			{
				return;
			}
			displayResult(result);
		}
	}

	protected void evaluationCleanup()
	{
		setEvaluating(false);
		setTargetPart(fNewTargetPart);
	}

	/**
	 * Display the given evaluation result.
	 */
	abstract protected void displayResult(IEvaluationResult result);

	protected void run()
	{
		// eval in context of object or stack frame
		final IRubyValue object = getObjectContext();
		final IRubyStackFrame stackFrame = getStackFrameContext();
		if (stackFrame == null)
		{
			reportError(ActionMessages.Evaluate_error_message_stack_frame_context);
			return;
		}

		// check for nested evaluation
		IThread thread = (IThread) stackFrame.getThread();
		// if (thread.isPerformingEvaluation()) {
		// reportError(ActionMessages.EvaluateAction_Cannot_perform_nested_evaluations__1);
		// return;
		// }

		setNewTargetPart(getTargetPart());

		IRunnableWithProgress runnable = new IRunnableWithProgress()
		{
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
			{
				if (stackFrame.isSuspended())
				{
					Object selection = getSelectedObject();
					if (!(selection instanceof String))
					{
						return;
					}
					String expression = (String) selection;
					setEvaluating(true);
					IEvaluationResult result = stackFrame.evaluate(expression);
					evaluationComplete(result);
					return;
				}
				// thread not suspended
				throw new InvocationTargetException(null,
						ActionMessages.EvaluateAction_Thread_not_suspended___unable_to_perform_evaluation__1);
			}
		};

		IWorkbench workbench = RdtDebugUiPlugin.getDefault().getWorkbench();
		try
		{
			workbench.getProgressService().busyCursorWhile(runnable);
		}
		catch (InvocationTargetException e)
		{
			evaluationCleanup();
			String message = e.getMessage();
			if (message == null)
			{
				message = e.getClass().getName();
				if (e.getCause() != null)
				{
					message = e.getCause().getClass().getName();
					if (e.getCause().getMessage() != null)
					{
						message = e.getCause().getMessage();
					}
				}
			}
			reportError(message);
		}
		catch (InterruptedException e)
		{
		}
	}

	/**
	 * Updates the enabled state of the action that this is a delegate for.
	 */
	protected void update()
	{
		IAction action = getAction();
		if (action != null)
		{
			resolveSelectedObject();
		}
	}

	/**
	 * Resolves the selected object in the target part, or <code>null</code> if there is no selection.
	 */
	protected void resolveSelectedObject()
	{
		Object selectedObject = null;
		fRegion = null;
		ISelection selection = getTargetSelection();
		if (selection instanceof ITextSelection)
		{
			ITextSelection ts = (ITextSelection) selection;
			String text = ts.getText();
			if (textHasContent(text))
			{
				selectedObject = text;
				fRegion = new Region(ts.getOffset(), ts.getLength());
			}
			else if (getTargetPart() instanceof IEditorPart)
			{
				IEditorPart editor = (IEditorPart) getTargetPart();
				if (editor instanceof ITextEditor)
				{
					selectedObject = resolveSelectedObjectUsingToken(selectedObject, ts, editor);
				}
			}
		}
		else if (selection instanceof IStructuredSelection)
		{
			if (!selection.isEmpty())
			{
				if (getTargetPart().getSite().getId().equals(IDebugUIConstants.ID_DEBUG_VIEW))
				{
					// work on the editor selection
					IEditorPart editor = getTargetPart().getSite().getPage().getActiveEditor();
					setTargetPart(editor);
					selection = getTargetSelection();
					if (selection instanceof ITextSelection)
					{
						ITextSelection ts = (ITextSelection) selection;
						String text = ts.getText();
						if (textHasContent(text))
						{
							selectedObject = text;
						}
						else if (editor instanceof ITextEditor)
						{
							selectedObject = resolveSelectedObjectUsingToken(selectedObject, ts, editor);
						}
					}
				}
				else
				{
					IStructuredSelection ss = (IStructuredSelection) selection;
					Iterator elements = ss.iterator();
					while (elements.hasNext())
					{
						if (!(elements.next() instanceof IRubyVariable))
						{
							setSelectedObject(null);
							return;
						}
					}
					selectedObject = ss;
				}
			}
		}
		setSelectedObject(selectedObject);
	}

	private Object resolveSelectedObjectUsingToken(Object selectedObject, ITextSelection ts, IEditorPart editor)
	{
		ITextEditor textEditor = (ITextEditor) editor;
		IDocument doc = textEditor.getDocumentProvider().getDocument(editor.getEditorInput());
		fRegion = RubyWordFinder.findWord(doc, ts.getOffset());
		if (fRegion != null)
		{
			try
			{
				selectedObject = doc.get(fRegion.getOffset(), fRegion.getLength());
			}
			catch (BadLocationException e)
			{
			}
		}
		return selectedObject;
	}

	protected ISelection getTargetSelection()
	{
		IWorkbenchPart part = getTargetPart();
		if (part != null)
		{
			ISelectionProvider provider = part.getSite().getSelectionProvider();
			if (provider != null)
			{
				return provider.getSelection();
			}
		}
		return null;
	}

	/**
	 * Resolve an editor input from the source element of the stack frame argument, and return whether it's equal to the
	 * editor input for the editor that owns this action.
	 */
	protected boolean compareToEditorInput(IStackFrame stackFrame)
	{
		ILaunch launch = stackFrame.getLaunch();
		if (launch == null)
		{
			return false;
		}
		ISourceLocator locator = launch.getSourceLocator();
		if (locator == null)
		{
			return false;
		}
		Object sourceElement = locator.getSourceElement(stackFrame);
		if (sourceElement == null)
		{
			return false;
		}
		IEditorInput sfEditorInput = getDebugModelPresentation().getEditorInput(sourceElement);
		if (getTargetPart() instanceof IEditorPart)
		{
			return ((IEditorPart) getTargetPart()).getEditorInput().equals(sfEditorInput);
		}
		return false;
	}

	protected Shell getShell()
	{
		if (getTargetPart() != null)
		{
			return getTargetPart().getSite().getShell();
		}
		return RdtDebugUiPlugin.getActiveWorkbenchShell();
	}

	protected IDataDisplay getDataDisplay()
	{
		IDataDisplay display = getDirectDataDisplay();
		if (display != null)
		{
			return display;
		}
		IWorkbenchPage page = RdtDebugUiPlugin.getActivePage();
		if (page != null)
		{
			IWorkbenchPart activePart = page.getActivePart();
			if (activePart != null)
			{
				IViewPart view = page.findView(RdtDebugUiConstants.ID_DISPLAY_VIEW);
				if (view == null)
				{
					try
					{
						view = page.showView(RdtDebugUiConstants.ID_DISPLAY_VIEW);
					}
					catch (PartInitException e)
					{
						RdtDebugUiPlugin.errorDialog(ActionMessages.EvaluateAction_Cannot_open_Display_view, e);
					}
					finally
					{
						page.activate(activePart);
					}
				}
				if (view != null)
				{
					page.bringToTop(view);
					return (IDataDisplay) view.getAdapter(IDataDisplay.class);
				}
			}
		}

		return null;
	}

	protected IDataDisplay getDirectDataDisplay()
	{
		IWorkbenchPart part = getTargetPart();
		if (part != null)
		{
			IDataDisplay display = (IDataDisplay) part.getAdapter(IDataDisplay.class);
			if (display != null)
			{
				IWorkbenchPage page = RdtDebugUiPlugin.getActivePage();
				if (page != null)
				{
					IWorkbenchPart activePart = page.getActivePart();
					if (activePart != null)
					{
						if (activePart != part)
						{
							page.activate(part);
						}
					}
				}
				return display;
			}
		}
		IWorkbenchPage page = RdtDebugUiPlugin.getActivePage();
		if (page != null)
		{
			IWorkbenchPart activePart = page.getActivePart();
			if (activePart != null)
			{
				IDataDisplay display = (IDataDisplay) activePart.getAdapter(IDataDisplay.class);
				if (display != null)
				{
					return display;
				}
			}
		}
		return null;
	}

	protected boolean textHasContent(String text)
	{
		if (text != null)
		{
			int length = text.length();
			if (length > 0)
			{
				for (int i = 0; i < length; i++)
				{
					if (Character.isLetterOrDigit(text.charAt(i)))
					{
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Displays a failed evaluation message in the data display.
	 */
	protected void reportErrors(IEvaluationResult result)
	{
		String message = getErrorMessage(result);
		reportError(message);
	}

	protected void reportError(String message)
	{
		IDataDisplay dataDisplay = getDirectDataDisplay();
		if (dataDisplay != null)
		{
			if (message.length() != 0)
			{
				dataDisplay.displayExpressionValue(MessageFormat.format(
						ActionMessages.EvaluateAction__evaluation_failed__Reason, new String[] { format(message) }));
			}
			else
			{
				dataDisplay.displayExpressionValue(ActionMessages.EvaluateAction__evaluation_failed__1);
			}
		}
		else
		{
			Status status = new Status(IStatus.ERROR, RdtDebugUiPlugin.getUniqueIdentifier(), IStatus.ERROR, message,
					null);
			ErrorDialog.openError(getShell(), ActionMessages.Evaluate_error_title_eval_problems, null, status);
		}
	}

	private String format(String message)
	{
		StringBuffer result = new StringBuffer();
		int index = 0, pos;
		while ((pos = message.indexOf('\n', index)) != -1)
		{
			result.append("\t\t").append(message.substring(index, index = pos + 1)); //$NON-NLS-1$
		}
		if (index < message.length())
		{
			result.append("\t\t").append(message.substring(index)); //$NON-NLS-1$
		}
		return result.toString();
	}

	public static String getExceptionMessage(Throwable exception)
	{
		if (exception instanceof CoreException)
		{
			CoreException ce = (CoreException) exception;
			Throwable throwable = ce.getStatus().getException();
			if (throwable instanceof CoreException)
			{
				// Traverse nested CoreExceptions
				return getExceptionMessage(throwable);
			}
			return ce.getStatus().getMessage();
		}
		String message = MessageFormat.format(ActionMessages.Evaluate_error_message_direct_exception,
				new Object[] { exception.getClass() });
		if (exception.getMessage() != null)
		{
			message = MessageFormat.format(ActionMessages.Evaluate_error_message_exception_pattern, new Object[] {
					message, exception.getMessage() });
		}
		return message;
	}

	protected String getErrorMessage(IEvaluationResult result)
	{
		String[] errors = result.getErrorMessages();
		if (errors.length == 0)
		{
			return getExceptionMessage(result.getException());
		}
		return getErrorMessage(errors);
	}

	protected String getErrorMessage(String[] errors)
	{
		String message = ""; //$NON-NLS-1$
		for (int i = 0; i < errors.length; i++)
		{
			String msg = errors[i];
			if (i == 0)
			{
				message = msg;
			}
			else
			{
				message = MessageFormat.format(ActionMessages.Evaluate_error_problem_append_pattern, new Object[] {
						message, msg });
			}
		}
		return message;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(IAction)
	 */
	public void run(IAction action)
	{
		update();
		run();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection)
	{
		setAction(action);
	}

	/**
	 * @see IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose()
	{
		disposeDebugModelPresentation();
		IWorkbenchWindow win = getWindow();
		if (win != null)
		{
			win.getPartService().removePartListener(this);
		}
	}

	/**
	 * @see IWorkbenchWindowActionDelegate#init(IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window)
	{
		setWindow(window);
		IWorkbenchPage page = window.getActivePage();
		if (page != null)
		{
			setTargetPart(page.getActivePart());
		}
		window.getPartService().addPartListener(this);
		update();
	}

	protected IAction getAction()
	{
		return fAction;
	}

	protected void setAction(IAction action)
	{
		fAction = action;
	}

	/**
	 * Returns a debug model presentation (creating one if necessary).
	 * 
	 * @return debug model presentation
	 */
	protected IDebugModelPresentation getDebugModelPresentation()
	{
		if (fPresentation == null)
		{
			fPresentation = DebugUITools.newDebugModelPresentation(RdtDebugCorePlugin.getPluginIdentifier());
		}
		return fPresentation;
	}

	/**
	 * Disposes this action's debug model presentation, if one was created.
	 */
	protected void disposeDebugModelPresentation()
	{
		if (fPresentation != null)
		{
			fPresentation.dispose();
		}
	}

	/**
	 * @see IEditorActionDelegate#setActiveEditor(IAction, IEditorPart)
	 */
	public void setActiveEditor(IAction action, IEditorPart targetEditor)
	{
		setAction(action);
		setTargetPart(targetEditor);
	}

	/**
	 * @see IPartListener#partActivated(IWorkbenchPart)
	 */
	public void partActivated(IWorkbenchPart part)
	{
		setTargetPart(part);
	}

	/**
	 * @see IPartListener#partBroughtToTop(IWorkbenchPart)
	 */
	public void partBroughtToTop(IWorkbenchPart part)
	{
	}

	/**
	 * @see IPartListener#partClosed(IWorkbenchPart)
	 */
	public void partClosed(IWorkbenchPart part)
	{
		if (part == getTargetPart())
		{
			setTargetPart(null);
		}
		if (part == getNewTargetPart())
		{
			setNewTargetPart(null);
		}
	}

	/**
	 * @see IPartListener#partDeactivated(IWorkbenchPart)
	 */
	public void partDeactivated(IWorkbenchPart part)
	{
	}

	/**
	 * @see IPartListener#partOpened(IWorkbenchPart)
	 */
	public void partOpened(IWorkbenchPart part)
	{
	}

	/**
	 * @see IViewActionDelegate#init(IViewPart)
	 */
	public void init(IViewPart view)
	{
		setTargetPart(view);
	}

	protected IWorkbenchPart getTargetPart()
	{
		return fTargetPart;
	}

	protected void setTargetPart(IWorkbenchPart part)
	{
		if (isEvaluating())
		{
			// do not want to change the target part while evaluating
			// see bug 8334
			setNewTargetPart(part);
		}
		else
		{
			fTargetPart = part;
		}
	}

	protected IWorkbenchWindow getWindow()
	{
		return fWindow;
	}

	protected void setWindow(IWorkbenchWindow window)
	{
		fWindow = window;
	}

	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart)
	{
		setAction(action);
		setTargetPart(targetPart);
		update();
	}

	protected Object getSelectedObject()
	{
		return fSelection;
	}

	protected void setSelectedObject(Object selection)
	{
		fSelection = selection;
	}

	protected IWorkbenchPart getNewTargetPart()
	{
		return fNewTargetPart;
	}

	protected void setNewTargetPart(IWorkbenchPart newTargetPart)
	{
		fNewTargetPart = newTargetPart;
	}

	protected boolean isEvaluating()
	{
		return fEvaluating;
	}

	protected void setEvaluating(boolean evaluating)
	{
		fEvaluating = evaluating;
	}

	/**
	 * Returns the selected text region, or <code>null</code> if none.
	 * 
	 * @return
	 */
	protected IRegion getRegion()
	{
		return fRegion;
	}

	/**
	 * Computes an anchor point for a popup dialog on top of a text viewer.
	 * 
	 * @param viewer
	 * @return desired anchor point
	 */
	public static Point getPopupAnchor(ITextViewer viewer)
	{
		StyledText textWidget = viewer.getTextWidget();
		Point docRange = textWidget.getSelectionRange();
		int midOffset = docRange.x + (docRange.y / 2);
		Point point = textWidget.getLocationAtOffset(midOffset);
		point = textWidget.toDisplay(point);

		GC gc = new GC(textWidget);
		gc.setFont(textWidget.getFont());
		int height = gc.getFontMetrics().getHeight();
		gc.dispose();
		point.y += height;
		return point;
	}
}
