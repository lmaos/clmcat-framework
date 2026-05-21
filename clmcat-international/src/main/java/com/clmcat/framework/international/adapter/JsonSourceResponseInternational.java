package com.clmcat.framework.international.adapter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import com.clmcat.basics.commons.https.HttpUtils;
import com.clmcat.basics.commons.https.streams.HttpStreamException;
import com.clmcat.framework.international.localemap.LocalLocalMessageMap;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
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
    protected boolean doLoad(LocalLocalMessageMap localeMessageMapTmp) {
        if (jsonSourceSupplier != null) {
            String json = jsonSourceSupplier.get();
            if (json == null || !json.startsWith("{") || !json.endsWith("}")) {
                log.error("JSON格式错误, {}", json);
                return false;
            }
            parseJson(json, localeMessageMapTmp);
        }
        if (properties != null) {
            List<String> configs = properties.getConfigs();
            if (configs != null && !configs.isEmpty()) {
                for (String config : configs) {
                    if (config.startsWith("http:") || config.startsWith("https:")) {
                        parseHttpUrlJson(config, localeMessageMapTmp);
                    } else if (config.startsWith("file:")) {
                        parseFileJson(config, localeMessageMapTmp);
                    }
                    parseJson(config, localeMessageMapTmp);
                }
            }
        }
        return true;
    }

    private void parseHttpUrlJson(String httpUrl, LocalLocalMessageMap localeMessageMapTmp) {
        // http://xxsadasd
        try {
            String json = HttpUtils.stream(httpUrl).get().request().getString("utf-8");
            parseJson(json, localeMessageMapTmp);
        } catch (HttpStreamException e) {
            log.info("无法访问：{}", e.getRequestStreamResult().getUrl());
        }
    }

    private void parseFileJson(String filePath, LocalLocalMessageMap localeMessageMapTmp) {
        filePath = filePath.substring("file:".length()).trim();
        /// classpath:
        try {
            if (filePath.startsWith("classpath:")) {
                String classpath = filePath.substring("classpath:".length());
                Enumeration<URL> resources = ExcelXlsxResponseInternational.class.getClassLoader().getResources(classpath);
                while (resources.hasMoreElements()) {
                    URL url = resources.nextElement();
                    try (InputStream inputStream = url.openStream();) {
                        String json = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
                        parseJson(json, localeMessageMapTmp);
                    } catch (Exception e) {
                        log.error("无法解析：{}: {}", filePath, url, e);
                    }
                }

            } else {
                String json = FileUtils.readFileToString(new File(filePath), StandardCharsets.UTF_8);
                parseJson(json, localeMessageMapTmp);
            }
        } catch (IOException e) {
            log.error("无法解析：file:{}", filePath, e);
        }
    }

    private void parseJson(String json, LocalLocalMessageMap localeMessageMapTmp) {
        // {"key": {"zh,zh-CN":""}}
        JSONObject jsonObject = JSON.parseObject(json);
        jsonObject.forEach((key, localeObject) -> {
            if (localeObject instanceof JSONObject valueObject) {
                valueObject.forEach((localeKey, message) -> {
                    /// {"zh,zh-CN":""}, key = zh,zh-CN
                    String[] split = localeKey.split(",");
                    for (String localeStr : split) {
                        localeStr = localeStr.trim();
                        String value = message.toString();
                        if (StringUtils.isNotBlank(localeStr) && StringUtils.isNotBlank(value)) {
                            Locale locale = Locale.forLanguageTag(localeStr);
                            localeMessageMapTmp.put(key, locale, value);
                        }
                    }
                });
            }
        });
    }
    /// 默认JSON源实现
    private String defaultJsonSource() {
        try {
            JSONObject defaultJsonObject = new JSONObject();
            Enumeration<URL> resources = ExcelXlsxResponseInternational.class.getClassLoader().getResources(DEFAULT_FILE_NAME);
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
