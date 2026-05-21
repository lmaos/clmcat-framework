package com.clmcat.framework.webmvc.result;

import java.util.concurrent.Callable;

import com.clmcat.framework.webmvc.HttpStatus;
import com.clmcat.framework.webmvc.ResponseEntity;
import com.clmcat.framework.webmvc.ResponseEntityBuild;
import com.clmcat.framework.webmvc.ResponseStatusAdapter;
import org.apache.commons.lang3.math.NumberUtils;
import com.clmcat.framework.webmvc.anns.ApiController;
import org.springframework.boot.webmvc.autoconfigure.error.BasicErrorController;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 组件 应答处理
 */
public class ComponentResponseHandler implements HandlerMethodReturnValueHandler, ApplicationContextAware {

	private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public boolean supportsReturnType(MethodParameter returnType) {

        Class<?> parameterType = returnType.getParameterType();
        Class<?> beanType = returnType.getDeclaringClass();

        // 判断是否是异步结果，如果是异步结果则不应用当前处理方案
        if (parameterType == DeferredResult.class
            || Callable.class.isAssignableFrom(parameterType)
        ) {
            return false;
        }

        // 判断是否是错误控制、如果是错误控制器则拦截
        if (beanType == BasicErrorController.class) {
            return true;
        }

        //returnType. 判断是否是 API控制器 或 ResponseEntity 实体
        return AnnotatedElementUtils.hasAnnotation(beanType, ApiController.class)
                || parameterType == ResponseEntity.class;

    }

    @Override
    public void handleReturnValue(Object returnValue, MethodParameter returnType, ModelAndViewContainer mavContainer, NativeWebRequest webRequest) throws Exception {
        mavContainer.setRequestHandled(true);
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        HttpServletResponse response = webRequest.getNativeResponse(HttpServletResponse.class);
        ResponseStatusAdapter responseStatusAdapter = (ResponseStatusAdapter) request.getAttribute(ResponseStatusAdapter.KEY);
        ResponseEntity responseEntity;
        ResponseEntityBuild responseEntityBuild;
        if (returnValue == null) {
            responseEntityBuild = responseStatusAdapter.success().create();
        } else if (returnValue instanceof org.springframework.http.ResponseEntity<?>) {
        	org.springframework.http.ResponseEntity<?> entry = (org.springframework.http.ResponseEntity<?>) returnValue;
        	int httpStatus = entry.getStatusCode().value();
        	responseEntityBuild = ResponseEntityBuild.create()
        			.setHttpStatus(httpStatus)
        			.setState(HttpStatus.getState(httpStatus))
        			.setStatus(httpStatus)
        			//.setMessage(entry.getStatusCode().getReasonPhrase())
        			.setContent(entry.getBody());
        	
        } else if (returnValue instanceof ResponseEntity) {
            responseEntityBuild = ResponseEntityBuild.create()
                    .setResponseEntity((ResponseEntity) returnValue);
        } else if (returnValue instanceof ModelAndView) {
            ModelAndView modelAndView = (ModelAndView)returnValue;
            String message = String.valueOf(modelAndView.getModel().getOrDefault("message", "系统错误"));
            String statusVal = String.valueOf(modelAndView.getModel().getOrDefault("status", "500"));
            Integer status = NumberUtils.toInt(statusVal, 500);

            responseEntityBuild = ResponseEntityBuild.create()
                    .setHttpStatus(status)
                    .setStatus(status)
                    .setState(HttpStatus.getState(status))
                    .setMessage(message);
        } else {
            responseEntityBuild = responseStatusAdapter.success().create().setContent(returnValue);
        }

        responseEntityBuild.setApplicationContext(applicationContext);
        responseEntity = responseEntityBuild.build(request);
        responseEntity.out(response);

    }
}
