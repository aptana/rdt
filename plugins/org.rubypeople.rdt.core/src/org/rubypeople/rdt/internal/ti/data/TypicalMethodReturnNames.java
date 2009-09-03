package org.rubypeople.rdt.internal.ti.data;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.rubypeople.rdt.internal.ti.BasicTypeGuess;
import org.rubypeople.rdt.internal.ti.ITypeGuess;

/**
 * Holds a bunch of typical returns for methods on core classes
 * 
 * @author Jason
 * @author cwilliams
 */
public abstract class TypicalMethodReturnNames
{

	public static Collection<ITypeGuess> get(String method)
	{
		if (method.endsWith("?"))
		{
			return createSet("TrueClass", "FalseClass");
		}
		Collection<ITypeGuess> result = TYPICAL_METHOD_RETURN_TYPE_NAMES.get(method);
		if (result == null)
			return Collections.emptySet();
		return result;
	}

	private static final Map<String, Collection<ITypeGuess>> TYPICAL_METHOD_RETURN_TYPE_NAMES = new HashMap<String, Collection<ITypeGuess>>();
	static
	{
		// TODO Read this in from some config file/property file rather than hardcode it!
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("capitalize", createSet("String"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("capitalize!", createSet("String"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("ceil", createSet("Fixnum"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("center", createSet("String"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("chomp", createSet("String"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("chomp!", createSet("String"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("chop", createSet("String"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("chop!", createSet("String"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("concat", createSet("String"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("count", createSet("Fixnum"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("crypt", createSet("String"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("downcase", createSet("String"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("downcase!", createSet("String"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("dump", createSet("String"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("floor", createSet("Fixnum"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("gets", createSet("String", "NilClass"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("gsub", createSet("String"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("gsub!", createSet("String"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("hash", createSet("Fixnum"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("index", createSet("Fixnum"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("inspect", createSet("String"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("intern", createSet("Symbol"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("length", createSet("Fixnum"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("now", createSet("Time"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("round", createSet("Fixnum"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("size", createSet("Fixnum"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("slice", createSet("String", "Array", "NilClass", "Object", "Fixnum"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("slice!", createSet("String", "Array", "NilClass", "Object", "Fixnum"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("strip", createSet("String"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("strip!", createSet("String"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("sub", createSet("String"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("sub!", createSet("String"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("swapcase", createSet("String"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("swapcase!", createSet("String"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("to_a", createSet("Array"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("to_ary", createSet("Array"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("to_i", createSet("Fixnum"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("to_int", createSet("Fixnum"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("to_f", createSet("Float"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("to_proc", createSet("Proc"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("to_s", createSet("String"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("to_str", createSet("String"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("to_string", createSet("String"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("to_sym", createSet("Symbol"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("unpack", createSet("Array"));
	}

	private static Set<ITypeGuess> createSet(String... strings)
	{
		// TODO Allow for un-equal weighting of types!
		int weight = 100 / strings.length;
		Set<ITypeGuess> set = new HashSet<ITypeGuess>();
		for (String string : strings)
		{
			set.add(new BasicTypeGuess(string, weight));
		}
		return set;
	}
}
