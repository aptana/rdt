/*
 * Author: David Corbin
 *
 * Copyright (c) 2005 RubyPeople.
 *
 * This file is part of the Ruby Development Tools (RDT) plugin for eclipse. 
 * RDT is subject to the "Common Public License (CPL) v 1.0". You may not use
 * RDT except in compliance with the License. For further information see 
 * org.rubypeople.rdt/rdt.license.
 */
package org.rubypeople.eclipse.shams.resources;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.QualifiedName;

public class ShamResourceProxy implements IResourceProxy {

    private IResource resource;

    public ShamResourceProxy(IResource resource) {
        this.resource = resource;
    }

    public long getModificationStamp() {
        return resource.getModificationStamp();
    }

    public boolean isAccessible() {
        return resource.isAccessible();
    }

    public boolean isDerived() {
        return resource.isDerived();
    }

    public boolean isLinked() {
        return resource.isLinked();
    }

    public boolean isPhantom() {
        return resource.isPhantom();
    }

    public boolean isTeamPrivateMember() {
        return resource.isTeamPrivateMember();
    }

    public String getName() {
        return resource.getName();
    }

    public Object getSessionProperty(QualifiedName key) {
        try {
            return resource.getSessionProperty(key);
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    public int getType() {
        return resource.getType();
    }

    public IPath requestFullPath() {
        return resource.getFullPath();
    }

    public IResource requestResource() {
        return resource;
    }

	public boolean isHidden()
	{
		// TODO Auto-generated method stub
		return false;
	}

}
