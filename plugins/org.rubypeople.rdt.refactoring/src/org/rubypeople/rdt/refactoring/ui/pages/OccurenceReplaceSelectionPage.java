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

package org.rubypeople.rdt.refactoring.ui.pages;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.TreeSet;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.jruby.lexer.yacc.ISourcePosition;
import org.rubypeople.rdt.refactoring.core.renamemethod.NodeSelector;
import org.rubypeople.rdt.refactoring.documentprovider.IDocumentProvider;
import org.rubypeople.rdt.refactoring.nodewrapper.INodeWrapper;
import org.rubypeople.rdt.refactoring.ui.RdtCodeViewer;
import org.rubypeople.rdt.refactoring.util.JRubyRefactoringUtils;

public class OccurenceReplaceSelectionPage extends RefactoringWizardPage {

	public static final String NAME = Messages.OccurenceReplaceSelectionPage_SelectCalls;
	private final static RGB highlightColor = new RGB(173, 193, 217);
	
	private NodeSelector selector;
	private Table possibilityTable;
	private IDocumentProvider docProvider;
	
	
	public OccurenceReplaceSelectionPage(NodeSelector selector, IDocumentProvider docProvider) {
		super(NAME);
		this.selector = selector;
		this.docProvider = docProvider;
	}

	public void createControl(Composite parent) {
		
		Composite control = new Composite(parent, SWT.NONE);
		FillLayout controlLayout = new FillLayout();
		controlLayout.spacing  = 10;
		controlLayout.marginHeight = 10;
		controlLayout.marginWidth = 10;
		control.setLayout(controlLayout);
		initPossibilityTable(control);
		initCodeView(control);
		setControl(control);
	}

	private void initCodeView(Composite control) {
		final RdtCodeViewer viewer = RdtCodeViewer.create(control);
		possibilityTable.addSelectionListener(new SelectionListener(){

			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				INodeWrapper currentCall = (INodeWrapper) e.item.getData();
				ISourcePosition pos = currentCall.getWrappedNode().getPosition();
				String file = docProvider.getFileContent(currentCall.getWrappedNode().getPosition().getFile());
				updateCodeViewer(viewer, pos, file);
				
				updateChecks();
			}

			private void updateCodeViewer(final RdtCodeViewer viewer, ISourcePosition pos, String content) {
				int length = pos.getEndOffset() - pos.getStartOffset();
				viewer.setPreviewText(content);
				viewer.setBackgroundColor(pos.getStartOffset(), length, highlightColor);
				viewer.getTextWidget().setSelection(pos.getStartOffset());
				viewer.getTextWidget().showSelection();
			}

			private void updateChecks() {
				ArrayList<INodeWrapper> checkedCalls = new ArrayList<INodeWrapper>();
				for(TableItem currentItem : possibilityTable.getItems()){
					if(currentItem.getChecked()){
						checkedCalls.add((INodeWrapper) currentItem.getData());			
					}
				}
				selector.setSelectedCalls(checkedCalls);
			}
		});
	}

	private void initPossibilityTable(Composite control) {
		possibilityTable = new Table(control, SWT.BORDER | SWT.CHECK);
		
		TreeSet<INodeWrapper> possibleCalls = new TreeSet<INodeWrapper>(new Comparator<INodeWrapper>() {

			public int compare(INodeWrapper left, INodeWrapper right) {
				return left.getWrappedNode().getPosition().getStartOffset() - right.getWrappedNode().getPosition().getStartOffset();
			}});
		
		possibleCalls.addAll(selector.getPossibleCalls());
		
		for(INodeWrapper currentCall : possibleCalls) {
			TableItem currentItem = new TableItem(possibilityTable, SWT.NONE);
			currentItem.setText(getTableCaption(currentCall));
			if(probableCall(currentCall)) {
				currentItem.setChecked(true);
			}
			currentItem.setData(currentCall);
		}
	}

	private String getTableCaption(INodeWrapper currentCall) {
		ISourcePosition pos = currentCall.getWrappedNode().getPosition();
		return pos.getFile() + Messages.OccurenceReplaceSelectionPage_Line + (pos.getStartLine());
	}

	private boolean probableCall(INodeWrapper currentCall) {
		
		for(INodeWrapper targetCall : selector.getSelectedCalls()){
			if(hasSamePosition(currentCall, targetCall)){
				return true;
			}
		}
		return false;
	}

	private boolean hasSamePosition(INodeWrapper currentCall, INodeWrapper targetCall) {
		return JRubyRefactoringUtils.hasSamePosition(currentCall.getWrappedNode(), targetCall.getWrappedNode());
	}
}
