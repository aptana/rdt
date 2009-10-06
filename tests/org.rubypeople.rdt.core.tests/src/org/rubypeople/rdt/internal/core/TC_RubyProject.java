package org.rubypeople.rdt.internal.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.rubypeople.rdt.core.IRubyProject;
import org.rubypeople.rdt.core.IRubyScript;
import org.rubypeople.rdt.core.tests.ModifyingResourceTest;

public class TC_RubyProject extends ModifyingResourceTest {
	
	public TC_RubyProject(String name) {
		super(name);
	}
	
	@Override
	protected void setUp() throws Exception {
		// TODO Only run once per suite/class, not every method
		super.setUp();
		setUpRubyProject("RubyProjectTests");
	}
	
	@Override
	protected void tearDown() throws Exception {
//		 TODO Only run once per suite/class, not every method
		deleteProject("RubyProjectTests");
		super.tearDown();
	}
	
	public void testGetRequiredProjectNames() throws CoreException {	
		try {
		IRubyProject p2 = createRubyProject("P2");
		waitForAutoBuild();
		editFile(
			"/P2/.loadpath", 
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?><loadpath><pathentry type=\"project\" path=\"/StoredReferencedProject\"/><pathentry type=\"project\" path=\"/AnotherStoredReferencedProject\"/></loadpath>"
		);	
		waitForAutoBuild();
		
		String[] required = p2.getRequiredProjectNames();
		assertEquals(2, required.length);
		assertEquals("StoredReferencedProject", required[0]);
		assertEquals("AnotherStoredReferencedProject", required[1]);
		} finally {
			deleteProject("P2");
		}
	}
	
	/*
	 * Ensures that adding a project prerequisite in the loadpath updates the referenced projects
	 */
	public void testAddProjectPrerequisite() throws CoreException {
		try {
			createRubyProject("P1");
			createRubyProject("P2");
			waitForAutoBuild();
			editFile(
				"/P2/.loadpath", 
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
				"<loadpath>\n" +
				"    <pathentry type=\"src\" path=\"/P1\"/>\n" +
				"</loadpath>"
			);
			waitForAutoBuild();
			IProject[] referencedProjects = getProject("P2").getReferencedProjects();
			assertResourcesEqual(
				"Unexpected project references", 
				"/P1", 
				referencedProjects);
		} finally {
			deleteProjects(new String[] {"P1", "P2"});
		}
	}
	
	/**
	 * Test that a ruby script has a corresponding resource.
	 * @throws CoreException 
	 */
	public void testRubyScriptCorrespondingResource() throws CoreException {
		addRubyNature("RubyProjectTests");
		createFolder("RubyProjectTests/q");
		createFile("RubyProjectTests/q/A.rb", "");
		IRubyScript element= getRubyScript("RubyProjectTests", "", "q", "A.rb");
		IResource corr= element.getCorrespondingResource();
		IResource res= getWorkspace().getRoot().getProject("RubyProjectTests").getFolder("q").getFile("A.rb");
		assertTrue("incorrect corresponding resource", corr.equals(res));
		assertEquals("Project is incorrect for the ruby script", "RubyProjectTests", corr.getProject().getName());
	}
	
	/*
	 * Ensures that opening a project update the project references
	 * (regression test for bug 73253 [model] Project references not set on project open)
	 */
	public void testProjectOpen() throws CoreException {
		try {
			createRubyProject("P1");
			createRubyProject("P2", new String[0], new String[0], new String[] {"/P1"});
			IProject p2 = getProject("P2");
			p2.close(null);
			p2.open(null);
			IProject[] references = p2.getDescription().getDynamicReferences();
			assertResourcesEqual(
				"Unexpected referenced projects",
				"/P1",
				references);
		} finally {
			deleteProjects(new String[] {"P1", "P2"});
		}
	}
	
	/*
	 * Ensures that importing a project correctly update the project references
	 * (regression test for bug 121569 [Import/Export] Importing projects in workspace, the default build order is alphabetical instead of by dependency)
	 */
	public void testProjectImport() throws CoreException {
		try {
			createRubyProject("P1");
			IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					createRubyProject("P2");
					editFile(
						"/P2/.loadpath", 
						"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
						"<loadpath>\n" +
						"    <pathentry type=\"src\" path=\"/P1\"/>\n" +
						"</loadpath>"
					);
				}
			};
			getWorkspace().run(runnable, null);
			waitForAutoBuild();
			IProject[] referencedProjects = getProject("P2").getReferencedProjects();
			assertResourcesEqual(
				"Unexpected project references", 
				"/P1", 
				referencedProjects);
		} finally {
			deleteProjects(new String[] {"P1", "P2"});
		}
	}
}