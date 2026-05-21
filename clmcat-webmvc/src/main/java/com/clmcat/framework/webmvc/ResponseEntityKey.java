package com.clmcat.framework.webmvc;

import com.alibaba.fastjson.serializer.ValueFilter;

import com.clmcat.framework.webmvc.result.SerializerValue;

/**
 * 应答实体输出的key名称.
 * 
 * @author zhangxingyu
 *
 */
public interface ResponseEntityKey {
	
	public static final String KEY = "ResponseEntityKey";

	/**
	 * 数字状态码名称， 默认： status， 比如要修改为 code， 不想输出： null
	 */
	default String getStatusName() {
		return "status";
	}

	/**
	 * 状态码名称， 默认： state， 比如要修改为 xxx， 不想输出： null
	 */
	default String getStateName() {
		return "state";
	}

	/**
	 * 内容名称， 默认： content， 比如要修改为 data， 不想输出： null
	 */
	default String getContentName() {
		return "content";
	}

	/**
	 * 状态信息名称， 默认： message， 比如要修改为 msg， 不想输出： null
	 */
	default String getMessageName() {
		return "message";
	}

	/**
	 * 错误位置名称， 默认： errplace， 比如要修改为 name， 不想输出： null
	 */
	default String getErrplaceName() {
		return "errplace";
	}

	/**
	 * 请求ID名称， 默认： requestId， 比如要修改为 xxx， 不想输出： null
	 */
	default String getRequestIdName() {
		return "requestId";
	}

	/**
	 * 错误信息多语言的的KEY， 默认： localeMessage， 比如要修改为 xxx， 不想输出： null
	 */
	default String getLocaleMessageName() {
		return null;
	}

	/**
	 * 是否存在状态码名称， 默认： true， 如果不想输出，则返回false
	 */
	default boolean existStatusName() {
		return getStatusName() != null;
	}
	/** 是否存在状态信息名称， 默认： true， 如果不想输出，则返回false */
	default boolean existStateName() {
		return getStateName() != null;
	}
	/** 是否存在内容名称， 默认： true， 如果不想输出，则返回false */
	default boolean existContentName() {
		return getContentName() != null;
	}
	/** 是否存在状态信息名称， 默认： true， 如果不想输出，则返回false */
	default boolean existMessageName() {
		return getMessageName() != null;
	}
	/** 是否存在错误位置名称， 默认： true， 如果不想输出，则返回false */
	default boolean existErrplaceName() {
		return getErrplaceName() != null;
	}
	/** 是否存在请求ID名称， 默认： true， 如果不想输出，则返回false */
	default boolean existRequestIdName() {
		return getRequestIdName() != null;
	}
	/** 是否存在错误信息多语言的KEY， 默认： true， 如果不想输出，则返回false */
	default boolean existLocaleMessageName() {
		return getLocaleMessageName() != null;
	}
	/** 值过滤器， 默认： SerializerValue.defaultSerializerValue */
	default ValueFilter valueformat() {
		return SerializerValue.defaultSerializerValue;
	}

	public static class DefaultResponseEntityKey implements ResponseEntityKey {

		public static DefaultResponseEntityKey defaultInstance = new DefaultResponseEntityKey();

		@Override
		public String getStatusName() {
			return "status";
		}

		@Override
		public String getStateName() {
			return "state";
		}

		@Override
		public String getContentName() {
			return "content";
		}

		@Override
		public String getMessageName() {
			return "message";
		}

		@Override
		public String getErrplaceName() {
			return "errplace";
		}

	}
}
