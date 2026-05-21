package com.clmcat.framework.international.adapter;

import jakarta.annotation.Resource;
import lombok.Setter;
import com.clmcat.basics.commons.format.HighValueFormat;
import com.clmcat.basics.commons.lang.StringUtils;
import com.clmcat.framework.international.config.InternationalProperties;
import com.clmcat.framework.international.localemap.LocalLocalMessageMap;
import com.clmcat.framework.international.localemap.LocaleMessageMap;
import com.clmcat.framework.webmvc.ResponseInternationalization;
import org.springframework.beans.factory.InitializingBean;

import java.util.Locale;

/**
 * @author zhangxingyu
 */
public abstract class AbstractResponseInternationalization implements ResponseInternationalization, InitializingBean {
    @Resource
    @Setter
    protected InternationalProperties properties;

    @Setter
    protected LocaleMessageMap localeMessageMap = new LocalLocalMessageMap();

    protected HighValueFormat format = new HighValueFormat("{?}");
    @Override
    public String convertResponseMessage(Message message) {
        String key = message.getLocaleMessage();
        Locale locale = message.getLocale();
        Object[] messageArgs = message.getMessageArgs();
        if (StringUtils.isNotBlank(key)) {
            String value = localeMessageMap.get(key, locale, messageArgs);
            if (StringUtils.isNotBlank(value)) {
                return format.format(value, messageArgs);
            }
        }
        return null;
    }

    @Override
    public Locale getDefaultLocale() {
        if (properties != null && StringUtils.isNotBlank(properties.getDefaultLocale())) {
            return Locale.forLanguageTag(properties.getDefaultLocale());
        }
        return ResponseInternationalization.super.getDefaultLocale();
    }

    @Override
    public void reload() {
        if (properties != null && StringUtils.isNotBlank(properties.getFormatParam())) {
            format = new HighValueFormat(properties.getFormatParam());
        }
        LocalLocalMessageMap localeMessageMapTmp = new LocalLocalMessageMap();
        if (doLoad(localeMessageMapTmp)) {
            this.localeMessageMap = localeMessageMapTmp;
        }

    }

    protected abstract boolean doLoad(LocalLocalMessageMap localeMessageMapTmp) ;

    @Override
    public void afterPropertiesSet() throws Exception {
        reload();
    }
}
