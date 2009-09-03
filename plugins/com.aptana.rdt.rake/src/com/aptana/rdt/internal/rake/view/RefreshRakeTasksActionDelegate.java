/*******************************************************************************
 * Copyright (c) 2005 RadRails.org and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.aptana.rdt.internal.rake.view;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

/**
 * Triggers a refresh of the list in RailsPluginsView.
 * 
 * @author mkent
 * 
 */
public class RefreshRakeTasksActionDelegate implements IViewActionDelegate {

	private IViewPart fView;

	public void init(IViewPart view) {
		fView = view;
	}

	public void run(IAction action) {
		RakeTasksView rtv = (RakeTasksView) fView;
		rtv.updateRakeTasks(true);
	}

	public void selectionChanged(IAction action, ISelection selection) {

	}

}
