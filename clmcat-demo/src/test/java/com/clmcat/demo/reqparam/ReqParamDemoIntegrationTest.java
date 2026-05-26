package com.clmcat.demo.reqparam;

import com.clmcat.demo.DemoApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = DemoApplication.class)
class ReqParamDemoIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void shouldBindBeanWithoutImplicitPrefix() throws Exception {
        mockMvc.perform(get("/reqparam/phone")
                        .param("phone", "1234")
                        .param("countryCode", "+86"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.phone").value("1234"))
                .andExpect(jsonPath("$.content.countryCode").value("+86"));
    }

    @Test
    void shouldMergeJsonBodyAndQueryParameter() throws Exception {
        mockMvc.perform(put("/reqparam/phone")
                        .param("phone", "1234")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"countryCode\":\"+86\",\"code\":\"4321\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.phone").value("1234"))
                .andExpect(jsonPath("$.content.countryCode").value("+86"))
                .andExpect(jsonPath("$.content.code").value("4321"));
    }

    @Test
    void shouldParseBraceWrappedBodyAsJsonWithoutContentType() throws Exception {
        mockMvc.perform(put("/reqparam/phone")
                        .param("phone", "1234")
                        .content("{\"countryCode\":\"+86\",\"code\":\"4321\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.phone").value("1234"))
                .andExpect(jsonPath("$.content.countryCode").value("+86"))
                .andExpect(jsonPath("$.content.code").value("4321"));
    }

    @Test
    void shouldResolveDeleteFormBody() throws Exception {
        mockMvc.perform(delete("/reqparam/phone")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content("id=12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.id").value(12));
    }

    @Test
    void shouldAutoFillClientIpInIntegrationPath() throws Exception {
        mockMvc.perform(get("/reqparam/phone")
                        .param("phone", "1234")
                        .header("X-Forwarded-For", "unknown, 203.0.113.10, 10.0.0.1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.phone").value("1234"))
                .andExpect(jsonPath("$.content.clientIp").value("203.0.113.10"));
    }
}
