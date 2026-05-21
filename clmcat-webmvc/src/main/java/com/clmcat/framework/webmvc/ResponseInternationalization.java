package com.clmcat.framework.webmvc;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.Locale;

/**
 * 国际化接口
 */
public interface ResponseInternationalization {

    public final static String DEFAULT_LOCALE = "DefaultLocale";
    public final static String KEY = "ResponseInternationalization";

    default Locale getDefaultLocale() {
        return Locale.getDefault();
    };

    default Locale getLocale(HttpServletRequest request) {
        return null;
    };

    /// 获取国际化消息
    String convertResponseMessage(Message message);


    void reload();


    @Getter
    @Setter
    static class Message {
        private HttpServletRequest request;
        private Locale locale;
        private String localeMessage;
        private Object[] messageArgs;
    }
}
