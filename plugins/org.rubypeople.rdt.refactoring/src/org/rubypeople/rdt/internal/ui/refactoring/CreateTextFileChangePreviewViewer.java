/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.rubypeople.rdt.internal.ui.refactoring;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.ui.refactoring.ChangePreviewViewerInput;
import org.eclipse.ltk.ui.refactoring.IChangePreviewViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.rubypeople.rdt.internal.corext.refactoring.nls.changes.CreateTextFileChange;
import org.rubypeople.rdt.internal.ui.RubyPlugin;
import org.rubypeople.rdt.internal.ui.util.ViewerPane;
import org.rubypeople.rdt.ui.PreferenceConstants;
import org.rubypeople.rdt.ui.text.RubySourceViewerConfiguration;
import org.rubypeople.rdt.ui.text.RubyTextTools;


public class CreateTextFileChangePreviewViewer implements IChangePreviewViewer {

	private ViewerPane fPane;
	private SourceViewer fSourceViewer;

	public void createControl(Composite parent) {
		fPane= new ViewerPane(parent, SWT.BORDER | SWT.FLAT);
		
		fSourceViewer= new SourceViewer(fPane, null, SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);
		fSourceViewer.setEditable(false);
		fSourceViewer.getControl().setFont(JFaceResources.getFont(PreferenceConstants.EDITOR_TEXT_FONT));
		fPane.setContent(fSourceViewer.getControl());
	}

	public Control getControl() {
		return fPane;
	}

	public void setInput(ChangePreviewViewerInput input) {
		Change change= input.getChange();
		if (!(change instanceof CreateTextFileChange)) {
			fSourceViewer.setInput(null);
			fPane.setText(""); //$NON-NLS-1$
			return;
		}
		CreateTextFileChange textFileChange= (CreateTextFileChange)change;
		fPane.setText(textFileChange.getName());
		IDocument document= new Document(textFileChange.getPreview());
		// This is a temporary work around until we get the
		// source viewer registry.
		fSourceViewer.unconfigure();
		if ("ruby".equals(textFileChange.getTextType())) { //$NON-NLS-1$
			RubyTextTools textTools= RubyPlugin.getDefault().getRubyTextTools();
			textTools.setupRubyDocumentPartitioner(document);
			IPreferenceStore store= RubyPlugin.getDefault().getCombinedPreferenceStore();
			fSourceViewer.configure(new RubySourceViewerConfiguration(textTools.getColorManager(), store, null, null));
		} else {
			fSourceViewer.configure(new SourceViewerConfiguration());
		}
		fSourceViewer.setInput(document);
	}

	public void refresh() {
		fSourceViewer.refresh();
	}
}
