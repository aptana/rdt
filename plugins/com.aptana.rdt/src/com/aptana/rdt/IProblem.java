package com.aptana.rdt;

public interface IProblem
{
	public static final int MisspelledConstructor = 128;
	public static final int ConstantNamingConvention = 129;
	public static final int MethodMissingWithoutRespondTo = 130;
	public static final int LocalAndMethodNamingConvention = 131;
	public static final int ComparableInclusionMissingCompareMethod = 132;
	public static final int EnumerableInclusionMissingEachMethod = 133;
	public static final int LocalVariablePossibleAttributeAccess = 134;
	public static final int PossibleAccidentalBooleanAssignment = 135;
	public static final int DeprecatedRequireGem = 136;
	public static final int RetryOutsideRescueBody = 137;
	public static final int DynamicVariableAliasesLocal = 138;
	public static final int DuplicateHashKey = 139;

	/**
	 * @since 1.3.0
	 */
	public static final int ControlCouple = 140;
	
	/**
	 * @since 1.3.0
	 */
	public static final int FeatureEnvy = 141;
	
	/**
	 * @since 1.3.0
	 */
	public static final int UncommunicativeName = 142;
	
	/**
	 * @since 1.3.0
	 */
	public static final int LocalMaskingMethod = 143;
	
	/**
	 * @since 1.3.1
	 */
	public static final int DuplicateCodeStructure = 144;
}
