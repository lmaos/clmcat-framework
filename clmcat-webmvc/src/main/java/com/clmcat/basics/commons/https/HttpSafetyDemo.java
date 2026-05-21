package com.clmcat.basics.commons.https;

import com.clmcat.basics.commons.https.safety.HttpSafetyFailRecord;
import com.clmcat.basics.commons.https.safety.HttpSafetyManage;
import com.clmcat.basics.commons.https.safety.HttpSafetyStreamConfig;

public class HttpSafetyDemo {
	public static void main(String[] args)  {
		HttpSafetyStreamConfig httpSafetyConfig = new HttpSafetyStreamConfig("我是默认只内容");
		// 配置 所有失败  10秒内失败5次, 一秒内使用默认值. 条件可以配置多个
		httpSafetyConfig.configFAIL("10秒拦截", new HttpSafetyFailRecord(5, 10_000, 1000, null));
		httpSafetyConfig.configFAIL("20秒拦截", new HttpSafetyFailRecord(8, 20_000, 1000, null));
		
		// 配置 60秒内出现5次 code = 50x 则睡眠5秒
		httpSafetyConfig.configSN(500, new HttpSafetyFailRecord(5, 60_000, 15000, null));
		//httpSafetyConfig.configSN(400, new HttpSafetyFailRecord(5, 60_000, 15000, null));
		
		// 配置拦截 http|https://abcdef
		HttpSafetyManage.getHttpSafetyManage().setConfig("abcdef", httpSafetyConfig);
		
		for (int i = 0; i < 20; i++) {
			try {
				String result = HttpUtils.stream("http://abcdef?a=100&b=100").get().request().getStringUtf8();
				System.out.println(result);// 错误的默认值就输出
				if ("我是默认只内容".equals(result)) { 
					Thread.sleep(1000);  // 延迟1秒 假装一下 1秒过期.
				}
			} catch (Exception e) {
				//e.printStackTrace();
				System.out.println("err"); // 访问不存在的URL会进入异常处理
			}
		}
	}
}
