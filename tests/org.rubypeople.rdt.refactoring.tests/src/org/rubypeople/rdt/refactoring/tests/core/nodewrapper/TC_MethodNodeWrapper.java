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
 * Copyright (C) 2007 Mirko Stocker <me@misto.ch>
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

package org.rubypeople.rdt.refactoring.tests.core.nodewrapper;

import junit.framework.TestCase;

import org.jruby.ast.ArgsNode;
import org.jruby.ast.ArgumentNode;
import org.jruby.ast.ArrayNode;
import org.jruby.ast.DefnNode;
import org.jruby.ast.NewlineNode;
import org.jruby.ast.RootNode;
import org.jruby.lexer.yacc.IDESourcePosition;
import org.jruby.lexer.yacc.ISourcePosition;
import org.jruby.parser.LocalStaticScope;
import org.rubypeople.rdt.refactoring.documentprovider.StringDocumentProvider;
import org.rubypeople.rdt.refactoring.nodewrapper.ClassNodeWrapper;
import org.rubypeople.rdt.refactoring.nodewrapper.MethodNodeWrapper;
import org.rubypeople.rdt.refactoring.nodewrapper.RealClassNodeWrapper;

public class TC_MethodNodeWrapper extends TestCase {

	private static final ISourcePosition EMPTY_POSITION = new IDESourcePosition();
	ClassNodeWrapper klass;
	
	public void setUp() {
		RootNode rootNode = new StringDocumentProvider("TC_MethodNodeWrapper", "class T; def i; @var1; @var2; @var3; end; end").getRootNode("TC_MethodNodeWrapper");
		klass = new ClassNodeWrapper(new RealClassNodeWrapper(((NewlineNode) rootNode.getBodyNode()).getNextNode()));
	}

	private MethodNodeWrapper createReaderMethod(String name) {
		MethodNodeWrapper wrapper = new MethodNodeWrapper(new DefnNode(EMPTY_POSITION, new ArgumentNode(EMPTY_POSITION, name), new ArgsNode(EMPTY_POSITION, null, null, null, null, null), new LocalStaticScope(null), null), klass);
		return wrapper;
	}
	
	private MethodNodeWrapper createWriterMethod(String name) {
		MethodNodeWrapper wrapper = new MethodNodeWrapper(new DefnNode(EMPTY_POSITION, new ArgumentNode(EMPTY_POSITION, name), new ArgsNode(EMPTY_POSITION, new ArrayNode(EMPTY_POSITION, new ArgumentNode(EMPTY_POSITION, "value")), null, null, null, null), new LocalStaticScope(null), null), klass);
		return wrapper;
	}
	
	private MethodNodeWrapper createInvalidWriterMethod(String name) {
		MethodNodeWrapper wrapper = new MethodNodeWrapper(new DefnNode(EMPTY_POSITION, new ArgumentNode(EMPTY_POSITION, name), new ArgsNode(EMPTY_POSITION, new ArrayNode(EMPTY_POSITION, new ArgumentNode(EMPTY_POSITION, "value")).add(new ArgumentNode(EMPTY_POSITION, "value2")), null, null, null, null), new LocalStaticScope(null), null), klass);
		return wrapper;
	}

	public void testIsNotAccessor() {
		MethodNodeWrapper wrapper = createReaderMethod("test");
		assertFalse(wrapper.isAccessor());
		
		wrapper = createReaderMethod("var");
		assertFalse(wrapper.isAccessor());
	}
	
	public void testIsAccessor() {
		MethodNodeWrapper wrapper = createReaderMethod("var1");
		assertTrue(wrapper.isAccessor());
		
		wrapper = createReaderMethod("var2");
		assertTrue(wrapper.isAccessor());
		
		wrapper = createReaderMethod("var3");
		assertTrue(wrapper.isAccessor());
	}
	
	public void testIsWriter() {
		MethodNodeWrapper wrapper = createWriterMethod("var1=");
		assertTrue(wrapper.isAccessor());
		
		wrapper = createWriterMethod("var2=");
		assertTrue(wrapper.isAccessor());
		
		wrapper = createWriterMethod("var3=");
		assertTrue(wrapper.isAccessor());
	}
	
	public void testIsNotWriter() {
		MethodNodeWrapper wrapper = createWriterMethod("var1");
		assertFalse(wrapper.isAccessor());
		
		wrapper = createWriterMethod("va=");
		assertFalse(wrapper.isAccessor());
		
		wrapper = createWriterMethod("v=");
		assertFalse(wrapper.isAccessor());
		
		wrapper = createInvalidWriterMethod("var1=");
		assertFalse(wrapper.isAccessor());
	}
	
	public void testWithoutClass() {
		MethodNodeWrapper nodeWrapper = new MethodNodeWrapper(null, null);
		assertFalse(nodeWrapper.isAccessor());
	}
}
