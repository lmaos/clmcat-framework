package com.clmcat.framework.webmvc.result;

import java.io.OutputStream;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.ModelAndViewContainer;

import jakarta.servlet.http.HttpServletResponse;

public class CustomResponseHandler  implements HandlerMethodReturnValueHandler, ApplicationContextAware {

	private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

	@Override
	public boolean supportsReturnType(MethodParameter returnType) {
		return returnType.getParameterType() == CustomResponseEntity.class;
	}

	@Override
	public void handleReturnValue(Object returnValue, MethodParameter returnType, ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest) throws Exception {
		
		HttpServletResponse response = webRequest.getNativeResponse(HttpServletResponse.class);
		CustomResponseEntity customResponseEntity = (CustomResponseEntity) returnValue;
		byte[] data = customResponseEntity.getData();
		data = data == null ? new byte[0] : data;
		String contentType = customResponseEntity.getContentType();
		response.setContentType(contentType);
		response.setContentLength(data.length);
		
		OutputStream printWriter = response.getOutputStream();
		printWriter.write(data);
		printWriter.flush();
		 
	}

}
