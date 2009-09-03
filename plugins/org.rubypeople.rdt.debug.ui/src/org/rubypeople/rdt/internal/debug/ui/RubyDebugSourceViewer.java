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
package org.rubypeople.rdt.internal.debug.ui;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.custom.BidiSegmentEvent;
import org.eclipse.swt.custom.BidiSegmentListener;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.rubypeople.rdt.internal.debug.ui.display.DisplayViewerConfiguration;
import org.rubypeople.rdt.internal.ui.text.IRubyPartitions;

/**
 * A source viewer configured to display Ruby source. This
 * viewer obeys the font and color preferences specified in
 * the Ruby UI plugin.
 */
public class RubyDebugSourceViewer extends SourceViewer implements IPropertyChangeListener {
	
	private Font fFont;
	private Color fBackgroundColor;
	private Color fForegroundColor;
	private IPreferenceStore fStore;
	private DisplayViewerConfiguration fConfiguration;

	public RubyDebugSourceViewer(Composite parent, IVerticalRuler ruler, int styles) {
		super(parent, ruler, styles);
		StyledText text= this.getTextWidget();
		text.addBidiSegmentListener(new  BidiSegmentListener() {
			public void lineGetSegments(BidiSegmentEvent event) {
				try {
					event.segments= getBidiLineSegments(event.lineOffset);
				} catch (BadLocationException x) {
					// ignore
				}
			}
		});
	}
	
	/**
	 * Updates the viewer's font to match the preferences.
	 */
	private void updateViewerFont() {
		IPreferenceStore store= getPreferenceStore();
		if (store != null) {
			FontData data= null;
			if (store.contains(JFaceResources.TEXT_FONT) && !store.isDefault(JFaceResources.TEXT_FONT)) {
				data= PreferenceConverter.getFontData(store, JFaceResources.TEXT_FONT);
			} else {
				data= PreferenceConverter.getDefaultFontData(store, JFaceResources.TEXT_FONT);
			}
			if (data != null) {
				Font font= new Font(getTextWidget().getDisplay(), data);
				applyFont(font);
				if (getFont() != null) {
					getFont().dispose();
				}
				setFont(font);
				return;
			}
		}
		// if all the preferences failed
		applyFont(JFaceResources.getTextFont());
	}
	
	/**
	 * Sets the current font.
	 * 
	 * @param font the new font
	 */
	private void setFont(Font font) {
		fFont= font;
	}
	
	/**
	 * Returns the current font.
	 * 
	 * @return the current font
	 */
	private Font getFont() {
		return fFont;
	}
	
	/**
	 * Sets the font for the given viewer sustaining selection and scroll position.
	 * 
	 * @param font the font
	 */
	private void applyFont(Font font) {
		IDocument doc= getDocument();
		if (doc != null && doc.getLength() > 0) {
			Point selection= getSelectedRange();
			int topIndex= getTopIndex();
			
			StyledText styledText= getTextWidget();
			styledText.setRedraw(false);
			
			styledText.setFont(font);
			setSelectedRange(selection.x , selection.y);
			setTopIndex(topIndex);
			
			styledText.setRedraw(true);
		} else {
			getTextWidget().setFont(font);
		}	
	}
	
	/**
	 * Updates the given viewer's colors to match the preferences.
	 */
	public void updateViewerColors() {
		IPreferenceStore store= getPreferenceStore();
		if (store != null) {
			StyledText styledText= getTextWidget();
			Color color= store.getBoolean(AbstractTextEditor.PREFERENCE_COLOR_FOREGROUND_SYSTEM_DEFAULT)
				? null
				: createColor(store, AbstractTextEditor.PREFERENCE_COLOR_FOREGROUND, styledText.getDisplay());
			styledText.setForeground(color);
			if (getForegroundColor() != null) {
				getForegroundColor().dispose();
			}
			setForegroundColor(color);
			
			color= store.getBoolean(AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT)
				? null
				: createColor(store, AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND, styledText.getDisplay());
			styledText.setBackground(color);
			if (getBackgroundColor() != null) {
				getBackgroundColor().dispose();
			}
			setBackgroundColor(color);
		}
	}
	
	/**
	 * Creates a color from the information stored in the given preference store.
	 * Returns <code>null</code> if there is no such information available.
	 */
	private Color createColor(IPreferenceStore store, String key, Display display) {
		RGB rgb= null;	
		if (store.contains(key)) {
			if (store.isDefault(key)) {
				rgb= PreferenceConverter.getDefaultColor(store, key);
			} else {
				rgb= PreferenceConverter.getColor(store, key);
			}
			if (rgb != null) {
				return new Color(display, rgb);
			}
		}
		return null;
	}
	
	/**
	 * Returns the current background color.
	 * 
	 * @return the current background color
	 */
	protected Color getBackgroundColor() {
		return fBackgroundColor;
	}

	/**
	 * Sets the current background color.
	 * 
	 * @param backgroundColor the new background color
	 */
	protected void setBackgroundColor(Color backgroundColor) {
		fBackgroundColor = backgroundColor;
	}

	/**
	 * Returns the current foreground color.
	 * 
	 * @return the current foreground color
	 */
	protected Color getForegroundColor() {
		return fForegroundColor;
	}

	/**
	 * Sets the current foreground color.
	 * 
	 * @param foregroundColor the new foreground color
	 */
	protected void setForegroundColor(Color foregroundColor) {
		fForegroundColor = foregroundColor;
	}
	
	/**
	 * @see IPropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		IContentAssistant assistant= getContentAssistant();
//		if (assistant instanceof ContentAssistant) {
//			JDIContentAssistPreference.changeConfiguration((ContentAssistant) assistant, event);
//		}
		String property= event.getProperty();
		
		if (JFaceResources.TEXT_FONT.equals(property)) {
			updateViewerFont();
		}
		if (AbstractTextEditor.PREFERENCE_COLOR_FOREGROUND.equals(property) || AbstractTextEditor.PREFERENCE_COLOR_FOREGROUND_SYSTEM_DEFAULT.equals(property) ||
			AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND.equals(property) ||	AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT.equals(property)) {
			updateViewerColors();
		}
		if (fConfiguration != null) {
			if (fConfiguration.affectsTextPresentation(event)) {
				fConfiguration.handlePropertyChangeEvent(event);
				invalidateTextPresentation();
			}
		}
	}
	
	/**
	 * Returns the current content assistant.
	 * 
	 * @return the current content assistant
	 */
	public IContentAssistant getContentAssistant() {
		return fContentAssistant;
	}
	
	/**
	 * Returns a segmentation of the line of the given document appropriate for bidi rendering.
	 * The default implementation returns only the string literals of a Ruby code line as segments.
	 * 
	 * @param document the document
	 * @param lineOffset the offset of the line
	 * @return the line's bidi segmentation
	 * @throws BadLocationException in case lineOffset is not valid in document
	 */
	protected int[] getBidiLineSegments(int lineOffset) throws BadLocationException {
		IDocument document= getDocument();
		if (document == null) {
			return null;
		}
		IRegion line= document.getLineInformationOfOffset(lineOffset);
		ITypedRegion[] linePartitioning= document.computePartitioning(lineOffset, line.getLength());
		
		List segmentation= new ArrayList();
		for (int i= 0; i < linePartitioning.length; i++) {
			if (IRubyPartitions.RUBY_STRING.equals(linePartitioning[i].getType()))
				segmentation.add(linePartitioning[i]);
		}
		
		
		if (segmentation.size() == 0) 
			return null;
			
		int size= segmentation.size();
		int[] segments= new int[size * 2 + 1];
		
		int j= 0;
		for (int i= 0; i < size; i++) {
			ITypedRegion segment= (ITypedRegion) segmentation.get(i);
			
			if (i == 0)
				segments[j++]= 0;
				
			int offset= segment.getOffset() - lineOffset;
			if (offset > segments[j - 1])
				segments[j++]= offset;
				
			if (offset + segment.getLength() >= line.getLength())
				break;
				
			segments[j++]= offset + segment.getLength();
		}
		
		if (j < segments.length) {
			int[] result= new int[j];
			System.arraycopy(segments, 0, result, 0, j);
			segments= result;
		}
		
		return segments;
	}
	
	/**
	 * Disposes the system resources currently in use by this viewer.
	 */
	public void dispose() {
		if (getFont() != null) {
			getFont().dispose();
			setFont(null);
		}
		if (getBackgroundColor() != null) {
			getBackgroundColor().dispose();
			setBackgroundColor(null);
		}
		if (getForegroundColor() != null) {
			getForegroundColor().dispose();
			setForegroundColor(null);
		}
		if (fStore != null) {
			fStore.removePropertyChangeListener(this);
			fStore = null;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.source.SourceViewer#configure(org.eclipse.jface.text.source.SourceViewerConfiguration)
	 */
	public void configure(SourceViewerConfiguration configuration) {
		super.configure(configuration);
		if (fStore != null) {
			fStore.removePropertyChangeListener(this);
			fStore = null;
		}
		if (configuration instanceof DisplayViewerConfiguration) {
			fConfiguration = (DisplayViewerConfiguration) configuration;
			fStore = fConfiguration.getTextPreferenceStore();
			fStore.addPropertyChangeListener(this);
		}
		updateViewerFont();
		updateViewerColors();		
	}

	/**
	 * Returns the preference store used to configure this source viewer or
	 * <code>null</code> if none;
	 */
	private IPreferenceStore getPreferenceStore() {
		return fStore;
	}
	
}
