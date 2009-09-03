package org.rubypeople.rdt.refactoring.ui.pages.movemethod;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.rubypeople.rdt.refactoring.ui.pages.movemethod.messages"; //$NON-NLS-1$

	public static String FirstMoveMethodPage_Title;

	public static String FirstMoveMethodPageComposite_DelegatesCalls;

	public static String FirstMoveMethodPageComposite_LeaveDelegate;

	public static String FirstMoveMethodPageComposite_MoveToClass;

	public static String FirstMoveMethodPageComposite_SelectedClass;

	public static String FirstMoveMethodPageComposite_SelectedMethod;

	public static String FirstMoveMethodPageComposite_Selection;

	public static String FirstMoveMethodPageComposite_Visibility;

	public static String SecondMoveMethodPage_Title;

	public static String SecondMoveMethodPageComposite_FieldReference;

	public static String SecondMoveMethodPageComposite_MaintainCalls;

	public static String SecondMoveMethodPageComposite_RequiredInClass;

	public static String SecondMoveMethodPageComposite_SelectField;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
