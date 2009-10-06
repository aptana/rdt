package org.rubypeople.rdt.internal.ti;

/**
 * @author Jason
 */
public class TypeInferrerTest extends CombinedTypeInferrerTest
{

	protected ITypeInferrer createTypeInferrer()
	{
		return new DefaultTypeInferrer();
	}
}
