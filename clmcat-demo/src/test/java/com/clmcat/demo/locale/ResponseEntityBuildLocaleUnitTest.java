package com.clmcat.demo.locale;

import com.clmcat.framework.webmvc.ResponseEntityBuild;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.lang.reflect.Method;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ResponseEntityBuildLocaleUnitTest {

    @Test
    void shouldPreferUserLocaleRequestAttribute() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/locale");
        request.setAttribute("userLocale", Locale.forLanguageTag("ja-JP"));
        request.setAttribute("userLanguage", Locale.forLanguageTag("zh-HK"));

        Locale locale = invokeGetLocale(ResponseEntityBuild.create(), request);

        assertEquals(Locale.forLanguageTag("ja-JP"), locale);
    }

    @Test
    void shouldReuseUserLanguageAsCompatibleFallback() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/locale");
        request.setAttribute("userLanguage", Locale.forLanguageTag("fr-CA"));

        Locale locale = invokeGetLocale(ResponseEntityBuild.create(), request);

        assertEquals(Locale.forLanguageTag("fr-CA"), locale);
        assertEquals(Locale.forLanguageTag("fr-CA"), request.getAttribute("userLocale"));
        assertEquals(Locale.forLanguageTag("fr-CA"), request.getAttribute("userLanguage"));
    }

    @Test
    void shouldFallbackToServletRequestLocale() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/locale");
        request.addPreferredLocale(Locale.forLanguageTag("en-GB"));

        Locale locale = invokeGetLocale(ResponseEntityBuild.create(), request);

        assertEquals(Locale.forLanguageTag("en-GB"), locale);
        assertEquals(Locale.forLanguageTag("en-GB"), request.getAttribute("userLocale"));
        assertNull(request.getAttribute("userLanguage"));
    }

    private Locale invokeGetLocale(ResponseEntityBuild responseEntityBuild, MockHttpServletRequest request) throws Exception {
        Method method = ResponseEntityBuild.class.getDeclaredMethod("getLocale", jakarta.servlet.http.HttpServletRequest.class);
        method.setAccessible(true);
        return (Locale) method.invoke(responseEntityBuild, request);
    }
}
