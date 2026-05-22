package com.clmcat.framework.international.localemap;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class LocaleMessageUtils {

    private LocaleMessageUtils() {
    }

    public static List<Locale> parseLocaleExpression(String localeExpression) {
        Set<Locale> locales = new LinkedHashSet<>();
        if (StringUtils.isBlank(localeExpression)) {
            return List.of();
        }
        String[] splits = localeExpression.split(",");
        for (String split : splits) {
            String localeTag = StringUtils.trimToEmpty(split);
            if (StringUtils.isBlank(localeTag)) {
                continue;
            }
            Locale locale = Locale.forLanguageTag(localeTag);
            if (StringUtils.isBlank(locale.getLanguage())) {
                continue;
            }
            locales.add(locale);
        }
        return List.copyOf(locales);
    }

    public static List<String> resolveLookupTags(Locale locale) {
        if (locale == null || StringUtils.isBlank(locale.getLanguage())) {
            return List.of();
        }
        Set<String> tags = new LinkedHashSet<>();
        String languageTag = normalizeLocaleTag(locale);
        if (StringUtils.isNotBlank(languageTag)) {
            tags.add(languageTag);
        }
        if (StringUtils.isNotBlank(locale.getCountry())) {
            tags.add(locale.getLanguage() + "-" + locale.getCountry());
        }
        tags.add(locale.getLanguage());
        return new ArrayList<>(tags);
    }

    public static String normalizeLocaleTag(Locale locale) {
        if (locale == null || StringUtils.isBlank(locale.getLanguage())) {
            return null;
        }
        String languageTag = locale.toLanguageTag();
        if (StringUtils.isBlank(languageTag) || "und".equalsIgnoreCase(languageTag)) {
            return locale.getLanguage();
        }
        return languageTag;
    }
}
