package org.rubypeople.rdt.internal.ti;

import java.util.Collection;

import junit.framework.TestCase;

public abstract class TypeInferrerTestCase extends TestCase
{

	protected ITypeInferrer inferrer;

	public TypeInferrerTestCase()
	{
		super();
	}

	public void setUp()
	{
		inferrer = createTypeInferrer();
	}

	/**
	 * Shortcut for testing that a particular type is the only one inferred, and is inferred with 100% confidence
	 * 
	 * @param guesses
	 * @param type
	 */
	protected void assertInfersTypeWithoutDoubt(Collection<ITypeGuess> guesses, String type)
	{
		assertEquals(1, guesses.size());
		ITypeGuess guess = guesses.iterator().next();
		assertEquals(type, guess.getType());
		assertEquals(100, guess.getConfidence());
	}

	/**
	 * Shortcut for testing that two types are inferred, each with 50% confidence
	 * 
	 * @param guesses
	 * @param type1
	 * @param type2
	 */
	protected void assertInfersTypeFiftyFifty(Collection<ITypeGuess> guesses, String type1, String type2)
	{
		assertEquals(2, guesses.size());
		ITypeGuess guess1 = findGuess(guesses, type1);
		assertNotNull("No Type Guess found with type: " + type1, guess1);
		assertEquals(guess1.getConfidence(), 50);

		ITypeGuess guess2 = findGuess(guesses, type2);
		assertNotNull("No Type Guess found with type: " + type2, guess2);
		assertEquals(guess2.getConfidence(), 50);
	}

	private ITypeGuess findGuess(Collection<ITypeGuess> guesses, String type1)
	{
		for (ITypeGuess typeGuess : guesses)
		{
			if (typeGuess.getType().equals(type1))
			{
				return typeGuess;
			}
		}
		return null;
	}

	/**
	 * Override this method in subclasses so that we can test any implementation of ITypeInferrer the same way.
	 * 
	 * @return an implementation of ITypeInferrer
	 */
	protected abstract ITypeInferrer createTypeInferrer();

}