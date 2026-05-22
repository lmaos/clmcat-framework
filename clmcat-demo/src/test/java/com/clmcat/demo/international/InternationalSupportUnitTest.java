package com.clmcat.demo.international;

import com.clmcat.framework.international.adapter.ExcelXlsxResponseInternational;
import com.clmcat.framework.international.adapter.JsonSourceResponseInternational;
import com.clmcat.framework.international.config.InternationalConfiguration;
import com.clmcat.framework.international.config.InternationalProperties;
import com.clmcat.framework.international.localemap.LocalLocalMessageMap;
import com.clmcat.framework.webmvc.ResponseInternationalization;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class InternationalSupportUnitTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldFallbackFromCountryToLanguageThenDefaultLocale() {
        LocalLocalMessageMap map = new LocalLocalMessageMap();
        map.setDefaultLocale(Locale.ENGLISH);
        map.put("close", Locale.CHINESE, "关闭");
        map.put("close", Locale.ENGLISH, "close");

        assertEquals("关闭", map.get("close", Locale.forLanguageTag("zh-CN")));
        assertEquals("关闭", map.get("close", Locale.forLanguageTag("zh-HK")));
        assertEquals("close", map.get("close", Locale.JAPANESE));
    }

    @Test
    void shouldParseJsonAliasesAndFormatMessageArguments() {
        JsonSourceResponseInternational international = new JsonSourceResponseInternational(() ->
                "{\"close\":{\"zh,zh-CN\":\"关闭\",\"zh-TW\":\"關閉\",\"en\":\"close\"},"
                        + "\"welcome\":{\"zh\":\"欢迎 {0}\",\"en\":\"welcome {0}\"}}");
        international.setProperties(properties("json.source", "en", null));
        international.reload();

        assertEquals("关闭", international.convertResponseMessage(message("close", Locale.forLanguageTag("zh-CN"))));
        assertEquals("关闭", international.convertResponseMessage(message("close", Locale.forLanguageTag("zh-HK"))));
        assertEquals("welcome Tom",
                international.convertResponseMessage(message("welcome", Locale.ENGLISH, "Tom")));
    }

    @Test
    void shouldLoadJsonFromConfigWhenSupplierResultIsInvalid() {
        JsonSourceResponseInternational international = new JsonSourceResponseInternational(() -> "invalid-json");
        international.setProperties(properties("json.source", "en",
                List.of("{\"close\":{\"zh\":\"关闭\",\"en\":\"close\"}}")));
        international.reload();

        assertEquals("关闭", international.convertResponseMessage(message("close", Locale.CHINESE)));
        assertEquals("close", international.convertResponseMessage(message("close", Locale.JAPANESE)));
    }

    @Test
    void shouldLoadExcelTableFromFileAndAllSheets() throws IOException {
        Path workbookPath = tempDir.resolve("message.xlsx");
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             OutputStream outputStream = Files.newOutputStream(workbookPath)) {
            var sheet1 = workbook.createSheet("sheet1");
            sheet1.createRow(0).createCell(0).setCellValue("--");
            sheet1.getRow(0).createCell(1).setCellValue("zh,zh-CN");
            sheet1.getRow(0).createCell(2).setCellValue("zh-TW");
            sheet1.getRow(0).createCell(3).setCellValue("en");
            sheet1.createRow(1).createCell(0).setCellValue("close");
            sheet1.getRow(1).createCell(1).setCellValue("关闭");
            sheet1.getRow(1).createCell(2).setCellValue("關閉");
            sheet1.getRow(1).createCell(3).setCellValue("close");

            var sheet2 = workbook.createSheet("sheet2");
            sheet2.createRow(0).createCell(0).setCellValue("--");
            sheet2.getRow(0).createCell(1).setCellValue("zh");
            sheet2.getRow(0).createCell(2).setCellValue("en");
            sheet2.createRow(1).createCell(0).setCellValue("welcome");
            sheet2.getRow(1).createCell(1).setCellValue("欢迎 {0}");
            sheet2.getRow(1).createCell(2).setCellValue("welcome {0}");

            workbook.write(outputStream);
        }

        ExcelXlsxResponseInternational international = new ExcelXlsxResponseInternational();
        international.setProperties(properties("excel.xlsx", "en", List.of("file:" + workbookPath)));
        international.reload();

        assertEquals("关闭", international.convertResponseMessage(message("close", Locale.forLanguageTag("zh-HK"))));
        assertEquals("close", international.convertResponseMessage(message("close", Locale.JAPANESE)));
        assertEquals("welcome Tom",
                international.convertResponseMessage(message("welcome", Locale.ENGLISH, "Tom")));
    }

    @Test
    void shouldCreateJsonInternationalByConfigurationMode() {
        InternationalConfiguration configuration = new InternationalConfiguration();

        assertInstanceOf(JsonSourceResponseInternational.class,
                configuration.responseInternationalization(properties("json.source", "en", null)));
    }

    @Test
    void shouldFallbackToExcelInternationalForUnknownMode() {
        InternationalConfiguration configuration = new InternationalConfiguration();

        assertInstanceOf(ExcelXlsxResponseInternational.class,
                configuration.responseInternationalization(properties("unknown", "en", null)));
    }

    private InternationalProperties properties(String mode, String defaultLocale, List<String> configs) {
        InternationalProperties properties = new InternationalProperties();
        properties.setMode(mode);
        properties.setDefaultLocale(defaultLocale);
        properties.setConfigs(configs);
        return properties;
    }

    private ResponseInternationalization.Message message(String key, Locale locale, Object... args) {
        ResponseInternationalization.Message message = new ResponseInternationalization.Message();
        message.setLocaleMessage(key);
        message.setLocale(locale);
        message.setMessageArgs(args);
        return message;
    }
}
