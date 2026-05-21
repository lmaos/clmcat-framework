package com.clmcat.basics.commons.calculate.node;

import java.math.BigDecimal;

/**
 * 符号节点
 * 
 * @author zhangxingyu
 *
 */
public interface SymbolNode {

	/**
	 * 计算值
	 * 
	 * @param left  左侧计算节点
	 * @param right 右侧计算节点
	 * @return
	 */
	BigDecimal calculateValue(CalculateParam param, CalculateNode left, CalculateNode right);

}
