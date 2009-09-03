package com.aptana.rdt.internal.rake;

import java.io.File;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

class RakeFileFinder implements IResourceProxyVisitor {

	private File workingDirectory;

	public boolean visit(IResourceProxy proxy) throws CoreException {
		if (proxy.getType() == IResource.FILE) {
			IPath path = proxy.requestFullPath();
			if (path.lastSegment().equalsIgnoreCase("rakefile")) {
				workingDirectory = path.removeLastSegments(1).toFile();
			}
		}
		return workingDirectory == null
				&& (proxy.getType() == IResource.FOLDER
						|| proxy.getType() == IResource.PROJECT || proxy
						.getType() == IResource.ROOT);
	}

	public File getWorkingDirectory() {
		return workingDirectory;
	}
}
