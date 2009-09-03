package org.rubypeople.rdt.internal.core.hierarchy;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.rubypeople.rdt.core.IOpenable;
import org.rubypeople.rdt.core.IType;
import org.rubypeople.rdt.core.RubyCore;
import org.rubypeople.rdt.core.RubyModelException;
import org.rubypeople.rdt.internal.codeassist.RubyElementRequestor;
import org.rubypeople.rdt.internal.core.LogicalType;

public class HierarchyResolver {

	private static final int RECURSE_DEPTH_BAILOUT = 50;
	private boolean superTypesOnly;
	private HierarchyBuilder builder;
	private HashSet<String> visitedTypes;

	public HierarchyResolver(Map options, HierarchyBuilder builder) {
		this.builder = builder;
	}

	public void resolve(IOpenable[] openables, HashSet<String> localTypes, IProgressMonitor monitor) {
		try {
			int openablesLength = openables.length;			
			IType focus = this.builder.getType();
			
			for (int i = 0; i < openablesLength; i++) {
				IOpenable openable = openables[i];
				if (openable instanceof org.rubypeople.rdt.core.IRubyScript) {
					org.rubypeople.rdt.core.IRubyScript cu = (org.rubypeople.rdt.core.IRubyScript)openable;
					
					// Grab the types from the script and then connect them up!
					IType[] types = cu.getAllTypes();
					for (int j = 0; j < types.length; j++) {
						IType type = types[j];
						if (focusIsInHierarchy(focus, type, 0)) { // if it's our focus type, or a subclass
							try {
								this.visitedTypes = new HashSet<String>();
								reportHierarchy(type);
								visitedTypes.clear();
							} catch (RubyModelException e) {
								// ignore
							}
						}						
					}
				}
			}			
		} catch (ClassCastException e){ // work-around for 1GF5W1S - can happen in case duplicates are fed to the hierarchy with binaries hiding sources
		} catch (RubyModelException e){ 
		} finally {
			reset();
		}		
	}

	private boolean focusIsInHierarchy(IType focus, IType type, int currentDepth) throws RubyModelException {
		if (currentDepth > RECURSE_DEPTH_BAILOUT)
		{
			RubyCore.log(IStatus.ERROR, "Current recurse depth is " + currentDepth + " for resolving type hierarchy for type " + type.getFullyQualifiedName(), new IllegalStateException().fillInStackTrace());
			return false;
		}
		if (focus == null || type == null) return false;
		if (type.getFullyQualifiedName().equals(focus.getFullyQualifiedName())) return true; // type is focus
		return focusIsInHierarchy(focus, findSuperClass(type), ++currentDepth);
	}

	private void reportHierarchy(IType type) throws RubyModelException {
		visitedTypes.add(type.getFullyQualifiedName());
		
		IType superclass;
		if (type.isModule()){ // do not connect modules to Object
			superclass = null;
		} else {
			superclass = findSuperClass(type);
		}
		IType[] superinterfaces = findSuperInterfaces(type);		
		this.builder.connect(type, superclass, superinterfaces);
		if (type.isClass() && superclass != null) {
			if (visitedTypes.contains(superclass.getFullyQualifiedName())) {
				throw new IllegalStateException("Invalid type hierarchy: Loop in tree. Class: " + type.getFullyQualifiedName() + ", superclass: " + superclass.getFullyQualifiedName()); // Safety valve for recursive type hierarchies
			}
			reportHierarchy(superclass);
		}
	}

	private IType[] findSuperInterfaces(IType type) throws RubyModelException {
		String[] names = type.getIncludedModuleNames();
		List<IType> types = new ArrayList<IType>();
		for (int i = 0; i < names.length; i++) {
			IType logical = getLogicalType(type, names[i]);
			if (logical == null) {
				// try to see if full name is in same namespace.
				String namespace = type.getFullyQualifiedName().substring(0, type.getFullyQualifiedName().length() - type.getElementName().length());
				logical = getLogicalType(type, namespace + names[i]);
				if (logical == null) continue;
			}
			types.add(logical);
		}
		return (IType[]) types.toArray(new IType[types.size()]);
	}

	private IType findSuperClass(IType type) throws RubyModelException {
		String name = type.getSuperclassName();
		if (name == null) return null;
		return getLogicalType(type, name);
	}

	private IType getLogicalType(IType type, String name) {
		RubyElementRequestor requestor = new RubyElementRequestor(type.getRubyScript());
		IType[] types = requestor.findType(name);
		if (types == null || types.length == 0) return null;
		return new LogicalType(types);
	}

	private void reset() {
//		this.focusType = null;
		this.superTypesOnly = false;		
	}

	public void resolve(IType type) {
		org.rubypeople.rdt.core.IRubyScript cu = type.getRubyScript();
		HashSet<String> localTypes = new HashSet<String>();
		localTypes.add(cu.getPath().toString());
		this.superTypesOnly = true;
		resolve(new IOpenable[] {cu}, localTypes, null);		
	}

}
