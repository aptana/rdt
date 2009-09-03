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

import org.jruby.ast.BlockNode;
import org.jruby.ast.InstAsgnNode;
import org.jruby.ast.InstVarNode;
import org.jruby.ast.types.INameNode;
import org.rubypeople.rdt.refactoring.core.NodeProvider;
import org.rubypeople.rdt.refactoring.core.encapsulatefield.EncapsulateFieldConditionChecker;
import org.rubypeople.rdt.refactoring.core.encapsulatefield.EncapsulateFieldConfig;
import org.rubypeople.rdt.refactoring.core.encapsulatefield.FieldEncapsulator;
import org.rubypeople.rdt.refactoring.editprovider.EditProvider;
import org.rubypeople.rdt.refactoring.nodewrapper.VisibilityNodeWrapper.METHOD_VISIBILITY;

public class GenerateAccessorAtSource {
	
	private class CustomTargetFieldEncapsulator extends FieldEncapsulator {
		public CustomTargetFieldEncapsulator(EncapsulateFieldConfig fieldConfig) {
			super(fieldConfig);
		}

		@Override
		protected BlockNode createGetterOrSetter(boolean writer, METHOD_VISIBILITY visibility) {
			BlockNode accessor = super.createGetterOrSetter(writer, visibility);
			INameNode targetNode = (INameNode) NodeProvider.getSubNodes(accessor, InstVarNode.class, InstAsgnNode.class).iterator().next();
			if(targetNode instanceof InstVarNode) {
				((InstVarNode) targetNode).setName(config.getTargetReference() + '.' + targetNode.getName().substring(1));
			} else {
				((InstAsgnNode)targetNode).setName(config.getTargetReference() + '.' + targetNode.getName().substring(1));
			}
			return accessor;
		}
	}

	private final MoveFieldConfig config;
	
	public GenerateAccessorAtSource(MoveFieldConfig config) {
		this.config = config;
	}
	
	public Collection<EditProvider> getEditProviders() {
		EncapsulateFieldConfig fieldConfig = new EncapsulateFieldConfig(config.getDocumentProvider(), config.getPos());
	
		new EncapsulateFieldConditionChecker(fieldConfig);
		
		FieldEncapsulator fieldEncapsulator = new CustomTargetFieldEncapsulator(fieldConfig);
		return fieldEncapsulator.getEditProviders();
	}
}
