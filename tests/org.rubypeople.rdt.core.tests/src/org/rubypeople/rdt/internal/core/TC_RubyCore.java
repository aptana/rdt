package org.rubypeople.rdt.internal.core;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.rubypeople.eclipse.testutils.ResourceTools;
import org.rubypeople.rdt.core.IRubyScript;
import org.rubypeople.rdt.core.RubyCore;
import org.rubypeople.rdt.core.tests.ModifyingResourceTest;

public class TC_RubyCore extends ModifyingResourceTest {

	public TC_RubyCore(String name) {
		super(name);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		try {
			this.createRubyProject("P", new String[] {""});
			this.createFolder("P/x/y");
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		this.deleteProject("P");
	}
	
	public void testCreate() throws CoreException {
		IFile file = createFile("P/x/y/theFile.rb", "");
		IRubyScript rubyFile = RubyCore.create(file);
		assertNotNull("The core should create an IRubyScript when the resource is a file with .rb extension.", rubyFile);
		assertEquals("The core should place the resource into the RubyFile.", file, rubyFile.getUnderlyingResource());
		
		file = createFile("P/x/y/theFile.xyz", "");
		assertNull("The core should not create a RubyFile when the resource is a file without the .rb extension.", RubyCore.create(file));
	}

	public void testAddRubyNature() throws Exception {
		IProject project = ResourceTools.createProject("someProject");
		RubyCore.addRubyNature(project, null);
		assertTrue(project.hasNature(RubyCore.NATURE_ID));
	}
}
