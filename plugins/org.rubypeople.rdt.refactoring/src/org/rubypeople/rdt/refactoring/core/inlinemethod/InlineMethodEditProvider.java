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

package org.rubypeople.rdt.refactoring.core.inlinemethod;

import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;
import org.jruby.ast.AssignableNode;
import org.jruby.ast.Node;
import org.rubypeople.rdt.refactoring.editprovider.EditProvider;
import org.rubypeople.rdt.refactoring.util.HsrFormatter;

public class InlineMethodEditProvider extends EditProvider {

	private final Node node;
	private final InlineMethodConfig config;

	public InlineMethodEditProvider(InlineMethodConfig config) {
		super(true, false);
		
		this.config = config;
		if(config.getSelectedCall() == null) {
			this.node = null;
			return;
		}
		Node parent = config.getCallParent();
		if(parent instanceof AssignableNode) {
			this.node = parent;
		} else {
			this.node = config.getSelectedCall().getWrappedNode();
		}
	}
	
	@Override
	public TextEdit getEdit(final String document) {
		return new ReplaceEdit(getOffset(), getOffsetLength(), format(document).replaceFirst("^\\s*", "")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private String format(final String document) {
		return HsrFormatter.format(document, config.getMethodDefDoc().getActiveFileContent(), getOffset());
	}

	protected int getOffsetLength() {
		return getOffsetLength(node);
	}

	private int getOffsetLength(final Node parent) {
		return parent.getPosition().getEndOffset() - parent.getPosition().getStartOffset();
	}

	@Override
	protected Node getEditNode(final int offset, final String document) {
		assert false : "should never be called"; //$NON-NLS-1$
		return null;
	}

	@Override
	protected int getOffset(final String document) {
		return getOffset();
	}
	
	private int getOffset() {
		return node.getPosition().getStartOffset();
	}

}
