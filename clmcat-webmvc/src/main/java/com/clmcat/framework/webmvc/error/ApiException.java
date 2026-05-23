package com.clmcat.framework.webmvc.error;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.clmcat.framework.webmvc.ResponseEntityBuild;
import com.clmcat.framework.webmvc.ResponseErrorStatus;
import com.clmcat.framework.webmvc.ResponseStatus.ErrorStatus;

public class ApiException extends RuntimeException implements Serializable {

    @Serial
	private static final long serialVersionUID = 1L;

	private int httpStatus = 200;

	private Integer status; // 错误状态 -- 数字状态
	
    private String state; // 错误状态 -- 文字状态

    private Object content; // 应答内容

    private String message; // 应答消息

    private String localeMessage; // 应答消息
    
    private String errplace; // 错误位置

    private Object[] messageArgs;

    private ResponseErrorStatus _errorStatus; // 原始的定义状态
    
    public ApiException(ResponseErrorStatus status, String message) {
        this(status, message, null);
//        this.message = message;
//        initLocaleMessage(message);
    }

    public ApiException(ResponseErrorStatus status, String message, String errplace) {
        super(status.getMessage());
        this.httpStatus = status.getHttpStatus();
        this.state = status.getState();
        this.status = status.getStatus();
        this.localeMessage = status.getLocaleMessage();
        this.message = message;
        this.errplace = errplace;
        init(_errorStatus);
        initLocaleMessage(message);
    }

    public ApiException(ResponseErrorStatus status) {
        this(status, status.getMessage(), null);
//        super(status.getMessage());
//        this.httpStatus = status.getHttpStatus();
//        this.state = status.getState();
//        this.message = status.getMessage();
//        this.status = status.getStatus();
//        this.localeMessage = status.getLocaleMessage();
    }

    public ApiException(int httpStatus, ErrorStatus errorStatus, Object content, String message, String errplace) {
        super(message);
        this.httpStatus = httpStatus;
        this.state = errorStatus.getState();
        this.status = errorStatus.getStatus();
        this.content = content;
        this.message = message;
        this.errplace = errplace;
        init(_errorStatus);
        initLocaleMessage(message);
    }

    public ApiException(int httpStatus, ErrorStatus errorStatus, String message) {
        super(message);
        this.httpStatus = httpStatus;
        this.state = errorStatus.getState();
        this.status = errorStatus.getStatus();
        this.message = message;
        init(_errorStatus);
        initLocaleMessage(message);
    }

    public ApiException(int httpStatus, ErrorStatus errorStatus, Object content, String message) {
        super(message);
        this.httpStatus = httpStatus;
        this.state = errorStatus.getState();
        this.status = errorStatus.getStatus();
        this.content = content;
        this.message = message;
        init(_errorStatus);
        initLocaleMessage(message);
    }


    private void init(ResponseErrorStatus _errorStatus) {
        this._errorStatus = _errorStatus;
    }

    private void initLocaleMessage(String message) {
    	if (message != null && !message.isEmpty() && message.startsWith("locale:")) {
    		int index = message.indexOf("=");
    		if (index == -1) {
    			localeMessage = message.substring(7);
    			message = null;
    		} else {
    			localeMessage = message.substring(7, index);
    			this.message = message.substring(index + 1);
    		}
    	} else if (localeMessage == null) {
    		if (state != null && !state.isEmpty()) {
    			localeMessage = state;
    		} else if (status != null) {
    			localeMessage = "status." + status;
    		}
    	}
    }
    
    public ApiException setLocaleMessage(String localeMessage) {
		this.localeMessage = localeMessage;
		return this;
	}
    
    public ApiException setMessageArgs(Object... messageArgs) {
		this.messageArgs = messageArgs;
		return this;
	}
    
    public ApiException setContent(Object content) {
		this.content = content;
		return this;
	}
    public ApiException setContentMap(Object... keyvals) {
    	Map<String, Object> map = new HashMap<>();
    	int size = keyvals.length/2;
    	for (int i = 0; i < size; i+=2) {
    		if (keyvals[i] != null) {
				String key = String.valueOf(keyvals[i]);
				Object value = keyvals[i + 1];
				map.put(key, value);
    		}
		}
    	this.content = map;
    	return this;
    }
    
    public ApiException setErrplace(String errplace) {
		this.errplace = errplace;
		return this;
	}
    
    public ApiException setMessage(String message) {
		this.message = message;
		initLocaleMessage(message);
		return this;
	}
    
    @Override
    public synchronized Throwable fillInStackTrace() {
        return null;
    }
    
    public void assertThrowEx(boolean condition) {
    	if (condition) {
    		throw this;
    	}
    }

    /**
     * 初始化状态是否一致。
     * @param errorStatus
     */
    public boolean equalsInitStatus(ResponseErrorStatus errorStatus) {
        return this._errorStatus.equals(errorStatus);
    }
    public ResponseErrorStatus getInitStatus() {
        return _errorStatus;
    }

    public ResponseEntityBuild create() {
        return ResponseEntityBuild.create()
                .setHttpStatus(httpStatus)
                .setStatus(status)
                .setState(state)
                .setMessage(message)
                .setLocaleMessage(localeMessage)
                .setMessageArgs(messageArgs)
                .setContent(content)
                .setErrplace(errplace);
    }

    public Integer getStatus() {
        return status;
    }

    public String getState() {
		return state;
	}

	public String getMessage() {
        return message;
    }
}
