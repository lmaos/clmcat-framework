package com.clmcat.demo.locale;

import com.clmcat.framework.webmvc.anns.NoLoginVerify;
import com.clmcat.framework.webmvc.interceptor.RequestInterceptor;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RequestInterceptorLocaleUnitTest {

    private final RequestInterceptor interceptor = new RequestInterceptor();

    @Test
    void shouldSetUserLocaleFromUserLanguageHeader() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/locale");
        request.addHeader("userLanguage", "zh_CN");

        boolean result = interceptor.preHandle(request, new MockHttpServletResponse(), handlerMethod("handle"));

        assertTrue(result);
        assertEquals(Locale.forLanguageTag("zh-CN"), request.getAttribute("userLocale"));
        assertEquals(Locale.forLanguageTag("zh-CN"), request.getAttribute("userLanguage"));
    }

    @Test
    void shouldTreatLocaleHeaderAsCompatibleUserLanguage() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/locale");
        request.addHeader("locale", "ja_JP");

        boolean result = interceptor.preHandle(request, new MockHttpServletResponse(), handlerMethod("handle"));

        assertTrue(result);
        assertEquals(Locale.forLanguageTag("ja-JP"), request.getAttribute("userLocale"));
        assertEquals(Locale.forLanguageTag("ja-JP"), request.getAttribute("userLanguage"));
    }

    @Test
    void shouldFallbackToAcceptLanguageOnlyForUserLocale() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/locale");
        request.addHeader("Accept-Language", "fr-CA,fr;q=0.9,en;q=0.8");

        boolean result = interceptor.preHandle(request, new MockHttpServletResponse(), handlerMethod("handle"));

        assertTrue(result);
        assertEquals(Locale.forLanguageTag("fr-CA"), request.getAttribute("userLocale"));
        assertNull(request.getAttribute("userLanguage"));
    }

    private HandlerMethod handlerMethod(String methodName) throws NoSuchMethodException {
        return new HandlerMethod(new TestController(), TestController.class.getDeclaredMethod(methodName));
    }

    private static class TestController {
        @NoLoginVerify
        public void handle() {
        }
    }
}
