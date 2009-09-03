/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.rubypeople.rdt.internal.ui.refactoring;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.util.Assert;
import org.eclipse.ltk.core.refactoring.TextEditBasedChange;
import org.eclipse.ltk.core.refactoring.TextEditBasedChangeGroup;
import org.eclipse.ltk.ui.refactoring.LanguageElementNode;
import org.eclipse.ltk.ui.refactoring.TextEditChangeNode;
import org.eclipse.text.edits.TextEdit;
import org.rubypeople.rdt.core.IRubyElement;
import org.rubypeople.rdt.core.IRubyScript;
import org.rubypeople.rdt.core.ISourceRange;
import org.rubypeople.rdt.core.ISourceReference;
import org.rubypeople.rdt.core.RubyModelException;
import org.rubypeople.rdt.internal.ui.viewsupport.RubyElementImageProvider;
import org.rubypeople.rdt.ui.RubyElementLabels;

public class RubyScriptChangeNode extends TextEditChangeNode {

	static final ChildNode[] EMPTY_CHILDREN= new ChildNode[0];
	
	private static class RubyLanguageNode extends LanguageElementNode {

		private IRubyElement fRubyElement;
		private static RubyElementImageProvider fgImageProvider= new RubyElementImageProvider();

		public RubyLanguageNode(TextEditChangeNode parent, IRubyElement element) {
			super(parent);
			fRubyElement= element;
			Assert.isNotNull(fRubyElement);
		}
		
		public RubyLanguageNode(ChildNode parent, IRubyElement element) {
			super(parent);
			fRubyElement= element;
			Assert.isNotNull(fRubyElement);
		}
		
		public String getText() {
			return RubyElementLabels.getElementLabel(fRubyElement, RubyElementLabels.ALL_DEFAULT);
		}
		
		public ImageDescriptor getImageDescriptor() {
			return fgImageProvider.getRubyImageDescriptor(
				fRubyElement, 
				RubyElementImageProvider.OVERLAY_ICONS | RubyElementImageProvider.SMALL_ICONS);
		}
		
		public IRegion getTextRange() throws CoreException {
			ISourceRange range= ((ISourceReference)fRubyElement).getSourceRange();
			return new Region(range.getOffset(), range.getLength());
		}	
	}	
	
	public RubyScriptChangeNode(TextEditBasedChange change) {
		super(change);
	}
	
	protected ChildNode[] createChildNodes() {
		final TextEditBasedChange change= getTextEditBasedChange();
		IRubyScript cunit= (IRubyScript) change.getAdapter(IRubyScript.class);
		if (cunit != null) {
			List children= new ArrayList(5);
			Map map= new HashMap(20);
			TextEditBasedChangeGroup[] changes= getSortedChangeGroups(change);
			for (int i= 0; i < changes.length; i++) {
				TextEditBasedChangeGroup tec= changes[i];
				try {
					IRubyElement element= getModifiedRubyElement(tec, cunit);
					if (element.equals(cunit)) {
						children.add(createTextEditGroupNode(this, tec));
					} else {
						RubyLanguageNode pjce= getChangeElement(map, element, children, this);
						pjce.addChild(createTextEditGroupNode(pjce, tec));
					}
				} catch (RubyModelException e) {
					children.add(createTextEditGroupNode(this, tec));
				}
			}
			return (ChildNode[]) children.toArray(new ChildNode[children.size()]);
		} else {
			return EMPTY_CHILDREN;
		}
	}
	
	private static class OffsetComparator implements Comparator {
		public int compare(Object o1, Object o2) {
			TextEditBasedChangeGroup c1= (TextEditBasedChangeGroup)o1;
			TextEditBasedChangeGroup c2= (TextEditBasedChangeGroup)o2;
			int p1= getOffset(c1);
			int p2= getOffset(c2);
			if (p1 < p2)
				return -1;
			if (p1 > p2)
				return 1;
			// same offset
			return 0;	
		}
		private int getOffset(TextEditBasedChangeGroup edit) {
			return edit.getRegion().getOffset();
		}
	}
	
	private TextEditBasedChangeGroup[] getSortedChangeGroups(TextEditBasedChange change) {
		TextEditBasedChangeGroup[] edits= change.getChangeGroups();
		List result= new ArrayList(edits.length);
		for (int i= 0; i < edits.length; i++) {
			if (!edits[i].getTextEditGroup().isEmpty())
				result.add(edits[i]);
		}
		Comparator comparator= new OffsetComparator();
		Collections.sort(result, comparator);
		return (TextEditBasedChangeGroup[])result.toArray(new TextEditBasedChangeGroup[result.size()]);
	}
	
	private IRubyElement getModifiedRubyElement(TextEditBasedChangeGroup edit, IRubyScript cunit) throws RubyModelException {
		IRegion range= edit.getRegion();
		if (range.getOffset() == 0 && range.getLength() == 0)
			return cunit;
		IRubyElement result= cunit.getElementAt(range.getOffset());
		if (result == null)
			return cunit;
		
		try {
			while(true) {
				ISourceReference ref= (ISourceReference)result;
				IRegion sRange= new Region(ref.getSourceRange().getOffset(), ref.getSourceRange().getLength());
				if (result.getElementType() == IRubyElement.SCRIPT || result.getParent() == null || coveredBy(edit, sRange))
					break;
				result= result.getParent();
			}
		} catch(RubyModelException e) {
			// Do nothing, use old value.
		} catch(ClassCastException e) {
			// Do nothing, use old value.
		}
		return result;
	}
	
	private RubyLanguageNode getChangeElement(Map map, IRubyElement element, List children, TextEditChangeNode cunitChange) {
		RubyLanguageNode result= (RubyLanguageNode)map.get(element);
		if (result != null)
			return result;
		IRubyElement parent= element.getParent();
		if (parent instanceof IRubyScript) {
			result= new RubyLanguageNode(cunitChange, element);
			children.add(result);
			map.put(element, result);
		} else {
			RubyLanguageNode parentChange= getChangeElement(map, parent, children, cunitChange);
			result= new RubyLanguageNode(parentChange, element);
			parentChange.addChild(result);
			map.put(element, result);
		}
		return result;
	}
	
	private boolean coveredBy(TextEditBasedChangeGroup group, IRegion sourceRegion) {
		int sLength= sourceRegion.getLength();
		if (sLength == 0)
			return false;
		int sOffset= sourceRegion.getOffset();
		int sEnd= sOffset + sLength - 1;
		TextEdit[] edits= group.getTextEdits();
		for (int i= 0; i < edits.length; i++) {
			TextEdit edit= edits[i];
			if (edit.isDeleted())
				return false;
			int rOffset= edit.getOffset();
			int rLength= edit.getLength();
			int rEnd= rOffset + rLength - 1;
		    if (rLength == 0) {
				if (!(sOffset < rOffset && rOffset <= sEnd))
					return false;
			} else {
				if (!(sOffset <= rOffset && rEnd <= sEnd))
					return false;
			}
		}
		return true;
	}
}