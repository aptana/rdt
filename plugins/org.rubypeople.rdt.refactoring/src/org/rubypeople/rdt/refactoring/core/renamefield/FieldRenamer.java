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
 * Copyright (C) 2006 Mirko Stocker <me@misto.ch>
 * Copyright (C) 2006 Thomas Corbat <tcorbat@hsr.ch>
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

package org.rubypeople.rdt.refactoring.core.renamefield;

import java.util.Collection;

import org.jruby.lexer.yacc.ISourcePosition;
import org.rubypeople.rdt.refactoring.core.renamefield.fielditems.FieldItem;
import org.rubypeople.rdt.refactoring.core.renamemethod.MethodRenamer;
import org.rubypeople.rdt.refactoring.core.renamemethod.RenameMethodConditionChecker;
import org.rubypeople.rdt.refactoring.core.renamemethod.RenameMethodConfig;
import org.rubypeople.rdt.refactoring.editprovider.EditProvider;
import org.rubypeople.rdt.refactoring.editprovider.FileEditProvider;
import org.rubypeople.rdt.refactoring.editprovider.FileMultiEditProvider;
import org.rubypeople.rdt.refactoring.editprovider.IMultiFileEditProvider;
import org.rubypeople.rdt.refactoring.editprovider.MultiFileEditProvider;
import org.rubypeople.rdt.refactoring.nodewrapper.ClassNodeWrapper;
import org.rubypeople.rdt.refactoring.nodewrapper.INodeWrapper;
import org.rubypeople.rdt.refactoring.nodewrapper.MethodNodeWrapper;

public class FieldRenamer implements IMultiFileEditProvider{

	private RenameFieldConfig config;
	
	public FieldRenamer(RenameFieldConfig config) {
		this.config = config;
	}

	
	public Collection<FileMultiEditProvider> getFileEditProviders() {
		
		MultiFileEditProvider fileEdits = new MultiFileEditProvider();
		
		for(INodeWrapper currentItem : config.getSelectedCalls()){
			String file = currentItem.getWrappedNode().getPosition().getFile();
			FieldRenameEditProvider currentRenameProvider = new FieldRenameEditProvider((FieldItem)currentItem, config.getNewName());	
			fileEdits.addEditProvider(new FileEditProvider(file, currentRenameProvider));
		}
		if(config.doRenameAccessorMethods()){
			addAccessorMethodRenamers(fileEdits);
		}

		return fileEdits.getFileEditProviders();	
	}


	
	private void addAccessorMethodRenamers(MultiFileEditProvider fileEdits) {
		ClassNodeWrapper wholeClass = config.getWholeClassNode();
		Collection<MethodNodeWrapper> methods = wholeClass.getMethods();
		for(MethodNodeWrapper currentMethod : methods){
			ISourcePosition methodPosition = currentMethod.getWrappedNode().getPosition();
			RenameMethodConfig methodConfig = new RenameMethodConfig(config.getDocumentProvider(), methodPosition.getStartOffset());
			new RenameMethodConditionChecker(methodConfig);
			methodConfig.setRenameFields(false);
			if(currentMethod.getName().equals(config.getSelectedName())){
				methodConfig.setNewName(config.getNewName());
				MethodRenamer methodRenamer = new MethodRenamer(methodConfig);
				addMethodEditProviders(fileEdits, methodRenamer);
			}
			else if(currentMethod.getName().equals(config.getSelectedName() + '=')){
				methodConfig.setNewName(config.getNewName() + "="); //$NON-NLS-1$
				MethodRenamer methodRenamer = new MethodRenamer(methodConfig);
				addMethodEditProviders(fileEdits, methodRenamer);
			}
		}
	}



	private void addMethodEditProviders(MultiFileEditProvider fileEdits, MethodRenamer methodRenamer) {
		Collection<FileMultiEditProvider> methodEdits = methodRenamer.getFileEditProviders();
		for(FileMultiEditProvider currentEditProvider : methodEdits){
			putEditProvider(fileEdits, currentEditProvider);
		}
	}


	private void putEditProvider(MultiFileEditProvider fileEdits, FileMultiEditProvider multiEditProvider) {
		String file = multiEditProvider.getFileName();
		Collection<EditProvider> editProviders = multiEditProvider.getEditProviders();
		for(EditProvider currentEditProvider : editProviders){
			fileEdits.addEditProvider(new FileEditProvider(file, currentEditProvider));
		}
	}

}
