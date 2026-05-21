package com.clmcat.basics.commons.https;

import com.clmcat.basics.commons.https.HttpAsyncResponseCallback.CallStatus;
import com.clmcat.basics.commons.https.streams.HttpRequestStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.message.HeaderGroup;
import org.apache.logging.log4j.ThreadContext;
import com.clmcat.basics.commons.util.ThreadNamed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class HttpUtils {
	private static CloseableHttpClient httpClient;    // 同步请求客户端
	private static CloseableHttpAsyncClient asyncHttpClient; // 异步请求客户端
	
	private static final Logger log = LoggerFactory.getLogger(HttpUtils.class);

	static {
		RequestConfig config = RequestConfig.custom() //
				.setConnectTimeout(10000) //
				.setConnectionRequestTimeout(5000) //
				.setSocketTimeout(10000) //
				.build(); //
		httpClient = HttpClientBuilder.create()//
				.setMaxConnPerRoute(100)
				.setMaxConnTotal(1000)
				.setConnectionTimeToLive(10, TimeUnit.SECONDS)//
				.disableContentCompression()
				.setDefaultRequestConfig(config).build();//
		asyncHttpClient = HttpAsyncClientBuilder.create()
				.setMaxConnPerRoute(100)
				.setMaxConnTotal(1000)
				.setThreadFactory(ThreadNamed.of("http-async-request"))
				.setDefaultRequestConfig(config).build();//
		asyncHttpClient.start();
	}

	/**
	 * 直接请求
	 * 
	 * @param uri        uri
	 * @param httpMethod HttpMethod.POST, HttpMethod.GET
	 * @param headers    http头
	 * @param content    post时候使用的content
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static CloseableHttpResponse request(String uri, HttpMethod httpMethod, HeaderGroup headers,
			HttpEntity content, RequestConfig requestConfig) throws ClientProtocolException, IOException {
		return request(uri, httpMethod, headers, content, requestConfig, false);
	}
	
	/**
	 * 直接请求
	 * 
	 * @param uri        uri
	 * @param httpMethod HttpMethod.POST, HttpMethod.GET
	 * @param headers    http头
	 * @param content    post时候使用的content
	 * @param trace 请求跟踪
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static CloseableHttpResponse request(String uri, HttpMethod httpMethod, HeaderGroup headers,
			HttpEntity content, RequestConfig requestConfig, boolean trace) throws ClientProtocolException, IOException {

		HttpRequestBase request = null;
		if (httpMethod == HttpMethod.POST) {
			HttpPost httoPost = new HttpPost(uri);
			if (content != null) {
				httoPost.setEntity(content);
			}
			request = httoPost;
		} else if (httpMethod == HttpMethod.GET) {
			HttpGet httpGet = new HttpGet(uri);
			request = httpGet;
		}
		if (headers != null) {
			request.setHeaders(headers.getAllHeaders());
		}
		if (requestConfig != null) {
			request.setConfig(requestConfig);
		}
		if (trace) {
			try {
				String requestId = ThreadContext.get("requestId");
				if (StringUtils.isNotBlank(requestId)) {
					request.addHeader("requestId", requestId);
				}
			} catch (Exception e) {
				log.error("请求跟踪,URL请求时获取ID错误", e);
			}
		}
		return httpClient.execute(request);
	}
	
	/**
	 * 直接请求
	 * 
	 * @param uri        uri
	 * @param httpMethod HttpMethod.POST, HttpMethod.GET
	 * @param headers    http头
	 * @param content    post时候使用的content
	 * @return 
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static Future<HttpResponse> asyncRequest(String uri, HttpMethod httpMethod, HeaderGroup headers,
			HttpEntity content, RequestConfig requestConfig,
			HttpAsyncResponseCallback callback) throws ClientProtocolException, IOException {
		return asyncRequest(uri, httpMethod, headers, content, requestConfig, callback, false);
	}
	
	/**
	 * 直接请求
	 * 
	 * @param uri        uri
	 * @param httpMethod HttpMethod.POST, HttpMethod.GET
	 * @param headers    http头
	 * @param content    post时候使用的content
	 * @param trace 请求跟踪
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static Future<HttpResponse> asyncRequest(String uri, HttpMethod httpMethod, HeaderGroup headers,
			HttpEntity content, RequestConfig requestConfig,
			HttpAsyncResponseCallback callback, boolean trace) throws ClientProtocolException, IOException {

		HttpRequestBase request = null;
		if (httpMethod == HttpMethod.POST) {
			HttpPost httoPost = new HttpPost(uri);
			if (content != null) {
				httoPost.setEntity(content);
			}
			request = httoPost;
		} else if (httpMethod == HttpMethod.GET) {
			HttpGet httpGet = new HttpGet(uri);
			request = httpGet;
		}
		if (headers != null) {
			request.setHeaders(headers.getAllHeaders());
		}
		if (requestConfig != null) {
			request.setConfig(requestConfig);
		}
		if (trace) {
			try {
				String requestId = ThreadContext.get("requestId");
				if (StringUtils.isNotBlank(requestId)) {
					request.addHeader("requestId", requestId);
				}
			} catch (Exception e) {
				log.error("请求跟踪,URL请求时获取ID错误", e);
			}
		}
		return asyncHttpClient.execute(request, new FutureCallback<HttpResponse>() {
			@Override
			public void failed(Exception ex) {
				callback.response(null, ex, CallStatus.fail);
			}
			@Override
			public void completed(HttpResponse result) {
				callback.response(result, null, CallStatus.ok);
			}
			@Override
			public void cancelled() {
				callback.response(null, null, CallStatus.cancel);
			}
		});
	}
	
	/**
	 * POST 请求
	 * 
	 * @param uri     请求的URI
	 * @param headers 头信息
	 * @param content 请求包体
	 * @return http应答对象
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static CloseableHttpResponse post(String uri, HeaderGroup headers, HttpEntity content)
			throws ClientProtocolException, IOException {
		return post(uri, headers, content, (RequestConfig)null);
	}

	/**
	 * POST 请求
	 * 
	 * @param uri     请求的URI
	 * @param headers 头信息
	 * @param content 请求包体
	 * @return http应答对象
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static CloseableHttpResponse post(String uri, HeaderGroup headers, HttpEntity content, RequestConfig requestConfig)
			throws ClientProtocolException, IOException {
		return request(uri, HttpMethod.POST, headers, content, requestConfig);
	}
	
	public static CloseableHttpResponse post(String uri, HeaderGroup headers, HttpEntity content, List<HttpUrlParam> urlParams)
			throws ClientProtocolException, IOException {
		return post(uri, headers, content, urlParams, (RequestConfig)null);
	}
	
	public static CloseableHttpResponse post(String uri, HeaderGroup headers, HttpEntity content, List<HttpUrlParam> urlParams, RequestConfig requestConfig)
			throws ClientProtocolException, IOException {
		return post(uri, headers, content, urlParams, requestConfig, false);
	}
	// @param trace 请求跟踪
	public static CloseableHttpResponse post(String uri, HeaderGroup headers, HttpEntity content, List<HttpUrlParam> urlParams, RequestConfig requestConfig, boolean trace)
			throws ClientProtocolException, IOException {
		if (urlParams != null && urlParams.size() > 0) {
			uri = formatUrl(uri, urlParams);
		}
		return request(uri, HttpMethod.POST, headers, content, requestConfig, trace);
	}
	
	public static Future<HttpResponse> post(String uri, HeaderGroup headers, HttpEntity content, List<HttpUrlParam> urlParams, RequestConfig requestConfig, HttpAsyncResponseCallback callback)
			throws ClientProtocolException, IOException {
		return post(uri, headers, content, urlParams, requestConfig, callback, false);
	}
	// @param trace 请求跟踪
	public static Future<HttpResponse> post(String uri, HeaderGroup headers, HttpEntity content, List<HttpUrlParam> urlParams, RequestConfig requestConfig, HttpAsyncResponseCallback callback, boolean trace)
			throws ClientProtocolException, IOException {
		if (urlParams != null && urlParams.size() > 0) {
			uri = formatUrl(uri, urlParams);
		}
		return asyncRequest(uri, HttpMethod.POST, headers, content, requestConfig, callback, trace);
	}

	/**
	 * GET 请求
	 * 
	 * @param uri       请求的URI
	 * @param headers   头信息
	 * @param urlParams uri可携带的参数
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static CloseableHttpResponse get(String uri, HeaderGroup headers, List<HttpUrlParam> urlParams)
			throws ClientProtocolException, IOException {
		return get(uri, headers, urlParams, null);
	}
	
	/**
	 * GET 请求
	 * 
	 * @param uri       请求的URI
	 * @param headers   头信息
	 * @param urlParams uri可携带的参数
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static CloseableHttpResponse get(String uri, HeaderGroup headers, List<HttpUrlParam> urlParams, RequestConfig requestConfig)
			throws ClientProtocolException, IOException {
		return get(uri, headers, urlParams, requestConfig, false);
	}
	
	public static CloseableHttpResponse get(String uri, HeaderGroup headers, List<HttpUrlParam> urlParams, RequestConfig requestConfig, boolean trace)
			throws ClientProtocolException, IOException {
		uri = formatUrl(uri, urlParams);
		return request(uri, HttpMethod.GET, headers, null, requestConfig, trace);
	}
	public static Future<HttpResponse> get(String uri, HeaderGroup headers, List<HttpUrlParam> urlParams, RequestConfig requestConfig, HttpAsyncResponseCallback callback)
			throws ClientProtocolException, IOException {
		return get(uri, headers, urlParams, requestConfig, callback, false);
	}
	public static Future<HttpResponse> get(String uri, HeaderGroup headers, List<HttpUrlParam> urlParams, RequestConfig requestConfig, HttpAsyncResponseCallback callback, boolean trace)
			throws ClientProtocolException, IOException {
		uri = formatUrl(uri, urlParams);
		return asyncRequest(uri, HttpMethod.GET, headers, null, requestConfig, callback, trace);
	}
	/**
	 * 格式化URL , url?xxx=t&ggg=y&
	 * @param uri
	 * @param urlParams
	 * @return
	 */
	public static String formatUrl(String uri, List<HttpUrlParam> urlParams) {
		if (urlParams == null || urlParams.size() == 0) {
			return uri;
		}
		StringBuilder nuri = new StringBuilder(128);
		nuri.append(uri);
		if (uri.lastIndexOf("?") != -1) {
			if (!uri.endsWith("&")) {
				nuri.append("&");
			}
		} else {
			nuri.append("?");
		}
		for (HttpUrlParam httpUrlParam : urlParams) {
			nuri.append(httpUrlParam.getName()).append("=");
			nuri.append(httpUrlParam.getEncodeUtf8Value());
			nuri.append("&");
		}
		return nuri.substring(0, nuri.length() - 1);

	}

	public static HttpUrlParams urlParams() {
		return new HttpUrlParams();
	}
	
	/**
	 * 开启一个请求的流程. 最后以调用 request() 结束. 这是一个引导发请求的处理方法.
	 * 
	 * @param url 发起请求的HTTP地址, 必须使用 https 或 http 开头.
	 * 
	 * @return
	 */
	public static HttpRequestStream stream(String url) {
		return new HttpRequestStream(url);
	}
	
	
	public static void main(String[] args) throws Exception {
		asyncDemo();
	}
	
	private static void asyncDemo() throws Exception {
		
		HttpUtils.stream("http://api-test.funbit.mes/funbit/advs/homelist")
		.get()    // get 请求
		.params() // 设置参数
		.add("userId", 0) // 添加参数
		.request((result)->{ // 异步应答.
			System.out.println("homelist:::"+result.getStringUtf8());
		}, "hello-error");
		
		Thread.sleep(5000);
	}
	
	private static void demo() throws Exception {
		
		String content1 = HttpUtils.stream("http://www.baidu.com")
				.get() // get 请求
				.request() // 发送请求
				.getStringUtf8(); // 返回字符串
		
		String content2 = HttpUtils.stream("http://api-test.funbit.me/funbit/advs/homelist")
				.get()    // get 请求
				.params() // 设置参数
				.add("userId", 0) // 添加参数
				.request()   // 发送请求
				.getStringUtf8(); // 获取字符串 UTF-8的
		
		String content3 = HttpUtils.stream("http://api-test.funbit.me/funbit/attention/set")
				.post() // POST请求
				 .header()
				.content().buildUrlContent() // content url参数构造
				.add("targetUserId", 55)
				.add("userId", 22222)
				.end() // 结束
				.request().getStringUtf8(); // 发起请求
		
		System.out.println(content1);
		System.out.println(content2);
		System.out.println(content3);
		
	}
}
