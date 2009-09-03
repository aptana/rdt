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

package org.rubypeople.rdt.core.formatter;

import java.util.HashMap;
import java.util.Map;

import org.rubypeople.rdt.internal.formatter.Indentor;

public class EditableFormatHelper implements FormatHelper
{

	private static final String DEFAULT_LINE_DELIMITER = "\n";

	// TODO Grab line delimeter from file or user's prefs or OS or something.
	private String lineDelimiter;
	private boolean spaceAfterCommaInListings;
	private boolean spacesBeforeAndAfterAssignments;
	private boolean alwaysParanthesizeMethodCalls;
	private boolean spacesAroundHashAssignment;
	private boolean spacesBeforeAndAfterHashContent;
	private boolean spaceBeforeIterVars;
	private boolean spaceAfterIterVars;
	private boolean newlineBetweenClassBodyElements;
	private boolean alwaysParanthesizeMethodDefs;
	private boolean spaceBeforeIterBrackets;
	private boolean spaceBeforeClosingIterBrackets;
	private boolean insertDoAfterWhileExpression;
	private boolean collpaseOperatorSelfAssignments;

	// FIXME Grab user's prefs for default indentation!
	private Indentor indentor = new Indentor(2, ' ');

	public EditableFormatHelper()
	{
		this(DEFAULT_LINE_DELIMITER);
	}

	public EditableFormatHelper(String lineDelimiter)
	{
		this(new HashMap<String, Object>());
		if (lineDelimiter != null)
			this.lineDelimiter = lineDelimiter;
	}

	public EditableFormatHelper(Map<String, Object> options)
	{
		spaceAfterCommaInListings = getBoolean(options, EditableFormatHelper.SPACE_AFTER_COMMA_IN_LISTS, true);
		alwaysParanthesizeMethodCalls = getBoolean(options,
				EditableFormatHelper.ALWAYS_SURROUND_METHOD_CALLS_IN_PARENS, false);
		alwaysParanthesizeMethodDefs = getBoolean(options,
				EditableFormatHelper.ALWAYS_SURROUND_METHOD_ARGUMENTS_IN_PARENS, false);
		spacesAroundHashAssignment = getBoolean(options, EditableFormatHelper.SPACES_AROUND_HASH_ASSIGNMENT, true);
		spacesBeforeAndAfterHashContent = getBoolean(options,
				EditableFormatHelper.SPACES_BEFORE_AND_AFTER_HASH_CONTENT, false);
		spacesBeforeAndAfterAssignments = getBoolean(options, EditableFormatHelper.SPACE_BEFORE_AND_AFTER_ASSIGNMENTS,
				true);
		spaceBeforeIterBrackets = getBoolean(options, EditableFormatHelper.SPACE_BEFORE_BLOCK_BRACKETS, true);
		spaceAfterIterVars = getBoolean(options, EditableFormatHelper.SPACE_AFTER_ITER_VARS, true);
		spaceBeforeIterVars = getBoolean(options, EditableFormatHelper.SPACES_BEFORE_ITER_VARS, true);
		spaceBeforeClosingIterBrackets = getBoolean(options, EditableFormatHelper.SPACE_BEFORE_CLOSING_BLOCK_BRACKET,
				true);
		insertDoAfterWhileExpression = getBoolean(options, EditableFormatHelper.INSERT_DO_AFTER_WHILE_EXPRESSION, false);
		newlineBetweenClassBodyElements = getBoolean(options, EditableFormatHelper.NEWLINE_BETWEEN_CLASS_BODY_ELEMENTS,
				false);
		collpaseOperatorSelfAssignments = getBoolean(options, EditableFormatHelper.COLLAPSE_OPERATOR_SELF_ASSIGNMENTS,
				true);
		// TODO Pass line delimiter in options map?
		lineDelimiter = DEFAULT_LINE_DELIMITER;
	}

	private boolean getBoolean(Map<String, Object> options, String key, boolean defaultValue)
	{
		Object value = options.get(key);
		if (value == null)
			return defaultValue;
		if (value instanceof Boolean)
			return (Boolean) value;
		if (value instanceof String)
		{
			String str = (String) value;
			if (str.trim().toLowerCase().equals(Boolean.TRUE.toString().toLowerCase()))
				return true;
			if (str.trim().toLowerCase().equals(Boolean.FALSE.toString().toLowerCase()))
				return false;
		}
		return defaultValue;
	}

	public Indentor getIndentor()
	{
		return indentor;
	}

	public void setTabInsteadOfSpaces(boolean tabInsteadOfSpaces)
	{
		if (tabInsteadOfSpaces)
		{
			indentor.setIndentationChar('\t');
		}
		else
		{
			indentor.setIndentationChar(' ');
		}
	}

	public void setIndentationSteps(int indentationSteps)
	{
		indentor.setIndentationSteps(indentationSteps);
	}

	public void setLineDelimiter(String lineDelimiter)
	{
		if (lineDelimiter != null)
			this.lineDelimiter = lineDelimiter;
	}

	/*
	 * | | v v test = 5
	 */

	public String beforeAssignment()
	{
		return spacesBeforeAndAfterAssignments ? " " : ""; //$NON-NLS-1$ //$NON-NLS-2$
	}

	public String afterAssignment()
	{
		return spacesBeforeAndAfterAssignments ? " " : ""; //$NON-NLS-1$ //$NON-NLS-2$
	}

	public String matchOperator()
	{
		return spacesBeforeAndAfterAssignments ? " =~ " : "=~"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	/*
	 * | | v v puts("hello")
	 */

	public String beforeCallArguments()
	{
		return alwaysParanthesizeMethodCalls ? "(" : " "; //$NON-NLS-1$ //$NON-NLS-2$
	}

	public String afterCallArguments()
	{
		return alwaysParanthesizeMethodCalls ? ")" : ""; //$NON-NLS-1$ //$NON-NLS-2$
	}

	/*
	 * | | v v { :a = 10 }
	 */

	public String beforeHashContent()
	{
		return spacesBeforeAndAfterHashContent ? " " : ""; //$NON-NLS-1$ //$NON-NLS-2$
	}

	public String afterHashContent()
	{
		return spacesBeforeAndAfterHashContent ? " " : ""; //$NON-NLS-1$ //$NON-NLS-2$
	}

	/*
	 * | | v v [].each_with_index { | e, i | p e }
	 */
	public String beforeIterVars()
	{
		return spaceBeforeIterVars ? " " : ""; //$NON-NLS-1$ //$NON-NLS-2$
	}

	public String afterIterVars()
	{
		return spaceAfterIterVars ? " " : ""; //$NON-NLS-1$ //$NON-NLS-2$
	}

	/*
	 * | | v v def test(par1, par2)
	 */
	public String beforeMethodArguments()
	{
		return alwaysParanthesizeMethodDefs ? "(" : " "; //$NON-NLS-1$ //$NON-NLS-2$
	}

	public String afterMethodArguments()
	{
		return alwaysParanthesizeMethodDefs ? ")" : ""; //$NON-NLS-1$ //$NON-NLS-2$
	}

	/*
	 * | | v v [].each_with_index { | e, i | p e }
	 */
	public String beforeIterBrackets()
	{
		return spaceBeforeIterBrackets ? " " : ""; //$NON-NLS-1$ //$NON-NLS-2$
	}

	public String beforeClosingIterBrackets()
	{
		return spaceBeforeClosingIterBrackets ? " " : ""; //$NON-NLS-1$ //$NON-NLS-2$
	}

	/*
	 * def ... end | v def ... end
	 */
	public String classBodyElementsSeparator()
	{
		return newlineBetweenClassBodyElements ? getLineDelimiter() : ""; //$NON-NLS-1$
	}

	/*
	 * | | | v v v [1, 2, 3, 4]
	 */

	public String getListSeparator()
	{
		return spaceAfterCommaInListings ? ", " : ","; //$NON-NLS-1$ //$NON-NLS-2$
	}

	/*
	 * | | v v { :a => 10 }
	 */
	public String hashAssignment()
	{
		return spacesAroundHashAssignment ? " => " : "=>"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void setAlwaysParanthesizeMethodCalls(boolean alwaysParanthesizeMethodCalls)
	{
		this.alwaysParanthesizeMethodCalls = alwaysParanthesizeMethodCalls;
	}

	public void setAlwaysParanthesizeMethodDefs(boolean alwaysParanthesizeMethodDefs)
	{
		this.alwaysParanthesizeMethodDefs = alwaysParanthesizeMethodDefs;
	}

	public void setNewlineBetweenClassBodyElements(boolean newlineBetweenClassBodyElements)
	{
		this.newlineBetweenClassBodyElements = newlineBetweenClassBodyElements;
	}

	public void setSpaceAfterCommaInListings(boolean spaceAfterCommaInListings)
	{
		this.spaceAfterCommaInListings = spaceAfterCommaInListings;
	}

	public void setSpaceAfterIterVars(boolean spaceAfterIterVars)
	{
		this.spaceAfterIterVars = spaceAfterIterVars;
	}

	public void setSpaceBeforeClosingIterBrackets(boolean spaceBeforeClosingIterBrackets)
	{
		this.spaceBeforeClosingIterBrackets = spaceBeforeClosingIterBrackets;
	}

	public void setSpaceBeforeIterBrackets(boolean spaceBeforeIterBrackets)
	{
		this.spaceBeforeIterBrackets = spaceBeforeIterBrackets;
	}

	public void setSpaceBeforeIterVars(boolean spaceBeforeIterVars)
	{
		this.spaceBeforeIterVars = spaceBeforeIterVars;
	}

	public void setSpacesAroundHashAssignment(boolean spacesAroundHashAssignment)
	{
		this.spacesAroundHashAssignment = spacesAroundHashAssignment;
	}

	public void setSpacesBeforeAndAfterAssignments(boolean spacesBeforeAndAfterAssignments)
	{
		this.spacesBeforeAndAfterAssignments = spacesBeforeAndAfterAssignments;
	}

	public void setSpacesBeforeAndAfterHashContent(boolean spacesBeforeAndAfterHashContent)
	{
		this.spacesBeforeAndAfterHashContent = spacesBeforeAndAfterHashContent;
	}

	public String getLineDelimiter()
	{
		return lineDelimiter;
	}

	public boolean insertDoAfterWhileExpression()
	{
		return insertDoAfterWhileExpression;
	}

	public boolean collapseOperatorSelfAssignments()
	{
		return collpaseOperatorSelfAssignments;
	}
}
