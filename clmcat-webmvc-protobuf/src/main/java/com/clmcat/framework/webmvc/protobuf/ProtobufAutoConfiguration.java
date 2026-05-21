package com.clmcat.framework.webmvc.protobuf;

import com.clmcat.framework.webmvc.protobuf.adapter.ProtobufResponseAdapter;
import com.clmcat.framework.webmvc.protobuf.param.ProtoRequestBodyHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * ark.webmvc.protobuf=true
 */
@Configuration
public class ProtobufAutoConfiguration implements WebMvcConfigurer {

    @Bean
    public ProtoRequestBodyHandler protoRequestBodyHandler() {
        return new ProtoRequestBodyHandler();
    }

    @Bean
    public ProtobufResponseProperties protobufResponseResources() {
        return new ProtobufResponseProperties();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "ark.webmvc.protobuf", name = "enable", havingValue = "true")
    public ProtobufResponseAdapter globalResultAdapter() {
        return new ProtobufResponseAdapter();
    }
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(protoRequestBodyHandler());
    }
}
