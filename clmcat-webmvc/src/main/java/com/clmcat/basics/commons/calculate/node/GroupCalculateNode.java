package com.clmcat.basics.commons.calculate.node;

import java.math.BigDecimal;

/**
 * 组计算
 * 
 * @author zhangxingyu
 *
 */
public class GroupCalculateNode implements CalculateNode {

	private CalculateNode left;
	private SymbolNode symbol;
	private CalculateNode right;

	@Override
	public BigDecimal calculateValue(CalculateParam param) {

		return symbol.calculateValue(param, left, right);
	}

}
