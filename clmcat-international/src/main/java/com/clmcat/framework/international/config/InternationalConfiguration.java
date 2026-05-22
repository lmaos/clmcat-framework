package com.clmcat.framework.international.config;

import com.clmcat.framework.international.adapter.ExcelXlsxResponseInternational;
import com.clmcat.framework.international.adapter.JsonSourceResponseInternational;
import com.clmcat.framework.webmvc.ResponseInternationalization;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(InternationalProperties.class)
public class InternationalConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public ResponseInternationalization responseInternationalization(InternationalProperties properties) {
        String mode = StringUtils.defaultIfBlank(properties.getMode(), "excel.xlsx");
        if ("json.source".equalsIgnoreCase(mode)) {
            return new JsonSourceResponseInternational();
        }
        if ("excel.xlsx".equalsIgnoreCase(mode)) {
            return new ExcelXlsxResponseInternational();
        }
        return new ExcelXlsxResponseInternational();
    }
}
