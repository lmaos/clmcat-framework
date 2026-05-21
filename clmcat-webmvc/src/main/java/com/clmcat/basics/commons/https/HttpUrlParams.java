package com.clmcat.basics.commons.https;

import java.util.ArrayList;
import java.util.List;

public class HttpUrlParams {

    private List<HttpUrlParam> urlParams = new ArrayList<>(6);

    public HttpUrlParams add(String name, Object value) {
        urlParams.add(new HttpUrlParam(name, value));
        return this;
    }
    public HttpUrlParams add(String param) {
        int index = param.indexOf("=");
        if (index != -1) {
            String name = param.substring(0, index);
            String value = param.substring(index + 1);
            urlParams.add(new HttpUrlParam(name, value));
        }

        return this;
    }
    public List<HttpUrlParam> getUrlParams() {
        return urlParams;
    }

    public String formatUrl(String url) {
        return HttpUtils.formatUrl(url, getUrlParams());
    }
}
