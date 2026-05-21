package com.clmcat.basics.commons.calculate.node;

import java.math.BigDecimal;
/**
 * 数字元素节点
 *
 */
public class NumberCalculateNode implements CalculateNode {

	private final BigDecimal value;
	
	
	public NumberCalculateNode(BigDecimal value) {
		this.value = value;
	}


	@Override
	public BigDecimal calculateValue(CalculateParam param) {
		
		return value;
	}

}
