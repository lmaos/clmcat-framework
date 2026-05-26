package com.clmcat.demo.locale;

import com.clmcat.framework.webmvc.anns.GetLocale;
import com.clmcat.framework.webmvc.interceptor.LocaleParameterInjector;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LocaleParameterInjectorUnitTest {

    private final LocaleParameterInjector injector = new LocaleParameterInjector();

    @Test
    void shouldParseAcceptLanguageByStandardOrder() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/locale");
        request.addHeader("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8");

        Object value = injector.resolveArgument(methodParameter("localeString"), new ModelAndViewContainer(),
                new ServletWebRequest(request, new MockHttpServletResponse()), null);

        assertEquals("zh-CN", value);
    }

    @Test
    void shouldParseLocaleHeaderWithUnderscore() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/locale");
        request.addHeader("locale", "zh_CN");

        Object value = injector.resolveArgument(methodParameter("localeObject"), new ModelAndViewContainer(),
                new ServletWebRequest(request, new MockHttpServletResponse()), null);

        assertEquals(Locale.forLanguageTag("zh-CN"), value);
    }

    @Test
    void shouldReuseUserLanguageRequestAttribute() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/locale");
        ServletWebRequest webRequest = new ServletWebRequest(request, new MockHttpServletResponse());
        webRequest.setAttribute("userLanguage", Locale.forLanguageTag("zh-HK"), RequestAttributes.SCOPE_REQUEST);

        Object value = injector.resolveArgument(methodParameter("localeString"), new ModelAndViewContainer(),
                webRequest, null);

        assertEquals("zh-HK", value);
    }

    @Test
    void shouldPreferUserLocaleRequestAttribute() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/locale");
        ServletWebRequest webRequest = new ServletWebRequest(request, new MockHttpServletResponse());
        webRequest.setAttribute("userLocale", Locale.forLanguageTag("ja-JP"), RequestAttributes.SCOPE_REQUEST);

        Object value = injector.resolveArgument(methodParameter("localeString"), new ModelAndViewContainer(),
                webRequest, null);

        assertEquals("ja-JP", value);
    }

    @Test
    void shouldFallbackToServletPreferredLocale() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/locale");
        request.addPreferredLocale(Locale.forLanguageTag("fr-CA"));

        Object value = injector.resolveArgument(methodParameter("localeObject"), new ModelAndViewContainer(),
                new ServletWebRequest(request, new MockHttpServletResponse()), null);

        assertEquals(Locale.forLanguageTag("fr-CA"), value);
    }

    private MethodParameter methodParameter(String methodName) throws NoSuchMethodException {
        for (var method : LocaleController.class.getDeclaredMethods()) {
            if (method.getName().equals(methodName)) {
                return new MethodParameter(method, 0);
            }
        }
        throw new NoSuchMethodException(methodName);
    }

    @SuppressWarnings("unused")
    private static class LocaleController {
        void localeObject(@GetLocale Locale locale) {
        }

        void localeString(@GetLocale String locale) {
        }
    }
}
