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

package org.rubypeople.rdt.refactoring.core.inlinelocal;

import java.util.ArrayList;
import java.util.Collection;

import org.rubypeople.rdt.refactoring.core.RefactoringContext;
import org.rubypeople.rdt.refactoring.core.extractmethod.ExtractMethodConditionChecker;
import org.rubypeople.rdt.refactoring.core.extractmethod.ExtractMethodConfig;
import org.rubypeople.rdt.refactoring.core.extractmethod.MethodExtractor;
import org.rubypeople.rdt.refactoring.editprovider.DeleteEditProvider;
import org.rubypeople.rdt.refactoring.editprovider.EditProvider;
import org.rubypeople.rdt.refactoring.editprovider.MultiEditProvider;
import org.rubypeople.rdt.refactoring.nodewrapper.LocalNodeWrapper;
import org.rubypeople.rdt.refactoring.util.JRubyRefactoringUtils;

public class LocalVariableInliner extends MultiEditProvider {

	private InlineLocalConfig config;

	private ExtractMethodConfig extractConfig;

	public LocalVariableInliner(InlineLocalConfig config) {
		this.config = config;
	}

	@Override
	protected Collection<EditProvider> getEditProviders() {
		ArrayList<EditProvider> editProviders = new ArrayList<EditProvider>();
		editProviders.add(createDeleteEdit());

		if (config.isReplaceTempWithQuery()) {
			editProviders.add(extractMethodProvider());
			for (LocalNodeWrapper currentLocalNode : config.getLocalOccurrences()) {
				editProviders.add(replaceWithMethodCallProvider(currentLocalNode));
			}
		} else {
			for (LocalNodeWrapper currentLocalNode : config.getLocalOccurrences()) {
				editProviders.add(replaceWithValueProvider(currentLocalNode));
			}
		}
		return editProviders;
	}

	private EditProvider replaceWithMethodCallProvider(LocalNodeWrapper targetNode) {
		return new MethodCallReplaceProvider(targetNode, extractConfig.getHelper().getMethodCallNode());
	}

	private EditProvider replaceWithValueProvider(LocalNodeWrapper targetNode) {

		boolean addBrackets = JRubyRefactoringUtils.isMathematicalExpression(config.getDefinitionNode().getValueNode());
		return new LocalValueReplaceProvider(targetNode, config, addBrackets);
	}

	private EditProvider extractMethodProvider() {

		int startPos = config.getDefinitionNode().getValueNode().getPosition().getStartOffset();
		int endPos = config.getDefinitionNode().getValueNode().getPosition().getEndOffset();

		extractConfig = new ExtractMethodConfig(config.getDocumentProvider(), new RefactoringContext(startPos, endPos, startPos, "")); //$NON-NLS-1$
		new ExtractMethodConditionChecker(extractConfig);
		MethodExtractor methodExtractor = new MethodExtractor(extractConfig);
		extractConfig.getHelper().setMethodName(config.getNewMethodName());

		return methodExtractor.getDefEdit();
	}

	private EditProvider createDeleteEdit() {
		return new DeleteEditProvider(config.getDefinitionNode().getWrappedNode());
	}

	public int getOccurrencesCount() {
		return config.getLocalOccurrences() == null ? 0 : config.getLocalOccurrences().size();
	}

	public String getSelectedItemName() {
		return config.getSelectedItemName();
	}

	public InlineLocalConfig getConfig() {
		return config;
	}
}
