package com.clmcat.demo.webmvc;

import com.clmcat.framework.webmvc.interceptor.RequestIdGenerator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RequestIdGeneratorUnitTest {

    @Test
    void shouldGenerateSnowflakeRequestIdWithProcessSuffix() {
        RequestIdGenerator.DefaultRequestIdGenerator generator = RequestIdGenerator.DefaultRequestIdGenerator.defaultInstance;

        String requestId1 = generator.generateRequestId();
        String requestId2 = generator.generateRequestId();

        assertNotEquals(requestId1, requestId2);
        assertTrue(requestId1.matches("\\d+-\\d+"), requestId1);
        assertTrue(requestId1.endsWith("-" + RequestIdGenerator.DefaultRequestIdGenerator.processId), requestId1);
    }
}
