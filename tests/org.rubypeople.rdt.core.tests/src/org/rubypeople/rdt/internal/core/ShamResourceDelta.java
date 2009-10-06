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

package org.rubypeople.rdt.internal.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

public class ShamResourceDelta implements IResourceDelta {

    private List children = new ArrayList();
    private IResource resource;
    private int kind;
    private int flags;

    public void accept(IResourceDeltaVisitor visitor) throws CoreException {
        if (visitor.visit(this)) {
            for (Iterator iter = children.iterator(); iter.hasNext();) {
                IResourceDelta delta = (IResourceDelta) iter.next();
                delta.accept(visitor);
            }
        }
    }

    public void accept(IResourceDeltaVisitor visitor, boolean includePhantoms) throws CoreException {
    }

    public void accept(IResourceDeltaVisitor visitor, int memberFlags) throws CoreException {
    }

    public IResourceDelta findMember(IPath path) {
        return null;
    }

    public IResourceDelta[] getAffectedChildren() {
        return (IResourceDelta[]) children.toArray(new IResourceDelta[0]);
    }

    public IResourceDelta[] getAffectedChildren(int kindMask) {
        return null;
    }

    public IResourceDelta[] getAffectedChildren(int kindMask, int memberFlags) {
        return null;
    }

    public int getFlags() {
        return flags;
    }

    public IPath getFullPath() {
        return resource.getFullPath();
    }

    public int getKind() {
        return kind;
    }

    public IMarkerDelta[] getMarkerDeltas() {
        return null;
    }

    public IPath getMovedFromPath() {
        return null;
    }

    public IPath getMovedToPath() {
        return null;
    }

    public IPath getProjectRelativePath() {
        return null;
    }

    public IResource getResource() {
        return resource;
    }

    public Object getAdapter(Class adapter) {
        return null;
    }

    public void setResource(IResource resource) {
        this.resource = resource;
    }

    public void addChildren(IResourceDelta delta) {
        children.add(delta);
    }

    public void setKind(int kind) {
        this.kind = kind;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }

}