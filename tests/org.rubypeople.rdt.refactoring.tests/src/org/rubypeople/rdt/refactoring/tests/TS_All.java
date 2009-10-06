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
 * Copyright (C) 2006 Lukas Felber <lfelber@hsr.ch>
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

package org.rubypeople.rdt.refactoring.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.rubypeople.rdt.refactoring.tests.classnodeprovider.TS_ClassNodeProvider;
import org.rubypeople.rdt.refactoring.tests.core.TS_Core;
import org.rubypeople.rdt.refactoring.tests.core.convertlocaltofield.TS_LocalToField;
import org.rubypeople.rdt.refactoring.tests.core.encapsulatefield.TS_EncapsulateField;
import org.rubypeople.rdt.refactoring.tests.core.extractconstant.TS_ExtractConstant;
import org.rubypeople.rdt.refactoring.tests.core.extractmethod.TS_ExtractMethod;
import org.rubypeople.rdt.refactoring.tests.core.generateaccessors.TS_GenerateAccessors;
import org.rubypeople.rdt.refactoring.tests.core.generateconstructor.TS_GenerateConstructor;
import org.rubypeople.rdt.refactoring.tests.core.inlineclass.TS_InlineClass;
import org.rubypeople.rdt.refactoring.tests.core.inlinelocal.TS_InlineLocal;
import org.rubypeople.rdt.refactoring.tests.core.inlinemethod.TS_InlineMethod;
import org.rubypeople.rdt.refactoring.tests.core.mergeclasspartsinfile.TS_MergeClassPartsInFile;
import org.rubypeople.rdt.refactoring.tests.core.mergewithexternalclassparts.TS_MergeWithExternalClassParts;
import org.rubypeople.rdt.refactoring.tests.core.movefield.TS_MoveField;
import org.rubypeople.rdt.refactoring.tests.core.movemethod.TS_MoveMethod;
import org.rubypeople.rdt.refactoring.tests.core.nodewrapper.TS_NodeWrapper;
import org.rubypeople.rdt.refactoring.tests.core.overridemethod.TS_OverrideMethod;
import org.rubypeople.rdt.refactoring.tests.core.pushdown.TS_PushDown;
import org.rubypeople.rdt.refactoring.tests.core.rename.TS_Rename;
import org.rubypeople.rdt.refactoring.tests.core.renameclass.TS_RenameClass;
import org.rubypeople.rdt.refactoring.tests.core.renamefield.TS_RenameField;
import org.rubypeople.rdt.refactoring.tests.core.renamelocal.TS_RenameLocal;
import org.rubypeople.rdt.refactoring.tests.core.renamemethod.TS_RenameMethod;
import org.rubypeople.rdt.refactoring.tests.core.renamemodule.TS_RenameModule;
import org.rubypeople.rdt.refactoring.tests.core.splitlocal.TS_SplitLocal;
import org.rubypeople.rdt.refactoring.tests.util.TS_Util;

public class TS_All
{

	public static Test suite()
	{
		TestSuite suite = new TestSuite("All Refactoring Tests");
		suite.addTest(TS_GenerateAccessors.suite());
		suite.addTest(TS_GenerateConstructor.suite());
		suite.addTest(TS_OverrideMethod.suite());
		suite.addTest(TS_PushDown.suite());
		suite.addTest(TS_RenameLocal.suite());
		suite.addTest(TS_Util.suite());
		suite.addTest(TS_ClassNodeProvider.suite());
		suite.addTest(TS_LocalToField.suite());
		suite.addTest(TS_Core.suite());
		suite.addTest(TS_ExtractMethod.suite());
		suite.addTest(TS_ExtractConstant.suite());
		suite.addTest(TS_MergeWithExternalClassParts.suite());
		suite.addTest(TS_MergeClassPartsInFile.suite());
		suite.addTest(TS_InlineLocal.suite());
		suite.addTest(TS_SplitLocal.suite());
		suite.addTest(TS_EncapsulateField.suite());
		suite.addTest(TS_InlineMethod.suite());
		suite.addTest(TS_RenameField.suite());
		suite.addTest(TS_RenameClass.suite());
		suite.addTest(TS_RenameMethod.suite());
		suite.addTest(TS_RenameModule.suite());
		suite.addTest(TS_InlineClass.suite());
		suite.addTest(TS_MoveMethod.suite());
		suite.addTest(TS_MoveField.suite());
		suite.addTest(TS_Rename.suite());
		suite.addTest(TS_NodeWrapper.suite());

		return suite;
	}
}