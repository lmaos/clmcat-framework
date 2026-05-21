package com.clmcat.basics.commons.calculate.node;

import java.util.HashMap;
import java.util.Map;

public class CalculateConfig {
	public final char[] DEFAULT_SYMBOL_NODE = "+-*/^%".toCharArray();
	private char[] symbolNodeChars = DEFAULT_SYMBOL_NODE;
	private Map<Character, SymbolNode> symbolNodes = new HashMap<Character, SymbolNode>();
	
	public char[] getSymbolNodeChars() {
		return symbolNodeChars;
	}

	public void setSymbolNodeChars(char[] symbolNodeChars) {
		this.symbolNodeChars = symbolNodeChars;
	}
	
	public SymbolNode getSymbolNode(char symbol) {
		return symbolNodes.get(symbol);
	}
	
	public void putSymbolNode(char symbol, SymbolNode node) {
		symbolNodes.put(symbol, node);
	}
}
