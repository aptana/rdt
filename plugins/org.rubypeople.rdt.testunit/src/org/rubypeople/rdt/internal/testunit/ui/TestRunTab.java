/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.rubypeople.rdt.internal.testunit.ui;

import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.dnd.Clipboard;


/**
 * A TestRunTab is shown as a tab in a tabbed folder.
 */
public abstract class TestRunTab {
	
	/**
	 * Create the tab control
	 * @param tabFolder the containing tab folder
	 * @param clipboard the clipboard to be used by the tab
	 * @param runner the testRunnerViewPart containing the tab folder
	 */
	public abstract void createTabControl(CTabFolder tabFolder, Clipboard clipboard, TestUnitView runner);
	
	/**
	 * Returns the name of the currently selected Test in the View
	 */
	public abstract String getSelectedTestId();

	/**
	 * Activates the TestRunView
	 */
	public void activate() {
	}
	
	/**
	 * Sets the focus in the TestRunView
	 */
	public void setFocus() {
	}
	
	/**
	 * Informs that the suite is about to start 
	 */
	public void aboutToStart() { 
	}

	/**
	 * Informs that the suite is about to start 
	 */
	public void aboutToEnd() {
	}
	
	/**
	 * Returns the name of the RunView
	 */
	public abstract String getName();
	
	/**
	 * Sets the current Test in the View
	 */
	public void setSelectedTest(String testId) {
	}
	
	/**
	 * A test has started
	 */
	public void startTest(String testId) {
	}

	/**
	 * A test has ended
	 */
	public void endTest(String testId) {
	}
	
	/**
	 * The status of a test has changed
	 */
	public void testStatusChanged(TestRunInfo newInfo) {
	}
	/**
	 * A new tree entry got posted.
	 */
	public void newTreeEntry(String treeEntry) {
	}
	
	/**
	 * Select next test failure.
	 */
	public void selectNext() {
	}
	
	/**
	 * Select previous test failure.
	 */
	public void selectPrevious() {
	}
}
