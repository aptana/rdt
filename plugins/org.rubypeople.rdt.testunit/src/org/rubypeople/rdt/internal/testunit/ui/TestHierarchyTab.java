/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sebastian Davids - sdavids@gmx.de bugs 26754, 41228
 *******************************************************************************/
package org.rubypeople.rdt.internal.testunit.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Vector;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.rubypeople.rdt.core.RubyConventions;
import org.rubypeople.rdt.testunit.ITestRunListener;

/*
 * A view that shows the contents of a test suite as a tree.
 */
public class TestHierarchyTab extends TestRunTab implements IMenuListener {

	private Tree fTree;

	private TreeItem fCachedParent;
	private TreeItem[] fCachedItems;

	private TreeItem fLastParent;
	private List<TreeItem> fExecutionPath;

	private boolean fMoveSelection = false;

	/**
	 * Helper used to resurrect test hierarchy
	 */
	private static class SuiteInfo {

		public int fTestCount;
		public TreeItem fTreeItem;

		public SuiteInfo(TreeItem treeItem, int testCount) {
			fTreeItem = treeItem;
			fTestCount = testCount;
		}
	}

	/**
	 * Vector of SuiteInfo items
	 */
	private Vector<SuiteInfo> fSuiteInfos = new Vector<SuiteInfo>();
	/**
	 * Maps test Ids to TreeItems.
	 */
	private Map<String, TreeItem> fTreeItemMap = new HashMap<String, TreeItem>();

	private TestUnitView fTestRunnerPart;

	private final Image fOkIcon = TestUnitView.createImage("obj16/testok.gif"); //$NON-NLS-1$
	private final Image fErrorIcon = TestUnitView.createImage("obj16/testerr.gif"); //$NON-NLS-1$
	private final Image fFailureIcon = TestUnitView.createImage("obj16/testfail.gif"); //$NON-NLS-1$
	private final Image fHierarchyIcon = TestUnitView.createImage("obj16/testhier.gif"); //$NON-NLS-1$
	private final Image fSuiteIcon = TestUnitView.createImage("obj16/tsuite.gif"); //$NON-NLS-1$
	private final Image fSuiteErrorIcon = TestUnitView.createImage("obj16/tsuiteerror.gif"); //$NON-NLS-1$
	private final Image fSuiteFailIcon = TestUnitView.createImage("obj16/tsuitefail.gif"); //$NON-NLS-1$
	private final Image fTestIcon = TestUnitView.createImage("obj16/test.gif"); //$NON-NLS-1$
	private final Image fTestRunningIcon = TestUnitView.createImage("obj16/testrun.gif"); //$NON-NLS-1$
	private final Image fSuiteRunningIcon = TestUnitView.createImage("obj16/tsuiterun.gif"); //$NON-NLS-1$

	private class ExpandAllAction extends Action {

		public ExpandAllAction() {
			setText(TestUnitMessages.ExpandAllAction_text);
			setToolTipText(TestUnitMessages.ExpandAllAction_tooltip);
		}

		public void run() {
			expandAll();
		}
	}

	public TestHierarchyTab() {}

	public void createTabControl(CTabFolder tabFolder, Clipboard clipboard, TestUnitView runner) {
		fTestRunnerPart = runner;

		CTabItem hierarchyTab = new CTabItem(tabFolder, SWT.NONE);
		hierarchyTab.setText(getName());
		hierarchyTab.setImage(fHierarchyIcon);

		Composite testTreePanel = new Composite(tabFolder, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		testTreePanel.setLayout(gridLayout);

		GridData gridData = new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
		testTreePanel.setLayoutData(gridData);

		hierarchyTab.setControl(testTreePanel);
		hierarchyTab.setToolTipText(TestUnitMessages.HierarchyRunView_tab_tooltip);

		fTree = new Tree(testTreePanel, SWT.V_SCROLL | SWT.SINGLE);
		gridData = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
		fTree.setLayoutData(gridData);

		initMenu();
		addListeners();
	}

	void disposeIcons() {
		fErrorIcon.dispose();
		fFailureIcon.dispose();
		fOkIcon.dispose();
		fHierarchyIcon.dispose();
		fTestIcon.dispose();
		fTestRunningIcon.dispose();
		fSuiteRunningIcon.dispose();
		fSuiteIcon.dispose();
		fSuiteErrorIcon.dispose();
		fSuiteFailIcon.dispose();
	}

	private void initMenu() {
		MenuManager menuMgr = new MenuManager();
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(this);
		Menu menu = menuMgr.createContextMenu(fTree);
		fTree.setMenu(menu);
	}

	private String getTestMethod() {
		return getTestInfo().getTestMethodName();
	}

	private TestRunInfo getTestInfo() {
		TreeItem[] treeItems = fTree.getSelection();
		if (treeItems.length == 0) return null;
		return ((TestRunInfo) treeItems[0].getData());
	}

	private boolean isSuiteSelected() {
		TreeItem[] treeItems = fTree.getSelection();
		if (treeItems.length != 1) return false;
		return treeItems[0].getItemCount() > 0;
	}

	private String getClassName() {
		return getTestInfo().getClassName();
	}

	public String getSelectedTestId() {
		TestRunInfo testInfo = getTestInfo();
		if (testInfo == null) return null;
		return testInfo.getTestId();
	}

	public String getName() {
		return TestUnitMessages.HierarchyRunView_tab_title;
	}

	public void setSelectedTest(String testId) {
		TreeItem treeItem = findTreeItem(testId);
		if (treeItem != null) fTree.setSelection(new TreeItem[] { treeItem});
	}

	public void startTest(String testId) {
		TreeItem treeItem = findTreeItem(testId);
		if (treeItem == null) return;
		TreeItem parent = treeItem.getParentItem();
		if (fLastParent != parent) {
			updatePath(parent);
			fLastParent = parent;
		}
		setCurrentItem(treeItem);
	}

	private void updatePath(TreeItem parent) {
		List<TreeItem> newPath = new ArrayList<TreeItem>();
		while (parent != null) {
			newPath.add(parent);
			parent = parent.getParentItem();
		}
		Collections.reverse(newPath);

		// common path
		ListIterator<TreeItem> old = fExecutionPath.listIterator();
		ListIterator<TreeItem> np = newPath.listIterator();
		int c = 0;
		while (old.hasNext() && np.hasNext()) {
			if (old.next() != np.next()) break;
			c++;
		}
		// clear old path
		for (ListIterator<TreeItem> iter = fExecutionPath.listIterator(c); iter.hasNext();)
			refreshItem(iter.next(), false);
		// update new path
		for (ListIterator<TreeItem> iter = newPath.listIterator(c); iter.hasNext();)
			refreshItem(iter.next(), true);
		fExecutionPath = newPath;
	}

	private void refreshItem(TreeItem item, boolean onPath) {
		if (onPath)
			item.setImage(fSuiteRunningIcon);
		else {
			TestRunInfo info = getTestRunInfo(item);
			switch (info.getStatus()) {
			case ITestRunListener.STATUS_ERROR:
				item.setImage(fSuiteErrorIcon);
				break;
			case ITestRunListener.STATUS_FAILURE:
				item.setImage(fSuiteFailIcon);
				break;
			default:
				item.setImage(fSuiteIcon);
			}
		}
	}

	private void setCurrentItem(TreeItem treeItem) {
		treeItem.setImage(fTestRunningIcon);

		TreeItem parent = treeItem.getParentItem();
		if (fTestRunnerPart.isAutoScroll()) {
			fTree.showItem(treeItem);
			while (parent != null) {
				if (parent.getExpanded()) break;
				parent.setExpanded(true);
				parent = parent.getParentItem();
			}
		}
	}

	public void endTest(String testId) {
		TreeItem treeItem = findTreeItem(testId);
		if (treeItem == null) return;

		TestRunInfo testInfo = fTestRunnerPart.getTestInfo(testId);
		// fix for 61709 NPE in JUnit view plus strange behavior
		// the testInfo map can already be destroyed at this point
		if (testInfo == null) return;

		updateItem(treeItem, testInfo);

		if (fTestRunnerPart.isAutoScroll()) {
			fTree.showItem(treeItem);
			cacheItems(treeItem);
			collapsePassedTests(treeItem);
		}
	}

	private void cacheItems(TreeItem treeItem) {
		TreeItem parent = treeItem.getParentItem();
		if (parent == fCachedParent) return;
		fCachedItems = parent.getItems();
		fCachedParent = parent;
	}

	private void collapsePassedTests(TreeItem treeItem) {
		TreeItem parent = treeItem.getParentItem();
		if (parent != null) {
			TreeItem[] items = null;
			if (parent == fCachedParent)
				items = fCachedItems;
			else
				items = parent.getItems();

			if (isLast(treeItem, items)) {
				boolean ok = true;
				for (int i = 0; i < items.length; i++) {
					if (isFailure(items[i])) {
						ok = false;
						break;
					}
				}
				if (ok) {
					parent.setExpanded(false);
					collapsePassedTests(parent);
				}
			}
		}
	}

	private boolean isLast(TreeItem treeItem, TreeItem[] items) {
		return items[items.length - 1] == treeItem;
	}

	private void updateItem(TreeItem treeItem, TestRunInfo testInfo) {
		treeItem.setData(testInfo);
		if (testInfo.getStatus() == ITestRunListener.STATUS_OK) {
			treeItem.setImage(fOkIcon);
			return;
		}
		if (testInfo.getStatus() == ITestRunListener.STATUS_FAILURE)
			treeItem.setImage(fFailureIcon);
		else if (testInfo.getStatus() == ITestRunListener.STATUS_ERROR) treeItem.setImage(fErrorIcon);
		propagateStatus(treeItem, testInfo.getStatus());
	}

	private void propagateStatus(TreeItem item, int status) {
		TreeItem parent = item.getParentItem();
		TestRunInfo testRunInfo = getTestRunInfo(item);

		if (parent == null) return;
		TestRunInfo parentInfo = getTestRunInfo(parent);
		int parentStatus = parentInfo.getStatus();

		if (status == ITestRunListener.STATUS_FAILURE) {
			if (parentStatus == ITestRunListener.STATUS_ERROR || parentStatus == ITestRunListener.STATUS_FAILURE) return;
			parentInfo.setStatus(ITestRunListener.STATUS_FAILURE);
			testRunInfo.setStatus(ITestRunListener.STATUS_FAILURE);
		} else {
			if (parentStatus == ITestRunListener.STATUS_ERROR) return;
			parentInfo.setStatus(ITestRunListener.STATUS_ERROR);
			testRunInfo.setStatus(ITestRunListener.STATUS_ERROR);
		}
		propagateStatus(parent, status);
	}

	private TestRunInfo getTestRunInfo(TreeItem item) {
		return (TestRunInfo) item.getData();
	}

	public void activate() {
		fMoveSelection = false;
		testSelected();
	}

	public void setFocus() {
		fTree.setFocus();
	}

	public void aboutToStart() {
		fTree.removeAll();
		fSuiteInfos.removeAllElements();
		fTreeItemMap = new HashMap<String, TreeItem>();
		fCachedParent = null;
		fCachedItems = null;
		fMoveSelection = false;
		fExecutionPath = new ArrayList<TreeItem>();
	}

	private void testSelected() {
		fTestRunnerPart.handleTestSelected(getSelectedTestId());
	}

	private void addListeners() {
		fTree.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				activate();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				activate();
			}
		});

		fTree.addDisposeListener(new DisposeListener() {

			public void widgetDisposed(DisposeEvent e) {
				disposeIcons();
			}
		});

		fTree.addMouseListener(new MouseAdapter() {

			public void mouseDoubleClick(MouseEvent e) {
				handleDoubleClick(e);
			}
		});
	}

	void handleDoubleClick(MouseEvent e) {
			TestRunInfo testInfo= getTestInfo();
			
			if (testInfo == null)
				return;
				
			IAction action = null;
			           
			if (isSuiteSelected())
				action= new OpenTestAction(fTestRunnerPart, getClassName());
			else
			    action= new OpenTestAction(fTestRunnerPart, getClassName(), getTestMethod());
	
			if (action != null && action.isEnabled())
				action.run();
	}

	public void menuAboutToShow(IMenuManager manager) {
		if (fTree.getSelectionCount() > 0) {
			if (isSuiteSelected()) {
				manager.add(new OpenTestAction(fTestRunnerPart, getClassName()));
				manager.add(new Separator());
				if (testClassExists(getClassName()) && !fTestRunnerPart.lastLaunchIsKeptAlive()) {
					manager.add(new RerunAction(fTestRunnerPart, getSelectedTestId(), getClassName(), null, ILaunchManager.RUN_MODE));
                    manager.add(new RerunAction(fTestRunnerPart, getSelectedTestId(), getClassName(), null, ILaunchManager.DEBUG_MODE));
				}
			} else {
			    manager.add(new OpenTestAction(fTestRunnerPart, getClassName(), getTestMethod(), true));
				manager.add(new Separator());
				manager.add(new RerunAction(fTestRunnerPart, getSelectedTestId(), getClassName(), getTestMethod(), ILaunchManager.RUN_MODE));
                manager.add(new RerunAction(fTestRunnerPart, getSelectedTestId(), getClassName(), getTestMethod(), ILaunchManager.DEBUG_MODE));                
			}
			manager.add(new Separator());
			manager.add(new ExpandAllAction());
		}
	}

	private boolean testClassExists(String className) {
		IStatus status = RubyConventions.validateRubyTypeName(className);
        return status.isOK();
	}

	public void newTreeEntry(String treeEntry) {
		// format: testId","testName","isSuite","testcount"
		String[] parts = treeEntry.split(",");
		String testId = parts[0];
		String testName = parts[1];
		Boolean isSuite = false;
		int testCount = 1;
		try
		{
			isSuite = Boolean.parseBoolean(parts[2]);
			testCount = Integer.parseInt(parts[3]);
		}
		catch (NumberFormatException e)
		{
			TestunitPlugin.log("Was unable to parse test count from full input: " + treeEntry);
			TestunitPlugin.log(e);
		}
		TestRunInfo testInfo = new TestRunInfo(testId, testName);

		TreeItem treeItem;
		while ((fSuiteInfos.size() > 0) && ((fSuiteInfos.lastElement()).fTestCount == 0)) {
			fSuiteInfos.removeElementAt(fSuiteInfos.size() - 1);
		}

		if (fSuiteInfos.size() == 0) {
			treeItem = new TreeItem(fTree, SWT.NONE);
			treeItem.setImage(fSuiteIcon);
			fSuiteInfos.addElement(new SuiteInfo(treeItem, testCount));
		} else if (isSuite) { //$NON-NLS-1$
			treeItem = new TreeItem((fSuiteInfos.lastElement()).fTreeItem, SWT.NONE);
			treeItem.setImage(fSuiteIcon);
			(fSuiteInfos.lastElement()).fTestCount -= 1;
			fSuiteInfos.addElement(new SuiteInfo(treeItem, testCount));
		} else {
			treeItem = new TreeItem((fSuiteInfos.lastElement()).fTreeItem, SWT.NONE);
			treeItem.setImage(fTestIcon);
			(fSuiteInfos.lastElement()).fTestCount -= 1;
			mapTest(testInfo, treeItem);
		}
		treeItem.setText(testInfo.getTestMethodName());
		treeItem.setData(testInfo);
	}

	private void mapTest(TestRunInfo info, TreeItem item) {
		fTreeItemMap.put(info.getTestId(), item);
	}

	private TreeItem findTreeItem(String testId) {
		Object o = fTreeItemMap.get(testId);
		if (o instanceof TreeItem) return (TreeItem) o;
		return null;
	}

	/*
	 * @see ITestRunView#testStatusChanged(TestRunInfo, int)
	 */
	public void testStatusChanged(TestRunInfo newInfo) {
		Object o = fTreeItemMap.get(newInfo.getTestId());
		if (o instanceof TreeItem) {
			updateItem((TreeItem) o, newInfo);
			return;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.internal.junit.ui.ITestRunView#selectNext()
	 */
	public void selectNext() {
		TreeItem selection = getInitialSearchSelection();
		if (!moveSelection(selection)) return;

		TreeItem failure = findFailure(selection, true, !isLeafFailure(selection));
		if (failure != null) selectTest(failure);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.internal.junit.ui.ITestRunView#selectPrevious()
	 */
	public void selectPrevious() {
		TreeItem selection = getInitialSearchSelection();
		if (!moveSelection(selection)) return;

		TreeItem failure = findFailure(selection, false, !isLeafFailure(selection));
		if (failure != null) selectTest(failure);
	}

	private boolean moveSelection(TreeItem selection) {
		if (!fMoveSelection) {
			fMoveSelection = true;
			if (isLeafFailure(selection)) {
				selectTest(selection);
				return false;
			}
		}
		return true;
	}

	private TreeItem getInitialSearchSelection() {
		TreeItem[] treeItems = fTree.getSelection();
		TreeItem selection = null;

		if (treeItems.length == 0)
			selection = fTree.getItems()[0];
		else
			selection = treeItems[0];
		return selection;
	}

	private boolean isFailure(TreeItem selection) {
		return !(getTestRunInfo(selection).getStatus() == ITestRunListener.STATUS_OK);
	}

	private boolean isLeafFailure(TreeItem selection) {
		boolean isLeaf = selection.getItemCount() == 0;
		return isLeaf && isFailure(selection);
	}

	private void selectTest(TreeItem selection) {
		fTestRunnerPart.showTest(getTestRunInfo(selection));
	}

	private TreeItem findFailure(TreeItem start, boolean next, boolean includeNode) {
		TreeItem[] sib = findSiblings(start, next, includeNode);
		if (next) {
			for (int i = 0; i < sib.length; i++) {
				TreeItem failure = findFailureInTree(sib[i]);
				if (failure != null) return failure;
			}
		} else {
			for (int i = sib.length - 1; i >= 0; i--) {
				TreeItem failure = findFailureInTree(sib[i]);
				if (failure != null) return failure;
			}
		}
		TreeItem parent = start.getParentItem();
		if (parent == null) return null;
		return findFailure(parent, next, false);
	}

	private TreeItem[] findSiblings(TreeItem item, boolean next, boolean includeNode) {
		TreeItem parent = item.getParentItem();
		TreeItem[] children = null;
		if (parent == null)
			children = item.getParent().getItems();
		else
			children = parent.getItems();

		for (int i = 0; i < children.length; i++) {
			TreeItem item2 = children[i];
			if (item2 == item) {
				TreeItem[] result = null;
				if (next) {
					if (!includeNode) {
						result = new TreeItem[children.length - i - 1];
						System.arraycopy(children, i + 1, result, 0, children.length - i - 1);
					} else {
						result = new TreeItem[children.length - i];
						System.arraycopy(children, i, result, 0, children.length - i);

					}
				} else {
					if (!includeNode) {
						result = new TreeItem[i];
						System.arraycopy(children, 0, result, 0, i);
					} else {
						result = new TreeItem[i + 1];
						System.arraycopy(children, 0, result, 0, i + 1);
					}
				}
				return result;
			}
		}
		return new TreeItem[0];
	}

	private TreeItem findFailureInTree(TreeItem item) {
		if (item.getItemCount() == 0) {
			if (isFailure(item)) return item;
		}
		TreeItem[] children = item.getItems();
		for (int i = 0; i < children.length; i++) {
			TreeItem item2 = findFailureInTree(children[i]);
			if (item2 != null) return item2;
		}
		return null;
	}

	protected void expandAll() {
		TreeItem[] treeItems = fTree.getSelection();
		fTree.setRedraw(false);
		for (int i = 0; i < treeItems.length; i++) {
			expandAll(treeItems[i]);
		}
		fTree.setRedraw(true);
	}

	private void expandAll(TreeItem item) {
		item.setExpanded(true);
		TreeItem[] items = item.getItems();
		for (int i = 0; i < items.length; i++) {
			expandAll(items[i]);
		}
	}

	public void aboutToEnd() {
		for (int i = 0; i < fExecutionPath.size(); i++) {
			refreshItem(fExecutionPath.get(i), false);
		}
	}
}