package com.clmcat.demo.reqparam;

import java.util.LinkedHashMap;
import java.util.Map;

import com.clmcat.framework.webmvc.anns.Params;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reqparam")
public class ReqParamDemoController {

    @GetMapping("/phone")
    public PhoneLoginDto phone(@Params PhoneLoginDto dto) {
        return dto;
    }

    @PutMapping("/phone")
    public PhoneLoginDto phonePut(@Params PhoneLoginDto dto) {
        return dto;
    }

    @DeleteMapping("/phone")
    public Map<String, Object> phoneDelete(@Params("id") Integer id) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", id);
        return result;
    }
}
