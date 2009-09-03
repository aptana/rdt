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

package org.rubypeople.rdt.refactoring.signatureprovider;

import java.util.ArrayList;
import java.util.Collection;

import org.rubypeople.rdt.refactoring.nodewrapper.ArgsNodeWrapper;
import org.rubypeople.rdt.refactoring.util.Constants;

public class MethodSignature {

	private String methodName;

	private Collection<String> args;

	public MethodSignature(String methodName, Collection<String> args) {
		this.methodName = methodName;
		this.args = args;
	}

	public MethodSignature(String methodName, int argCount) {
		this(methodName, getAnnonymousArgs(argCount));
	}

	public MethodSignature(String methodName, ArgsNodeWrapper args) {
		this(methodName, args.getArgsList());
	}

	private static Collection<String> getAnnonymousArgs(int argCount) {
		Collection<String> args = new ArrayList<String>();
		for (int i = 0; i < argCount; i++) {
			args.add("arg" + i); //$NON-NLS-1$
		}
		return args;
	}

	public String getMethodName() {
		return methodName;
	}

	public Collection<String> getArguments() {
		return args;
	}

	public String getArgListAsString() {
		if (args.isEmpty())
			return ""; //$NON-NLS-1$
		StringBuilder argList = new StringBuilder();
		for (String arg : args) {
			argList.append(arg + ", "); //$NON-NLS-1$
		}
		return ' ' + argList.substring(0, argList.length() - 2);
	}

	public boolean isConstructor() {
		return methodName.equals(Constants.CONSTRUCTOR_NAME);
	}

	public String getNameWithArgs() {
		String argsList = getArgListAsString();
		return getMethodName() + ((argsList.length() != 0) ? argsList : ""); //$NON-NLS-1$
	}

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((args == null) ? 0 : args.hashCode());
		result = PRIME * result + ((methodName == null) ? 0 : methodName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof MethodSignature) {
			MethodSignature otherSignature = (MethodSignature) obj;
			if(getNameWithArgs().equals(otherSignature.getNameWithArgs()))
				return true;
		}
		return false;
	}
}
