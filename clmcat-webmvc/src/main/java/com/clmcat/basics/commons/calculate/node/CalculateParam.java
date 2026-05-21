package com.clmcat.basics.commons.calculate.node;

/**
 * 计算可用的参数
 *
 */

import java.util.HashMap;
import java.util.Map;

public class CalculateParam {
	private Map<String, Object> attrs = new HashMap<>();

	public void setAttrs(Map<String, Object> attrs) {
		this.attrs = attrs;
	}

	public Map<String, Object> getAttrs() {
		return attrs;
	}

}
