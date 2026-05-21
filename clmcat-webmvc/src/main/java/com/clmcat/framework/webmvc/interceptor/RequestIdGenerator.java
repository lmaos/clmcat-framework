package com.clmcat.framework.webmvc.interceptor;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.InetAddress;

import org.apache.logging.log4j.ThreadContext;
import com.clmcat.basics.commons.lang.NumberUtils;
import com.clmcat.basics.commons.lang.StringUtils;

import jakarta.servlet.http.HttpServletRequest;

public interface RequestIdGenerator {
    public static final String REQUEST_ID_KEY = "requestId";

    default String getRequestId(HttpServletRequest request) {
        String requestId = (String) request.getAttribute(REQUEST_ID_KEY);
        if (requestId == null) {
            requestId = request.getParameter(REQUEST_ID_KEY);
        }
        if (requestId == null) {
            requestId = request.getHeader(REQUEST_ID_KEY);
        }
        if (requestId == null) {
            requestId = generateRequestId();
        }
        return requestId;
    }
    
    default void requestTrace(HttpServletRequest request) {
    	String requestId = (String) request.getAttribute(REQUEST_ID_KEY);
        if (requestId == null) {
            requestId = request.getParameter(REQUEST_ID_KEY);
        }
        if (requestId == null) {
            requestId = request.getHeader(REQUEST_ID_KEY);
        }
        
        if (requestId == null) {
        	requestId = generateRequestId();
        	request.setAttribute(REQUEST_ID_KEY, requestId);
        	ThreadContext.put(REQUEST_ID_KEY, requestId);
        } else { // 请求跳转
        	request.setAttribute(REQUEST_ID_KEY, requestId);
        	ThreadContext.put(REQUEST_ID_KEY, requestId + ";" + DefaultRequestIdGenerator.getServerId());
        }
    }

    String generateRequestId();

    class DefaultRequestIdGenerator implements RequestIdGenerator {
        public static DefaultRequestIdGenerator defaultInstance = new DefaultRequestIdGenerator();

        private static String SERVICE_ID;
        public static Integer processId;
        static {
        	try {
        		SERVICE_ID = InetAddress.getLocalHost().getHostAddress();
        		if (StringUtils.isNotBlank(SERVICE_ID)) {
        			String[] items = SERVICE_ID.split("\\.");
        			if (items.length == 4) {
        				SERVICE_ID = items[2] + "0" + items[3];
        			}
        		}
    		} catch (Exception e) {
    		}
        	if (StringUtils.isBlank(SERVICE_ID) || "127.0.0.1".equals(SERVICE_ID)) {
        		SERVICE_ID = String.valueOf((int) (Math.random() * 100000)); 
        	}
            RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
            String name = runtimeMXBean.getName();
            int index = name.indexOf("@");
            if (index == -1) {
            	processId = (int) (Math.random() * 100000);
            } else {
            	String pid = name.substring(0, index).trim();
            	processId = NumberUtils.toInt(pid);
            }
        }
        public static String getServerId() {
        	StringBuilder sb = new StringBuilder(64);
    		sb.append(SERVICE_ID)
    		.append("-")
    		.append(processId)
    		.append("-")
    		.append(Thread.currentThread().getId());
    		return sb.toString();
        }
        
        @Override
        public String generateRequestId() {
    		StringBuilder sb = new StringBuilder(64);
    		sb.append(SERVICE_ID)
    		.append("-")
    		.append(processId)
    		.append("-")
    		.append(Thread.currentThread().getId())
    		.append("-")
    		.append(System.currentTimeMillis())
    		;
    		return sb.toString();
        }
    }
}
