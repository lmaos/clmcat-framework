package com.clmcat.basics.commons.https;

import com.alibaba.fastjson.JSON;
import com.clmcat.basics.commons.https.streams.HttpStreamException;

public class HttpUtilsDemo {
	public static void main(String[] args) throws HttpStreamException {
		String state =
		HttpUtils.stream("https://api-test.funbit.me/litaearth/start/aws")
		.get()                                         // GET请求
		.header().setContentType("application/json")   // 设置 header
		.params().add("hello", "1234")                 // 设置请求参数
		.request()                                     // 发送请求
		.call()                                        // 链式结果处理
		.next(n->n.getStringUtf8())                    // 解析结果返回UTF8String
		.next(JSON::parseObject)                       // 解析成JSON对象
		.next(n->n.getString("state"))                 // 解析状态
		.next(n->n.equals("OK")?"成功":"失败")           // 转译状态名
		.callCatch(n->"解析异常")                        // 异常处理
		.getResult("无结果")                            // 获取最终结果,如果结果不存在使用默认值
		;
		
		System.out.println(state); // 成功
	}
}
