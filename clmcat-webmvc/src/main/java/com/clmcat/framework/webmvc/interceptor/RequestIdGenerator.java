package com.clmcat.framework.webmvc.interceptor;

import com.clmcat.basics.commons.snowflake.CustomSnowflake;
import com.clmcat.basics.commons.snowflake.SnowflakeCustomBuilder;
import com.clmcat.basics.commons.snowflake.strategy.MachineStrategy;
import com.clmcat.basics.commons.snowflake.strategy.SequenceStrategy;
import com.clmcat.basics.commons.snowflake.strategy.TimeStrategy;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import com.clmcat.basics.commons.lang.NumberUtils;

import jakarta.servlet.http.HttpServletRequest;

public interface RequestIdGenerator {
    public static final String REQUEST_ID_KEY = "requestId";
    public final static Logger log = org.apache.logging.log4j.LogManager.getLogger(RequestIdGenerator.class);

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
        try {
            String requestId = getRequestId(request);
            request.setAttribute("requestId", requestId);
            ThreadContext.put("requestId", requestId);
        } catch (Exception e) {
            log.error("请求追踪", e);
        }
    }

    String generateRequestId();

    class DefaultRequestIdGenerator implements RequestIdGenerator {
        public static DefaultRequestIdGenerator defaultInstance = new DefaultRequestIdGenerator();
        private static final long REQUEST_ID_BASE_TIME = 1704067200000L;
        private static final CustomSnowflake REQUEST_ID_SNOWFLAKE = SnowflakeCustomBuilder.builder()
                .add(0)
                .add("time", 41, TimeStrategy.millisecond(REQUEST_ID_BASE_TIME))
                .add("machine", 10, MachineStrategy.autoByIp())
                .add("sequence", 12, SequenceStrategy.standard(), "time")
                .build();

        public static Integer processId;
        static {
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

        @Override
        public String generateRequestId() {
            // requestId 主要用于链路追踪，不要求纯数字；雪花ID提供时序性，进程号补充实例可读性。
            return REQUEST_ID_SNOWFLAKE.nextId() + "-" + processId;
        }
    }
}
