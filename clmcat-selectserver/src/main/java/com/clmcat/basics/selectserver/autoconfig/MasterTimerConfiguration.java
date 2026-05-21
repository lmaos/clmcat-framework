package com.clmcat.basics.selectserver.autoconfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MasterTimerConfiguration  {

	@Bean
	MasterTimerProcessor masterTimerProcessor() {
		return new MasterTimerProcessor();
	}

//	@Override
//	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
//		try {
//			beanFactory.addBeanPostProcessor(masterTimerProcessor());
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}

}
