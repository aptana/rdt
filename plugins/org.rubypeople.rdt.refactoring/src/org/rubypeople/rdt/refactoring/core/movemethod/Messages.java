package org.rubypeople.rdt.refactoring.core.movemethod;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.rubypeople.rdt.refactoring.core.movemethod.messages"; //$NON-NLS-1$

	public static String MethodMover_An;

	public static String MethodMover_DuToNameConflicts;

	public static String MethodMover_ForField;

	public static String MethodMover_IsChangedFrom;

	public static String MethodMover_NameWillBeChangedTo;

	public static String MethodMover_TheVisibilityOfMethod;

	public static String MethodMover_TheVisibilityOfTheMovingMethod;

	public static String MethodMover_To;

	public static String MethodMover_WillBeChangedToPublic;

	public static String MethodMover_WillBeGenerated;

	public static String MoveMethodConditionChecker_CanBeCalledFromOutside;

	public static String MoveMethodConditionChecker_CannotMoveConstructor;

	public static String MoveMethodConditionChecker_ContainsClassField;

	public static String MoveMethodConditionChecker_MightNotGetReplaced;

	public static String MoveMethodConditionChecker_MovingMightAffectTheFunctionality;

	public static String MoveMethodConditionChecker_NeedsToBeInsideClass;

	public static String MoveMethodConditionChecker_NeedsToBeInsideMethod;

	public static String MoveMethodConditionChecker_NoFieldOfTargetType;

	public static String MoveMethodConditionChecker_NoTarget;

	public static String MoveMethodConditionChecker_TheMethod;

	public static String MoveMethodRefactoring_Name;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
