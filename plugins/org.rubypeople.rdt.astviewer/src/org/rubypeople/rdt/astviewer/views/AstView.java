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

package org.rubypeople.rdt.astviewer.views;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.ui.part.DrillDownAdapter;
import org.eclipse.ui.part.ViewPart;
import org.jruby.ast.Node;
import org.rubypeople.rdt.astviewer.Activator;
import org.rubypeople.rdt.internal.ui.rubyeditor.RubyEditor;

public class AstView extends ViewPart {
	private TreeViewer viewer;
	private DrillDownAdapter drillDownAdapter;
	private Action refreshAction;
	private Action dumpToConsoleAction;
	private Action dumpJRubyTestFormatAction;
	private Action doubleClickAction;
	private Action clickAction;
	private ViewContentProvider viewContentProvider;
	private SashForm sashForm;
	private SourceViewer detailsViewer;

	public void createPartControl(Composite parent) {
		sashForm = new SashForm(parent, SWT.NONE);
		sashForm.setOrientation(SWT.VERTICAL);
		PatternFilter patternFilter = new PatternFilter();
	    final FilteredTree filter = new FilteredTree(sashForm, SWT.MULTI
	            | SWT.H_SCROLL | SWT.V_SCROLL, patternFilter);
	    
		viewer = filter.getViewer();
		drillDownAdapter = new DrillDownAdapter(viewer);
		viewContentProvider = new ViewContentProvider(getViewSite());
		viewer.setContentProvider(viewContentProvider);
		viewer.setLabelProvider(new ViewLabelProvider());

		viewer.setInput(getViewSite());
		makeActions();
		hookContextMenu();
		hookClickAction();
		hookDoubleClickAction();
		contributeToActionBars();
		//setupListeners();
		viewer.setAutoExpandLevel(TreeViewer.ALL_LEVELS);

		detailsViewer= new SourceViewer(sashForm, null, SWT.V_SCROLL | SWT.H_SCROLL);
		detailsViewer.setEditable(false);
		detailsViewer.setDocument(new Document());
		Control control = detailsViewer.getControl();
		GridData gd = new GridData(GridData.FILL_BOTH);
		control.setLayoutData(gd);
		
		sashForm.setWeights(new int[]{90, 10});
	}
	
//  Tried to automatically update the view, but could not figure out yet how to do it properly.
//	private void setupListeners(){
//		
//		final IPartListener partListener = new IPartListener(){
//			public void partActivated(IWorkbenchPart part) {
//				partWasActivated();
//			}
//			public void partBroughtToTop(IWorkbenchPart part) {}
//			public void partClosed(IWorkbenchPart part) {}
//			public void partDeactivated(IWorkbenchPart part) {}
//			public void partOpened(IWorkbenchPart part) {
//				partWasActivated();
//			}
//		};
//		
//		PlatformUI.getWorkbench().getActiveWorkbenchWindow().addPageListener(new IPageListener(){
//
//			public void pageActivated(final IWorkbenchPage page) {
//				page.addPartListener(partListener);
//			}
//
//			public void pageClosed(IWorkbenchPage page) {}
//
//			public void pageOpened(IWorkbenchPage page) {
//				page.addPartListener(partListener);
//		}});
//	}
//	
//	private void partWasActivated() {
//		if(viewContentProvider.updateContent()) {
//			viewer.setInput(getViewSite());
//			viewer.setAutoExpandLevel(TreeViewer.ALL_LEVELS);
//		}
//	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				AstView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(refreshAction);
		manager.add(dumpToConsoleAction);
		manager.add(dumpJRubyTestFormatAction);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(refreshAction);
		manager.add(dumpToConsoleAction);
		//manager.add(dumpJRubyTestFormatAction);
		drillDownAdapter.addNavigationActions(manager);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(refreshAction);
		manager.add(dumpToConsoleAction);
		manager.add(dumpJRubyTestFormatAction);
		drillDownAdapter.addNavigationActions(manager);
	}
	

	
	private void makeActions() {
		makeRefreshAction();
		makeDumpAction();
		makeDumpJRubyFormatAction();
	}

	private void makeDumpJRubyFormatAction() {
		dumpJRubyTestFormatAction = new Action() {
			public void run() {

				AstViewConsole.print(AstUtility.nodeListJRubyFormat(AstUtility.findAllNodes(getRootNode())));
				AstViewConsole.print("test_tree(list, <<END)");
				AstViewConsole.print(textEditorContentToString().toString().trim());
				AstViewConsole.print("END");
			}

			private StringBuilder textEditorContentToString() {
				IEditorInput editorInput = getEditor().getEditorInput();
				IFile aFile = null;
				if(editorInput instanceof IFileEditorInput){
					aFile = ((IFileEditorInput)editorInput).getFile();
				}
				
				BufferedReader br = null;
				StringBuilder sb = new StringBuilder();
				try {
					br = new BufferedReader(new InputStreamReader(aFile.getContents()));
					String line = null;

					while ((line = br.readLine()) != null) {
						sb.append(line + "\n");
					}
					br.close();

				} catch (CoreException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return sb;
			}
		};
		
		dumpJRubyTestFormatAction.setText("Dump in JRuby-Test");
		dumpJRubyTestFormatAction.setToolTipText("Dumps node and all child-nodes to the console, formatted like as in JRuby's testPositions.rb");
		dumpJRubyTestFormatAction.setImageDescriptor(Activator.getImageDescriptor("icons/dump_jruby.gif"));
	}
	
	private void makeDumpAction() {
		dumpToConsoleAction = new Action() {
			public void run() {
				AstViewConsole.print(AstUtility.nodeList(AstUtility.findAllNodes(getSelectedNode())));
			}
		};
		
		dumpToConsoleAction.setText("Dump Node");
		dumpToConsoleAction.setToolTipText("Dumps node and all child-nodes to the console.");
		dumpToConsoleAction.setImageDescriptor(Activator.getImageDescriptor("icons/dump.gif"));
	}

	private void makeRefreshAction() {
		refreshAction = new Action() {
			public void run() {
				viewContentProvider.forceUpdateContent();
				viewer.setInput(getViewSite());
				viewer.setAutoExpandLevel(TreeViewer.ALL_LEVELS);
			}
		};
		
		refreshAction.setText("Refresh the AST View");
		refreshAction.setToolTipText("Performs a complete refresh over the whole AST.");
		refreshAction.setImageDescriptor(Activator.getImageDescriptor("icons/refresh.gif"));
	}
	
	private void setSelection(RubyEditor editor, Node n) {
		if (n == null || n.getPosition() == null)
			return;
		editor.selectAndReveal(n.getPosition().getStartOffset(), n.getPosition().getEndOffset() - n.getPosition().getStartOffset());
	}
	
	private void hookClickAction() {
		
		clickAction = new Action(){
			public void run() {
				detailsViewer.getDocument().set(AstUtility.formatedPosition(getSelectedNode()));
			}
		};
		
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(@SuppressWarnings("unused")
			SelectionChangedEvent event) {
				clickAction.run();
			}
		});
	}
	
	private Node getSelectedNode() {
		TreeItem[] selection = viewer.getTree().getSelection();
		if(selection.length <= 0)
			return null;
		return ((TreeObject) selection[0].getData()).getNode();
	}	
	
	private Node getRootNode() {
		TreeItem root = viewer.getTree().getItem(0);
		return ((TreeObject) root.getData()).getNode();
	}
	
	private RubyEditor getEditor() {
		return (RubyEditor) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
	}

	private void hookDoubleClickAction() {
		
		doubleClickAction = new Action(){
			public void run() {
				setSelection(getEditor(), getSelectedNode());
			}
		};
		
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(@SuppressWarnings("unused")
			DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}

	public void setFocus() {
		viewer.getControl().setFocus();
	}
}