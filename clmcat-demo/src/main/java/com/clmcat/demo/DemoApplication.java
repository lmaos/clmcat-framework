package com.clmcat.demo;

import com.clmcat.framework.webmvc.WebMvcConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(WebMvcConfiguration.class)
public class DemoApplication {
}
