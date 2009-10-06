package org.rubypeople.rdt.core.tests;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;

public abstract class ModifyingResourceTest extends AbstractRubyModelTest {
	
	public ModifyingResourceTest(String name) {
		super(name);
	}
	
	protected IFile editFile(String path, String content) throws CoreException {
		IFile file = this.getFile(path);
		InputStream input = new ByteArrayInputStream(content.getBytes());
		file.setContents(input, IResource.FORCE, null);
		return file;
	}
	
	protected IFolder createFolder(String path) throws CoreException {
		return createFolder(new Path(path));
	}
	
	protected IFile createFile(String path, String content) throws CoreException {
		return createFile(path, content.getBytes());
	}
	
	protected IFile createFile(String path, byte[] content) throws CoreException {
		return createFile(path, new ByteArrayInputStream(content));
	}
	
	protected IFile createFile(String path, InputStream content) throws CoreException {
		IFile file = getFile(path);
		file.create(content, true, null);
		try {
			content.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return file;
	}
	
	protected void deleteFile(String filePath) throws CoreException {
		deleteResource(this.getFile(filePath));
	}
	protected void deleteFolder(String folderPath) throws CoreException {
		deleteFolder(new Path(folderPath));
	}
}
