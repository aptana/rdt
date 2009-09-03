/*******************************************************************************
 * Author: Markus Barchfeld
 * 
 * Copyright (c) 2004 RubyPeople.
 * 
 * This file is part of the Ruby Development Tools (RDT) plugin for eclipse. RDT
 * is subject to the "Common Public License (CPL) v 1.0". You may not use RDT
 * except in compliance with the License. For further information see
 * org.rubypeople.rdt/rdt.license. This source code is based on
 * org.eclipse.jdt.internal.debug.ui.console.JavaConsoleTracker /
 * 
 * /*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Common Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.rubypeople.rdt.internal.debug.ui.console;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.ui.console.IConsole;
import org.eclipse.debug.ui.console.IConsoleLineTracker;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ui.console.IHyperlink;
import org.rubypeople.rdt.internal.ui.RubyPlugin;
import org.rubypeople.rdt.internal.ui.util.StackTraceLine;
import org.rubypeople.rdt.launching.IRubyLaunchConfigurationConstants;

/**
 * Provides links for stack traces, eg: /data/EMAILTOSMSBRIDGE/configFile.rb:1:in `require': No such file to load --
 * inifile (LoadError) from /data/EMAILTOSMSBRIDGE/configFile.rb:1 from /data/EMAILTOSMSBRIDGE/fetchmail.rb:2:in
 * `require' from /data/EMAILTOSMSBRIDGE/fetchmail.rb:2 from /data/EMAILTOSMSBRIDGE/startEmailToSms.rb:1:in `require'
 * from /data/EMAILTOSMSBRIDGE/startEmailToSms.rb:1
 */
public class RubyConsoleTracker implements IConsoleLineTracker
{

	public interface FileExistanceChecker
	{
		boolean fileExists(String filename);
	}

	public static class StandardFileExistanceChecker implements FileExistanceChecker
	{

		public boolean fileExists(String filename)
		{
			File file = new File(filename);
			if (file.exists())
				return true;
			IPath path = new Path(filename);
			if (!path.isValidPath(filename))
			{
				return false;
			}
			try
			{
				IFile iFile = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
				if (iFile != null)
					return iFile.exists();
			}
			catch (IllegalArgumentException e)
			{
				return false;
			}
			return false;
		}
	}

	private final FileExistanceChecker existanceChecker;

	public RubyConsoleTracker()
	{
		this(new StandardFileExistanceChecker());
	}

	public RubyConsoleTracker(FileExistanceChecker existance)
	{
		this.existanceChecker = existance;
	}

	/**
	 * The console associated with this line tracker
	 */
	protected IConsole fConsole;

	private ILaunch fLastLaunch;
	private IProject fProject;

	/**
	 * @see org.eclipse.debug.ui.console.IConsoleLineTracker#init(org.eclipse.debug.ui.console.IConsole)
	 */
	public void init(IConsole pConsole)
	{
		fConsole = pConsole;
	}

	/**
	 * @see org.eclipse.debug.ui.console.IConsoleLineTracker#lineAppended(org.eclipse.jface.text.IRegion)
	 */
	public void lineAppended(IRegion line)
	{
		try
		{
			int prefix = 0;

			String text = getText(line);
			while (StackTraceLine.isTraceLine(text))
			{
				StackTraceLine stackTraceLine = new StackTraceLine(text, getProject());
				if (!existanceChecker.fileExists(stackTraceLine.getFilename()))
					return;
				IHyperlink link = new RubyStackTraceHyperlink(fConsole, stackTraceLine);
				fConsole.addLink(link, line.getOffset() + prefix + stackTraceLine.offset(), stackTraceLine.length());

				prefix = stackTraceLine.offset() + stackTraceLine.length();
				int substring = stackTraceLine.offset() + stackTraceLine.length();
				if (text.length() < substring - 1)
				{
					text = "";
				}
				else
				{
					text = text.substring(substring);
					if (text.startsWith(":in `require':"))
					{
						text = text.substring(14);
						prefix += 14;
					}
				}
			}
		}
		catch (BadLocationException e)
		{
		}
	}

	protected IProject getProject()
	{
		if (fLastLaunch == null
				|| (fConsole.getProcess() != null && !fLastLaunch.equals(fConsole.getProcess().getLaunch())))
		{
			if (fConsole.getProcess() == null)
				return null;
			fLastLaunch = fConsole.getProcess().getLaunch();
			String projectName = null;
			try
			{
				if (fConsole.getProcess() != null && fConsole.getProcess().getLaunch() != null
						&& fConsole.getProcess().getLaunch().getLaunchConfiguration() != null)
					projectName = fConsole.getProcess().getLaunch().getLaunchConfiguration().getAttribute(
							IRubyLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String) null);
			}
			catch (Exception e)
			{
				RubyPlugin.log(e);
			}
			if (projectName == null)
				return null;
			fProject = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		}
		return fProject;
	}

	protected String getText(IRegion line) throws BadLocationException
	{
		return fConsole.getDocument().get(line.getOffset(), line.getLength());
	}

	/**
	 * @see org.eclipse.debug.ui.console.IConsoleLineTracker#dispose()
	 */
	public void dispose()
	{
		fConsole = null;
	}

}
