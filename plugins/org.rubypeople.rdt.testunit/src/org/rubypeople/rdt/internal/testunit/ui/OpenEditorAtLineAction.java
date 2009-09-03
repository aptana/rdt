/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sebastian Davids: sdavids@gmx.de bug 37333 Failure Trace cannot 
 * 			navigate to non-public class in CU throwing Exception

 *******************************************************************************/
package org.rubypeople.rdt.internal.testunit.ui;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.rubypeople.rdt.core.IRubyProject;
import org.rubypeople.rdt.internal.debug.ui.RubySourceLocator;
import org.rubypeople.rdt.internal.debug.ui.RubySourceLocator.SourceElement;

/**
 * Open a test in the Ruby editor and reveal a given line
 */
public class OpenEditorAtLineAction extends Action {

    private int fLineNumber;
    private String fileName;
    private TestUnitView fTestRunner;

    /**
     * Constructor for OpenEditorAtLineAction.
     */
    public OpenEditorAtLineAction(TestUnitView testRunner, String fileName, int line) {
        super(TestUnitMessages.OpenEditorAction_action_label);
        // TODO Uncomment when we have context help system set up
        // WorkbenchHelp.setHelp(this, IJUnitHelpContextIds.OPENEDITORATLINE_ACTION);
        fLineNumber = line;
        fTestRunner = testRunner;        
        IRubyProject project = testRunner.getLaunchedProject();
        if (project != null) { // If fileName contains "#{RAILS_ROOT}", expand that out properly yes this is rails code creeping into RDT Test::Unit plugin, but I don't see hwo welse to do it
        	this.fileName = fileName.replace("#{RAILS_ROOT}", project.getProject().getLocation().toPortableString());     
        } else {
        	this.fileName = fileName;
        }
        
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.rubypeople.rdt.internal.testunit.ui.OpenEditorAction#run()
     */
    public void run() {
        try {
            IEditorInput input = getInput();
            if (input == null) {
                if (TestunitPlugin.getDefault().isDebugging()) {
                    System.out
                            .println("Could not create editor input for stack trace: " + fileName);
                }
                // wrongly detected stack trace
                return;
            }
            IEditorPart editorPart = getEditorPart(input);
            setEditorToLine(editorPart, input);
        } catch (CoreException e) {
            TestunitPlugin.log(new Status(IStatus.ERROR, TestunitPlugin.PLUGIN_ID, 0,
                    "Could not open editor or set line in editor." + fileName, e));
        }
    }

    /**
     * @param input
     * @return
     * @throws PartInitException
     */
    private IEditorPart getEditorPart(IEditorInput input) throws PartInitException {
        ISourceLocator sourceLocator = getSourceLocator();
        if (!(sourceLocator instanceof RubySourceLocator)) { return null; }
        RubySourceLocator rubySourceLocator = (RubySourceLocator) sourceLocator;

        SourceElement sourceElement = (SourceElement) rubySourceLocator.getSourceElement(fileName);
        IEditorDescriptor descriptor = IDE.getEditorDescriptor(sourceElement.getFilename());        
        return IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), input, descriptor.getId());
    }

    private void setEditorToLine(IEditorPart pEditorPart, IEditorInput pInput) throws CoreException {
        if (!(pEditorPart instanceof ITextEditor)) { return; }
        // documents start at 0
        if (fLineNumber > 0) {
            fLineNumber--;
        }
        if (fLineNumber == 0) { return; }
        ITextEditor textEditor = (ITextEditor) pEditorPart;
        IDocumentProvider provider = textEditor.getDocumentProvider();
        provider.connect(pInput);
        IDocument document = provider.getDocument(pInput);
        try {
            IRegion line = document.getLineInformation(fLineNumber);
            textEditor.selectAndReveal(line.getOffset(), line.getLength());
        } catch (BadLocationException e) {
            if (TestunitPlugin.getDefault().isDebugging()) {
                System.out.println("Could not set editor to line: " + fLineNumber);
            }
        }
        provider.disconnect(pInput);
    }

    protected void reveal(ITextEditor textEditor) {
        if (fLineNumber >= 0) {
            try {
                IDocument document = textEditor.getDocumentProvider().getDocument(
                        textEditor.getEditorInput());
                textEditor.selectAndReveal(document.getLineOffset(fLineNumber - 1), document
                        .getLineLength(fLineNumber - 1));
            } catch (BadLocationException x) {
                // marker refers to invalid text position -> do nothing
            }
        }
    }

    public boolean isEnabled() {
        return getInput() != null;
    }

    /**
     * @return
     */
    private IEditorInput getInput() {
        ISourceLocator sourceLocator = getSourceLocator();
        if (!(sourceLocator instanceof RubySourceLocator)) { return null; }
        RubySourceLocator rubySourceLocator = (RubySourceLocator) sourceLocator;

        Object sourceElement = rubySourceLocator.getSourceElement(fileName);
        IEditorInput input = rubySourceLocator.getEditorInput(sourceElement);
        return input;
    }

    /**
     * @return
     */
    private ISourceLocator getSourceLocator() {
        ILaunch launch = fTestRunner.getLastLaunch();
        if (launch == null) { return null; }
        return launch.getSourceLocator();
    }

    protected Shell getShell() {
        return fTestRunner.getSite().getShell();
    }

    protected IRubyProject getLaunchedProject() {
        return fTestRunner.getLaunchedProject();
    }

}
