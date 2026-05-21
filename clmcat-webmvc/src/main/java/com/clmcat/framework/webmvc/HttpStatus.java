package com.clmcat.framework.webmvc;

import java.util.HashMap;
import java.util.Map;

public class HttpStatus {

    public final static Map<Integer, String> statusError = new HashMap<>();
    public final static Map<Integer, String> statusMap = new HashMap<>();

    static {

        // 1 消息
        statusError.put(100 ,"Continue");
        statusError.put(101 ,"Switching Protocols");
        statusError.put(102 ,"Processing");

        statusError.put(200 ,"OK");
        statusError.put(201 ,"Created");
        statusError.put(202 ,"Accepted");
        statusError.put(203 ,"Non - Authoritative Information");
        statusError.put(204 ,"No Content");
        statusError.put(205 ,"Reset Content");
        statusError.put(206 ,"Partial Content");
        statusError.put(207 ,"Multi - Status");
                // 3 重定向
        statusError.put(300 ,"Multiple Choices");
        statusError.put(301 ,"Moved Permanently");
        statusError.put(302 ,"Move Temporarily");
        statusError.put(303 ,"See Other");
        statusError.put(304 ,"Not Modified");
        statusError.put(305 ,"Use Proxy");
        statusError.put(306 ,"Switch Proxy");
        statusError.put(307 ,"Temporary Redirect");
                // 4 请求错误
        statusError.put(400 ,"Bad Request");
        statusError.put(401 ,"Unauthorized");
        statusError.put(402 ,"Payment Required");
        statusError.put(403 ,"Forbidden");
        statusError.put(404 ,"Not Found");
        statusError.put(405 ,"Method Not Allowed");
        statusError.put(406 ,"Not Acceptable");
        statusError.put(407 ,"Proxy Authentication Required");
        statusError.put(408 ,"Request Timeout");
        statusError.put(409 ,"Conflict");
        statusError.put(410 ,"Gone");
        statusError.put(411 ,"Length Required");
        statusError.put(412 ,"Precondition Failed");
        statusError.put(413 ,"Request Entity Too Large");
        statusError.put(414 ,"Request - URI Too Long");
        statusError.put(415 ,"Unsupported Media Type");
        statusError.put(416 ,"Requested Range Not Satisfiable");
        statusError.put(417 ,"Expectation Failed");
        statusError.put(418 ,"I 'm a teapot");
        statusError.put(421 ,"Misdirected Request");
        statusError.put(422 ,"Unprocessable Entity");
        statusError.put(423 ,"Locked");
        statusError.put(424 ,"Failed Dependency");
        statusError.put(425 ,"Too Early");
        statusError.put(426 ,"Upgrade Required");
        statusError.put(449 ,"Retry With");
        statusError.put(451 ,"Unavailable For Legal Reasons");
                // 5 服务器错误
        statusError.put(500 ,"Internal Server Error");
        statusError.put(501 ,"Not Implemented");
        statusError.put(502 ,"Bad Gateway");
        statusError.put(503 ,"Service Unavailable");
        statusError.put(504 ,"Gateway Timeout");
        statusError.put(505 ,"HTTP Version Not Supported");
        statusError.put(506 ,"Variant Also Negotiates");
        statusError.put(507 ,"Insufficient Storage");
        statusError.put(509 ,"Bandwidth Limit Exceeded");
        statusError.put(510 ,"Not Extended");
        statusError.put(600 ,"Unparseable Response Headers");

        // 1 消息
        statusMap.put(100 ,"CONTINUE");
        statusMap.put(101 ,"SWITCHING_PROTOCOLS");
        statusMap.put(102 ,"PROCESSING");
        // 2 成功
        statusMap.put(200 ,"OK");
        statusMap.put(201 ,"CREATED");
        statusMap.put(202 ,"ACCEPTED");
        statusMap.put(203 ,"NON-AUTHORITATIVE_INFORMATION");
        statusMap.put(204 ,"NO_CONTENT");
        statusMap.put(205 ,"RESET_CONTENT");
        statusMap.put(206 ,"PARTIAL_CONTENT");
        statusMap.put(207 ,"MULTI-STATUS");
        // 3 重定向
        statusMap.put(300 ,"MULTIPLE_CHOICES");
        statusMap.put(301 ,"MOVED_PERMANENTLY");
        statusMap.put(302 ,"MOVE_TEMPORARILY");
        statusMap.put(303 ,"SEE_OTHER");
        statusMap.put(304 ,"NOT_MODIFIED");
        statusMap.put(305 ,"USE_PROXY");
        statusMap.put(306 ,"SWITCH_PROXY");
        statusMap.put(307 ,"TEMPORARY_REDIRECT");
        // 4 请求错误
        statusMap.put(400 ,"BAD_REQUEST");
        statusMap.put(401 ,"UNAUTHORIZED");
        statusMap.put(402 ,"PAYMENT_REQUIRED");
        statusMap.put(403 ,"FORBIDDEN");
        statusMap.put(404 ,"NOT_FOUND");
        statusMap.put(405 ,"METHOD_NOT_ALLOWED");
        statusMap.put(406 ,"NOT_ACCEPTABLE");
        statusMap.put(407 ,"PROXY_AUTHENTICATION_REQUIRED");
        statusMap.put(408 ,"REQUEST_TIMEOUT");
        statusMap.put(409 ,"CONFLICT");
        statusMap.put(410 ,"GONE");
        statusMap.put(411 ,"LENGTH_REQUIRED");
        statusMap.put(412 ,"PRECONDITION_FAILED");
        statusMap.put(413 ,"REQUEST_ENTITY_TOO_LARGE");
        statusMap.put(414 ,"REQUEST-URI_TOO_LONG");
        statusMap.put(415 ,"UNSUPPORTED_MEDIA_TYPE");
        statusMap.put(416 ,"REQUESTED_RANGE_NOT_SATISFIABLE");
        statusMap.put(417 ,"EXPECTATION_FAILED");
        statusMap.put(418 ,"I_'M_A_TEAPOT");
        statusMap.put(421 ,"MISDIRECTED_REQUEST");
        statusMap.put(422 ,"UNPROCESSABLE_ENTITY");
        statusMap.put(423 ,"LOCKED");
        statusMap.put(424 ,"FAILED_DEPENDENCY");
        statusMap.put(425 ,"TOO_EARLY");
        statusMap.put(426 ,"UPGRADE_REQUIRED");
        statusMap.put(449 ,"RETRY_WITH");
        statusMap.put(451 ,"UNAVAILABLE_FOR_LEGAL_REASONS");
        // 5 服务器错误
        statusMap.put(500 ,"INTERNAL_SERVER_ERROR");
        statusMap.put(501 ,"NOT_IMPLEMENTED");
        statusMap.put(502 ,"BAD_GATEWAY");
        statusMap.put(503 ,"SERVICE_UNAVAILABLE");
        statusMap.put(504 ,"GATEWAY_TIMEOUT");
        statusMap.put(505 ,"HTTP_VERSION_NOT_SUPPORTED");
        statusMap.put(506 ,"VARIANT_ALSO_NEGOTIATES");
        statusMap.put(507 ,"INSUFFICIENT_STORAGE");
        statusMap.put(509 ,"BANDWIDTH_LIMIT_EXCEEDED");
        statusMap.put(510 ,"NOT_EXTENDED");
        statusMap.put(600 ,"UNPARSEABLE_RESPONSE_HEADERS");

    }


    public static String getState(Integer status) {
        return statusMap.getOrDefault(status, "SYSTEM_ERROR");
    }

//    public static void main(String[] args){
//        statusError.forEach((k, v)-> {
//            System.out.println(String.format("status.put(%d ,\"%s\");", k,
//                    v.replace(" - ", "-")
//                            .replace(" ", "_").toUpperCase()));
//        });
//    }
}
