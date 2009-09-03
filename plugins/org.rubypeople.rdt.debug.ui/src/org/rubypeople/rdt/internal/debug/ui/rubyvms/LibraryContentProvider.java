/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.rubypeople.rdt.internal.debug.ui.rubyvms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;

public class LibraryContentProvider implements ITreeContentProvider {
	
	private Viewer fViewer;

	private HashMap fChildren= new HashMap();

	private LibraryStandin[] fLibraries= new LibraryStandin[0];

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		fViewer = viewer;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements(Object inputElement) {
		return fLibraries;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof LibraryStandin) {
			LibraryStandin standin= (LibraryStandin) parentElement;
			Object[] children= (Object[])fChildren.get(standin);
			return children;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
	 */
	public Object getParent(Object element) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
	 */
	public boolean hasChildren(Object element) {
		return false;
	}

	public void setLibraries(IPath[] libs) {
		fLibraries = new LibraryStandin[libs.length];
		for (int i = 0; i < libs.length; i++) {
			fLibraries[i] = new LibraryStandin(libs[i]);
		}
		fViewer.refresh();
	}

	public IPath[] getLibraries() {
		IPath[] locations = new IPath[fLibraries.length];
		for (int i = 0; i < locations.length; i++) {
			locations[i] = fLibraries[i].toLibraryLocation();
		}
		return locations;
	}

	/**
	 * Returns the list of libraries in the given selection. SubElements
	 * are replaced by their parent libraries.
	 */
	private Set getSelectedLibraries(IStructuredSelection selection) {
		Set libraries= new HashSet();
		for (Iterator iter= selection.iterator(); iter.hasNext();) {
			Object element= iter.next();
			if (element instanceof LibraryStandin) {
				libraries.add(element);
			}
		}
		return libraries;
	}

	/**
	 * Move the libraries of the given selection up.
	 */
	public void up(IStructuredSelection selection) {
		Set libraries= getSelectedLibraries(selection);
		for (int i= 0; i < fLibraries.length - 1; i++) {
			if (libraries.contains(fLibraries[i + 1])) {
				LibraryStandin temp= fLibraries[i];
				fLibraries[i]= fLibraries[i + 1];
				fLibraries[i + 1]= temp;
			}
		}
		fViewer.refresh();
		fViewer.setSelection(selection);
	}

	/**
	 * Move the libraries of the given selection down.
	 */
	public void down(IStructuredSelection selection) {
		Set libraries= getSelectedLibraries(selection);
		for (int i= fLibraries.length - 1; i > 0; i--) {
			if (libraries.contains(fLibraries[i - 1])) {
				LibraryStandin temp= fLibraries[i];
				fLibraries[i]= fLibraries[i - 1];
				fLibraries[i - 1]= temp;
			}
		}
		fViewer.refresh();
		fViewer.setSelection(selection);
	}

	/**
	 * Remove the libraries contained in the given selection.
	 */
	public void remove(IStructuredSelection selection) {
		List newLibraries = new ArrayList();
		for (int i = 0; i < fLibraries.length; i++) {
			newLibraries.add(fLibraries[i]);
		}
		Iterator iterator = selection.iterator();
		while (iterator.hasNext()) {
			Object element = iterator.next();
			if (element instanceof LibraryStandin) {
				newLibraries.remove(element);
			}
		}
		fLibraries= (LibraryStandin[]) newLibraries.toArray(new LibraryStandin[newLibraries.size()]);
		fViewer.refresh();
	}

	/**
	 * Add the given libraries before the selection, or after the existing libraries
	 * if the selection is empty.
	 */
	public void add(IPath[] libs, IStructuredSelection selection) {
		List newLibraries = new ArrayList(fLibraries.length + libs.length);
		for (int i = 0; i < fLibraries.length; i++) {
			newLibraries.add(fLibraries[i]);
		}
		List toAdd = new ArrayList(libs.length);
		for (int i = 0; i < libs.length; i++) {
			toAdd.add(new LibraryStandin(libs[i]));
		}
		if (selection.isEmpty()) {
			newLibraries.addAll(toAdd);
		} else {
			Object element= selection.getFirstElement();
			LibraryStandin firstLib= (LibraryStandin) element;
			int index = newLibraries.indexOf(firstLib);
			newLibraries.addAll(index, toAdd);
		}
		fLibraries= (LibraryStandin[]) newLibraries.toArray(new LibraryStandin[newLibraries.size()]);
		fViewer.refresh();
		fViewer.setSelection(new StructuredSelection(libs), true);
	}
	
	/**
	 * Returns the standin libraries being edited.
	 * 
	 * @return standins
	 */
	LibraryStandin[] getStandins() {
		return fLibraries;
	}
	
}