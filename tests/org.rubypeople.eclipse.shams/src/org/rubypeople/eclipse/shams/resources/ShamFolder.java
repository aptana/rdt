package org.rubypeople.eclipse.shams.resources;


import java.net.URI;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;

public class ShamFolder extends ShamContainer implements IFolder {

	public ShamFolder(String aPathString) {
		super(new Path(aPathString));
	}

	public ShamFolder(IPath aPath) {
		super(aPath);
	}

	public void setDefaultCharset(String charset, IProgressMonitor monitor)
	    throws CoreException {
	}


	public int getType() {
		return FOLDER;
	}


    public void create(boolean force, boolean local, IProgressMonitor monitor) throws CoreException {
        throw new RuntimeException("Sham needs to implement");
    }

    public void create(int updateFlags, boolean local, IProgressMonitor monitor) throws CoreException {
        throw new RuntimeException("Sham needs to implement");
    }

    public void createLink(IPath localLocation, int updateFlags, IProgressMonitor monitor) throws CoreException {
        throw new RuntimeException("Sham needs to implement");
    }

    public void delete(boolean force, boolean keepHistory, IProgressMonitor monitor) throws CoreException {
        throw new RuntimeException("Sham needs to implement");
    }

    public IFile getFile(String name) {
        throw new RuntimeException("Sham needs to implement");
    }

    public IFolder getFolder(String name) {
        throw new RuntimeException("Sham needs to implement");
    }

    public void move(IPath destination, boolean force, boolean keepHistory, IProgressMonitor monitor) throws CoreException {
        throw new RuntimeException("Sham needs to implement");
    }

    public void createLink(URI location, int updateFlags, IProgressMonitor monitor) throws CoreException {
        // TODO Auto-generated method stub
        
    }
}
