package org.rubypeople.rdt.internal.core.builder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.IContentType;
import org.rubypeople.rdt.core.IRubyProject;
import org.rubypeople.rdt.core.RubyCore;
import org.rubypeople.rdt.core.compiler.BuildContext;

public class BuildContextCollector implements IResourceProxyVisitor {

	private static final String RUBY_SOURCE_CONTENT_TYPE_ID = "org.rubypeople.rdt.core.rubySource";
	private final List<BuildContext> contexts;
	private HashSet<String> visitedLinks;
	private IRubyProject rubyProject;

	public BuildContextCollector(IProject project) {
		this.contexts = new ArrayList<BuildContext>();
		this.visitedLinks = new HashSet<String>();
		this.rubyProject = RubyCore.create(project);
	}

	public boolean visit(IResourceProxy proxy) throws CoreException {
		switch (proxy.getType()) {
		case IResource.FILE:
			if (org.rubypeople.rdt.internal.core.util.Util
					.isRubyLikeFileName(proxy.getName())) {
				IFile file = getFile(proxy);
				contexts.add(new BuildContext(file));

				return false;
			}
			if (isERB(proxy.getName())) {
				IFile file = getFile(proxy);
				contexts.add(new ERBBuildContext(file));

				return false;
			}
			IFile file = getFile(proxy);
			if (isRubySourceContentType(file)) {
				contexts.add(new BuildContext(file));

				return false;
			}

			// 
			return false;
		case IResource.FOLDER:
			try { // Avoid recursive symlinks!
				IResource resource = proxy.requestResource();
				// HACK Don't create problems in vendor folders of ruby projects
				if (resource.getProjectRelativePath()
						.equals(new Path("vendor"))) {
					return false;
				}
				IPath path = resource.getLocation();
				if (path == null)
				{
					return false;
				}
				String unique = path.toOSString();
				if (path.toFile() != null)
				{
					unique = path.toFile().getCanonicalPath();
				}
				if (visitedLinks.contains(unique))
					return false;
				visitedLinks.add(unique);
			} catch (IOException e) {
				RubyCore.log(e);
				return false;
			}
		}
		return true;
	}

	private IFile getFile(IResourceProxy proxy) {
		return (IFile) proxy.requestResource();
	}

	public static boolean isERB(String name) {
		return name.endsWith(".erb") || name.endsWith(".rhtml");
	}

	private boolean isRubySourceContentType(IFile file) throws CoreException {
		IContentDescription contentDescription = file.getContentDescription();
		if (contentDescription != null) {
			IContentType type = contentDescription.getContentType();
			if (type != null)
				if (type.getId().equals(RUBY_SOURCE_CONTENT_TYPE_ID))
					return true;
		}
		return false;
	}

	public List<BuildContext> getContexts() {
		return contexts;
	}

}
