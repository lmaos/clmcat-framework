package com.clmcat.framework.webmvc;

/**
 * @author zhangxingyu
 *
 * 通用处理的应答状态。 如果需要自定义的应答状态可以实现 ResponseErrorStatus
 */
public enum ResponseStatus implements ResponseErrorStatus {

	OK(200, 0, "OK", "一个成功的请求"),

	NOT_FOUND(404, 404, "Not Found", "页面不存在"),
	SYSTEM_ERROR(HttpStatusValue.S_ERROR_STATUS, 500, "system error", "系统发生不确定异常需要管理员查看"),
	ILLEGAL_STATE(HttpStatusValue.S_ERROR_STATUS, 500, "Illegal State", "非法状态"),

	// 权限相关

	AUTH_NO_PERMISSION(HttpStatusValue.A_ERROR_STATUS, 100001, "权限验证失败"),

	AUTH_LOGIN_FAIL(HttpStatusValue.A_ERROR_STATUS, 100002, "登录失败"),

	AUTH_TOKEN_INVALID(HttpStatusValue.A_ERROR_STATUS, 100003, "TOKEN无效"),

	AUTH_VERIFY_FAIL(HttpStatusValue.A_ERROR_STATUS, 100004, "验证失败"),
    /** 被拒绝访问 */
    A_ACCESS_DENIED(HttpStatusValue.A_ERROR_STATUS, 100005, "access denied", "访问被拒绝, 没有权限访问"),
    /** 限制访问 */
    A_ACCESS_LIMITED(HttpStatusValue.A_ERROR_STATUS, 100006, "access limited", "访问受限, 请求过于频繁被限制访问"),

    // 参数相关

	P_ERROR(HttpStatusValue.P_ERROR_STATUS, 300001, "参数错误"),

	P_NOTNULL(HttpStatusValue.P_ERROR_STATUS, 300002, "参数不能为空", "请求时携带参数值不可以是空"),

	P_NOTZERO(HttpStatusValue.P_ERROR_STATUS, 300003, "参数必须大于0", "请求时携带参数值必须大于0的值"),

	P_VALUE_ERROR(HttpStatusValue.P_ERROR_STATUS, 300004, "参数值错误", "请求时携带的参数值验证过程值内容不通过"),

	// 结果相关

	R_ERROR(HttpStatusValue.R_ERROR_STATUS, 400001, "结果错误", "结果异常段 400000 < status < 500000"),

	R_OPERATION_FAIL(HttpStatusValue.R_ERROR_STATUS, 400002, "operation failure", "通用的操作失败返回结果"),

	R_NOEXIST_DATA(HttpStatusValue.R_ERROR_STATUS, 400003, "data does not exist", "数据不存在, 查询更新数据时候, 没有数据可以返回这个错"),

	R_EXIST_DATA(HttpStatusValue.R_ERROR_STATUS, 400004, "data already exists", "数据已存在, 新建,插入,保存数据时,已经存在同样内容时的错"),
    /** 访问频繁 */
	R_FREQUENT_ACCESS(HttpStatusValue.R_ERROR_STATUS, 400005, "frequently accessed", "操作过于频繁, 或请求太频繁, 一般使用: 请求限制一段时间内只能请求一次,却请求了更多次"),
	/** IP限制 */
	R_IP_LIMIT(HttpStatusValue.R_ERROR_STATUS, 400006, "IP limit", "接口请求时 限制ip访问次数, 或限制ip不允许访问时使用这个状态"),

	/** 活动暂未开始 */
	R_NOT_START(HttpStatusValue.R_ERROR_STATUS, 400007, "not started", "没有启动, 没有开始, 或者开始时间未到"),
	/** 活动已经开始 */
	R_ALREADY_START(HttpStatusValue.R_ERROR_STATUS, 400008, "already started", "已经开始, 依然触发启动时发生错误"),
	/** 活动已经结束 */
	R_ALREADY_OVER(HttpStatusValue.R_ERROR_STATUS, 400009, "over.", "已经结束, 或者已经超过使用时间,超过活动时间, 超过业务进行时间"),
	/** 数据已经失效 */
	R_ALREADY_EXPIRED(HttpStatusValue.R_ERROR_STATUS, 400010, "expired.", "数据已经失效, 缓存已经超时, 状态已经超时, 或者无效了"),
	/** 状态标记隐藏 */
	R_STATUS_HIDE(HttpStatusValue.R_ERROR_STATUS, 400011, "", "功能隐藏, 状态标记隐藏"),
	/** 状态标记关闭 */
	R_STATUS_CLOSE(HttpStatusValue.R_ERROR_STATUS, 400012, "close", "功能隐藏, 状态标记隐藏"),
	/** 非法的内容 */
	R_ILLEGALITY_CONTENT(HttpStatusValue.R_ERROR_STATUS, 400013, "illegality content", "非法内容"),
	/** 非法的内容 */
	R_ILLEGALITY_TEXT(HttpStatusValue.R_ERROR_STATUS, 400014, "illegality text", "非法内容"),


	R_ACCOUNT_ERROR(HttpStatusValue.R_ERROR_STATUS, 400015, "account error", "账户错误"),
	R_ACCOUNT_LESS_MONEY(HttpStatusValue.R_ERROR_STATUS, 400016, "余额不足", "账户余额不足"),
	R_ACCOUNT_SIGN_ERROR(HttpStatusValue.R_ERROR_STATUS, 400017, "签名失败", "签名失败"),
	R_ACCOUNT_NOT_EXIST(HttpStatusValue.R_ERROR_STATUS, 400018, "账户不存在", "账户不存在"),
	R_BUSINESS_REQUEST_FAIL(HttpStatusValue.R_ERROR_STATUS, 400019, "业务请求失败", "业务请求失败"),
    /** 不支持的操作 */
    R_NOT_SUPPORTED(HttpStatusValue.R_ERROR_STATUS, 400020, "not supported", "不支持的操作"),

    // 用户相关
	U_INFO_ERROR(HttpStatusValue.U_ERROR_STATUS, 200001, "用户信息错误"),

	U_REGISTER_FAIL(HttpStatusValue.U_ERROR_STATUS, 200002, "用户注册失败", "通用的注册失败"),

	U_EXIST_ACCOUNT(HttpStatusValue.U_ERROR_STATUS, 200003, "已存在账户"),

	U_FREEZE(HttpStatusValue.U_ERROR_STATUS, 200005, "账户被冻结", "账户发生违规行为被官方标记冻结状态"),

	U_BLACKLIST(HttpStatusValue.U_ERROR_STATUS, 200006, "账户被拉黑", "请求查看用户信息或其他对当前用户操作行为时，被这个用户拉黑了则无法使用这个业务"),

	;

	
	
	/**
	 * 定义自定义错误时返回的错误状态, Http Status
	 * 
	 * @author zhangxingyu
	 *
	 */
	public final static class HttpStatusValue {

		public static final int A_ERROR_STATUS = 403; // 权限错误
		public static final int U_ERROR_STATUS = 401; // 用户错误
		public static final int P_ERROR_STATUS = 400; // 参数错误
		public static final int R_ERROR_STATUS = 200; // 结果错误 - 业务状态码
		public static final int S_ERROR_STATUS = 500; // 系统错误状态
	}

	public final static class ErrorStatus {
		private Integer status;
		private String state;

		public ErrorStatus(Integer status, String state) {
			this.status = status;
			this.state = state;
		}

		public Integer getStatus() {
			return status;
		}

		public String getState() {
			return state;
		}

		public static final ErrorStatus of(Integer status, String state) {
			return new ErrorStatus(status, state);
		}

		public static final ErrorStatus of(Integer status) {
			return new ErrorStatus(status, "NOSTATE");
		}

		public static final ErrorStatus of(String state) {
			return new ErrorStatus(500, state);
		}

	}

	private int httpStatus; // HTTP 状态码
	private Integer status; // HTTP 状态码
	private String message; // HTTP 错误内容
	private String describe;

	ResponseStatus(int httpStatus, int status, String message) {
		this.httpStatus = httpStatus;
		this.message = message;
		this.describe = message;
		this.status = status;
	}

	ResponseStatus(int httpStatus, int status, String message, String describe) {
		this.httpStatus = httpStatus;
		this.message = message;
		this.describe = describe;
		this.status = status;
	}

	ResponseStatus(int httpStatus, String message) {
		this.httpStatus = httpStatus;
		this.message = message;
		this.describe = message;
	}

	ResponseStatus(int httpStatus, String message, String describe) {
		this.httpStatus = httpStatus;
		this.message = message;
		this.describe = describe;
	}

	public ResponseEntityBuild create() {
		return ResponseEntityBuild.create()
				.setHttpStatus(httpStatus)
				.setStatus(status)
				.setState(this.name())
				.setMessage(message)
				.setLocaleMessage(name());
	}

	@Override
	public String getState() {
		return name();
	}
	
	public int getHttpStatus() {
		return httpStatus;
	}

	public String getMessage() {
		return message;
	}

	public String getDescribe() {
		return describe;
	}

	public Integer getStatus() {
		return status;
	}

	public ErrorStatus toErrorStatus() {
		return ErrorStatus.of(status, getState());
	}

	public String toString() {
		StringBuilder text = new StringBuilder();
		text.append("{").append("\"httpStatus\":\"").append(httpStatus).append("\",").append("\"status\":\"")
				.append(status).append("\",").append("\"state\":\"").append(name()).append("\",")
				.append("\"message\":\"").append(message).append("\",").append("\"describe\":\"").append(describe)
				.append("\"").append("}");
		return text.toString();
	}

	public static void main(String[] args) {
		ResponseStatus[] values = ResponseStatus.values();
		for (ResponseStatus value : values) {
			System.out.println(value);
		}
	}
}
