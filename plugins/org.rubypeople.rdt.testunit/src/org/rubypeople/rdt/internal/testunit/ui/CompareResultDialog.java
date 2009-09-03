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
package org.rubypeople.rdt.internal.testunit.ui;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareViewerPane;
import org.eclipse.compare.IEncodedStreamContentAccessor;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.contentmergeviewer.TextMergeViewer;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.presentation.IPresentationDamager;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.IPresentationRepairer;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class CompareResultDialog extends TrayDialog {
    private static class CompareResultMergeViewer extends TextMergeViewer {
         private CompareResultMergeViewer(Composite parent, int style, CompareConfiguration configuration) {
             super(parent, style, configuration);
         }
         
     	protected void createControls(Composite composite) {
     		super.createControls(composite);
    		//PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, IJUnitHelpContextIds.RESULT_COMPARE_DIALOG);
     	}
         
        protected void configureTextViewer(TextViewer textViewer) {
            if (textViewer instanceof SourceViewer) {
                ((SourceViewer)textViewer).configure(new CompareResultViewerConfiguration());   
            }
        }
    }
    
    public static class CompareResultViewerConfiguration extends SourceViewerConfiguration {
        public static class SimpleDamagerRepairer implements IPresentationDamager, IPresentationRepairer {
            private IDocument fDocument;

            public void setDocument(IDocument document) {
                fDocument= document;
            }

            public IRegion getDamageRegion(ITypedRegion partition, DocumentEvent event, boolean changed) {
                return new Region(0, fDocument.getLength());
            }

            public void createPresentation(TextPresentation presentation, ITypedRegion damage) {
                int suffix= CompareResultDialog.fgThis.fSuffix;
                int prefix= CompareResultDialog.fgThis.fPrefix;
                TextAttribute attr= new TextAttribute(Display.getDefault().getSystemColor(SWT.COLOR_RED), null, SWT.BOLD);
                presentation.addStyleRange(new StyleRange(prefix, fDocument.getLength()-suffix-prefix, attr.getForeground(), attr.getBackground(), attr.getStyle()));
            }
        }
        
        public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
            PresentationReconciler reconciler= new PresentationReconciler();
            SimpleDamagerRepairer dr= new SimpleDamagerRepairer();
            reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
            reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);
            return reconciler;
        }
    }
        
	private static class CompareElement implements ITypedElement, IEncodedStreamContentAccessor {
	    private String fContent;
	    
	    public CompareElement(String content) {
	        fContent= content;
	    }
	    public String getName() {
	        return "<no name>"; //$NON-NLS-1$
	    }
	    public Image getImage() {
	        return null;
	    }
	    public String getType() {
	        return "txt"; //$NON-NLS-1$
	    }
	    public InputStream getContents() {
		    try {
		        return new ByteArrayInputStream(fContent.getBytes("UTF-8")); //$NON-NLS-1$
		    } catch (UnsupportedEncodingException e) {
		        return new ByteArrayInputStream(fContent.getBytes());
		    }
	    }
        public String getCharset() throws CoreException {
            return "UTF-8"; //$NON-NLS-1$
        }
	}

    private TextMergeViewer fViewer;
    private String fExpected;
    private String fActual;
    private String fTestName;
    
    /* workaround - to make prefix and suffix accessible to the CompareResultViewerConfiguration */
    private static CompareResultDialog fgThis;
    
    private int fPrefix;
    private int fSuffix;
	
	public CompareResultDialog(Shell parentShell, TestRunInfo element) {
		super(parentShell);
		fgThis= this;
		setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
        fTestName= element.getTestName();
        fExpected= element.getExpected();
        fActual= element.getActual();
        computePrefixSuffix();
	}
		
	private void computePrefixSuffix() {
		int end= Math.min(fExpected.length(), fActual.length());
		int i= 0;
		for(; i < end; i++) 
			if (fExpected.charAt(i) != fActual.charAt(i))
				break;
		fPrefix= i;
		
		int j= fExpected.length()-1;
		int k= fActual.length()-1;
		int l= 0;
		for (; k >= fPrefix && j >= fPrefix; k--,j--) {
			if (fExpected.charAt(j) != fActual.charAt(k))
				break;
			l++;
		}
		fSuffix= l;
	}

    protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(TestUnitMessages.CompareResultDialog_title);
		//PlatformUI.getWorkbench().getHelpSystem().setHelp(newShell, IJUnitHelpContextIds.RESULT_COMPARE_DIALOG);
	}

	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, TestUnitMessages.CompareResultDialog_labelOK, true); 
	}

	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite)super.createDialogArea(parent);
		GridLayout layout= new GridLayout();
		layout.numColumns= 1;
		composite.setLayout(layout);
		
		CompareViewerPane pane = new CompareViewerPane(composite, SWT.BORDER | SWT.FLAT);
		pane.setText(fTestName);
		GridData data= new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL);
		data.widthHint= convertWidthInCharsToPixels(120);
		data.heightHint= convertHeightInCharsToPixels(13);
		pane.setLayoutData(data);
		
		Control previewer= createPreviewer(pane);
		pane.setContent(previewer);
		GridData gd= new GridData(GridData.FILL_BOTH);
		previewer.setLayoutData(gd);
		applyDialogFont(parent);
		return composite;
	}
	
	private Control createPreviewer(Composite parent) {
	    final CompareConfiguration compareConfiguration= new CompareConfiguration();
	    compareConfiguration.setLeftLabel(TestUnitMessages.CompareResultDialog_expectedLabel); 
	    compareConfiguration.setLeftEditable(false);
	    compareConfiguration.setRightLabel(TestUnitMessages.CompareResultDialog_actualLabel);	 
	    compareConfiguration.setRightEditable(false);
	    compareConfiguration.setProperty(CompareConfiguration.IGNORE_WHITESPACE, Boolean.FALSE);

	    fViewer= new CompareResultMergeViewer(parent, SWT.NONE, compareConfiguration);
	    fViewer.setInput(new DiffNode( 
        new CompareElement(fExpected), 
        new CompareElement(fActual)));

	    Control control= fViewer.getControl();
	    control.addDisposeListener(new DisposeListener() {
	        public void widgetDisposed(DisposeEvent e) {
	            if (compareConfiguration != null)
	                compareConfiguration.dispose();
	        }
	    });
	    return  control;
	}	
}
