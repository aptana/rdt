package org.rubypeople.rdt.internal.debug.ui.actions;

import org.jruby.ast.DefnNode;
import org.jruby.ast.Node;
import org.jruby.ast.RootNode;
import org.rubypeople.rdt.internal.core.parser.InOrderVisitor;

public class ValidBreakpointLocationLocator extends InOrderVisitor
{
	public static final int LOCATION_NOT_FOUND = 0;
	public static final int LOCATION_LINE = 1;
	public static final int LOCATION_METHOD = 2;
	public static final int LOCATION_FIELD = 3;

	private RootNode fCompilationUnit;
	private int fLineNumber;
	private boolean fBestMatch;

	private int fLocationType;
	private boolean fLocationFound;
	private int fLineLocation;

	/**
	 * @param compilationUnit
	 *            the JDOM CompilationUnit of the source code.
	 * @param lineNumber
	 *            the line number in the source code where to put the breakpoint.
	 * @param bestMatch
	 *            if <code>true</code> look for the best match, otherwise look only for a valid line
	 */
	public ValidBreakpointLocationLocator(RootNode compilationUnit, int lineNumber, boolean bestMatch)
	{
		fCompilationUnit = compilationUnit;
		fLineNumber = lineNumber;
		fBestMatch = bestMatch;
		fLocationFound = false;
	}

	public int getLocationType()
	{
		return fLocationType;
	}

	@Override
	public Object visitDefnNode(DefnNode node)
	{
		if (visit(node, false))
		{
			if (fBestMatch)
			{
				// check if we are on the line which contains the method name
				int nameStartLine = node.getNameNode().getPosition().getStartLine();
				if (nameStartLine == fLineNumber)
				{
					// fMemberOffset= nameOffset;
					fLocationType = LOCATION_METHOD;
					fLocationFound = true;
					return false;
				}
			}
			super.visitDefnNode(node);
		}
		return false;
	}

	/**
	 * Return <code>true</code> if this node children may contain a valid location for the breakpoint.
	 * 
	 * @param node
	 *            the node.
	 * @param isCode
	 *            true indicated that the first line of the given node always contains some executable code, even if
	 *            split in multiple lines.
	 */
	private boolean visit(Node node, boolean isCode)
	{
		// if we already found a correct location
		// no need to check the element inside.
		if (fLocationFound)
		{
			return false;
		}
		int startPosition = node.getPosition().getStartOffset();
		int endLine = node.getPosition().getEndLine();
		// if the position is not in this part of the code
		// no need to check the element inside.
		if (endLine < fLineNumber)
		{
			return false;
		}
		// if the first line of this node always represents some executable code and the
		// breakpoint is requested on this line or on a previous line, this is a valid
		// location
		int startLine = node.getPosition().getStartLine();
		if (isCode && (fLineNumber <= startLine))
		{
			fLineLocation = startLine;
			fLocationFound = true;
			fLocationType = LOCATION_LINE;
			// fTypeName= computeTypeName(node);
			return false;
		}
		return true;
	}

}
