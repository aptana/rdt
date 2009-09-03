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

package org.rubypeople.rdt.refactoring.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.jruby.ast.Colon3Node;
import org.jruby.ast.IterNode;
import org.jruby.ast.MethodDefNode;
import org.jruby.ast.NilImplicitNode;
import org.jruby.ast.Node;
import org.jruby.lexer.yacc.IDESourcePosition;
import org.jruby.lexer.yacc.ISourcePosition;
import org.jruby.parser.StaticScope;

public class NodeUtil
{
	public static boolean hasScope(Node node)
	{
		Method[] methods = node.getClass().getMethods();
		for (int i = 0; i < methods.length; i++)
		{
			if (methods[i].getName().equals("getScope") || methods[i].equals("getStaticScope")) { //$NON-NLS-1$ //$NON-NLS-2$
				return true;
			}
		}
		return false;
	}

	public static Node getBody(Node node)
	{
		try
		{
			Method method = node.getClass().getMethod("getBodyNode", new Class[] {}); //$NON-NLS-1$
			return (Node) method.invoke(node, new Object[] {});
		}
		catch (SecurityException e)
		{
			e.printStackTrace();
		}
		catch (NoSuchMethodException e)
		{
			e.printStackTrace();
		}
		catch (IllegalArgumentException e)
		{
			e.printStackTrace();
		}
		catch (IllegalAccessException e)
		{
			e.printStackTrace();
		}
		catch (InvocationTargetException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	public static StaticScope getScope(Node node)
	{

		String methodName = "getStaticScope"; //$NON-NLS-1$
		if (node instanceof MethodDefNode || node instanceof IterNode)
		{
			methodName = "getScope"; //$NON-NLS-1$
		}

		try
		{
			Method method = node.getClass().getMethod(methodName, new Class[] {});
			return (StaticScope) method.invoke(node, new Object[] {});
		}
		catch (SecurityException e)
		{
			e.printStackTrace();
		}
		catch (NoSuchMethodException e)
		{
			e.printStackTrace();
		}
		catch (IllegalArgumentException e)
		{
			e.printStackTrace();
		}
		catch (IllegalAccessException e)
		{
			e.printStackTrace();
		}
		catch (InvocationTargetException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	public static boolean nodeAssignableFrom(Node n, Class... klasses)
	{
		if (n == null)
		{
			return false;
		}
		for (Class<?> klass : klasses)
		{
			if (klass.isAssignableFrom(n.getClass()))
			{
				return true;
			}
		}
		return false;
	}

	public static ISourcePosition subPositionUnion(Node node)
	{

		ISourcePosition enclosingPosition = node.getPosition();
		try
		{
			enclosingPosition = node.getPositionIncludingComments();
		}
		catch (Throwable t)
		{
			// ignore - may throw an unsupported exception...
		}

		List<Node> childList = node.childNodes();
		for (Node currentChild : childList)
		{
			// combinePosition() is not yet available in JRuby (SourcePosition), hope it will we soon.
			// While waiting for this use the posUnion method.
			// enclosingPosition = SourcePosition.combinePosition(enclosingPosition, subPositionUnion(currentChild));
			if (currentChild.equals(NilImplicitNode.NIL))
			{
				continue;
			}
			enclosingPosition = posUnion(enclosingPosition, subPositionUnion(currentChild));
		}

		return enclosingPosition;
	}

	private static ISourcePosition posUnion(ISourcePosition firstPos, ISourcePosition secondPos)
	{
		String fileName = firstPos.getFile();
		int startOffset = firstPos.getStartOffset();
		int endOffset = firstPos.getEndOffset();
		int startLine = firstPos.getStartLine();
		int endLine = firstPos.getEndLine();

		if (startOffset > secondPos.getStartOffset())
		{
			startOffset = secondPos.getStartOffset();
			startLine = secondPos.getStartLine();
		}

		if (endOffset < secondPos.getEndOffset())
		{
			endOffset = secondPos.getEndOffset();
			endLine = secondPos.getEndLine();
		}

		return new IDESourcePosition(fileName, startLine, endLine, startOffset, endOffset);
	}

	public static boolean positionIsInNode(int offset, Colon3Node path)
	{
		return offset >= path.getPosition().getStartOffset() && offset <= path.getPosition().getEndOffset();
	}
}
