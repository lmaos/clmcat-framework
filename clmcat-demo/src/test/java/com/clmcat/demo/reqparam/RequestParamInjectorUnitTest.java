package com.clmcat.demo.reqparam;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.filter.FormContentFilter;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.clmcat.framework.webmvc.anns.Params;
import com.clmcat.framework.webmvc.anns.Params.ParamsAuthEncrypt;
import com.clmcat.framework.webmvc.anns.Params.ParamsScope;
import com.clmcat.framework.webmvc.interceptor.reqparam.CustomRequestParameter;
import com.clmcat.framework.webmvc.interceptor.reqparam.RequestParamInjector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class RequestParamInjectorUnitTest {

    private final RequestParamInjector injector = new RequestParamInjector();

    @Test
    void shouldResolvePutJsonNestedParam() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("PUT", "/reqparam");
        request.setContentType(MediaType.APPLICATION_JSON_VALUE);
        request.setContent("{\"user\":{\"name\":\"neo\"}}".getBytes(StandardCharsets.UTF_8));

        Object value = injector.resolveArgument(methodParameter("putJsonName"), new ModelAndViewContainer(),
                new ServletWebRequest(request, new MockHttpServletResponse()), null);

        assertEquals("neo", value);
    }

    @Test
    void shouldResolveDeleteFormParamAfterFilter() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("DELETE", "/reqparam");
        request.setContentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        request.setContent("id=12".getBytes(StandardCharsets.UTF_8));

        HttpServletRequest filteredRequest = applyFormContentFilter(request);
        Object value = injector.resolveArgument(methodParameter("deleteFormId"), new ModelAndViewContainer(),
                new ServletWebRequest(filteredRequest, new MockHttpServletResponse()), null);

        assertEquals(12, value);
    }

    @Test
    void shouldBindBeanWithoutImplicitParameterPrefix() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/phone");
        request.addParameter("phone", "1234");
        request.addParameter("countryCode", "+86");

        PhoneLoginDto value = (PhoneLoginDto) injector.resolveArgument(methodParameter("phoneLogin"),
                new ModelAndViewContainer(), new ServletWebRequest(request, new MockHttpServletResponse()), null);

        assertEquals("1234", value.getPhone());
        assertEquals("+86", value.getCountryCode());
    }

    @Test
    void shouldMergeJsonBodyAndQueryParamsForBean() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("PUT", "/phone");
        request.setContentType(MediaType.APPLICATION_JSON_VALUE);
        request.setContent("{\"countryCode\":\"+86\",\"code\":\"4321\"}".getBytes(StandardCharsets.UTF_8));
        request.addParameter("phone", "1234");

        PhoneLoginDto value = (PhoneLoginDto) injector.resolveArgument(methodParameter("phoneLogin"),
                new ModelAndViewContainer(), new ServletWebRequest(request, new MockHttpServletResponse()), null);

        assertEquals("1234", value.getPhone());
        assertEquals("+86", value.getCountryCode());
        assertEquals("4321", value.getCode());
    }

    @Test
    void shouldTreatBraceWrappedBodyAsJsonWithoutContentType() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("PUT", "/phone");
        request.setContent("{\"countryCode\":\"+86\",\"code\":\"4321\"}".getBytes(StandardCharsets.UTF_8));
        request.addParameter("phone", "1234");

        PhoneLoginDto value = (PhoneLoginDto) injector.resolveArgument(methodParameter("phoneLogin"),
                new ModelAndViewContainer(), new ServletWebRequest(request, new MockHttpServletResponse()), null);

        assertEquals("1234", value.getPhone());
        assertEquals("+86", value.getCountryCode());
        assertEquals("4321", value.getCode());
    }

    @Test
    void shouldUseNameAttributeForSimpleParameter() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/reqparam");
        request.addParameter("namedId", "7");

        Object value = injector.resolveArgument(methodParameter("namedId"), new ModelAndViewContainer(),
                new ServletWebRequest(request, new MockHttpServletResponse()), null);

        assertEquals(7, value);
    }

    @Test
    void shouldUseDefaultValueWhenParameterMissing() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/reqparam");

        Object value = injector.resolveArgument(methodParameter("defaultedName"), new ModelAndViewContainer(),
                new ServletWebRequest(request, new MockHttpServletResponse()), null);

        assertEquals("guest", value);
    }

    @Test
    void shouldResolveHeaderScopedFieldOnBean() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/reqparam");
        request.addParameter("phone", "1234");
        request.addHeader("device-id", "ios");

        HeaderAwarePhoneLoginDto value = (HeaderAwarePhoneLoginDto) injector.resolveArgument(
                methodParameter("headerAwarePhoneLogin"), new ModelAndViewContainer(),
                new ServletWebRequest(request, new MockHttpServletResponse()), null);

        assertEquals("1234", value.getPhone());
        assertEquals("ios", value.getDeviceId());
    }

    @Test
    void shouldResolveCookieScopedFieldOnBean() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/reqparam");
        request.addParameter("phone", "1234");
        request.setCookies(new Cookie("session-id", "cookie-1"));

        CookieAwarePhoneLoginDto value = (CookieAwarePhoneLoginDto) injector.resolveArgument(
                methodParameter("cookieAwarePhoneLogin"), new ModelAndViewContainer(),
                new ServletWebRequest(request, new MockHttpServletResponse()), null);

        assertEquals("1234", value.getPhone());
        assertEquals("cookie-1", value.getSessionId());
    }

    @Test
    void shouldIgnoreCustomRequestParameterWhenScopeIsNone() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/reqparam");
        ServletWebRequest webRequest = new ServletWebRequest(request, new MockHttpServletResponse());
        CustomRequestParameter.getOrCreate(webRequest).put("id", "99");

        Object value = injector.resolveArgument(methodParameter("noneScopedId"), new ModelAndViewContainer(),
                webRequest, null);

        assertNull(value);
    }

    @Test
    void shouldDecodeBase64JsonBody() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("PUT", "/reqparam");
        request.setContentType(MediaType.TEXT_PLAIN_VALUE);
        request.setContent("eyJwaG9uZSI6IjEyMzQiLCJjb2RlIjoiNDMyMSJ9".getBytes(StandardCharsets.UTF_8));

        PhoneLoginDto value = (PhoneLoginDto) injector.resolveArgument(methodParameter("base64PhoneLogin"),
                new ModelAndViewContainer(), new ServletWebRequest(request, new MockHttpServletResponse()), null);

        assertEquals("1234", value.getPhone());
        assertEquals("4321", value.getCode());
    }

    private HttpServletRequest applyFormContentFilter(MockHttpServletRequest request) throws Exception {
        AtomicReference<ServletRequest> filteredRequest = new AtomicReference<>();
        FormContentFilter filter = new FormContentFilter();
        filter.doFilter(request, new MockHttpServletResponse(), (ServletRequest req, ServletResponse res) ->
                filteredRequest.set(req));
        assertNotNull(filteredRequest.get());
        return (HttpServletRequest) filteredRequest.get();
    }

    private MethodParameter methodParameter(String methodName) throws NoSuchMethodException {
        for (var method : UnitTestController.class.getDeclaredMethods()) {
            if (method.getName().equals(methodName)) {
                return new MethodParameter(method, 0);
            }
        }
        throw new NoSuchMethodException(methodName);
    }

    @SuppressWarnings("unused")
    private static class UnitTestController {
        void putJsonName(@Params("user.name") String name) {
        }

        void deleteFormId(@Params("id") Integer id) {
        }

        void phoneLogin(@Params PhoneLoginDto dto) {
        }

        void namedId(@Params(name = "namedId") Integer id) {
        }

        void defaultedName(@Params(name = "name", required = false, defaultValue = "guest") String name) {
        }

        void headerAwarePhoneLogin(@Params HeaderAwarePhoneLoginDto dto) {
        }

        void cookieAwarePhoneLogin(@Params CookieAwarePhoneLoginDto dto) {
        }

        void noneScopedId(@Params(name = "id", required = false, scope = ParamsScope.NONE) Integer id) {
        }

        void base64PhoneLogin(@Params(authEncrypt = ParamsAuthEncrypt.BASE64) PhoneLoginDto dto) {
        }
    }

    private static class HeaderAwarePhoneLoginDto {
        private String phone;

        @Params(name = "device-id", scope = ParamsScope.HEADER, required = false)
        private String deviceId;

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getDeviceId() {
            return deviceId;
        }

        public void setDeviceId(String deviceId) {
            this.deviceId = deviceId;
        }
    }

    private static class CookieAwarePhoneLoginDto {
        private String phone;

        @Params(name = "session-id", scope = ParamsScope.COOKIE, required = false)
        private String sessionId;

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getSessionId() {
            return sessionId;
        }

        public void setSessionId(String sessionId) {
            this.sessionId = sessionId;
        }
    }
}
