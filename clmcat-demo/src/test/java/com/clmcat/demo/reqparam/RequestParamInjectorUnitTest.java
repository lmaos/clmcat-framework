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
import com.clmcat.framework.webmvc.anns.Params.ParamsScope;
import com.clmcat.framework.webmvc.interceptor.reqparam.CustomRequestParameter;
import com.clmcat.framework.webmvc.interceptor.reqparam.RequestParamInjector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

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
    void shouldResolveSimpleHeaderParameter() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/reqparam");
        request.addHeader("token", "header-token");

        Object value = injector.resolveArgument(methodParameter("headerToken"), new ModelAndViewContainer(),
                new ServletWebRequest(request, new MockHttpServletResponse()), null);

        assertEquals("header-token", value);
    }

    @Test
    void shouldResolveSimpleCookieParameter() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/reqparam");
        request.setCookies(new Cookie("session", "cookie-token"));

        Object value = injector.resolveArgument(methodParameter("cookieToken"), new ModelAndViewContainer(),
                new ServletWebRequest(request, new MockHttpServletResponse()), null);

        assertEquals("cookie-token", value);
    }

    @Test
    void shouldResolveIpAliasFromForwardHeaders() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/reqparam");
        request.addHeader("X-Forwarded-For", "unknown, 203.0.113.7, 10.0.0.8");

        Object value = injector.resolveArgument(methodParameter("clientIpAlias"), new ModelAndViewContainer(),
                new ServletWebRequest(request, new MockHttpServletResponse()), null);

        assertEquals("203.0.113.7", value);
    }

    @Test
    void shouldResolveIpScopeParameter() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/reqparam");
        request.addHeader("Forwarded", "for=198.51.100.9;proto=https");

        Object value = injector.resolveArgument(methodParameter("clientIpScoped"), new ModelAndViewContainer(),
                new ServletWebRequest(request, new MockHttpServletResponse()), null);

        assertEquals("198.51.100.9", value);
    }

    @Test
    void shouldResolveRequestScopedSimpleParameter() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/reqparam");
        request.setAttribute("requestId", "req-1");

        Object value = injector.resolveArgument(methodParameter("requestScopedId"), new ModelAndViewContainer(),
                new ServletWebRequest(request, new MockHttpServletResponse()), null);

        assertEquals("req-1", value);
    }

    @Test
    void shouldResolveRequestScopedBeanDirectly() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/reqparam");
        PhoneLoginDto dto = new PhoneLoginDto();
        dto.setPhone("1234");
        dto.setCode("4321");
        request.setAttribute("loginDto", dto);

        PhoneLoginDto value = (PhoneLoginDto) injector.resolveArgument(methodParameter("requestScopedPhoneLogin"),
                new ModelAndViewContainer(), new ServletWebRequest(request, new MockHttpServletResponse()), null);

        assertSame(dto, value);
    }

    @Test
    void shouldResolveRequestScopedBeanFields() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/reqparam");
        request.setAttribute("phone", "1234");
        request.setAttribute("code", "4321");

        PhoneLoginDto value = (PhoneLoginDto) injector.resolveArgument(methodParameter("requestScopedBeanFields"),
                new ModelAndViewContainer(), new ServletWebRequest(request, new MockHttpServletResponse()), null);

        assertEquals("1234", value.getPhone());
        assertEquals("4321", value.getCode());
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
    void shouldBindBeanWithExplicitPrefix() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/reqparam");
        request.addParameter("payload.phone", "1234");
        request.addParameter("payload.countryCode", "+86");

        PhoneLoginDto value = (PhoneLoginDto) injector.resolveArgument(methodParameter("prefixedPhoneLogin"),
                new ModelAndViewContainer(), new ServletWebRequest(request, new MockHttpServletResponse()), null);

        assertEquals("1234", value.getPhone());
        assertEquals("+86", value.getCountryCode());
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
    void shouldApplyFieldDefaultValueOnBean() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/reqparam");
        request.addParameter("phone", "1234");

        DefaultValuePhoneLoginDto value = (DefaultValuePhoneLoginDto) injector.resolveArgument(
                methodParameter("defaultValuePhoneLogin"), new ModelAndViewContainer(),
                new ServletWebRequest(request, new MockHttpServletResponse()), null);

        assertEquals("1234", value.getPhone());
        assertEquals("guest", value.getCode());
    }

    @Test
    void shouldAutoFillClientIpFieldOnBean() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/phone");
        request.addParameter("phone", "1234");
        request.addHeader("X-Real-IP", "198.51.100.15");

        PhoneLoginDto value = (PhoneLoginDto) injector.resolveArgument(methodParameter("phoneLogin"),
                new ModelAndViewContainer(), new ServletWebRequest(request, new MockHttpServletResponse()), null);

        assertEquals("1234", value.getPhone());
        assertEquals("198.51.100.15", value.getClientIp());
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

        void headerToken(@Params(name = "token", scope = ParamsScope.HEADER) String token) {
        }

        void cookieToken(@Params(name = "session", scope = ParamsScope.COOKIE) String token) {
        }

        void clientIpAlias(@Params(name = "IP", required = false) String clientIp) {
        }

        void clientIpScoped(@Params(name = "clientIp", scope = ParamsScope.IP, required = false) String clientIp) {
        }

        void requestScopedId(@Params(name = "requestId", scope = ParamsScope.REQUEST, required = false) String requestId) {
        }

        void requestScopedPhoneLogin(@Params(name = "loginDto", scope = ParamsScope.REQUEST) PhoneLoginDto dto) {
        }

        void requestScopedBeanFields(@Params(scope = ParamsScope.REQUEST) PhoneLoginDto dto) {
        }

        void headerAwarePhoneLogin(@Params HeaderAwarePhoneLoginDto dto) {
        }

        void cookieAwarePhoneLogin(@Params CookieAwarePhoneLoginDto dto) {
        }

        void prefixedPhoneLogin(@Params("payload") PhoneLoginDto dto) {
        }

        void noneScopedId(@Params(name = "id", required = false, scope = ParamsScope.NONE) Integer id) {
        }

        void defaultValuePhoneLogin(@Params DefaultValuePhoneLoginDto dto) {
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

    private static class DefaultValuePhoneLoginDto {
        private String phone;

        @Params(required = false, defaultValue = "guest")
        private String code;

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }
    }
}
