package org.rubypeople.rdt.internal.ui;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.rubypeople.rdt.core.IRubyElement;
import org.rubypeople.rdt.core.IRubyProject;
import org.rubypeople.rdt.core.IRubyScript;
import org.rubypeople.rdt.core.tests.ModifyingResourceTest;
import org.rubypeople.rdt.internal.core.RubyProject;
import org.rubypeople.rdt.internal.core.RubyScript;

public class TC_ResourceAdapterFactory extends ModifyingResourceTest {

	private static final String PROJECT_NAME = "adapterTest";
	private ResourceAdapterFactory factory;
	private IProject project;
	
	public TC_ResourceAdapterFactory(String name) {
		super(name);
	}
	
	protected void setUp() throws Exception {
		super.setUp();
		this.project = createProject(PROJECT_NAME);
		factory = new ResourceAdapterFactory();	
	}
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		deleteProject(PROJECT_NAME);
	}

	public void testGetAdapterForRBFile() throws CoreException {
		IFile file = createFile(PROJECT_NAME + "/mustBeA.rb", "");
		assertEquals(RubyScript.class, factory.getAdapter(file, IRubyElement.class).getClass());
		assertTrue(factory.getAdapter(file, IRubyElement.class) instanceof IRubyScript);
	}
	
	public void testGetAdapterForRBWFile() throws CoreException {		
		IFile file = createFile(PROJECT_NAME + "/mustBeA.rbw", "");
		assertEquals(RubyScript.class, factory.getAdapter(file, IRubyElement.class).getClass());
		assertTrue(factory.getAdapter(file, IRubyElement.class) instanceof IRubyScript);
	}

	public void testGetAdapterForProject() throws CoreException {
		addRubyNature(PROJECT_NAME);
		assertEquals(RubyProject.class, factory.getAdapter(project, IRubyElement.class).getClass());
		assertTrue(factory.getAdapter(project, IRubyElement.class) instanceof IRubyProject);
	}
}
