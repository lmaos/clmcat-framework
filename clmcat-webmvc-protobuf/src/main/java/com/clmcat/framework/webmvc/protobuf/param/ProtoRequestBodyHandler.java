package com.clmcat.framework.webmvc.protobuf.param;

import com.google.protobuf.GeneratedMessage;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import com.clmcat.framework.webmvc.protobuf.entity.ProtoResponseEntity;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.lang.reflect.Method;

public class ProtoRequestBodyHandler implements HandlerMethodArgumentResolver {
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        Class<?> parameterType = parameter.getParameterType();
        return GeneratedMessage.class.isAssignableFrom(parameterType);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        if ("GET".equalsIgnoreCase(request.getMethod())) {

            return null;
        }
        String contentLength = webRequest.getHeader("Content-Length");
        ServletInputStream inputStream = request.getInputStream();
        int length = NumberUtils.toInt(contentLength);
        if (length <= 0) {
            return null;
        }
        byte[] data = new byte[length];
        IOUtils.readFully(inputStream, data, 0, data.length);
        Class<?> parameterType = parameter.getParameterType();
        ProtoResponseEntity.parseFrom(inputStream);
        Method parseFrom = parameterType.getMethod("parseFrom", byte[].class);
        return parseFrom.invoke(null, data);
    }
}
