/***** BEGIN LICENSE BLOCK *****
 * Version: CPL 1.0/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Common Public
 * License Version 1.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * Copyright (C) 2006 Lukas Felber <lfelber@hsr.ch>
 * 
 * Alternatively, the contents of this file may be used under the terms of
 * either of the GNU General Public License Version 2 or later (the "GPL"),
 * or the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the CPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the CPL, the GPL or the LGPL.
 ***** END LICENSE BLOCK *****/

package org.rubypeople.rdt.refactoring.editprovider;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.text.edits.TextEdit;
import org.rubypeople.rdt.refactoring.classnodeprovider.ClassNodeProvider;
import org.rubypeople.rdt.refactoring.nodewrapper.ClassNodeWrapper;
import org.rubypeople.rdt.refactoring.ui.TreeContentProvider;

public abstract class EditAndTreeContentProvider extends TreeContentProvider implements IEditProvider {

	private IEditProvider multiEditProvider;
	private Collection<ITreeClass> treeClasses = new ArrayList<ITreeClass>();

	public EditAndTreeContentProvider() {
		multiEditProvider = new MultiEditProvider() {
			@Override
			protected Collection<EditProvider> getEditProviders() {
				return EditAndTreeContentProvider.this.getEditProviders();
			}
		};
	}
	
	@Override
	public Object[] getElements(Object inputElement) {
		return treeClasses.toArray();
	}
	
	protected void initTreeClasses(ClassNodeProvider classNodeProvider) {
		if (classNodeProvider != null) {
			for (ClassNodeWrapper classNode : classNodeProvider.getAllClassNodes()) {
				ITreeClass treeClass = createTreeClass(classNode);
				if (treeClass.hasChildren())
					treeClasses.add(treeClass);
			}
		}
	}
	
	protected ITreeClass createTreeClass(@SuppressWarnings("unused") //$NON-NLS-1$
	ClassNodeWrapper classNode) {
		return null;
	}

	public TextEdit getEdit(String document) {
		return multiEditProvider.getEdit(document);
	}

	public abstract Collection<EditProvider> getEditProviders();
}
