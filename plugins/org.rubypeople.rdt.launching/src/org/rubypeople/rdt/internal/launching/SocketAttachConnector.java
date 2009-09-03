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
package org.rubypeople.rdt.internal.launching;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.rubypeople.rdt.internal.debug.core.RubyDebuggerProxy;
import org.rubypeople.rdt.internal.debug.core.model.RubyDebugTarget;
import org.rubypeople.rdt.internal.debug.core.model.RubyProcessingException;
import org.rubypeople.rdt.launching.IRubyLaunchConfigurationConstants;
import org.rubypeople.rdt.launching.IVMConnector;

/**
 * A standard socket attaching connector
 */
public class SocketAttachConnector implements IVMConnector {

	/**
	 * @see IVMConnector#getIdentifier()
	 */
	public String getIdentifier() {
		return IRubyLaunchConfigurationConstants.ID_SOCKET_ATTACH_VM_CONNECTOR;
	}

	/**
	 * @see IVMConnector#getName()
	 */
	public String getName() {
		return LaunchingMessages.SocketAttachConnector_Standard__Socket_Attach__4;
	}

	/**
	 * Throws a core exception with an error status object built from the given
	 * message, lower level exception, and error code.
	 * 
	 * @param message
	 *            the status message
	 * @param exception
	 *            lower level exception associated with the error, or
	 *            <code>null</code> if none
	 * @param code
	 *            error code
	 */
	protected static void abort(String message, Throwable exception, int code)
			throws CoreException {
		throw new CoreException(new Status(IStatus.ERROR, LaunchingPlugin
				.getUniqueIdentifier(), code, message, exception));
	}

	/*
	 * (non-Rubydoc)
	 * 
	 * @see org.rubypeople.rdt.launching.IVMConnector#connect(java.util.Map,
	 * org.eclipse.core.runtime.IProgressMonitor,
	 * org.eclipse.debug.core.ILaunch)
	 */
	public void connect(Map<String, Object> arguments,
			IProgressMonitor monitor, ILaunch launch) throws CoreException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}

		IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 1);
		subMonitor.beginTask(
				LaunchingMessages.SocketAttachConnector_Connecting____1, 2);
		subMonitor
				.subTask(LaunchingMessages.SocketAttachConnector_Configuring_connection____1);

		String portNumberString = (String) arguments.get("port"); //$NON-NLS-1$
		if (portNumberString == null) {
			abort(
					LaunchingMessages.SocketAttachConnector_Port_unspecified_for_remote_connection__2,
					null,
					IRubyLaunchConfigurationConstants.ERR_UNSPECIFIED_PORT);
		}
		int port = Integer.parseInt(portNumberString);
		String host = (String) arguments.get("hostname"); //$NON-NLS-1$
		if (host == null) {
			abort(
					LaunchingMessages.SocketAttachConnector_Hostname_unspecified_for_remote_connection__4,
					null,
					IRubyLaunchConfigurationConstants.ERR_UNSPECIFIED_HOSTNAME);
		}

		subMonitor.worked(1);
		subMonitor
				.subTask(LaunchingMessages.SocketAttachConnector_Establishing_connection____2);

		try {
			RubyDebugTarget debugTarget = new RubyDebugTarget(launch, host,
					port);
			RubyDebuggerProxy proxy = new RubyDebuggerProxy(debugTarget, true);
			proxy.start();
			launch.addDebugTarget(debugTarget);
			subMonitor.worked(1);
			subMonitor.done();
		} catch (IOException e) {
			abort(
					LaunchingMessages.SocketAttachConnector_Failed_to_connect_to_remote_VM_1,
					e,
					IRubyLaunchConfigurationConstants.ERR_REMOTE_VM_CONNECTION_FAILED);
		} catch (RubyProcessingException e) {
			abort(
					LaunchingMessages.SocketAttachConnector_Failed_to_connect_to_remote_VM_1,
					e,
					IRubyLaunchConfigurationConstants.ERR_REMOTE_VM_CONNECTION_FAILED);
		}
	}

	/**
	 * @see IVMConnector#getDefaultArguments()
	 */
	public Map<String, Object> getDefaultArguments() throws CoreException {
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("hostname", "localhost");
		args.put("port", 1234);
		return args;
	}

	/**
	 * @see IVMConnector#getArgumentOrder()
	 */
	public List<String> getArgumentOrder() {
		List<String> list = new ArrayList<String>(2);
		list.add("hostname"); //$NON-NLS-1$
		list.add("port"); //$NON-NLS-1$
		return list;
	}

}
