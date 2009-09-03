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
package org.rubypeople.rdt.internal.debug.ui.display;


import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;
import org.rubypeople.rdt.internal.debug.ui.RdtDebugUiPlugin;
import org.rubypeople.rdt.ui.PreferenceConstants;
import org.rubypeople.rdt.ui.text.RubySourceViewerConfiguration;

/**
 *  The source viewer configuration for the Display view
 */
public class DisplayViewerConfiguration extends RubySourceViewerConfiguration {
		
	public DisplayViewerConfiguration() {
		super(RdtDebugUiPlugin.getDefault().getRubyTextTools().getColorManager(), 
				new ChainedPreferenceStore(new IPreferenceStore[] {
						PreferenceConstants.getPreferenceStore(),
						EditorsUI.getPreferenceStore()}),
				null, null);
	}
	
	/**
	 * Returns the preference store this source viewer configuration is associated with.
	 * 
	 * @return
	 */
	public IPreferenceStore getTextPreferenceStore() {
		return fPreferenceStore;
	}

	public IContentAssistProcessor getContentAssistantProcessor() {
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getContentAssistant(org.eclipse.jface.text.source.ISourceViewer)
	 */
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {

		ContentAssistant assistant = new ContentAssistant();
		assistant.setContentAssistProcessor(
			getContentAssistantProcessor(),
			IDocument.DEFAULT_CONTENT_TYPE);

//		JDIContentAssistPreference.configure(assistant, getColorManager());

		assistant.setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_ABOVE);
		assistant.setInformationControlCreator(
			getInformationControlCreator(sourceViewer));

		return assistant;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getDoubleClickStrategy(org.eclipse.jface.text.source.ISourceViewer, java.lang.String)
	 */
	public ITextDoubleClickStrategy getDoubleClickStrategy(ISourceViewer sourceViewer, String contentType) {
		ITextDoubleClickStrategy clickStrat = new ITextDoubleClickStrategy() {
			// Highlight the whole line when double clicked. See Bug#45481 
			public void doubleClicked(ITextViewer viewer) {
				try {
					IDocument doc = viewer.getDocument();
					int caretOffset = viewer.getSelectedRange().x;
					int lineNum = doc.getLineOfOffset(caretOffset);
					int start = doc.getLineOffset(lineNum);
					int length = doc.getLineLength(lineNum);
					viewer.setSelectedRange(start, length);
				} catch (BadLocationException e) {
					RdtDebugUiPlugin.log(e);
				}
			}
		};
		return clickStrat;
	}	
}
