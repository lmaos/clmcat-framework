# clmcat-webmvc

`clmcat-webmvc` 提供了一套偏业务化的 Spring WebMVC 扩展，核心能力包括：

- `@ApiController` 统一 API 返回结构
- `@Params` 统一请求参数注入
- `@GetLocale` 注入当前请求语言
- `@Token` / `@LoginVerify` / `@NoLoginVerify` 登录态处理
- `ResponseStatus` / `ResponseEntityBuild` 统一业务响应
- `CustomResponseEntity` 直接输出自定义字节响应

## 1. 启用方式

在 Spring Boot 应用上启用：

```java
import com.clmcat.framework.webmvc.anns.EnableBasicWeb;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableBasicWeb
@SpringBootApplication
public class DemoApplication {
}
```

启用后会自动注册：

- `RequestInterceptor`
- `RequestParamInjector`
- `LocaleParameterInjector`
- `TokenParameterInjector`
- `ComponentResponseHandler`
- `CustomResponseHandler`
- `FormContentFilter`

## 2. @ApiController

`@ApiController` 是当前模块推荐使用的控制器注解，等价于 `@RestController`，同时会进入统一响应包装链路。

```java
import com.clmcat.framework.webmvc.anns.ApiController;
import org.springframework.web.bind.annotation.GetMapping;

@ApiController
public class UserController {

    @GetMapping("/user/profile")
    public UserProfile profile() {
        return new UserProfile();
    }
}
```

默认会被包装为统一 JSON 结构，常见字段包括：

```json
{
  "requestId": "xxx",
  "status": 0,
  "state": "OK",
  "content": {},
  "message": "OK"
}
```

`@ApiController` 可选能力：

```java
@ApiController(
    entityKey = MyEntityKey.class,
    statusAdapter = MyStatusAdapter.class,
    resultAdapterName = "myResultAdapter"
)
```

## 3. @Params

`@Params` 用于注入简单参数和对象参数，支持：

- query parameter
- form parameter
- JSON body
- header
- cookie
- client IP
- request attribute

### 3.1 简单参数

```java
import com.clmcat.framework.webmvc.anns.Params;

@ApiController
public class DemoController {

    public Object one(@Params("id") Long id) {
        return id;
    }

    public Object two(@Params(name = "name", required = false, defaultValue = "guest") String name) {
        return name;
    }
}
```

### 3.2 对象参数

```java
public class PhoneLoginDto {
    private String phone;
    private String countryCode;

    public void setPhone(String phone) { this.phone = phone; }
    public void setCountryCode(String countryCode) { this.countryCode = countryCode; }
}

public Object login(@Params PhoneLoginDto dto) {
    return dto;
}
```

对象参数规则：

1. `GET` / form 请求：按字段名注入
2. JSON body：先按 JSON 反序列化对象
3. 再用 query/form/custom 参数覆盖同名字段
4. `PUT` / `PATCH` / `DELETE` 也支持 body 解析

### 3.3 显式前缀

```java
public Object login(@Params("payload") PhoneLoginDto dto) {
    return dto;
}
```

对应参数：

```text
payload.phone=1234
payload.countryCode=+86
```

### 3.4 字段级 @Params

字段上也可以单独指定来源：

```java
public class HeaderAwarePhoneLoginDto {
    private String phone;

    @Params(name = "device-id", scope = Params.ParamsScope.HEADER, required = false)
    private String deviceId;

    public void setPhone(String phone) { this.phone = phone; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
}
```

### 3.5 scope 说明

| scope | 说明 |
|---|---|
| `PARAM` | 默认值。优先从 body/form/query/custom 参数中读取 |
| `HEADER` | 从请求头读取 |
| `COOKIE` | 从 cookie 读取 |
| `IP` | 注入客户端 IP |
| `REQUEST` | 从 `request.getAttribute(...)` 读取 |
| `NONE` | 只走原始 `request.getParameter(...)`，不使用自定义参数扩展 |

### 3.6 REQUEST scope

适合读取拦截器、过滤器、前置业务逻辑提前放入 request 的对象。

#### 简单值

```java
public Object id(@Params(name = "requestId", scope = Params.ParamsScope.REQUEST, required = false) String requestId) {
    return requestId;
}
```

#### 整体对象

```java
public Object user(@Params(name = "loginUser", scope = Params.ParamsScope.REQUEST) LoginUser loginUser) {
    return loginUser;
}
```

如果 `request.getAttribute("loginUser")` 本身就是 `LoginUser`，会直接返回该对象。

#### 按字段装配

```java
public Object dto(@Params(scope = Params.ParamsScope.REQUEST) PhoneLoginDto dto) {
    return dto;
}
```

这时会按字段名从 request attribute 中继续组装：

```java
request.setAttribute("phone", "1234");
request.setAttribute("code", "4321");
```

### 3.7 IP 注入

```java
public Object ip(@Params(name = "clientIp", scope = Params.ParamsScope.IP, required = false) String clientIp) {
    return clientIp;
}
```

当前会兼容常见代理头：

- `X-Forwarded-For`
- `X-Real-IP`
- `Forwarded`
- 以及常见网关/代理扩展头

推荐生产环境通过可信代理（如 Nginx）统一覆盖这些头。

## 4. @GetLocale

`@GetLocale` 用于注入当前请求语言。

```java
import com.clmcat.framework.webmvc.anns.GetLocale;

public Object locale1(@GetLocale Locale locale) {
    return locale;
}

public Object locale2(@GetLocale String locale) {
    return locale;
}
```

`String` 类型默认返回标准语言标签，例如：

- `zh-CN`
- `en-US`
- `ja-JP`

当前语言解析顺序大致为：

1. `request attribute: userLocale`
2. `request attribute: userLanguage`
3. `header/param: userLanguage`
4. `header/param: locale`
5. `Accept-Language`
6. `request.getLocale()`
7. 默认 locale

其中：

- `userLanguage`：表示用户显式指定
- `userLocale`：表示当前请求最终生效的语言

## 5. @Token

`@Token` 用于读取 token 或 token 解析后的用户信息。

```java
import com.clmcat.framework.webmvc.anns.Token;
import com.clmcat.framework.webmvc.verify.TokenInfo;

public Object token(
    @Token String token,
    @Token Long userId,
    @Token TokenInfo tokenInfo
) {
    return userId;
}
```

默认参数名：

- token 参数名：`token`
- 用户 ID 参数名：`userId`

也可以自定义：

```java
public Object token(@Token("accessToken") String token) {
    return token;
}
```

## 6. @LoginVerify / @NoLoginVerify

### 6.1 开启登录校验

```java
import com.clmcat.framework.webmvc.anns.LoginVerify;

@ApiController
@LoginVerify
public class UserController {
}
```

也可以加在方法上：

```java
@LoginVerify
public Object profile() {
    return "ok";
}
```

可配置项：

```java
@LoginVerify(
    token = "token",
    userId = "userId",
    mustLogin = true
)
```

还可以指定：

- 自定义登录验证实现：`loginVerify`
- 自定义登录失败响应：`loginError`

### 6.2 忽略登录校验

```java
import com.clmcat.framework.webmvc.anns.NoLoginVerify;

@NoLoginVerify
public Object publicApi() {
    return "public";
}
```

## 7. ResponseStatus / ResponseEntityBuild

### 7.1 直接返回普通对象

在 `@ApiController` 下，普通对象会自动包装为统一响应：

```java
public Object detail() {
    return Map.of("id", 1);
}
```

### 7.2 使用 ResponseStatus

```java
import com.clmcat.framework.webmvc.ResponseStatus;

public Object ok() {
    return ResponseStatus.OK.create().setContent("success").build();
}
```

### 7.3 自定义业务响应

```java
import com.clmcat.framework.webmvc.ResponseEntityBuild;

public Object result() {
    return ResponseEntityBuild.create()
            .setStatus(0)
            .setState("OK")
            .setMessage("success")
            .setContent(Map.of("id", 1))
            .build();
}
```

### 7.4 国际化消息

```java
public Object message() {
    return ResponseStatus.OK.create()
            .setLocaleMessage("user.login.success")
            .setMessageArgs(new Object[]{"Tom"})
            .build();
}
```

## 8. CustomResponseEntity

如果你不想走统一 JSON 包装，而是要直接输出字节流、自定义内容类型，可以返回 `CustomResponseEntity`。

```java
import com.clmcat.framework.webmvc.result.CustomResponseEntity;

public CustomResponseEntity text() {
    return new CustomResponseEntity()
            .setContentType("text/plain;charset=UTF-8")
            .setData("hello".getBytes());
}
```

适合：

- 文件下载
- 文本输出
- 自定义二进制响应

## 9. requestId / locale 请求上下文

框架会在请求入口统一处理部分上下文：

- `requestId`
- `userLocale`
- `userLanguage`

默认 `requestId` 格式为：

```text
{snowflakeId}-{pid}
```

因此在：

- 统一响应
- request attribute
- 日志上下文

中都可以继续复用这条链路。

## 10. 一个完整示例

```java
import com.clmcat.framework.webmvc.ResponseStatus;
import com.clmcat.framework.webmvc.anns.ApiController;
import com.clmcat.framework.webmvc.anns.GetLocale;
import com.clmcat.framework.webmvc.anns.LoginVerify;
import com.clmcat.framework.webmvc.anns.Params;
import com.clmcat.framework.webmvc.anns.Token;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@ApiController
@LoginVerify
public class UserController {

    @PostMapping("/user/login")
    public Object login(
            @Params PhoneLoginDto dto,
            @GetLocale Locale locale,
            @Token Long userId,
            @Params(name = "requestId", scope = Params.ParamsScope.REQUEST, required = false) String requestId) {

        Map<String, Object> result = new HashMap<>();
        result.put("phone", dto.getPhone());
        result.put("locale", locale.toLanguageTag());
        result.put("userId", userId);
        result.put("requestId", requestId);
        return ResponseStatus.OK.create().setContent(result).build();
    }
}
```

## 11. 当前建议

- 控制器优先使用 `@ApiController`
- 常规参数优先使用 `@Params`
- request 上下文对象使用 `@Params(scope = REQUEST)`
- 当前语言统一使用 `@GetLocale`
- 登录相关统一使用 `@LoginVerify` / `@Token`
- 业务状态优先使用 `ResponseStatus`

