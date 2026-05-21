package com.clmcat.basics.selectserver.autoconfig;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;
import com.clmcat.basics.selectserver.mastertimer.MasterTimer;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotatedElementUtils;

/**
 * @author zhangxingyu
 */
@Slf4j
public class MasterTimerProcessor implements BeanPostProcessor, ApplicationContextAware, SmartInitializingSingleton {
	public MasterTimerProcessor() {
		log.info("定时器 - MasterTimerProcessor");
	}	
	private ApplicationContext applicationContext;
	private Map<String, MasterTimer> masterTimers;
	private MasterTimer defaultMasterTimer;


	public final static String DEFAULT_MASTER_TIMER_BEAN_NAME = "defaultMasterTimer";

	private static Map<Class<?> , List<MethodTimer>> methodTimerMap = new ConcurrentHashMap<>();

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	private void init()  {
		if (masterTimers == null) {
			masterTimers = new HashMap<>();
			try {
				Map<String, MasterTimer> masterTimersTmp = applicationContext.getBeansOfType(MasterTimer.class);
				if (masterTimersTmp != null && !masterTimersTmp.isEmpty()) {
					if (masterTimersTmp.containsKey(DEFAULT_MASTER_TIMER_BEAN_NAME)) {
						defaultMasterTimer = masterTimersTmp.get(DEFAULT_MASTER_TIMER_BEAN_NAME);
					} else {
						defaultMasterTimer = masterTimersTmp.values().iterator().next();
					}
				}
				for (Entry<String, MasterTimer> entry : masterTimersTmp.entrySet()) {
					MasterTimer masterTimer = entry.getValue();
					masterTimers.put(masterTimer.getMasterName(), masterTimer);
				}
			} catch (Exception e) {
				log.error("初始化定时器失败", e);
			}
		}
	}
	
	private MasterTimer getMasterTimer(String masterName) {
		init();
		MasterTimer masterTimer;
		if (masterName.isEmpty()) {
			masterTimer = defaultMasterTimer;
		} else {
			masterTimer = masterTimers.get(masterName);
		}
		return masterTimer;
	}
	

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		try {
			Class<?> targetType = AopUtils.getTargetClass(bean);
			String targetName = targetType.getName();
			// 过滤掉这些前缀.
			if (targetName.startsWith("org.springframework.")
					|| targetName.startsWith("org.apache.")
					|| targetName.startsWith("java.")
					|| targetName.startsWith("javax.")
					|| targetName.startsWith("org.mybatis.")
					|| targetName.startsWith("com.alibaba.druid.")
					|| targetName.startsWith("com.sun.")
					|| targetName.startsWith("org.redisson.")
					|| targetName.startsWith("com.google.")
					|| targetName.startsWith("com.fasterxml.jackson")
					) {
				return bean;
			}
			// 查找可以做的任务
			List<MethodTimer> methods = find(targetType, bean);
			if (methods.size() > 0) {
				
				methodTimerMap.put(targetType, methods);
			}
		} catch (Exception e) {
			log.error("定时器创建出现故障: " + beanName , e);
		}
		return bean;
	}
	
	private void finishRegistration() {
		methodTimerMap.forEach((beanType, methods)->{
			String typeName = beanType.getSimpleName();
			Map<String, List<MethodTimer>> group = group(methods);
			for (Entry<String, List<MethodTimer>> entry : group.entrySet()) {
				String masterName = entry.getKey().trim();
				
				MasterTimer masterTimer = getMasterTimer(masterName);
				
				if (masterTimer == null) {
					for (MethodTimer methodTimer : entry.getValue()) {
						log.warn("MasterTimer 的名字不存在 (name no exist) " + masterName + "---" + typeName + "---" + beanType + "." + methodTimer);
					}
				} else {
					for (MethodTimer methodTimer : entry.getValue()) {
						String timerName = methodTimer.getTimerName();
						if (methodTimer.isDelay()) {
							int delay = methodTimer.getDelay();
							TimeUnit timeUnit = methodTimer.getTimeUnit();
							masterTimer.addTimer(timerName, methodTimer, delay, timeUnit);
							log.info("[timer-delay] " + typeName +"." + methodTimer);
						} else if (methodTimer.isCron()) {
							String cron = methodTimer.getCron();
							TimeZone timeZone = methodTimer.getTimeZone();
							masterTimer.addTimer(timerName, methodTimer, cron, timeZone);
							log.info("[timer-cron] " + typeName +"." + methodTimer);
						}
					}
				}
			}
			
		});
	}
	
	@Override
	public void afterSingletonsInstantiated() {
		finishRegistration();
	}

	List<MethodTimer> find(Class<?> targetType, Object bean) {
		List<MethodTimer> methods = new ArrayList<MethodTimer>();
		List<Class<?>> types = new ArrayList<Class<?>>();
		types.add(targetType);
		for (int i = 0; i < types.size(); i++) {
			Class<?> type = types.get(i);

			Method[] ms = type.getDeclaredMethods();
			for (Method method : ms) {
				if (method.getParameterCount() == 0) {
					Timer timer = AnnotatedElementUtils.getMergedAnnotation(method, Timer.class);
					if (timer != null) {
						if (!method.isAccessible()) {
							method.setAccessible(true);
						}
						methods.add(new MethodTimer(method, timer, bean));
					}
				}
			}
			Class<?> superType = type.getSuperclass();
			if (superType != Object.class && superType != null) {
				types.add(superType);
			}
		}
		return methods;
	}
	
	Map<String, List<MethodTimer>> group(List<MethodTimer> methodTimers) {
		Map<String, List<MethodTimer>> map = new HashMap<String, List<MethodTimer>>();
		for (MethodTimer methodTimer : methodTimers) {
			List<MethodTimer> list = map.get(methodTimer.getMasterName());
			if (list == null) {
				map.put(methodTimer.getMasterName(), list = new ArrayList<MethodTimer>());
			}
			list.add(methodTimer);
		}
		return map;
	}

	/**
	 * 方法定时器
	 * 
	 * @author zhangxingyu
	 *
	 */
	public static class MethodTimer implements Runnable {
		Method method;
		Timer timer;
		Object bean;
		MethodTimer(Method method, Timer timer, Object bean) {
			this.method = method;
			this.timer = timer;
			this.bean = bean;
		}

		public boolean isCron() {
			return !isDelay() && !timer.cron().trim().isEmpty();
		}

		public boolean isDelay() {
			return timer.delay() > 0;
		}

		public TimeZone getTimeZone() {
			if (timer.timeZone().trim().isEmpty()) {
				return TimeZone.getDefault();
			}
			return TimeZone.getTimeZone(timer.timeZone().trim());
		}

		public String getCron() {
			return timer.cron();
		}

		public int getDelay() {
			return timer.delay();
		}

		public TimeUnit getTimeUnit() {
			return timer.timeUnit();
		}

		public Method getMethod() {
			return method;
		}

		public String getMasterName() {
			return timer.value();
		}
		
		public String getTimerName() {
			return bean.getClass().getSimpleName() + "." + method.getName();
		}

		@Override
		public void run() {
			try {
				method.invoke(bean);
			} catch (Exception e) {
				throw new MethodTimerException("MethodTimer 执行异常! 请自行捕获.", e, this);
			}
		}
		@Override
		public String toString() {
			return method.getName()+"() -> " + timer;
		}
	}
	
	public static class MethodTimerException extends RuntimeException {

		private static final long serialVersionUID = 1L;
		private MethodTimer methodTimer;
		public MethodTimerException(String message, Throwable cause, MethodTimer methodTimer) {
			super(message, cause);
			this.methodTimer = methodTimer;
		}

		public MethodTimerException(String message, MethodTimer methodTimer) {
			super(message);
			this.methodTimer = methodTimer;
		}
		
		public MethodTimer getMethodTimer() {
			return methodTimer;
		}
	}

}
