package com.clmcat.framework.webmvc.protobuf;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ark.webmvc.protobuf")
@Getter
@Setter
public class ProtobufResponseProperties {
    /**
     * 启用默认的全局响应适配器。 protobuf 响应适配器
     */
    private boolean enable;
}
