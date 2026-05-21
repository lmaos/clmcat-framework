package com.clmcat.framework.webmvc.interceptor;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.ThreadContext;
import com.clmcat.basics.commons.lang.StringUtils;
import com.clmcat.framework.webmvc.ResponseInternationalization;
import com.clmcat.framework.webmvc.ResponseStatusAdapter;
import com.clmcat.framework.webmvc.ResponseEntityKey;
import com.clmcat.framework.webmvc.ResponseEntityKey.DefaultResponseEntityKey;
import com.clmcat.framework.webmvc.ResponseEntityResultAdapter;
import com.clmcat.framework.webmvc.anns.ApiController;
import com.clmcat.framework.webmvc.anns.LoginVerify;
import com.clmcat.framework.webmvc.anns.NoLoginVerify;
import com.clmcat.framework.webmvc.support.SingletonUtils;
import com.clmcat.framework.webmvc.verify.DefaultTokenCodec;
import com.clmcat.framework.webmvc.verify.LoginVerifyFunction;
import com.clmcat.framework.webmvc.verify.LoginVerifyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * @author zhangxingyu
 *
 * 请求拦截器、每一个请求会通过当前拦截器进行预处理。 拦截器会进行登录验证、请求参数处理， 初始化当前请求依赖的配置。
 */
public class RequestInterceptor implements HandlerInterceptor, ApplicationContextAware  {
	
	private static final Logger log = LoggerFactory.getLogger(RequestInterceptor.class);

    @Autowired
	LoginVerifyService loginVerifyService;
    
    @Autowired(required = false)
	ResponseEntityKey responseEntityKey = DefaultResponseEntityKey.defaultInstance;
    @Autowired(required = false)
    RequestIdGenerator requestIdGenerator = RequestIdGenerator.DefaultRequestIdGenerator.defaultInstance;
    @Autowired(required = false)
    @Qualifier("globalResultAdapter")
    ResponseEntityResultAdapter globalResultAdapter;
    @Autowired(required = false)
    @Qualifier("responseStatusAdapter")
    ResponseStatusAdapter responseStatusAdapter = ResponseStatusAdapter.DefaultResponseStatusAdapter.defaultInstance;
    /// 国际化
    @Autowired(required = false)
    ResponseInternationalization responseInternationalization;

    ApplicationContext applicationContext;
    
    @Autowired(required = false)
    CorsConfig corsConfig = CorsConfig.defaultInstance;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

//    public void setLoginVerifyService(LoginVerifyService loginVerifyService) {
//        this.loginVerifyService = loginVerifyService;
//    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        configRequestAttribute(request, response);
    	requestTrace(request, response);
    	corsConfig.setCors(request, response);
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            configHandlerMethodRequestAttribute(request, response, handlerMethod);
            if (!loginVerify(request, response, handlerMethod)) {
                return false;
            }
        }
      //org.springframework.web.servlet.handler.AbstractHandlerMapping.PreFlightHandler
        if (handler.getClass().getSimpleName().equals("PreFlightHandler")) {
            // option 请求， 直接返回成功。
        }
        if (handler instanceof ResourceHttpRequestHandler) {
            // 资源请求，暂不处理。
        }

        return true;
    }
    
    // ApiController 缓存
    Map<Class<?>, Optional<ApiController>> controllers = new ConcurrentHashMap<>();
    
    private Optional<ApiController> getApiController(HandlerMethod handlerMethod) {
    	Class<?> beanType = handlerMethod.getBeanType();
    	Optional<ApiController> apiControllerOt = controllers.get(beanType);
    	if (apiControllerOt == null) {
    		ApiController apiController = AnnotatedElementUtils.findMergedAnnotation(beanType, ApiController.class);
    		apiControllerOt = Optional.ofNullable(apiController);
    		controllers.put(beanType, apiControllerOt);
    	}
    	return apiControllerOt;
    }

    private void configRequestAttribute(HttpServletRequest request, HttpServletResponse response) {
        request.setAttribute(ResponseEntityKey.KEY, responseEntityKey);  // 默认应答实体名字. 低优先级.
        request.setAttribute(ResponseEntityResultAdapter.KEY, globalResultAdapter); // 结果适配器 优先级更大.
        request.setAttribute(ResponseStatusAdapter.KEY, responseStatusAdapter); // 状态适配方式。
        /// 当前请求的国际化
        if (responseInternationalization != null) {
            request.setAttribute(ResponseInternationalization.KEY, responseInternationalization);
            request.setAttribute(ResponseInternationalization.DEFAULT_LOCALE, responseInternationalization.getDefaultLocale());
        }
    }
   
    // 配置当前应答格式
	private void configHandlerMethodRequestAttribute(HttpServletRequest request, HttpServletResponse response,
                                                     HandlerMethod handlerMethod) {

		Optional<ApiController> apiControllerOt = getApiController(handlerMethod);
		if (apiControllerOt != null && apiControllerOt.isPresent()) {
            ApiController apiController = apiControllerOt.get();
            String resultAdapterName = apiController.resultAdapterName();
			if (StringUtils.isNotBlank(resultAdapterName) && applicationContext.containsBean(resultAdapterName)) {
				try {
					ResponseEntityResultAdapter resultAdapter = applicationContext.getBean(resultAdapterName,
							ResponseEntityResultAdapter.class);
					request.setAttribute(ResponseEntityResultAdapter.KEY, resultAdapter);
				} catch (Exception e) {
					log.error("bean 不存在 " + resultAdapterName, e);
				}
			}
			Class<? extends ResponseEntityKey> entityKeyType = apiController.entityKey();
			if (entityKeyType != ResponseEntityKey.class && !entityKeyType.isInterface()) {
				ResponseEntityKey entityKey = SingletonUtils.getSingleton(entityKeyType);
				if (entityKey != null) {
					request.setAttribute(ResponseEntityKey.KEY, entityKey);
				}
			}

            Class<? extends ResponseStatusAdapter> StatusAdapterType = apiController.statusAdapter();
            if (StatusAdapterType != ResponseStatusAdapter.class && !StatusAdapterType.isInterface()) {
                ResponseStatusAdapter statusAdapter = SingletonUtils.getSingleton(StatusAdapterType);
                if (statusAdapter != null) {
                    request.setAttribute(ResponseStatusAdapter.KEY, statusAdapter);
                }
            }
            ApiController.LogMode logMode = apiController.logMode();

            request.setAttribute(ApiController.LogMode.REQUEST_LOG_MODE_KET, logMode);
        }
	}
    
    /**
     * 登录验证
     */
    private boolean loginVerify(HttpServletRequest request, HttpServletResponse response, HandlerMethod handlerMethod) throws Exception {
    	// 设置当前请求使用的登陆验证服务的实例。
        request.setAttribute("LoginVerifyService", loginVerifyService);
        // 存在不需要登陆验证注解标记、直接返回通过。。
        if (handlerMethod.hasMethodAnnotation(NoLoginVerify.class)) {
            return true;
        }
        // 获得请求请求中的登陆验证的状态。
        Object status = request.getAttribute(DefaultTokenCodec.TOKEN_STATUS_KEY);

		if (status == null) {
			// 没做过登陆验证， 继续验证。
		} else if ("1".equals(status)) {
            // 已经登陆成功、不再验证。
			return true;
		} else {
            // 已经登陆失败、不再验证。
			return false;
		}

        // 获得控制处理器方法的所处的 BeanType。先查询类是否存在登陆验证的要求。
        Class<?> beanType = handlerMethod.getBeanType();
        LoginVerify loginVerify = AnnotatedElementUtils.findMergedAnnotation(beanType, LoginVerify.class);
        // 如果类不存在登陆验证的要求， 查询方法层面的注解。
        if (loginVerify == null) {
            loginVerify = handlerMethod.getMethodAnnotation(LoginVerify.class);
        }
        // 不必须登陆则不校验登陆状态
        if (loginVerify == null) {
            return true;
        }
        // 登陆验证功能、通过这个功能来完成登陆验证的方法。
        LoginVerifyFunction loginVerifyFunction = loginVerifyService.getLoginVerifyFunction(handlerMethod);
        
        if (loginVerifyFunction == null) {
        	// 不存在登陆验证函数
        	log.warn("无法使用LoginVerify的验证功能, 因为请求拦截器不存在登陆验证函数! ");
        	return false;
        }

        // 当前请求使用的登陆验证功能。
        request.setAttribute(LoginVerifyFunction.KEY, loginVerifyFunction);

        // 调用 verifyLogin 判断登陆验证状态， true 登陆成功。
        boolean loginState = loginVerifyFunction.verifyLogin(loginVerify, request, response, handlerMethod);
        // mustLogin = true 必须进行登陆验证， 登录失败执行下面的方式。
        if (!loginState && loginVerify.mustLogin()) {
            LoginVerifyFailResponse loginVerifyFailResponse = loginVerifyService.getLoginVerifyFailResponse(handlerMethod.getMethod());
            
            // 错误输出方式, 分为 API输出和PAGE输出.
            if (AnnotatedElementUtils.hasAnnotation(handlerMethod.getBeanType(), ResponseBody.class) || handlerMethod.hasMethodAnnotation(ResponseBody.class)) {
                // API 方式输出
                loginVerifyFailResponse.doApi(request, response, handlerMethod);
            } else {
                // PAGE 方式输出
                loginVerifyFailResponse.doPage(request, response, handlerMethod);
            }
            // 登陆失败， 设置一个请求标记、我失败了！。
            request.setAttribute(DefaultTokenCodec.TOKEN_STATUS_KEY, "0");
        } else {
            // 登陆成功， 设置一个请求标记、我成功了！。
        	request.setAttribute(DefaultTokenCodec.TOKEN_STATUS_KEY, "1");
        	loginState = true;
        }
        return loginState;
    }
  
    private void requestTrace(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	try {
    		String requestId = requestIdGenerator.getRequestId(request);
        	request.setAttribute("requestId", requestId);
        	ThreadContext.put("requestId", requestId);
		} catch (Exception e) {
			log.error("请求追踪", e);
		}
    }
    
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }
}
