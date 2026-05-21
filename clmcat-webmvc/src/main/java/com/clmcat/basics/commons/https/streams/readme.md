流程处理的HTTP请求

```java
		// 最精简的请求
		String content1 = HttpUtils.stream("http://www.baidu.com")
				.get() // GET 请求
				.request() // 发送请求
				.getStringUtf8(); // 返回字符串
		
		// 有参数的GET请求
		String content2 = HttpUtils.stream("http://api-test.funbit.me/funbit/advs/homelist")
				.get()    // GET 请求
				.params() // 请求URL参数构建
				.add("userId", 0) // 添加参数
				.request()   // 发送请求
				.getStringUtf8(); // 获取字符串 UTF-8的
		
		// 复杂的POST请求
		String content3 = HttpUtils.stream("http://api-test.funbit.me/funbit/attention/set")
				.post() // POST 请求
				.header() // 构建请求头
				.set("xxx","ccc") // 设置请求头
				.content() // 构建请求包体
				.buildUrlContent() // content url参数构造
				.add("targetUserId", 55)
				.add("userId", 22222)
				.end() // 构建结束
				.request() // 发起请求
				.getStringUtf8(); // 只会获取字符串
		
		System.out.println(content1);
		System.out.println(content2);
		System.out.println(content3);

```
