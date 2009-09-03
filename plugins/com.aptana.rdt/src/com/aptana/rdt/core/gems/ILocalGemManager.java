package com.aptana.rdt.core.gems;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;

public interface ILocalGemManager extends IGemManager
{
	/**
	 * Run a command straight up.
	 * 
	 * @param args
	 * @throws CoreException
	 */
	public ILaunchConfiguration run(String args) throws CoreException;
}
