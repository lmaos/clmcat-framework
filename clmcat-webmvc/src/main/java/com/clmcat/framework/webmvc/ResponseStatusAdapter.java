package com.clmcat.framework.webmvc;

/**
 * @author zhangxingyu
 *
 * 系统内应答状态适配器。 beanName = responseStatusAdapter， 可以重新定义。
 * <br>
 * ExceptionHandler.REGISTER_STATUS; 注册转译异常抛出的状态。
 */
public interface ResponseStatusAdapter {

    public static final String KEY = "ResponseStatusAdapter";

    /**
     * 成功状态
     * @return
     */
    default ResponseErrorStatus success() {
        return ResponseStatus.OK;
    }


    /**
     * 没有权限操作, 登陆失败。
     */
    default ResponseErrorStatus noPermission() {
        return ResponseStatus.AUTH_NO_PERMISSION;
    }

    /**
     * 参数值错误。
     */
    default ResponseErrorStatus paramValueError() {
        return ResponseStatus.P_VALUE_ERROR;
    }

    /**
     * 参数不能为空。
     */
    default ResponseErrorStatus paramNotNullError() {
        return ResponseStatus.P_NOTNULL;
    }

    default ResponseErrorStatus paramNotZeroError() {
        return ResponseStatus.P_NOTZERO;
    }

    /**
     * 系统错误
     */
    default ResponseErrorStatus systemError() {
        return ResponseStatus.SYSTEM_ERROR;
    }

    /**
     * 非法状态
     */
    default ResponseErrorStatus illegalState() {
        return ResponseStatus.ILLEGAL_STATE;
    }
    /** 已经失效 **/
    default ResponseErrorStatus alreadyExpired() {
        return ResponseStatus.R_ALREADY_EXPIRED;
    }
    /** 已经开始 **/
    default ResponseErrorStatus alreadyStart() {
        return ResponseStatus.R_ALREADY_START;
    }
    /** 已经结束 **/
    default ResponseErrorStatus alreadyOver() {
        return ResponseStatus.R_ALREADY_OVER;
    }
    /** ip限制 **/
    default ResponseErrorStatus ipLlimit() {
        return ResponseStatus.R_IP_LIMIT;
    }
    /* 没有开始 **/
    default ResponseErrorStatus notStart() {
        return ResponseStatus.R_NOT_START;
    }
    /** 非法的内容 */
    default ResponseErrorStatus illegalContent() {
        return ResponseStatus.R_ILLEGALITY_CONTENT;
    }

//    R_OPERATION_FAIL
    /** 操作失败 */
    default ResponseErrorStatus operationFail() {
        return ResponseStatus.R_OPERATION_FAIL;
    }
    // R_STATUS_HIDE
    /** 状态隐藏 */
    default ResponseErrorStatus statusHide() {
        return ResponseStatus.R_STATUS_HIDE;
    }
    // statusClose
    /** 状态关闭 */
    default ResponseErrorStatus statusClose() {
        return ResponseStatus.R_STATUS_CLOSE;
    }
    /** 账户不存在 */
    default ResponseErrorStatus accountNotExist() {
        return ResponseStatus.R_ACCOUNT_NOT_EXIST;
    }
    /** 业务内调用远程API请求失败 */
    default ResponseErrorStatus businessRequestFail() {
        return ResponseStatus.R_BUSINESS_REQUEST_FAIL;
    }
    /** 账户签名错误错误 */
    default ResponseErrorStatus accountSignError() {
        return ResponseStatus.R_ACCOUNT_SIGN_ERROR;
    }
    // accountError
    /** 账户错误 */
    default ResponseErrorStatus accountError() {
        return ResponseStatus.R_ACCOUNT_ERROR;
    }
    /** 账户余额不足 */
    default ResponseErrorStatus accountLessMoney() {
        return ResponseStatus.R_ACCOUNT_LESS_MONEY;
    }


    /** 数据不存在 */
    default ResponseErrorStatus noExistData() {
        return ResponseStatus.R_NOEXIST_DATA;
    }
    /** 数据已存在 */
    default ResponseErrorStatus existData() {
        return ResponseStatus.R_EXIST_DATA;
    }

    /** 频繁访问 */
    default ResponseErrorStatus frequentlyAccess() {
        return ResponseStatus.R_FREQUENT_ACCESS;
    }

    /** TOKEN无效 */
    default ResponseErrorStatus authTokenInvalid() {
        return ResponseStatus.AUTH_TOKEN_INVALID;
    }

    /** 登录失败 */
    default ResponseErrorStatus authLoginFail() {
        return ResponseStatus.AUTH_LOGIN_FAIL;
    }

    // U_FREEZE
    /** 账户被冻结 */
    default ResponseErrorStatus userFreeze() {
        return ResponseStatus.U_FREEZE;
    }

    // U_BLACKLIST
    /** 账户被拉黑 */
    default ResponseErrorStatus userBlacklist() {
        return ResponseStatus.U_BLACKLIST;
    }




    public static class DefaultResponseStatusAdapter implements ResponseStatusAdapter {
        public static final DefaultResponseStatusAdapter defaultInstance = new DefaultResponseStatusAdapter();
    }
}
