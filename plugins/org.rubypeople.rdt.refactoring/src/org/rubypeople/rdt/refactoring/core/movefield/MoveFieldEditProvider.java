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
 * Copyright (C) 2006 Mirko Stocker <me@misto.ch>
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

package org.rubypeople.rdt.refactoring.core.movefield;

import java.util.Collection;

import org.rubypeople.rdt.refactoring.core.renamefield.FieldRenamer;
import org.rubypeople.rdt.refactoring.core.renamefield.RenameFieldConditionChecker;
import org.rubypeople.rdt.refactoring.core.renamefield.RenameFieldConfig;
import org.rubypeople.rdt.refactoring.documentprovider.DocumentWithIncluding;
import org.rubypeople.rdt.refactoring.editprovider.EditProvider;
import org.rubypeople.rdt.refactoring.editprovider.FileEditProvider;
import org.rubypeople.rdt.refactoring.editprovider.FileMultiEditProvider;
import org.rubypeople.rdt.refactoring.editprovider.IMultiFileEditProvider;
import org.rubypeople.rdt.refactoring.editprovider.MultiFileEditProvider;

public class MoveFieldEditProvider implements IMultiFileEditProvider {

	private final MoveFieldConfig config;

	public MoveFieldEditProvider(MoveFieldConfig config) {
		this.config = config;
	}

	public Collection<FileMultiEditProvider> getFileEditProviders() {
		MultiFileEditProvider providers = new MultiFileEditProvider();
	 
		addTargetAccessorGenerator(providers);
		addSourceAccessorGenerator(providers);
		addFieldRenamers(providers);
		
		return providers.getFileEditProviders();
	}

	private void addFieldRenamers(MultiFileEditProvider providers) {
		RenameFieldConfig renameFieldConfig = new RenameFieldConfig(new DocumentWithIncluding(config.getDocumentProvider()), config.getPos());
		
		new RenameFieldConditionChecker(renameFieldConfig);
		renameFieldConfig.setDoRenameAccessorMethods(false);
		renameFieldConfig.setDoRenameAccessors(false);
		renameFieldConfig.setNewName(config.getTargetReference() + '.' + config.getSelectedFieldName());
		
		FieldRenamer renamer = new FieldRenamer(renameFieldConfig);
		for(FileMultiEditProvider fileMultiEditProvider : renamer.getFileEditProviders()) {
			for (EditProvider editProvider : fileMultiEditProvider.getEditProviders()) {
				providers.addEditProvider(new FileEditProvider(fileMultiEditProvider.getFileName(), editProvider));
			}
		}
	}

	private void addTargetAccessorGenerator(MultiFileEditProvider providers) {
		GenerateAccessorsAtTarget generateAccessors = new GenerateAccessorsAtTarget(config.getDocumentProvider(), config.getTargetClass(), config.getSelectedFieldName());
		providers.addEditProvider(new FileEditProvider(generateAccessors.getFileName(), generateAccessors.getEditProvider()));
	}
	
	private void addSourceAccessorGenerator(MultiFileEditProvider providers) {
		GenerateAccessorAtSource accessorAtSource = new GenerateAccessorAtSource(config);

		for(EditProvider edit : accessorAtSource.getEditProviders()) {
			providers.addEditProvider(new FileEditProvider(config.getDocumentProvider().getActiveFileName(), edit));
		}
	}
}
