package com.clmcat.framework.international.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "ark.international")
@Getter
@Setter
public class InternationalProperties {
    /// 默认处理, 选择。 默认为 Excel.xlsx
    private String mode = "Excel.xlsx";
    /// 默认大区
    private String defaultLocale ;

    private String formatMode = "ValueFormat";
    private String formatParam = "{?}";
    /// 其他的文件为位置。
    private List<String> configs;
}
