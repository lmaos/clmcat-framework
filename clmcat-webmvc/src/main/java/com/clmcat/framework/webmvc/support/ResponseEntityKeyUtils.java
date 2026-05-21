package com.clmcat.framework.webmvc.support;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.clmcat.framework.webmvc.ResponseEntityKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * 
 * @author zhangxingyu
 *
 */
public class ResponseEntityKeyUtils {
	
	public static Map<Class<? extends ResponseEntityKey>, Optional<ResponseEntityKey>> singleton = new ConcurrentHashMap<>();
	
	private static Logger logger = LoggerFactory.getLogger(ResponseEntityKeyUtils.class);
	
	public static ResponseEntityKey getSingleton(Class<? extends ResponseEntityKey> entityType) {
		if (entityType == null || entityType.isInterface()) {
			return null;
		}
		Optional<ResponseEntityKey> optional = singleton.get(entityType);
		if (optional == null) {
			try {
				optional = Optional.ofNullable(entityType.newInstance());
			} catch (Exception e) {
				logger.error("response key 创建失败!", e);
				optional = Optional.empty();
			}
			singleton.put(entityType, optional);
		}
		return optional.isPresent() ? optional.get() : null;
	}
	
	public static void main(String[] args) {
		System.out.println(ResponseEntityKeyUtils.getSingleton(LakisaResponseKey.class));
	}
}
