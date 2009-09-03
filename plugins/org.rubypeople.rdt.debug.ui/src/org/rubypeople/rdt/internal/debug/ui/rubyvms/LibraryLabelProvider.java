/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.rubypeople.rdt.internal.debug.ui.rubyvms;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.rubypeople.rdt.internal.debug.ui.RDTImageDescriptor;
import org.rubypeople.rdt.internal.debug.ui.RdtDebugUiPlugin;
import org.rubypeople.rdt.ui.ISharedImages;
import org.rubypeople.rdt.ui.RubyUI;

/**
 * Label provider for Ruby vM libraries.
 * 
 * @since 0.9.0
 */
public class LibraryLabelProvider extends LabelProvider {

	public Image getImage(Object element) {
		if (element instanceof LibraryStandin) {
			LibraryStandin library= (LibraryStandin) element;
			String key = ISharedImages.IMG_OBJS_LIBRARY;
			IStatus status = library.validate();
			if (!status.isOK()) {
				ImageDescriptor base = RubyUI.getSharedImages().getImageDescriptor(key);
				RDTImageDescriptor descriptor= new RDTImageDescriptor(base, RDTImageDescriptor.IS_OUT_OF_SYNCH);
				return RdtDebugUiPlugin.getImageDescriptorRegistry().get(descriptor);
			}
			return RubyUI.getSharedImages().getImage(key);
		}
		return null;
	}

	public String getText(Object element) {
		if (element instanceof LibraryStandin) {
			return ((LibraryStandin)element).getSystemLibraryPath().toOSString();
		}
		return null;
	}

}