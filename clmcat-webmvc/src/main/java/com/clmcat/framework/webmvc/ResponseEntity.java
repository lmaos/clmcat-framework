package com.clmcat.framework.webmvc;

import java.io.IOException;
import java.util.Map;

import com.clmcat.framework.webmvc.ResponseEntityKey.DefaultResponseEntityKey;
import org.springframework.web.servlet.View;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 应答实体 JSON
 */
public class ResponseEntity implements View {

    private Integer httpStatus; // HTTP 状态

    private String requestId;
    
    private Integer status; // 应答状态--数字展示的效果
    
    private String state; // 应答状态--文字展示的效果

    private Object content; // 应答内容

    private String message; // 应答消息

    private String errplace; // 错误位置

    private String callback; // 函数回调
    
    private String localeMessage; // 国际化KEY
    // 结果KEY
    private ResponseEntityKey entityKey = DefaultResponseEntityKey.defaultInstance;
    // 结果适配器
    private ResponseEntityResultAdapter resultAdapter = ResponseEntityResultAdapter.defaultInstance;
    
	public ResponseEntity(Integer httpStatus, String requestId, Integer status, String state, Object content,
			String message, String errplace, String callback, String localeMessage, ResponseEntityKey entityKey,
			ResponseEntityResultAdapter resultAdapter) {
        if (httpStatus != null && httpStatus > 0) {
            this.httpStatus = httpStatus;
        }
        if (entityKey == null) {
        	entityKey = DefaultResponseEntityKey.defaultInstance;
        }
        if (resultAdapter == null) {
        	resultAdapter = ResponseEntityResultAdapter.defaultInstance;
        }
        this.requestId = requestId;
        this.status = status;
        this.state = state;
        this.content = content;
        this.message = message;
        this.errplace = errplace;
        this.callback = callback;
        this.entityKey = entityKey;
        this.resultAdapter = resultAdapter;
        this.localeMessage = localeMessage;
    }

    public void out(HttpServletResponse response) throws IOException {
        if (httpStatus != null) {
        	response.setStatus(httpStatus);
        	// response.sendError(httpStatus, state); //            response.setStatus(httpStatus, state);
        }
        resultAdapter.out(response, this);
//        String contentType = "application/json;charset=UTF-8";
//        if (StringUtils.isNotBlank(callback)) {
//            contentType = "application/javascript;charset=UTF-8";
//        }
//        response.setContentType(contentType);
//        // body
//        String json = toJson();
//        byte[] body = json.getBytes(Charset.forName("UTF-8"));
//        ServletOutputStream out = response.getOutputStream();
//        out.write(body);
//        out.flush();
    }

//    public String toJson(String callback) {
//    	if (resultAdapter == null) {
//    		resultAdapter = ResponseEntityResultAdapter.defaultInstance;
//    	}
//    	ValueFilter valueFilter = SerializerValue.defaultSerializerValue;
//    	if (this.resultAdapter.valueFormat(this) != null) {
//    		valueFilter = this.resultAdapter.valueFormat(this);
//    	}
//
//        // 应答包装
//        Map<String, Object> responseWrapper = resultAdapter.toMap(this);
//        // 转化为 JSON
//        String json = JSON.toJSONString(responseWrapper, valueFilter, SerializerFeature.DisableCircularReferenceDetect);
//
//        // 判断是否是 JSONP的消息
//        if (StringUtils.isNotBlank(callback)) {
//            return callback + "(" + json + ")";
//        } else {
//            return json;
//        }
//    }

//    public String toJson() {
//       return toJson(callback);
//    }

//    @Override
//    public String toString() {
//        return toJson();
//    }

    public Integer getHttpStatus() {
        return httpStatus;
    }
    
    public Integer getStatus() {
		return status;
	}

    public String getState() {
        return state;
    }

    public String getRequestId() {
        return requestId;
    }

    public Object getContent() {
        return content;
    }

    public String getMessage() {
        return message;
    }

    public String getErrplace() {
        return errplace;
    }

    public String getCallback() {
        return callback;
    }
    public String getLocaleMessage() {
		return localeMessage;
	}


    @Override
    public String getContentType() {
        return "application/json;charset=UTF-8";
    }

    public ResponseEntityKey getEntityKey() {
		return entityKey;
	}
    
    @Override
    public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (model == null) {
            return;
        }
        for (Map.Entry<String, ?> entry : model.entrySet()) {
            request.setAttribute(entry.getKey(), entry.getValue());
        }
    }
}
