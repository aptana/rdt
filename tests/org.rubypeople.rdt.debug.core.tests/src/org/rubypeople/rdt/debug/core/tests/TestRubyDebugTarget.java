package org.rubypeople.rdt.debug.core.tests;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IThread;
import org.rubypeople.rdt.internal.debug.core.RubyDebuggerProxy;
import org.rubypeople.rdt.internal.debug.core.SuspensionPoint;
import org.rubypeople.rdt.internal.debug.core.model.IRubyDebugTarget;

public class TestRubyDebugTarget implements IRubyDebugTarget {
	private List<SuspensionPoint> suspensionPoints = new ArrayList<SuspensionPoint>();
	
	public SuspensionPoint getLastSuspensionPoint() {
		if (suspensionPoints.isEmpty()) {
			return null;
		}
		return suspensionPoints.get(suspensionPoints.size()-1);
	}


	public void setRubyDebuggerProxy(RubyDebuggerProxy rubyDebuggerProxy) {
	}

	public void suspensionOccurred(SuspensionPoint suspensionPoint) {
		suspensionPoints.add(suspensionPoint);
	}

	public void terminate()  {
	}

	public void updateThreads() {
	}

	public String getName() throws DebugException {
		return null;
	}

	public IProcess getProcess() {
		return null;
	}

	public IThread[] getThreads() throws DebugException {
		return null;
	}

	public boolean hasThreads() throws DebugException {
		return false;
	}

	public boolean supportsBreakpoint(IBreakpoint breakpoint) {
		return false;
	}

	public IDebugTarget getDebugTarget() {
		return null;
	}

	public ILaunch getLaunch() {
		return null;
	}

	public String getModelIdentifier() {
		return null;
	}

	public boolean canTerminate() {
		return false;
	}

	public boolean isTerminated() {
		return false;
	}

	public boolean canResume() {
		return false;
	}

	public boolean canSuspend() {
		return false;
	}

	public boolean isSuspended() {
		return false;
	}

	public void resume() throws DebugException {
	}

	public void suspend() throws DebugException {
	}

	public void breakpointAdded(IBreakpoint breakpoint) {
	}

	public void breakpointChanged(IBreakpoint breakpoint, IMarkerDelta delta) {
	}

	public void breakpointRemoved(IBreakpoint breakpoint, IMarkerDelta delta) {
	}

	public boolean canDisconnect() {
		return false;
	}

	public void disconnect() throws DebugException {
	}

	public boolean isDisconnected() {
		return false;
	}

	public IMemoryBlock getMemoryBlock(long startAddress, long length) throws DebugException {
		return null;
	}

	public boolean supportsStorageRetrieval() {
		return false;
	}

	public Object getAdapter(Class arg0) {
		return null;
	}


	public int getPort() {		
		return -1;
	}


	public String getHost() {
		return "localhost";
	}


	public RubyDebuggerProxy getRubyDebuggerProxy()
	{
		// TODO Auto-generated method stub
		return null;
	}


	public IStatus load(String filename)
	{
		// TODO Auto-generated method stub
		return null;
	}

}
