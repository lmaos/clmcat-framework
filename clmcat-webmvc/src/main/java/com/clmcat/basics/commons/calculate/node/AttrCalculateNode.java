package com.clmcat.basics.commons.calculate.node;

import java.math.BigDecimal;

/**
 * 属性元素节点
 *
 */
public class AttrCalculateNode implements CalculateNode {

	private BigDecimal defValue;
	private String name;

	public AttrCalculateNode(String name) {
		this.defValue = BigDecimal.ZERO;
	}

	public AttrCalculateNode(String name, BigDecimal defValue) {
		this.defValue = defValue;
		this.name = name;
	}

	@Override
	public BigDecimal calculateValue(CalculateParam param ) {
		if (param.getAttrs() != null) {
			Object value = param.getAttrs().get(name);
			if (value != null) {
				return new BigDecimal(String.valueOf(value).trim());
			}
		}
		return defValue;
	}

}
