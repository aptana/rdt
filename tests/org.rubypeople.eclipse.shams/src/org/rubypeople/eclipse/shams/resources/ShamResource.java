/*
 * Author: 
 *
 * Copyright (c) 2005 RubyPeople.
 *
 * This file is part of the Ruby Development Tools (RDT) plugin for eclipse. 
 * RDT is subject to the "Common Public License (CPL) v 1.0". You may not use
 * RDT except in compliance with the License. For further information see 
 * org.rubypeople.rdt/rdt.license.
 */
package org.rubypeople.eclipse.shams.resources;

import java.net.URI;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IPathVariableManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.jobs.ISchedulingRule;

public class ShamResource implements IResource {
	protected IPath path;

	public ShamResource(IPath aPath) {
		path = aPath;
	}

    public boolean equals(Object obj) {
        if (obj instanceof ShamResource) {
            ShamResource that = (ShamResource) obj;
            return path.equals(that.path);
        }
        return false;
    }
    
	public void accept(IResourceVisitor visitor) throws CoreException {
		throw new RuntimeException("Need to implement on sham.");
	}

	public void accept(IResourceVisitor visitor, int depth, boolean includePhantoms) throws CoreException {
		throw new RuntimeException("Need to implement on sham.");
	}

	public void accept(IResourceVisitor visitor, int depth, int memberFlags) throws CoreException {
		throw new RuntimeException("Need to implement on sham.");
	}

	public void clearHistory(IProgressMonitor monitor) throws CoreException {
		throw new RuntimeException("Need to implement on sham.");
	}

	public void copy(IProjectDescription description, boolean force, IProgressMonitor monitor) throws CoreException {
		throw new RuntimeException("Need to implement on sham.");
	}

	public void copy(IPath destination, boolean force, IProgressMonitor monitor) throws CoreException {
		throw new RuntimeException("Need to implement on sham.");
	}

	public void copy(IProjectDescription description, int updateFlags, IProgressMonitor monitor) throws CoreException {
		throw new RuntimeException("Need to implement on sham.");
	}

	public void copy(IPath destination, int updateFlags, IProgressMonitor monitor) throws CoreException {
		throw new RuntimeException("Need to implement on sham.");
	}

	public IMarker createMarker(String type) {
		throw new RuntimeException("Need to implement on sham.");
	}

	public void delete(boolean force, IProgressMonitor monitor) throws CoreException {
		throw new RuntimeException("Need to implement on sham.");
	}

	public void delete(int updateFlags, IProgressMonitor monitor) throws CoreException {
		throw new RuntimeException("Need to implement on sham.");
	}

	public void deleteMarkers(String type, boolean includeSubtypes, int depth) throws CoreException {
		throw new RuntimeException("Need to implement on sham.");
	}

	public boolean exists() {
		throw new RuntimeException("Need to implement on sham.");
	}

	public IMarker findMarker(long id) throws CoreException {
		throw new RuntimeException("Need to implement on sham.");
	}

	public IMarker[] findMarkers(String type, boolean includeSubtypes, int depth) throws CoreException {
		throw new RuntimeException("Need to implement on sham.");
	}

	public String getFileExtension() {
		return path.getFileExtension();
	}

	public IPath getFullPath() {
		return path;
	}

	public IPath getLocation() {
		return path;
	}

	public IMarker getMarker(long id) {
		throw new RuntimeException("Need to implement on sham.");
	}

	public long getModificationStamp() {
		throw new RuntimeException("Need to implement on sham.");
	}

	public String getName() {
		return path.lastSegment();
	}

	public IContainer getParent() {
		throw new RuntimeException("Need to implement on sham.");
	}

	public String getPersistentProperty(QualifiedName key) throws CoreException {
		throw new RuntimeException("Need to implement on sham.");
	}

	public IProject getProject() {
		throw new RuntimeException("Need to implement on sham.");
	}

	public IPath getProjectRelativePath() {
		throw new RuntimeException("Need to implement on sham.");
	}

	public Object getSessionProperty(QualifiedName key) throws CoreException {
		throw new RuntimeException("Need to implement on sham.");
	}

	public int getType() {
		throw new RuntimeException("This should be returned by subclasses.");
	}

	public IWorkspace getWorkspace() {
		throw new RuntimeException("Need to implement on sham.");
	}

	public boolean isAccessible() {
		throw new RuntimeException("Need to implement on sham.");
	}

	public boolean isLocal(int depth) {
		throw new RuntimeException("Need to implement on sham.");
	}

	public boolean isPhantom() {
		throw new RuntimeException("Need to implement on sham.");
	}

	public boolean isReadOnly() {
		throw new RuntimeException("Need to implement on sham.");
	}

	public boolean isSynchronized(int depth) {
		throw new RuntimeException("Need to implement on sham.");
	}

	public void move(IProjectDescription description, boolean force, boolean keepHistory, IProgressMonitor monitor) throws CoreException {
		throw new RuntimeException("Need to implement on sham.");
	}

	public void move(IPath destination, boolean force, IProgressMonitor monitor) throws CoreException {
		throw new RuntimeException("Need to implement on sham.");
	}

	public void move(IProjectDescription description, int updateFlags, IProgressMonitor monitor) throws CoreException {
		throw new RuntimeException("Need to implement on sham.");
	}

	public void move(IPath destination, int updateFlags, IProgressMonitor monitor) throws CoreException {
		throw new RuntimeException("Need to implement on sham.");
	}

	public void refreshLocal(int depth, IProgressMonitor monitor) throws CoreException {
		throw new RuntimeException("Need to implement on sham.");
	}

	public void setLocal(boolean flag, int depth, IProgressMonitor monitor) throws CoreException {
		throw new RuntimeException("Need to implement on sham.");
	}

	public void setPersistentProperty(QualifiedName key, String value) throws CoreException {
		throw new RuntimeException("Need to implement on sham.");
	}

	public void setReadOnly(boolean readOnly) {
		throw new RuntimeException("Need to implement on sham.");
	}

	public void setSessionProperty(QualifiedName key, Object value) throws CoreException {
		throw new RuntimeException("Need to implement on sham.");
	}

	public void touch(IProgressMonitor monitor) throws CoreException {
		throw new RuntimeException("Need to implement on sham.");
	}

	public boolean isDerived() {
		throw new RuntimeException("Need to implement on sham.");
	}

	public void setDerived(boolean isDerived) throws CoreException {
		throw new RuntimeException("Need to implement on sham.");
	}

	public boolean isTeamPrivateMember() {
		throw new RuntimeException("Need to implement on sham.");
	}

	public void setTeamPrivateMember(boolean isTeamPrivate) throws CoreException {
		throw new RuntimeException("Need to implement on sham.");
	}

	public Object getAdapter(Class adapter) {
        if (adapter.isAssignableFrom(getClass()))
            return this;
        return null;
	}
	
	public IPath getRawLocation() {
		throw new RuntimeException("Need to implement on sham.");
	}

	public boolean isLinked() {
		throw new RuntimeException("Need to implement on sham.");
	}

    public void accept(IResourceProxyVisitor visitor, int flags) throws CoreException {
        throw new RuntimeException("Need to implement on sham.");
	}

	public long getLocalTimeStamp() {
		throw new RuntimeException("Need to implement on sham.");
	}

	public long setLocalTimeStamp(long value) throws CoreException {
		throw new RuntimeException("Need to implement on sham.");
	}

	public boolean contains(ISchedulingRule rule) {
		throw new RuntimeException("Need to implement on sham.");
	}

	public boolean isConflicting(ISchedulingRule rule) {
		throw new RuntimeException("Need to implement on sham.");
	}

	public ResourceAttributes getResourceAttributes() {
	    throw new RuntimeException("Need to implement on sham.");
	}

	public void revertModificationStamp(long value) throws CoreException {
	    throw new RuntimeException("Need to implement on sham.");
	}

	public void setResourceAttributes(ResourceAttributes attributes) throws CoreException {
	    throw new RuntimeException("Need to implement on sham.");
	}
    
    public String toString() {
        return "Resource [" + path + "]";
    }

	public URI getLocationURI() {
		return path.toFile().toURI();
	}

    public URI getRawLocationURI() {
        return null;
    }

    public boolean isLinked(int options) {
        return false;
    }

	public IResourceProxy createProxy() {
		return null;
	}

	public int findMaxProblemSeverity(String type, boolean includeSubtypes, int depth) throws CoreException
	{
		return 0;
	}

	public Map getPersistentProperties() throws CoreException
	{
		return null;
	}

	public Map getSessionProperties() throws CoreException
	{
		return null;
	}

	public boolean isDerived(int options)
	{
		return false;
	}

	public boolean isHidden()
	{
		return false;
	}

	public void setHidden(boolean isHidden) throws CoreException
	{
	}

	public boolean isHidden(int options)
	{
		return false;
	}

	public boolean isTeamPrivateMember(int options)
	{
		return false;
	}

	public IPathVariableManager getPathVariableManager()
	{
		return null;
	}

	public boolean isVirtual()
	{
		return false;
	}

	public void setDerived(boolean isDerived, IProgressMonitor monitor) throws CoreException
	{
	}

}
