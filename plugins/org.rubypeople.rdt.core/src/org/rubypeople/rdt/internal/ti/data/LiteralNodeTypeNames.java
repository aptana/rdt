package org.rubypeople.rdt.internal.ti.data;

import java.util.HashMap;
import java.util.Map;

import org.jruby.ast.ArrayNode;
import org.jruby.ast.BignumNode;
import org.jruby.ast.DRegexpNode;
import org.jruby.ast.DStrNode;
import org.jruby.ast.DSymbolNode;
import org.jruby.ast.DXStrNode;
import org.jruby.ast.FalseNode;
import org.jruby.ast.FixnumNode;
import org.jruby.ast.FloatNode;
import org.jruby.ast.HashNode;
import org.jruby.ast.NilImplicitNode;
import org.jruby.ast.NilNode;
import org.jruby.ast.RegexpNode;
import org.jruby.ast.StrNode;
import org.jruby.ast.SymbolNode;
import org.jruby.ast.TrueNode;
import org.jruby.ast.XStrNode;
import org.jruby.ast.ZArrayNode;

/**
 * Maps from JRuby AST Literal Node classnames to the Ruby type they represent.
 * 
 * @author Jason
 * @author cwilliams
 */
public abstract class LiteralNodeTypeNames
{
	public static String get(String nodeType)
	{
		return CONST_NODE_TYPE_NAMES.get(nodeType);
	}

	private static final Map<String, String> CONST_NODE_TYPE_NAMES = new HashMap<String, String>();
	static
	{
		CONST_NODE_TYPE_NAMES.put(ArrayNode.class.getSimpleName(), "Array");
		CONST_NODE_TYPE_NAMES.put(BignumNode.class.getSimpleName(), "Bignum");
		CONST_NODE_TYPE_NAMES.put(DRegexpNode.class.getSimpleName(), "Regexp");
		CONST_NODE_TYPE_NAMES.put(DStrNode.class.getSimpleName(), "String");
		CONST_NODE_TYPE_NAMES.put(DSymbolNode.class.getSimpleName(), "Symbol");
		CONST_NODE_TYPE_NAMES.put(DXStrNode.class.getSimpleName(), "String");
		CONST_NODE_TYPE_NAMES.put(FalseNode.class.getSimpleName(), "FalseClass");
		CONST_NODE_TYPE_NAMES.put(FixnumNode.class.getSimpleName(), "Fixnum");
		CONST_NODE_TYPE_NAMES.put(FloatNode.class.getSimpleName(), "Float");
		CONST_NODE_TYPE_NAMES.put(HashNode.class.getSimpleName(), "Hash");
		CONST_NODE_TYPE_NAMES.put(NilNode.class.getSimpleName(), "NilClass");
		CONST_NODE_TYPE_NAMES.put(NilImplicitNode.class.getSimpleName(), "NilClass");
		CONST_NODE_TYPE_NAMES.put(RegexpNode.class.getSimpleName(), "Regexp");
		CONST_NODE_TYPE_NAMES.put(StrNode.class.getSimpleName(), "String");
		CONST_NODE_TYPE_NAMES.put(SymbolNode.class.getSimpleName(), "Symbol");
		CONST_NODE_TYPE_NAMES.put(TrueNode.class.getSimpleName(), "TrueClass");
		CONST_NODE_TYPE_NAMES.put(XStrNode.class.getSimpleName(), "String");
		CONST_NODE_TYPE_NAMES.put(ZArrayNode.class.getSimpleName(), "Array");
	}
}
