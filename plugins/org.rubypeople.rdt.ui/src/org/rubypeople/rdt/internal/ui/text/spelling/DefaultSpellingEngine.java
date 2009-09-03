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

package org.rubypeople.rdt.internal.ui.text.spelling;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.spelling.ISpellingEngine;
import org.eclipse.ui.texteditor.spelling.ISpellingProblemCollector;
import org.eclipse.ui.texteditor.spelling.SpellingContext;
import org.eclipse.ui.texteditor.spelling.SpellingEngineDescriptor;
import org.rubypeople.rdt.core.RubyCore;
import org.rubypeople.rdt.internal.ui.RubyPlugin;

/**
 * Default spelling engine.
 * 
 * @since 3.1
 */
public class DefaultSpellingEngine implements ISpellingEngine
{

	/** Text content type */
	private static final IContentType TEXT_CONTENT_TYPE = Platform.getContentTypeManager().getContentType(
			IContentTypeManager.CT_TEXT);

	/** Ruby source content type */
	private static final IContentType RUBY_CONTENT_TYPE = Platform.getContentTypeManager().getContentType(
			RubyCore.RUBY_SOURCE_CONTENT_TYPE);

	/** Java source content type */
	private static final IContentType JAVA_CONTENT_TYPE = Platform.getContentTypeManager().getContentType(
			"org.eclipse.jdt.core.javaSource"); //$NON-NLS-1$

	/** Java properties content type */
	private static final IContentType PROPERTIES_CONTENT_TYPE = Platform.getContentTypeManager().getContentType(
			"org.eclipse.jdt.core.javaProperties"); //$NON-NLS-1$

	/** Available spelling engines by content type */
	private Map<IContentType, ISpellingEngine> fEngines = new HashMap<IContentType, ISpellingEngine>();

	/**
	 * Initialize concrete engines.
	 */
	public DefaultSpellingEngine()
	{
		if (RUBY_CONTENT_TYPE != null)
			fEngines.put(RUBY_CONTENT_TYPE, new RubySpellingEngine());
		if (TEXT_CONTENT_TYPE != null)
			fEngines.put(TEXT_CONTENT_TYPE, new TextSpellingEngine());
	}

	/*
	 * @see org.eclipse.ui.texteditor.spelling.ISpellingEngine#check(org.eclipse.jface.text.IDocument,
	 * org.eclipse.jface.text.IRegion[], org.eclipse.ui.texteditor.spelling.SpellingContext,
	 * org.eclipse.ui.texteditor.spelling.ISpellingProblemCollector, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void check(IDocument document, IRegion[] regions, SpellingContext context,
			ISpellingProblemCollector collector, IProgressMonitor monitor)
	{
		ISpellingEngine engine = getEngine(context.getContentType());
		if (engine == null)
			engine = getEngine(TEXT_CONTENT_TYPE);
		if (engine != null)
			engine.check(document, regions, context, collector, monitor);
	}

	/**
	 * Returns a spelling engine for the given content type or <code>null</code> if none could be found.
	 * 
	 * @param contentType
	 *            the content type
	 * @return a spelling engine for the given content type or <code>null</code> if none could be found
	 */
	private ISpellingEngine getEngine(IContentType contentType)
	{
		if (contentType == null)
			return null;

		if (fEngines.containsKey(contentType))
			return (ISpellingEngine) fEngines.get(contentType);

		if (contentType.equals(JAVA_CONTENT_TYPE) || contentType.equals(PROPERTIES_CONTENT_TYPE))
		{
			SpellingEngineDescriptor[] descriptors = EditorsUI.getSpellingService().getSpellingEngineDescriptors();
			for (SpellingEngineDescriptor desc : descriptors)
			{
				String id = desc.getId();
				if (id.equals("org.eclipse.jdt.internal.ui.text.spelling.DefaultSpellingEngine")) //$NON-NLS-1$
				{
					try
					{
						return desc.createEngine();
					}
					catch (CoreException e)
					{
						RubyPlugin.log(e.getStatus());
					}
				}
			}
		}

		return getEngine(contentType.getBaseType());
	}
}
