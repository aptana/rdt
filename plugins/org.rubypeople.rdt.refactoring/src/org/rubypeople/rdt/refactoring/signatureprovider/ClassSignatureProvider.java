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

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import org.rubypeople.rdt.refactoring.classnodeprovider.ClassNodeProvider;
import org.rubypeople.rdt.refactoring.exception.UnknownClassNameException;
import org.rubypeople.rdt.refactoring.exception.UnknownMethodNameException;
import org.rubypeople.rdt.refactoring.nodewrapper.ClassNodeWrapper;
import org.rubypeople.rdt.refactoring.util.Constants;

public abstract class ClassSignatureProvider implements IClassSignatureProvider {

	private Map<String, Object> methodSignatures;

	private IClassSignatureProvider superProvider;

	public abstract String getClassName();

	protected abstract MethodSignature getSignature(String methodName, Object data);

	public ClassSignatureProvider(IClassSignatureProvider superProvider) {
		this.methodSignatures = new LinkedHashMap<String, Object>();
		this.superProvider = superProvider;
	}

	protected void addMethodSignature(String methodName, Object data) {
		methodSignatures.put(methodName, data);
	}

	public boolean hasMethodSignature(String methodName) {
		if (methodSignatures.containsKey(methodName)) {
			return true;
		}
		if (superProvider != null) {
			return superProvider.hasMethodSignature(methodName);
		}
		return false;
	}

	public MethodSignature getMethodSignature(String methodName) throws UnknownMethodNameException {

		if (methodSignatures.containsKey(methodName))
			return getSignature(methodName, methodSignatures.get(methodName));
		if (superProvider != null)
			return superProvider.getMethodSignature(methodName);
		throw new UnknownMethodNameException();
	}

	public Collection<MethodSignature> getMethodSignatures() {
		Collection<MethodSignature> signs = new LinkedHashSet<MethodSignature>();
		for (String methodName : methodSignatures.keySet()) {
			signs.add(getSignature(methodName, methodSignatures.get(methodName)));
		}
		if (superProvider != null) {
			signs.addAll(superProvider.getMethodSignatures());
		}
		return signs;
	}

	public boolean hasConstructorSignature() {
		return hasMethodSignature(Constants.CONSTRUCTOR_NAME);
	}

	public MethodSignature getConstructorSignature() throws UnknownMethodNameException {
		return getMethodSignature(Constants.CONSTRUCTOR_NAME);
	}

	// Support for JRubyBuilt in Classes disabled (uncommented Code) because the
	// Arity of the methods
	// cannot propperly be evaluated.
	public static IClassSignatureProvider getClassSignatureProvider(String className, ClassNodeProvider classNodeProvider) throws UnknownClassNameException {
		// IRuby ruby = RefactoringPlugin.getRuby();
		ClassNodeWrapper classNode = null;
		if (classNodeProvider != null)
			classNode = classNodeProvider.getClassNode(className);
		if (classNode != null)
			return new ClassNodeSignatureProvider(classNode, classNodeProvider);
		// else if(ruby.getClass(className) != null &&
		// !className.equals(Constants.OBJECT_NAME))
		// return new JRubyClassSignatureProvider(ruby.getClass(className));
		throw new UnknownClassNameException();
	}

	protected Object getData(String methodName) {
		return methodSignatures.get(methodName);
	}
}
