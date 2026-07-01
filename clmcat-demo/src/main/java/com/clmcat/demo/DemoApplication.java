package com.clmcat.demo;

import com.clmcat.framework.webmvc.WebMvcConfiguration;
import com.clmcat.framework.webmvc.anns.EnableBasicWeb;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@EnableBasicWeb
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
