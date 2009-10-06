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

import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceDelta;
import org.rubypeople.eclipse.shams.resources.ShamProject;

public class ShamResourceChangeEvent implements IResourceChangeEvent {
    public static ShamResourceChangeEvent forClose(ShamProject project) {
        return new ShamResourceChangeEvent(PRE_CLOSE, project);
    }

    public static ShamResourceChangeEvent forDelete(ShamProject project) {
        return new ShamResourceChangeEvent(PRE_DELETE, project);
    }

    private final int type;
    private final IResourceDelta delta;
    private final IResource resource;

    public ShamResourceChangeEvent(int type, IResourceDelta delta) {
        this.type = type;
        this.delta = delta;
        this.resource = null;
    }

    public ShamResourceChangeEvent(int type, IResource resource) {
        this.type = type;
        this.resource = resource;
        this.delta = null;
    }

    public IMarkerDelta[] findMarkerDeltas(String type, boolean includeSubtypes) {
        return null;
    }

    public int getBuildKind() {
        return 0;
    }

    public IResourceDelta getDelta() {
        return delta;
    }

    public IResource getResource() {
        return resource;
    }

    public Object getSource() {
        return null;
    }

    public int getType() {
        return type;
    }




}
