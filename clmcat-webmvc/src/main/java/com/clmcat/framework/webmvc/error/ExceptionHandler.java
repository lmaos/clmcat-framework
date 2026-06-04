package com.clmcat.framework.webmvc.error;

import com.clmcat.framework.webmvc.*;
import com.clmcat.framework.webmvc.anns.ApiController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.Order;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

import java.util.Map;
import java.util.function.Function;

/**
 * @author zhangxingyu
 *
 * 全局异常处理器。
 */
@Order(Integer.MIN_VALUE)
public class ExceptionHandler implements HandlerExceptionResolver, ApplicationContextAware {
	
	private static final Logger log = LoggerFactory.getLogger(ExceptionHandler.class);

	private ApplicationContext applicationContext;

    public final static Map<ResponseStatus, Function<ResponseStatusAdapter, ResponseErrorStatus>> REGISTER_STATUS = new java.util.HashMap<>(128);
    static {
        REGISTER_STATUS.put(ResponseStatus.AUTH_NO_PERMISSION, (a) -> a.noPermission());
        REGISTER_STATUS.put(ResponseStatus.P_NOTNULL, (a) -> a.paramNotNullError());
        REGISTER_STATUS.put(ResponseStatus.P_NOTZERO, (a) -> a.paramNotZeroError());
        REGISTER_STATUS.put(ResponseStatus.P_VALUE_ERROR, (a) -> a.paramValueError());
        REGISTER_STATUS.put(ResponseStatus.SYSTEM_ERROR, (a) -> a.systemError());
        REGISTER_STATUS.put(ResponseStatus.OK, (a) -> a.success());
        REGISTER_STATUS.put(ResponseStatus.ILLEGAL_STATE, (a) -> a.illegalState());
        REGISTER_STATUS.put(ResponseStatus.R_ALREADY_EXPIRED, (a) -> a.alreadyExpired());
        REGISTER_STATUS.put(ResponseStatus.L_IP_LIMIT, (a) -> a.ipLlimit());
        REGISTER_STATUS.put(ResponseStatus.R_NOT_START, (a) -> a.notStart());
        REGISTER_STATUS.put(ResponseStatus.R_ALREADY_START, (a) -> a.alreadyStart());
        REGISTER_STATUS.put(ResponseStatus.R_ALREADY_OVER, (a) -> a.alreadyOver());
        REGISTER_STATUS.put(ResponseStatus.R_ILLEGALITY_CONTENT, (a) -> a.illegalContent());
        REGISTER_STATUS.put(ResponseStatus.R_OPERATION_FAIL, (a) -> a.operationFail());
        REGISTER_STATUS.put(ResponseStatus.R_ACCOUNT_ERROR, (a) -> a.accountError());
        REGISTER_STATUS.put(ResponseStatus.R_ACCOUNT_LESS_MONEY, (a)-> a.accountLessMoney());
        REGISTER_STATUS.put(ResponseStatus.R_STATUS_HIDE, (a) -> a.statusHide());
        REGISTER_STATUS.put(ResponseStatus.R_STATUS_CLOSE, (a) -> a.statusClose());
        REGISTER_STATUS.put(ResponseStatus.R_ACCOUNT_NOT_EXIST, (a) -> a.accountNotExist());
        REGISTER_STATUS.put(ResponseStatus.F_BUSINESS_REQUEST_FAIL, (a) -> a.businessRequestFail());
        REGISTER_STATUS.put(ResponseStatus.R_ACCOUNT_SIGN_ERROR, (a) -> a.accountSignError());
        REGISTER_STATUS.put(ResponseStatus.R_NOEXIST_DATA, (a) -> a.noExistData());
        REGISTER_STATUS.put(ResponseStatus.R_EXIST_DATA, (a) -> a.existData());
        REGISTER_STATUS.put(ResponseStatus.L_FREQUENT_ACCESS, (a) -> a.frequentlyAccess());
        REGISTER_STATUS.put(ResponseStatus.AUTH_LOGIN_FAIL, (a) -> a.authLoginFail());
        REGISTER_STATUS.put(ResponseStatus.AUTH_TOKEN_INVALID, (a) -> a.authTokenInvalid());
        REGISTER_STATUS.put(ResponseStatus.U_FREEZE, (a) -> a.userFreeze());
        REGISTER_STATUS.put(ResponseStatus.R_BLACKLIST, (a) -> a.userBlacklist());
    }


	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}
    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
		if (null != handler && handler instanceof HandlerMethod) {
			if (!((HandlerMethod) handler).getBeanType().isAnnotationPresent(ApiController.class)) {
				return null;
			}
		}
        ResponseStatusAdapter responseStatusAdapter = (ResponseStatusAdapter) request.getAttribute(ResponseStatusAdapter.KEY);
        try {
            ResponseEntity responseEntity;
            ResponseEntityBuild responseEntityBuild;
            if (ex instanceof ApiException) { // api 异常
                ApiException apiex = (ApiException) ex;
                Function<ResponseStatusAdapter, ResponseErrorStatus> responseStatusAdapterResponseStatusFunction = REGISTER_STATUS.get(apiex.getInitStatus());
                if (responseStatusAdapterResponseStatusFunction != null ) {
                    responseEntityBuild = responseStatusAdapterResponseStatusFunction.apply(responseStatusAdapter).create();
                } else {
                    // API 异常
                    responseEntityBuild = ((ApiException) ex).create();
                }
            } else if (ex instanceof MethodArgumentNotValidException) { // @Validated验证
            	MethodArgumentNotValidException m = (MethodArgumentNotValidException) ex;
    			ObjectError error = m.getBindingResult().getAllErrors().get(0);
    			String msg = error.getDefaultMessage();
    			String errplace = error.getObjectName();
    			responseEntityBuild = responseStatusAdapter.paramValueError()// ResponseStatus.P_VALUE_ERROR
	            	.create()
	            	.setMessage(msg)
	            	.setErrplace(errplace)
	            	;
            } else if (ex instanceof ConstraintViolationException) { // 方法参数使用@Validated验证 参数不正确
            	ConstraintViolationException cve = (ConstraintViolationException) ex;
            	ConstraintViolation<?> cv = cve.getConstraintViolations().iterator().next();
            	responseEntityBuild = responseStatusAdapter.paramValueError().apiEx() //ResponseStatus.P_VALUE_ERROR.apiEx()
            	.setMessage(cv.getMessage())
            	.create()
            	;
            }
            else if (ex instanceof MissingPathVariableException) {
                MissingPathVariableException mex = (MissingPathVariableException) ex;
                responseEntityBuild = responseStatusAdapter.paramNotNullError().create() //ResponseStatus.P_NOTNULL.create()
                        .setErrplace(mex.getVariableName());
                error("参数不存在异常: MissingPathVariableException", mex.getVariableName(), ex, request);
            } else if (ex instanceof MissingServletRequestParameterException) {
                // 参数必填的异常
                MissingServletRequestParameterException mex = (MissingServletRequestParameterException) ex;
                responseEntityBuild = responseStatusAdapter.paramNotNullError().create() //ResponseStatus.P_NOTNULL.create()
                        .setErrplace(mex.getParameterName());
                error("参数值异常: MissingServletRequestParameterException", mex.getParameterName(), ex, request);
            } else if (ex instanceof NumberFormatException) {
                // 参数值错误 - 数字格式转化失败
            	responseEntityBuild = responseStatusAdapter.paramValueError() //ResponseStatus.P_VALUE_ERROR
                        .create();
                error("参数值异常: NumberFormatException", null, ex, request);
            } else if (ex instanceof MethodArgumentTypeMismatchException) {
                // 参数值错误 - 类型转化失败
                MethodArgumentTypeMismatchException mex = (MethodArgumentTypeMismatchException) ex;
                responseEntityBuild = responseStatusAdapter.paramValueError() // ResponseStatus.P_VALUE_ERROR
                        .create()
                        .setErrplace(mex.getName());
                error("参数值异常: MethodArgumentTypeMismatchException", mex.getName(), ex, request);
            } 
            else if (ex instanceof org.springframework.web.servlet.resource.NoResourceFoundException) {
            	org.springframework.web.servlet.resource.NoResourceFoundException
            	ne = (org.springframework.web.servlet.resource.NoResourceFoundException)ex;
            	responseEntityBuild = ResponseStatus.NOT_FOUND.create()
            			.setErrplace(ne.getResourcePath());
            }
            else { // 其他异常

                if (ex instanceof IllegalStateException) {
                	responseEntityBuild = responseStatusAdapter.illegalState().create(); //ResponseStatus.ILLEGAL_STATE.create();
                } else {
                	responseEntityBuild = responseStatusAdapter.systemError().create();  //ResponseStatus.SYSTEM_ERROR.create();
                }
                if (log.isErrorEnabled()) {
                	try {
                		log.error("err-uri: " + request.getRequestURI(), ex);
                	} catch (Exception e) {
                		log.error("err-uri: null", ex);
					}
                } else {
                	ex.printStackTrace();
                }
            }
            
            responseEntityBuild.setApplicationContext(applicationContext);
            responseEntity = responseEntityBuild.build(request);
            responseEntity.out(response);

        } catch (Exception e) {
            log.error("系统内部发生异常： ", e);
        }
        return new ModelAndView();
    }

    private void error(String  msg, String uri, Exception ex, HttpServletRequest request) {
        try {
            ApiController.LogMode logMode = (ApiController.LogMode) request.getAttribute(ApiController.LogMode.REQUEST_LOG_MODE_KET);
            logMode = logMode == null ? ApiController.LogMode.NONE : logMode;
            if (logMode == ApiController.LogMode.NONE) {
                return;
            }
            if (logMode == ApiController.LogMode.ERROR) {
                uri = uri == null ? request.getRequestURI() : uri;
                log.error("msg: {}, uri: {}", msg, uri, ex);
            } else if (logMode == ApiController.LogMode.INFO) {
                uri = uri == null ? request.getRequestURI() : uri;
                log.info("msg: {}, uri: {}", msg, uri);
            }
        } catch (Exception e) {
            log.error(msg + ": null", e);
        }
    }
}
