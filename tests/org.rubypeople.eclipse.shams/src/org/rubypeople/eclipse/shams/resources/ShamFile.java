package org.rubypeople.eclipse.shams.resources;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URI;
import java.nio.charset.Charset;

import junit.framework.Assert;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFileState;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.IContentType;

public class ShamFile extends ShamResource implements IFile {

    public static final String WORKSPACE_ROOT = "/test/workspaceRoot";
    protected String contents = "";
	protected boolean readContentFromFile;
    private InputStream inputStream;
    private IProject project;
	
	public void setCharset(String newCharset, IProgressMonitor monitor)
	    throws CoreException {
	}
	
	public ShamFile(String fullPath) {
		this(fullPath, false);
	}

	public ShamFile(IPath aPath) {
		this(aPath, false);
	}
	
	public String getCharset() throws CoreException {
		return Charset.defaultCharset().name();
	}

	public ShamFile(String fullPath, boolean readContentFromFile) {
		this(new Path(fullPath), readContentFromFile);
	}

	public ShamFile(IPath aPath, boolean readContentFromFile) {
		super(aPath);
		this.readContentFromFile = readContentFromFile;
        project = new ShamProject("not specified");
	}

	public void appendContents(InputStream source, boolean force, boolean keepHistory, IProgressMonitor monitor) throws CoreException {
	}

	public void appendContents(InputStream source, int updateFlags, IProgressMonitor monitor) throws CoreException {
	}

	public void create(InputStream source, boolean force, IProgressMonitor monitor) throws CoreException {
	}

	public void create(InputStream source, int updateFlags, IProgressMonitor monitor) throws CoreException {
	}

	public void delete(boolean force, boolean keepHistory, IProgressMonitor monitor) throws CoreException {
	}

	public InputStream getContents() throws CoreException {
		if (readContentFromFile) {
			try {
				return openStream(new FileInputStream(this.path.toString()));
			} catch (FileNotFoundException e) {
				throw new RuntimeException(e.toString());
			}
		}
		return openStream(new ByteArrayInputStream(contents.getBytes()));
	}

	private InputStream openStream(InputStream newStream) {
        Assert.assertNull("Unexpected second opening of stream", inputStream);
        inputStream = new MonitoredInputStream(newStream);
        return inputStream;
    }
    
    public void assertContentStreamClosed() {
        Assert.assertNull("Unexpected found open stream", inputStream);
    }

    public InputStream getContents(boolean force) throws CoreException {
		return getContents();
	}

	public int getEncoding() throws CoreException {
		throw new RuntimeException("Unimplemented method in sham");
	}

	public IFileState[] getHistory(IProgressMonitor monitor) throws CoreException {
		throw new RuntimeException("Unimplemented method in sham");
	}

	public boolean isReadOnly() {
		throw new RuntimeException("Unimplemented method in sham");
	}

	public void move(IPath destination, boolean force, boolean keepHistory, IProgressMonitor monitor) throws CoreException {
	}

	public void setContents(InputStream source, boolean force, boolean keepHistory, IProgressMonitor monitor) throws CoreException {
	}

	public void setContents(IFileState source, boolean force, boolean keepHistory, IProgressMonitor monitor) throws CoreException {
	}

	public void setContents(InputStream source, int updateFlags, IProgressMonitor monitor) throws CoreException {
	}

	public void setContents(IFileState source, int updateFlags, IProgressMonitor monitor) throws CoreException {
	}

	public void setContents(String shamContents) {
		this.contents = shamContents;
	}

	public void accept(IResourceVisitor visitor) throws CoreException {
	}

	public void accept(IResourceVisitor visitor, int depth, boolean includePhantoms) throws CoreException {
	}

	public void accept(IResourceVisitor visitor, int depth, int memberFlags) throws CoreException {
	}

	public void clearHistory(IProgressMonitor monitor) throws CoreException {
	}

	public void copy(IProjectDescription description, boolean force, IProgressMonitor monitor) throws CoreException {
	}

	public void copy(IPath destination, boolean force, IProgressMonitor monitor) {
	}

	public void copy(IProjectDescription description, int updateFlags, IProgressMonitor monitor) throws CoreException {
	}

	public void copy(IPath destination, int updateFlags, IProgressMonitor monitor) throws CoreException {
	}

	public IMarker createMarker(String type) {
		throw new RuntimeException("Unimplemented method in sham");
	}

	public void delete(boolean force, IProgressMonitor monitor) throws CoreException {
	}

	public void delete(int updateFlags, IProgressMonitor monitor) throws CoreException {
	}

	public void deleteMarkers(String type, boolean includeSubtypes, int depth) throws CoreException {
	}

	public boolean exists() {
		return true;
	}

	public IMarker findMarker(long id) throws CoreException {
		throw new RuntimeException("Unimplemented method in sham");
	}

	public IMarker[] findMarkers(String type, boolean includeSubtypes, int depth) throws CoreException {
		throw new RuntimeException("Unimplemented method in sham");
	}

	public IPath getLocation() {
        return new Path(WORKSPACE_ROOT).append(getFullPath());
	}

	public IMarker getMarker(long id) {
		throw new RuntimeException("Unimplemented method in sham");
	}

	public long getModificationStamp() {
		throw new RuntimeException("Unimplemented method in sham");
	}

	public IContainer getParent() {
		throw new RuntimeException("Unimplemented method in sham");
	}

	public String getPersistentProperty(QualifiedName key) throws CoreException {
		throw new RuntimeException("Unimplemented method in sham");
	}

	public IProject getProject() {
        return project;
	}

	public IPath getProjectRelativePath() {
		return new Path("");
	}

	public Object getSessionProperty(QualifiedName key) throws CoreException {
		throw new RuntimeException("Unimplemented method in sham");
	}

	public int getType() {
		return FILE;
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

	public boolean isSynchronized(int depth) {
		throw new RuntimeException("Unimplemented method in sham");
	}

	public void move(IProjectDescription description, boolean force, boolean keepHistory, IProgressMonitor monitor) throws CoreException {
	}

	public void move(IPath destination, boolean force, IProgressMonitor monitor) throws CoreException {
	}

	public void move(IProjectDescription description, int updateFlags, IProgressMonitor monitor) throws CoreException {
	}

	public void move(IPath destination, int updateFlags, IProgressMonitor monitor) throws CoreException {
	}

	public void refreshLocal(int depth, IProgressMonitor monitor) throws CoreException {
	}

	public void setLocal(boolean flag, int depth, IProgressMonitor monitor) throws CoreException {
	}

	public void setPersistentProperty(QualifiedName key, String value) throws CoreException {
	}

	public void setReadOnly(boolean readOnly) {
	}

	public void setSessionProperty(QualifiedName key, Object value) throws CoreException {
	}

	public void touch(IProgressMonitor monitor) throws CoreException {
	}

	public boolean isDerived() {
		throw new RuntimeException("Unimplemented method in sham");
	}

	public void setDerived(boolean isDerived) throws CoreException {
	}

	public boolean isTeamPrivateMember() {
		throw new RuntimeException("Unimplemented method in sham");
	}

	public void setTeamPrivateMember(boolean isTeamPrivate) throws CoreException {
	}

	public Object getAdapter(Class adapter) {
		throw new RuntimeException("Unimplemented method in sham");
	}

	public void createLink(
		IPath localLocation,
		int updateFlags,
		IProgressMonitor monitor)
		throws CoreException {
	}

	public IPath getRawLocation() {
		return null;
	}

	public boolean isLinked() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IFile#setCharset(java.lang.String)
	 */
	public void setCharset(String newCharset) throws CoreException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IFile#getCharset(boolean)
	 */
	public String getCharset(boolean checkImplicit) throws CoreException {
		return getCharset();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IFile#getContentDescription()
	 */
	public IContentDescription getContentDescription() throws CoreException {
		return new ShamContentDescription();
	}

	public String getCharsetFor(Reader reader) throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

	public ResourceAttributes getResourceAttributes() {
		// TODO Auto-generated method stub
		return null;
	}

	public void revertModificationStamp(long value) throws CoreException {
		// TODO Auto-generated method stub
		
	}

	public void setResourceAttributes(ResourceAttributes attributes) throws CoreException {
		// TODO Auto-generated method stub
		
	}

    public void setProject(IProject project) {
        this.project = project;
    }

    private final class ShamContentDescription implements IContentDescription {
        public boolean isRequested(QualifiedName key) {
            // TODO Auto-generated method stub
            return false;
        }

        public String getCharset() {
            // TODO Auto-generated method stub
            return null;
        }

        public IContentType getContentType() {
            // TODO Auto-generated method stub
            return null;
        }

        public Object getProperty(QualifiedName key) {
            // TODO Auto-generated method stub
            return null;
        }

        public void setProperty(QualifiedName key, Object value) {
            // TODO Auto-generated method stub
            
        }
    }

    private class MonitoredInputStream extends InputStream {

        private final InputStream inputStream;

        public MonitoredInputStream(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        public int read() throws IOException {
            return inputStream.read();
        }

        public void close() throws IOException {
            super.close();
            ShamFile.this.inputStream = null;
        }
    }

    public void createLink(URI location, int updateFlags, IProgressMonitor monitor) throws CoreException {
        // TODO Auto-generated method stub
        
    }

    public URI getRawLocationURI() {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean isLinked(int options) {
        // TODO Auto-generated method stub
        return false;
    }

}
