package org.rubypeople.rdt.refactoring.ui.pages;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.rubypeople.rdt.refactoring.ui.pages.messages"; //$NON-NLS-1$

	public static String AccessorSelectionPage_AccessorMethod;

	public static String AccessorSelectionPage_ExampleAccessorMethod;

	public static String AccessorSelectionPage_ExampleSimpleAccessor;

	public static String AccessorSelectionPage_GenerateMethods;

	public static String AccessorSelectionPage_GenerateSimple;

	public static String AccessorSelectionPage_SelectType;

	public static String AccessorSelectionPage_SimpeAccessor;

	public static String AccessorSelectionPage_Title;

	public static String ConstructorSelectionPage_EmptyConstructor;

	public static String ConstructorSelectionPage_EmptyConstructorCode;

	public static String ConstructorSelectionPage_ParametrisedConstructor;

	public static String ConstructorSelectionPage_ParametrisedConstructorCode;

	public static String ConstructorSelectionPage_SelectConstructor;

	public static String ConvertTempToFieldPage_ClassConstructor;

	public static String ConvertTempToFieldPage_ConvertLocalVariableToField;

	public static String ConvertTempToFieldPage_CurrentMethod;

	public static String ConvertTempToFieldPage_DeclareAsClassField;

	public static String ConvertTempToFieldPage_FieldName;

	public static String ConvertTempToFieldPage_InitializeIn;

	public static String ConvertTempToFieldPage_IsNotValid;

	public static String EncapsulateFieldPage_FieldAccessor;

	public static String EncapsulateFieldPage_Name;

	public static String EncapsulateFieldPage_Reader;

	public static String EncapsulateFieldPage_SelectedField;

	public static String EncapsulateFieldPage_Title;

	public static String EncapsulateFieldPage_Writer;

	public static String ExtractMethodPage_Title;

	public static String FormatSourcePage_AlwaysParenthesize;

	public static String FormatSourcePage_Blocks;

	public static String FormatSourcePage_General;

	public static String FormatSourcePage_Indentation;

	public static String FormatSourcePage_IndentationSteps;

	public static String FormatSourcePage_MethodCallArguments;

	public static String FormatSourcePage_MethodDefArguments;

	public static String FormatSourcePage_Methods;

	public static String FormatSourcePage_NewlineBetweenClassElements;

	public static String FormatSourcePage_ParenthesizeWhereNecesary;

	public static String FormatSourcePage_SpaceAfterComma;

	public static String FormatSourcePage_SpaceAfterIterVars;

	public static String FormatSourcePage_SpaceBeforeClosingIterBracket;

	public static String FormatSourcePage_SpaceBeforeIterBrackets;

	public static String FormatSourcePage_SpaceBeforeIterVars;

	public static String FormatSourcePage_Spaces;

	public static String FormatSourcePage_SpacesAroundAssignment;

	public static String FormatSourcePage_SpacesAroundHash;

	public static String FormatSourcePage_SpacesAroundHashOperator;

	public static String FormatSourcePage_UseTab;

	public static String InlineClassPage_InlineTemp;

	public static String InlineClassPage_SelectTargetClass;

	public static String InlineMethodPage_DeleteDeclaration;

	public static String InlineMethodPage_MakeSureIsntUsed;

	public static String InlineMethodPage_Name;

	public static String InlineTempPage_ExtractToMethod;

	public static String InlineTempPage_IsNotValidName;

	public static String InlineTempPage_NewMethodName;

	public static String InlineTempPage_Occurences;

	public static String InlineTempPage_Replace;

	public static String InlineTempPage_ReplaceTempWithQuery;

	public static String MergeClassPartsInFilePage_Description;

	public static String MergeClassPartsInFilePage_Explanation;

	public static String MergeClassPartsInFilePage_SelectClassParts;

	public static String MergeWithExternalClassPartsPage_Description;

	public static String MergeWithExternalClassPartsPage_DescriptionText;

	public static String MergeWithExternalClassPartsPage_SelectParts;

	public static String MethodDownPusherSelectionPage_Title;

	public static String MoveFieldPage_AccessibleBy;

	public static String MoveFieldPage_MoveToClass;

	public static String MoveFieldPage_Target;

	public static String OccurenceReplaceSelectionPage_Line;

	public static String OccurenceReplaceSelectionPage_SelectCalls;

	public static String OverrideMethodSelectionPage_SelectMethods;

	public static String RenameFieldPage_Name;

	public static String RenameFieldPage_RenameAccessors;

	public static String RenamePage_NewName;

	public static String RenamePage_Title;

	public static String SplitTempPage_ChooseNewNames;

	public static String SplitTempPage_InvalidVariableName;

	public static String SplitTempPage_PleaseEnterName;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
