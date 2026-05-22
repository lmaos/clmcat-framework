package com.clmcat.framework.international.adapter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import com.clmcat.framework.international.localemap.LocaleMessageMap;
import com.clmcat.framework.international.localemap.LocaleMessageUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import com.clmcat.basics.commons.https.HttpUtils;
import com.clmcat.basics.commons.https.streams.HttpStreamException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author zhangxingyu
 *
 * JSON 源的国际化。 configs 的配置可以是：["file:/xxx", "file:classpath:cccc", "http:/xczz.ccc/qwe"]
 *
 *
 * 默认：在 classpath 下读取， "message.json"
 */
@Slf4j
public class JsonSourceResponseInternational extends AbstractResponseInternationalization {
    // private static final long serialVersionUID = 1L;
    /// 指定JSON获得来源进行获取JSON数据。
    private Supplier<String> jsonSourceSupplier;

    public final String DEFAULT_FILE_NAME = "message.json";

    /**
     * 获得JSON的来源。
     * @param jsonSourceSupplier
     */
    public JsonSourceResponseInternational(Supplier<String> jsonSourceSupplier) {
        this.jsonSourceSupplier = jsonSourceSupplier;
    }

    public JsonSourceResponseInternational() {
        this.jsonSourceSupplier = this::defaultJsonSource;
    }



    @Override
    protected boolean doLoad(LocaleMessageMap localeMessageMapTmp) {
        boolean loaded = false;
        if (jsonSourceSupplier != null) {
            String json = jsonSourceSupplier.get();
            loaded |= parseJson(json, localeMessageMapTmp);
        }
        if (properties != null) {
            List<String> configs = properties.getConfigs();
            if (configs != null && !configs.isEmpty()) {
                for (String config : configs) {
                    loaded |= loadConfig(config, localeMessageMapTmp);
                }
            }
        }
        return loaded;
    }

    private boolean loadConfig(String config, LocaleMessageMap localeMessageMapTmp) {
        String source = StringUtils.trimToEmpty(config);
        if (StringUtils.isBlank(source)) {
            return false;
        }
        if (source.startsWith("http:") || source.startsWith("https:")) {
            return parseHttpUrlJson(source, localeMessageMapTmp);
        }
        if (source.startsWith("file:")) {
            return parseFileJson(source, localeMessageMapTmp);
        }
        return parseJson(source, localeMessageMapTmp);
    }

    private boolean parseHttpUrlJson(String httpUrl, LocaleMessageMap localeMessageMapTmp) {
        // http://xxsadasd
        try {
            String json = HttpUtils.stream(httpUrl).get().request().getString("utf-8");
            return parseJson(json, localeMessageMapTmp);
        } catch (HttpStreamException e) {
            log.info("无法访问：{}", e.getRequestStreamResult().getUrl());
        }
        return false;
    }

    private boolean parseFileJson(String filePath, LocaleMessageMap localeMessageMapTmp) {
        boolean loaded = false;
        filePath = filePath.substring("file:".length()).trim();
        /// classpath:
        try {
            if (filePath.startsWith("classpath:")) {
                String classpath = filePath.substring("classpath:".length());
                Enumeration<URL> resources = getClass().getClassLoader().getResources(classpath);
                while (resources.hasMoreElements()) {
                    URL url = resources.nextElement();
                    try (InputStream inputStream = url.openStream();) {
                        String json = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
                        loaded |= parseJson(json, localeMessageMapTmp);
                    } catch (Exception e) {
                        log.error("无法解析：{}: {}", filePath, url, e);
                    }
                }

            } else {
                String json = FileUtils.readFileToString(new File(filePath), StandardCharsets.UTF_8);
                loaded |= parseJson(json, localeMessageMapTmp);
            }
        } catch (IOException e) {
            log.error("无法解析：file:{}", filePath, e);
        }
        return loaded;
    }

    private boolean parseJson(String json, LocaleMessageMap localeMessageMapTmp) {
        String jsonText = StringUtils.trimToEmpty(json);
        if (!jsonText.startsWith("{") || !jsonText.endsWith("}")) {
            if (StringUtils.isNotBlank(jsonText)) {
                log.error("JSON格式错误, {}", jsonText);
            }
            return false;
        }
        // {"key": {"zh,zh-CN":""}}
        try {
            JSONObject jsonObject = JSON.parseObject(jsonText);
            if (jsonObject == null || jsonObject.isEmpty()) {
                return false;
            }
            jsonObject.forEach((key, localeObject) -> {
                if (localeObject instanceof JSONObject valueObject) {
                    valueObject.forEach((localeKey, message) -> {
                        String value = message == null ? null : message.toString();
                        if (StringUtils.isBlank(value)) {
                            return;
                        }
                        for (var locale : LocaleMessageUtils.parseLocaleExpression(localeKey)) {
                            localeMessageMapTmp.put(key, locale, value);
                        }
                    });
                }
            });
            return true;
        } catch (Exception e) {
            log.error("无法解析JSON", e);
            return false;
        }
    }
    /// 默认JSON源实现
    private String defaultJsonSource() {
        try {
            JSONObject defaultJsonObject = new JSONObject();
            Enumeration<URL> resources = getClass().getClassLoader().getResources(DEFAULT_FILE_NAME);
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                try (InputStream inputStream = url.openStream();) {
                    String json = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
                    JSONObject jsonObject = JSON.parseObject(json);
                    jsonObject.forEach((key, value) -> {
                        if (value instanceof JSONObject valueObject) {
                            JSONObject subJsonObject = (JSONObject) defaultJsonObject.computeIfAbsent(key, k -> new JSONObject());
                            valueObject.forEach(subJsonObject::put);
                        }
                    });
                } catch (Exception e) {
                    log.error("无法解析：{}: {}", DEFAULT_FILE_NAME, url, e);
                }
            }
            return defaultJsonObject.toJSONString();
        } catch (Exception e) {
            log.error("无法解析： {}", DEFAULT_FILE_NAME, e);
        }
        return null;
    }
}
