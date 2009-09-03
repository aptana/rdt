package com.aptana.rdt.core.gems;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;

public abstract class AbstractGemManager implements IGemManager
{

	protected Set<GemListener> listeners;

	protected AbstractGemManager()
	{
		listeners = new HashSet<GemListener>();
	}

	public synchronized void addGemListener(GemListener listener)
	{
		listeners.add(listener);
	}

	public synchronized void removeGemListener(GemListener listener)
	{
		listeners.remove(listener);
	}

	public ILaunchConfiguration run(String args) throws CoreException
	{
		return null;
	}

}
