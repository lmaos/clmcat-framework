package com.clmcat.framework.international.localemap;

import java.util.Locale;

public interface LocaleMessageMap {
    /// 获取国际化信息
    String get(String key, Locale locale);
    /// 设置国际化信息
    void put(String key, Locale locale, String value);
    /// 设置默认语言
    void setDefaultLocale(Locale locale);
}
