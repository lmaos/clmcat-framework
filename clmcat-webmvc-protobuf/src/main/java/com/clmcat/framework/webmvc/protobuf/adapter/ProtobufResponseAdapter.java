package com.clmcat.framework.webmvc.protobuf.adapter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.ValueFilter;
import com.google.protobuf.ByteString;
import com.google.protobuf.GeneratedMessage;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import com.clmcat.framework.webmvc.ResponseEntity;
import com.clmcat.framework.webmvc.ResponseEntityResultAdapter;
import com.clmcat.framework.webmvc.protobuf.entity.DataType;
import com.clmcat.framework.webmvc.protobuf.entity.ProtoResponseEntity;
import com.clmcat.framework.webmvc.result.SerializerValue;
import org.springframework.beans.factory.InitializingBean;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author zhangxingyu
 *
 * protobuf 应答通用适配。使用：ProtoResponseEntity 包装数据。
 */
@Slf4j
public class ProtobufResponseAdapter implements ResponseEntityResultAdapter, InitializingBean {
    @Override
    public void out(HttpServletResponse response, ResponseEntity responseEntity) throws IOException {
        String requestId = responseEntity.getRequestId();
        Object content = responseEntity.getContent();
        String state = responseEntity.getState();
        String message = responseEntity.getMessage();
        String errplace = responseEntity.getErrplace();
        Integer status = responseEntity.getStatus();
        // String localeMessage = responseEntity.getLocaleMessage();

        ProtoResponseEntity.Builder builder = ProtoResponseEntity.newBuilder();
        if (requestId != null) {
            builder.setRequestId(requestId);
        }
        if (status != null) {
            builder.setStatus(status);
        }
        if (message != null) {
            builder.setMessage(message);
        }
        if (state != null) {
            builder.setState(state);
        }
        if (errplace != null) {
            builder.setErrplace(errplace);
        }
        if (content == null) {
            builder.setDataType(DataType.NONE);
        } else if (content instanceof GeneratedMessage) {
            builder.setDataType(DataType.PROTOBUF);
            builder.setData(((GeneratedMessage) content).toByteString());
        } else {
            ValueFilter valueFilter = valueFormat(responseEntity);
            if (valueFilter == null) {
                valueFilter = SerializerValue.defaultSerializerValue;
            }
            builder.setDataType(DataType.JSON_STRING);
            String json = JSON.toJSONString(content, valueFilter);
            builder.setData(ByteString.copyFrom(json.getBytes(StandardCharsets.UTF_8)));
        }
        response.setHeader("Content-Type", "application/x-protobuf");
        ServletOutputStream out = response.getOutputStream();
        ProtoResponseEntity build = builder.build();
        out.write(build.toByteArray());
        out.flush();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("ProtobufResponseAdapter init");
    }
}
