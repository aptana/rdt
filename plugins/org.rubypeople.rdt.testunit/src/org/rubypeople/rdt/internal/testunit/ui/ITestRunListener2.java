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

import org.rubypeople.rdt.testunit.ITestRunListener;

/**
 * Extends ITestRunListener with a call back to trace the test contents
 */
public interface ITestRunListener2 extends ITestRunListener {

	/**
	 * Information about a member of the test suite that is about to be run. The
	 * format of the string is:
	 * 
	 * <pre>
	 * 
	 *  &quot;testId&quot;,&quot;testName&quot;,&quot;isSuite&quot;,&quot;testcount&quot;
	 *  
	 *  testId: a unique id for the test
	 *  testName: the name of the test
	 *  isSuite: true or false depending on whether the test is a suite
	 *  testCount: an integer indicating the number of tests 
	 *  
	 *  Example: &quot;324968,testPass(junit.tests.MyTest),false,1&quot;
	 *  
	 * </pre>
	 * 
	 * @param description
	 *            a string describing a tree entry
	 */
	public void testTreeEntry(String description);
}