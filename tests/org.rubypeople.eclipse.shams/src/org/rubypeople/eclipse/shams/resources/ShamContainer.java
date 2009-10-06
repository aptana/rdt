package org.rubypeople.eclipse.shams.resources;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

public class ShamContainer extends ShamResource implements IContainer {

    protected List childResources = new ArrayList();

    public ShamContainer(IPath path) {
        super(path);
    }
    
    public void accept(IResourceProxyVisitor visitor, int flags) throws CoreException {
        for (Iterator iter = childResources.iterator(); iter.hasNext();) {
            IResource resource  = (IResource) iter.next();
            visitor.visit(new ShamResourceProxy(resource));
            if (resource instanceof IContainer) {
                IContainer container = (IContainer) resource;
                container.accept(visitor, flags);
            }
        }
    }

    public boolean exists(IPath path) {
        throw new RuntimeException("Sham must implement");
    }

    public IResource findMember(String name) {
        throw new RuntimeException("Sham must implement");
    }

    public IResource findMember(String name, boolean includePhantoms) {
        throw new RuntimeException("Sham must implement");
    }

    public IResource findMember(IPath path) {
        throw new RuntimeException("Sham must implement");
    }

    public IResource findMember(IPath path, boolean includePhantoms) {
        throw new RuntimeException("Sham must implement");
    }

    public String getDefaultCharset() throws CoreException {
        throw new RuntimeException("Sham must implement");
    }

    public String getDefaultCharset(boolean checkImplicit) throws CoreException {
        throw new RuntimeException("Sham must implement");
    }

    public IFile getFile(IPath path) {
        throw new RuntimeException("Sham must implement");
    }

    public IFolder getFolder(IPath path) {
        throw new RuntimeException("Sham must implement");
    }

    public IResource[] members() throws CoreException {
        return (IResource[]) childResources.toArray(new IResource[0]);
    }

    public IResource[] members(boolean includePhantoms) throws CoreException {
        throw new RuntimeException("Sham must implement");
    }

    public IResource[] members(int memberFlags) throws CoreException {
        throw new RuntimeException("Sham must implement");
    }

    public IFile[] findDeletedMembersWithHistory(int depth, IProgressMonitor monitor) throws CoreException {
        throw new RuntimeException("Sham must implement");
    }

    public void setDefaultCharset(String charset) throws CoreException {
        throw new RuntimeException("Sham must implement");
    }

    public void setDefaultCharset(String charset, IProgressMonitor monitor) throws CoreException {
        throw new RuntimeException("Sham must implement");
    }

    public void addResource(IResource resource) {
        childResources.add(resource);
    }

}
