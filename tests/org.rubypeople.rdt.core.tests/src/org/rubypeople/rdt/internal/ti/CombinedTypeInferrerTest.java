/**
 * This file Copyright (c) 2005-2008 Aptana, Inc. This program is
 * dual-licensed under both the Aptana Public License and the GNU General
 * Public license. You may elect to use one or the other of these licenses.
 * 
 * This program is distributed in the hope that it will be useful, but
 * AS-IS and WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE, TITLE, or
 * NONINFRINGEMENT. Redistribution, except as permitted by whichever of
 * the GPL or APL you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or modify this
 * program under the terms of the GNU General Public License,
 * Version 3, as published by the Free Software Foundation.  You should
 * have received a copy of the GNU General Public License, Version 3 along
 * with this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 * Aptana provides a special exception to allow redistribution of this file
 * with certain Eclipse Public Licensed code and certain additional terms
 * pursuant to Section 7 of the GPL. You may view the exception and these
 * terms on the web at http://www.aptana.com/legal/gpl/.
 * 
 * 2. For the Aptana Public License (APL), this program and the
 * accompanying materials are made available under the terms of the APL
 * v1.0 which accompanies this distribution, and is available at
 * http://www.aptana.com/legal/apl/.
 * 
 * You may view the GPL, Aptana's exception and additional terms, and the
 * APL in the file titled license.html at the root of the corresponding
 * plugin containing this source file.
 * 
 * Any modifications to this file must keep this entire header intact.
 */
package org.rubypeople.rdt.internal.ti;

/**
 * Contains all the tests that each implementation can pass in common.
 * 
 * @author Chris Williams (cwilliams@aptana.com)
 */
public abstract class CombinedTypeInferrerTest extends TypeInferrerTestCase
{
	public void testFixnum() throws Exception
	{
		assertInfersTypeWithoutDoubt(inferrer.infer("5", 0), "Fixnum");
	}

	public void testArrayLiteral() throws Exception
	{
		assertInfersTypeWithoutDoubt(inferrer.infer("[1, 2, 3]", 8), "Array");
	}

	public void testString() throws Exception
	{
		assertInfersTypeWithoutDoubt(inferrer.infer("'string'", 3), "String");
	}

	public void testFixnumAssignment() throws Exception
	{
		assertInfersTypeWithoutDoubt(inferrer.infer("var=5", 1), "Fixnum");
	}

	public void testLocalVariableAfterAssignment() throws Exception
	{
		assertInfersTypeWithoutDoubt(inferrer.infer("x=5;x", 4), "Fixnum");
	}

	public void testLocalVariableAfterAssignmentInsideScope() throws Exception
	{
		assertInfersTypeWithoutDoubt(inferrer.infer("module M;x=5;x;end", 13), "Fixnum");
	}

	public void testNamespacedClass() throws Exception
	{
		String src = "module M3\n" + "  class C\n" + "  end\n" + "end\n" + "\n" + "ob = M3::C.new\n" + "ob";
		assertInfersTypeWithoutDoubt(inferrer.infer(src, src.length() - 1), "M3::C");
	}

	public void testLocalVariableAssignmentToLocalVariable() throws Exception
	{
		String script = "x=5;y=x;x;y";
		assertInfersTypeWithoutDoubt(inferrer.infer(script, 8), "Fixnum"); // "x"
		assertInfersTypeWithoutDoubt(inferrer.infer(script, 10), "Fixnum"); // "y"
	}

	public void testLocalVariableAssignmentToLocalVariableTwice() throws Exception
	{
		String script = "x=5;y=x;z=y;z;y;x";
		assertInfersTypeWithoutDoubt(inferrer.infer(script, 12), "Fixnum"); // "z"
		assertInfersTypeWithoutDoubt(inferrer.infer(script, 14), "Fixnum"); // "y"
		assertInfersTypeWithoutDoubt(inferrer.infer(script, 16), "Fixnum"); // "x"
	}

	public void testLocalVariableAssignmentToWellKnownMethodCall() throws Exception
	{
		assertInfersTypeWithoutDoubt(inferrer.infer("x=5.to_s;x", 9), "String");
	}

	public void testLocalVariableAssignmentToClassInstantiation() throws Exception
	{
		assertInfersTypeWithoutDoubt(inferrer.infer("x=Regexp.new;x", 13), "Regexp");
	}

	public void testInfiniteLoop() throws Exception
	{
		inferrer.infer("@inst = 1;@inst = @inst.blah", 15);
		assertTrue(true);
	}

	// FIXME: Will need access to core stubs
	// public void testLocalVariableAssignmentToWellKnownMethodCall() throws Exception {
	// assertInfersTypeWithoutDoubt(inferrer.infer("x=5.to_s;x", 9), "String");
	// }

	public void testInstVarAssignment() throws Exception
	{
		assertInfersTypeWithoutDoubt(inferrer.infer("@x=5;@x", 6), "Fixnum");
	}

	public void testGlobalVarAssignment() throws Exception
	{
		assertInfersTypeWithoutDoubt(inferrer.infer("$x=5;$x", 6), "Fixnum");
	}

	public void testLocalVariableAssignmentWithSameNameAcrossTwoScopes() throws Exception
	{
		String script = "module M;x=5;x;end;module N;x='foo';x;end";
		assertInfersTypeWithoutDoubt(inferrer.infer(script, 13), "Fixnum");
		assertInfersTypeWithoutDoubt(inferrer.infer(script, 36), "String");
	}
}
