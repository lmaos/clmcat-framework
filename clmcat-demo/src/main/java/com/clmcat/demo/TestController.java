package com.clmcat.demo;

import com.clmcat.framework.webmvc.anns.ApiController;
import org.springframework.web.bind.annotation.RequestMapping;

@ApiController
@RequestMapping("/test")
public class TestController {

    @RequestMapping("")
    public String value() {
        return "test";
    }

}
