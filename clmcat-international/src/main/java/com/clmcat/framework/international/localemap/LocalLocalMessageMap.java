package com.clmcat.framework.international.localemap;

import org.apache.commons.lang3.StringUtils;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LocalLocalMessageMap implements LocaleMessageMap {

    private final Map<String, Map<String, String>> map = new ConcurrentHashMap<>(128);
    private volatile Locale defaultLocale;

    @Override
    public String get(String key, Locale locale) {
        if (StringUtils.isBlank(key)) {
            return null;
        }
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
        for (String lookupTag : LocaleMessageUtils.resolveLookupTags(locale)) {
            String message = localeStringMap.get(lookupTag);
            if (message != null) {
                return message;
            }
        }
        return null;
    }

    @Override
    public void put(String key, Locale locale, String value) {
        if (StringUtils.isBlank(key) || locale == null || StringUtils.isBlank(value)) {
            return;
        }
        String localeTag = LocaleMessageUtils.normalizeLocaleTag(locale);
        if (StringUtils.isBlank(localeTag)) {
            return;
        }
        Map<String, String> localeStringMap = map.computeIfAbsent(key, k -> new ConcurrentHashMap<>());
        localeStringMap.put(localeTag, value);
    }

    @Override
    public void setDefaultLocale(Locale defaultLocale) {
        this.defaultLocale = defaultLocale;
    }
}
