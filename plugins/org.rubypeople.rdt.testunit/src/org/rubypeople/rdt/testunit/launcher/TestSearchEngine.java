/*
 * Author: C.Williams
 * 
 * Copyright (c) 2004 RubyPeople.
 * 
 * This file is part of the Ruby Development Tools (RDT) plugin for eclipse. You
 * can get copy of the GPL along with further information about RubyPeople and
 * third party software bundled with RDT in the file
 * org.rubypeople.rdt.core_x.x.x/RDT.license or otherwise at
 * http://www.rubypeople.org/RDT.license.
 * 
 * RDT is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * RDT is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * RDT; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
 */
package org.rubypeople.rdt.testunit.launcher;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.rubypeople.rdt.core.IRubyElement;
import org.rubypeople.rdt.core.IRubyScript;
import org.rubypeople.rdt.core.IType;
import org.rubypeople.rdt.core.RubyCore;
import org.rubypeople.rdt.core.RubyModelException;
import org.rubypeople.rdt.internal.ui.RubyPlugin;
import org.rubypeople.rdt.internal.ui.util.RubyElementVisitor;

/**
 * @author Chris
 * 
 */
public class TestSearchEngine {
	// FIXME Hook this up to the real SearchEngine and search for subtypes of Test::Unit::TestCase
    public static IType[] findTests(final IFile file) {
        IRubyScript script = RubyCore.create(file);
        try {
			script.reconcile();
			IRubyElement[] children = script.getChildren();
			List<IType> types = new ArrayList<IType>();
			for (int i = 0; i < children.length; i++) {
			    if (children[i].isType(IRubyElement.TYPE)) types.add((IType)children[i]);
			}
			IType[] array = new IType[types.size()];
			System.arraycopy(types.toArray(), 0, array, 0, types.size());
			return array;
		} catch (RubyModelException e) {
			RubyPlugin.log(e);
		}
		return new IType[0];
    }

    /**
     * @param rubyProject
     */
    public static IType[] findTests(IProject rubyProject) {
        if (rubyProject == null) { return new IType[0]; }
        try {
            List<IType> tests = new ArrayList<IType>();
            RubyElementVisitor visitor = new RubyElementVisitor();
            rubyProject.accept(visitor);
            Object[] rubyFiles = visitor.getCollectedRubyFiles();
            for (int i = 0; i < rubyFiles.length; i++) {
                IFile rubyFile = (IFile) rubyFiles[i];
                IType[] elements = TestSearchEngine.findTests(rubyFile);
                for (int j = 0; j < elements.length; j++) {
                    tests.add(elements[j]);
                }
            }
            Object[] listArray = tests.toArray();
            IType[] array = new IType[tests.size()];
            System.arraycopy(listArray, 0, array, 0, listArray.length);
            return array;
        } catch (CoreException e) {
            e.printStackTrace();
        }
        return new IType[0];
    }

}