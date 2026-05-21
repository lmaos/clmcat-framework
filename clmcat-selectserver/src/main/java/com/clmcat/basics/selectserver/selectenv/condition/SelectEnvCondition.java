package com.clmcat.basics.selectserver.selectenv.condition;

import java.util.Map;

import com.clmcat.basics.commons.lang.StringUtils;
import com.clmcat.basics.selectserver.selectenv.anns.SelectEnv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * function.timer.{env}.enable=true
 */
public class SelectEnvCondition implements Condition {
	
	private static final Logger log = LoggerFactory.getLogger(SelectEnvCondition.class);

	@Override
	public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
		try {
			Map<String, Object> map = metadata.getAnnotationAttributes(SelectEnv.class.getName());
			String value = (String)map.get("value");
			Environment env = context.getEnvironment();
			// function.timer.{env}.enable=true
			String functionTimeEnv = "function.timer." + value + ".enable";
			String enable = env.getProperty(functionTimeEnv, "false");
			if (StringUtils.isBlank(enable)) {
				return false;
			}
			if ("true".equals(enable.trim().toLowerCase())) {
				log.info("启用定时任务配置: " + functionTimeEnv);
				return true;
			}
		} catch (Exception e) {
			log.error("条件选择异常： " , e);
		}
		return false;
	}


}
