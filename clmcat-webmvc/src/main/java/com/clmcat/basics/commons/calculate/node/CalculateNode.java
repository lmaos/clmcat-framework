package com.clmcat.basics.commons.calculate.node;

import java.math.BigDecimal;

/**
 * 计算节点
 * 
 * @author zhangxingyu
 *
 */
public interface CalculateNode {
	/**
	 * 计算的值
	 * 
	 */
	BigDecimal calculateValue(CalculateParam param);
}
