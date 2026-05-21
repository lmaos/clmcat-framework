package com.clmcat.framework.international.config;

import com.clmcat.framework.international.adapter.ExcelXlsxResponseInternational;
import com.clmcat.framework.international.adapter.JsonSourceResponseInternational;
import com.clmcat.framework.webmvc.ResponseInternationalization;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InternationalConfiguration {
    @Bean
    InternationalProperties InternationalProperties() {
        return new InternationalProperties();
    }

    @Bean
    @ConditionalOnMissingBean
    ResponseInternationalization responseInternationalization() {
        InternationalProperties properties = InternationalProperties();
        String mode = properties.getMode();
        if ("Excel.xlsx".equalsIgnoreCase(mode)) {
            return new ExcelXlsxResponseInternational();
        } else if ("Json.source".equalsIgnoreCase(mode)) {
            return new JsonSourceResponseInternational();
        } else {
            return null;
        }
    }
}
