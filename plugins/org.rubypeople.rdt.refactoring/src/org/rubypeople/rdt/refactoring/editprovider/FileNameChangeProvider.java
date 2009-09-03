package org.rubypeople.rdt.refactoring.editprovider;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;

public class FileNameChangeProvider {
	
	public Map<String, String> getFilesToRename(Collection<IFile> objects) {
		return new HashMap<String, String>(); 
	}

}
