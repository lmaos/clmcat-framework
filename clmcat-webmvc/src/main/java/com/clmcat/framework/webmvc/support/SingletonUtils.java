package com.clmcat.framework.webmvc.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 
 * @author zhangxingyu
 *
 */
public class SingletonUtils {
	
	public static Map<Class<?>, Optional<?>> singleton = new ConcurrentHashMap<>();
	
	private static Logger logger = LoggerFactory.getLogger(SingletonUtils.class);
	
	public static <T>T getSingleton(Class<T> entityType) {
		if (entityType == null || entityType.isInterface()) {
			return null;
		}
		Optional<?> optional = singleton.get(entityType);
		if (optional == null) {
			try {
				optional = Optional.ofNullable(entityType.newInstance());
			} catch (Exception e) {
				logger.error("单例对象 {} 创建失败!", entityType, e);
				optional = Optional.empty();
			}
			singleton.put(entityType, optional);
		}
		return optional.isPresent() ? (T)optional.get() : null;
	}
	
	public static void main(String[] args) {
		System.out.println(SingletonUtils.getSingleton(LakisaResponseKey.class));
	}
}
