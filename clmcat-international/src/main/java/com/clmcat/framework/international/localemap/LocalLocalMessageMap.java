package com.clmcat.framework.international.localemap;

import com.clmcat.basics.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class LocalLocalMessageMap implements LocaleMessageMap {

    private final Map<String, Map<String, String>> map = new HashMap<>(128);
    private Locale defaultLocale;
    @Override
    public String get(String key, Locale locale, Object... args) {
        Map<String, String> localeStringMap = map.get(key);
        String message = get(localeStringMap, locale);
        if (message == null) {
            message = get(localeStringMap, defaultLocale);
        }
        return message;
    }


    private String get(Map<String, String> localeStringMap, Locale locale) {
        if (locale == null || localeStringMap == null) {
            return null;
        }
        String language = locale.getLanguage();
        String country = locale.getCountry();
        String message = null;
        if (StringUtils.isNotBlank(country)) {
            message = localeStringMap.get(language + "-" + country);
        }
        if (message == null) {
            message = localeStringMap.get(language);
        }
        return message;
    }

    @Override
    public void put(String key, Locale locale, String value) {
        Map<String, String> localeStringMap = map.computeIfAbsent(key, k -> new HashMap<>());
        String language = locale.getLanguage();
        String country = locale.getCountry();
        if (StringUtils.isNotBlank(country)) {
            localeStringMap.put(language + "-" + country, value);
        } else {
            localeStringMap.put(language, value);
        }

    }

    @Override
    public void setDefaultLocale(Locale defaultLocale) {
        this.defaultLocale = defaultLocale;
    }
}
