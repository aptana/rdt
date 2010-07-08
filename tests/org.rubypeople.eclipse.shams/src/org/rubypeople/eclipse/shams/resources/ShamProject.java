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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.content.IContentTypeMatcher;

public class ShamProject extends ShamContainer implements IProject, IContainer {
	protected String projectName;
	protected List natures = new ArrayList();
    public ShamProject(String theProjectName) {
		this(new Path("/" + theProjectName), theProjectName);
	}

	public void setDefaultCharset(String charset, IProgressMonitor monitor) throws CoreException {
	}
	
	public ShamProject(IPath aPath, String theProjectName) {
		super(aPath);
		projectName = theProjectName;
	}

    public void build(int kind, String builderName, Map args, IProgressMonitor monitor) throws CoreException {}

	public void build(int kind, IProgressMonitor monitor) throws CoreException {}

	public void close(IProgressMonitor monitor) throws CoreException {}

	public void create(IProjectDescription description, IProgressMonitor monitor) throws CoreException {}

	public void create(IProgressMonitor monitor) throws CoreException {}

	public void delete(boolean deleteContent, boolean force, IProgressMonitor monitor) throws CoreException {}

	public IProjectDescription getDescription() throws CoreException {
		throw new RuntimeException("Unimplemented method in sham");
	}

	public IFile getFile(String name) {
		return new ShamFile(getFullPath().append(name));
	}

	public IFolder getFolder(String name) {
		return new ShamFolder(getFullPath().append(name));
	}

	public IProjectNature getNature(String natureId) throws CoreException {
		throw new RuntimeException("Unimplemented method in sham");
	}

	public IPath getPluginWorkingLocation(IPluginDescriptor plugin) {
		throw new RuntimeException("Unimplemented method in sham");
	}

	public IProject[] getReferencedProjects() throws CoreException {
		throw new RuntimeException("Unimplemented method in sham");
	}

	public IProject[] getReferencingProjects() {
		throw new RuntimeException("Unimplemented method in sham");
	}

	public boolean hasNature(String natureId) throws CoreException {
		return natures.contains(natureId);
	}

	public boolean isNatureEnabled(String natureId) throws CoreException {
		throw new RuntimeException("Unimplemented method in sham");
	}

	public boolean isOpen() {
		throw new RuntimeException("Unimplemented method in sham");
	}

	public void move(IProjectDescription description, boolean force, IProgressMonitor monitor) throws CoreException {}

	public void open(IProgressMonitor monitor) throws CoreException {}

	public void setDescription(IProjectDescription description, IProgressMonitor monitor) throws CoreException {}

	public void setDescription(IProjectDescription description, int updateFlags, IProgressMonitor monitor) throws CoreException {}

	public boolean exists(IPath path) {
		throw new RuntimeException("Unimplemented method in sham");
	}

	public IResource findMember(String name) {
		throw new RuntimeException("Unimplemented method in sham");
	}

	public IResource findMember(String name, boolean includePhantoms) {
		throw new RuntimeException("Unimplemented method in sham");
	}

	public IResource findMember(IPath path) {
		throw new RuntimeException("Unimplemented method in sham");
	}

	public IResource findMember(IPath path, boolean includePhantoms) {
		throw new RuntimeException("Unimplemented method in sham");
	}

	public IFile getFile(IPath path) {
		throw new RuntimeException("Unimplemented method in sham");
	}

	public IFolder getFolder(IPath path) {
		throw new RuntimeException("Unimplemented method in sham");
	}

	public IResource[] members() throws CoreException {
        return (IResource[]) childResources.toArray(new IResource[0]);
	}

	public IResource[] members(boolean includePhantoms) throws CoreException {
		throw new RuntimeException("Unimplemented method in sham");
	}

	public IResource[] members(int memberFlags) throws CoreException {
		throw new RuntimeException("Unimplemented method in sham");
	}

	public IFile[] findDeletedMembersWithHistory(int depth, IProgressMonitor monitor) throws CoreException {
		throw new RuntimeException("Unimplemented method in sham");
	}


	public void accept(IResourceVisitor visitor) throws CoreException {}

	public void accept(IResourceVisitor visitor, int depth, boolean includePhantoms) throws CoreException {}

	public void accept(IResourceVisitor visitor, int depth, int memberFlags) throws CoreException {}

	public void clearHistory(IProgressMonitor monitor) throws CoreException {}

	public void copy(IProjectDescription description, boolean force, IProgressMonitor monitor) throws CoreException {}

	public void copy(IPath destination, boolean force, IProgressMonitor monitor) throws CoreException {}

	public void copy(IProjectDescription description, int updateFlags, IProgressMonitor monitor) throws CoreException {}

	public void copy(IPath destination, int updateFlags, IProgressMonitor monitor) throws CoreException {}

	public IMarker createMarker(String type) {
		throw new RuntimeException("Unimplemented method in sham");
	}

	public void delete(boolean force, IProgressMonitor monitor) throws CoreException {}

	public void delete(int updateFlags, IProgressMonitor monitor) throws CoreException {}

	public void deleteMarkers(String type, boolean includeSubtypes, int depth) throws CoreException {}

	public boolean exists() {
		throw new RuntimeException("Unimplemented method in sham");
	}

	public IMarker findMarker(long id) throws CoreException {
		throw new RuntimeException("Unimplemented method in sham");
	}

	public IMarker[] findMarkers(String type, boolean includeSubtypes, int depth) throws CoreException {
		throw new RuntimeException("Unimplemented method in sham");
	}

	public IMarker getMarker(long id) {
		throw new RuntimeException("Unimplemented method in sham");
	}

	public long getModificationStamp() {
		throw new RuntimeException("Unimplemented method in sham");
	}

	public String getName() {
		return projectName;
	}

	public IContainer getParent() {
		throw new RuntimeException("Unimplemented method in sham");
	}

	public String getPersistentProperty(QualifiedName key) throws CoreException {
		throw new RuntimeException("Unimplemented method in sham");
	}

	public IProject getProject() {
		throw new RuntimeException("Unimplemented method in sham");
	}

	public IPath getProjectRelativePath() {
		throw new RuntimeException("Unimplemented method in sham");
	}

	public Object getSessionProperty(QualifiedName key) throws CoreException {
		throw new RuntimeException("Unimplemented method in sham");
	}

	public int getType() {
		return IResource.PROJECT;
	}

	public IWorkspace getWorkspace() {
		throw new RuntimeException("Unimplemented method in sham");
	}

	public boolean isAccessible() {
		throw new RuntimeException("Unimplemented method in sham");
	}

	public boolean isLocal(int depth) {
		throw new RuntimeException("Unimplemented method in sham");
	}

	public boolean isPhantom() {
		throw new RuntimeException("Unimplemented method in sham");
	}

	public boolean isReadOnly() {
		throw new RuntimeException("Unimplemented method in sham");
	}

	public boolean isSynchronized(int depth) {
		throw new RuntimeException("Unimplemented method in sham");
	}

	public void move(IProjectDescription description, boolean force, boolean keepHistory, IProgressMonitor monitor) throws CoreException {}

	public void move(IPath destination, boolean force, IProgressMonitor monitor) throws CoreException {}

	public void move(IProjectDescription description, int updateFlags, IProgressMonitor monitor) throws CoreException {}

	public void move(IPath destination, int updateFlags, IProgressMonitor monitor) throws CoreException {}

	public void refreshLocal(int depth, IProgressMonitor monitor) throws CoreException {}

	public void setLocal(boolean flag, int depth, IProgressMonitor monitor) throws CoreException {}

	public void setPersistentProperty(QualifiedName key, String value) throws CoreException {}

	public void setReadOnly(boolean readOnly) {}

	public void setSessionProperty(QualifiedName key, Object value) throws CoreException {}

	public void touch(IProgressMonitor monitor) throws CoreException {}

	public boolean isDerived() {
		throw new RuntimeException("Unimplemented method in sham");
	}

	public void setDerived(boolean isDerived) throws CoreException {}

	public boolean isTeamPrivateMember() {
		throw new RuntimeException("Unimplemented method in sham");
	}

	public void setTeamPrivateMember(boolean isTeamPrivate) throws CoreException {}

	public void addNature(String string) {
		natures.add(string);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IContainer#getDefaultCharset()
	 */
	public String getDefaultCharset() throws CoreException {
		throw new RuntimeException("Need to implement on sham.");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IContainer#setDefaultCharset(java.lang.String)
	 */
	public void setDefaultCharset(String charset) throws CoreException {
		throw new RuntimeException("Need to implement on sham.");		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IProject#getWorkingLocation(java.lang.String)
	 */
	public IPath getWorkingLocation(String id) {
		throw new RuntimeException("Need to implement on sham.");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IContainer#getDefaultCharset(boolean)
	 */
	public String getDefaultCharset(boolean checkImplicit) throws CoreException {
		throw new RuntimeException("Need to implement on sham.");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IProject#open(int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void open(int updateFlags, IProgressMonitor monitor) throws CoreException {
		throw new RuntimeException("Need to implement on sham.");
	}

	public IContentTypeMatcher getContentTypeMatcher() throws CoreException {
		return null;
	}

	public ResourceAttributes getResourceAttributes() {
		return null;
	}

	public void revertModificationStamp(long value) throws CoreException {
	}

	public void setResourceAttributes(ResourceAttributes attributes) throws CoreException {
	}

	public void create(IProjectDescription description, int updateFlags, IProgressMonitor monitor) throws CoreException
	{
	}

	public void loadSnapshot(int options, URI snapshotLocation, IProgressMonitor monitor) throws CoreException
	{
	}

	public void saveSnapshot(int options, URI snapshotLocation, IProgressMonitor monitor) throws CoreException
	{
	}

}
