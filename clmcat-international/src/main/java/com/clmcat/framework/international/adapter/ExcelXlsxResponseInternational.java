package com.clmcat.framework.international.adapter;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.commons.lang3.StringUtils;
import com.clmcat.framework.international.localemap.LocaleMessageMap;
import com.clmcat.framework.international.localemap.LocaleMessageUtils;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

@Slf4j
public class ExcelXlsxResponseInternational extends AbstractResponseInternationalization   {

    public final String DEFAULT_FILE_NAME = "message.xlsx";
    private final DataFormatter dataFormatter = new DataFormatter();

    public ExcelXlsxResponseInternational() {
    }


    @Override
    public boolean doLoad(LocaleMessageMap localeMessageMapTmp) {
        boolean loaded = parseExcel(DEFAULT_FILE_NAME, localeMessageMapTmp);
        /// 其他的配置文件
        if(properties != null && properties.getConfigs() != null) {
            for (String config : properties.getConfigs()) {
                loaded |= parseExcel(config, localeMessageMapTmp);
            }
        }
        return loaded;
    }


    private boolean parseExcel(String fileName,  LocaleMessageMap localeMessageMapTmp) {
        String location = StringUtils.trimToEmpty(fileName);
        if (StringUtils.isBlank(location)) {
            return false;
        }
        if (location.startsWith("file:")) {
            return parseFileExcel(location, localeMessageMapTmp);
        }
        return parseClasspathExcel(location, localeMessageMapTmp);
    }

    private boolean parseClasspathExcel(String fileName, LocaleMessageMap localeMessageMapTmp) {
        if(!fileName.endsWith(".xlsx")) {
            log.error("无法解析, 文件后缀不正确！： {}" , fileName);
            return false;
        }

        boolean loaded = false;
        try {
            Enumeration<URL> resources = getClass().getClassLoader().getResources(fileName);
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();

                try (InputStream inputStream = url.openStream();
                     Workbook workbook = WorkbookFactory.create(inputStream)) {
                    loaded |= parseWorkbook(workbook, localeMessageMapTmp);
                } catch (Exception e) {
                    log.error("无法解析： {}" , url, e);
                }

            }
        } catch (Exception e) {
            log.error("无法解析： {}" , fileName, e);
        }
        return loaded;
    }

    private boolean parseFileExcel(String fileName, LocaleMessageMap localeMessageMapTmp) {
        String filePath = fileName.substring("file:".length()).trim();
        if (filePath.startsWith("classpath:")) {
            return parseClasspathExcel(filePath.substring("classpath:".length()), localeMessageMapTmp);
        }
        if (!filePath.endsWith(".xlsx")) {
            log.error("无法解析, 文件后缀不正确！： {}" , fileName);
            return false;
        }
        try (InputStream inputStream = new FileInputStream(filePath);
             Workbook workbook = WorkbookFactory.create(inputStream)) {
            return parseWorkbook(workbook, localeMessageMapTmp);
        } catch (Exception e) {
            log.error("无法解析： {}" , fileName, e);
            return false;
        }
    }

    private boolean parseWorkbook(Workbook workbook, LocaleMessageMap localeMessageMapTmp) {
        boolean loaded = false;
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            Sheet sheet = workbook.getSheetAt(i);
            loaded |= parseSheet(sheet, localeMessageMapTmp);
        }
        return loaded;
    }

    private boolean parseSheet(Sheet sheet, LocaleMessageMap localeMessageMapTmp) {
        if (sheet == null) {
            return false;
        }
        Row row = sheet.getRow(0);
        if (row == null) {
            return false;
        }
        List<List<Locale>> locales = new ArrayList<>();
        for (int i = 1; i < row.getLastCellNum(); i++) {
            String localeStr = getCellValue(row.getCell(i));
            locales.add(LocaleMessageUtils.parseLocaleExpression(localeStr));
        }

        boolean loaded = false;
        /// 第二行开始解析
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            row = sheet.getRow(i);
            if (row == null) {
                continue;
            }
            String key = getCellValue(row.getCell(0));
            if (StringUtils.isNotBlank(key)) {
                for (int j = 1; j <= locales.size(); j++) {
                    List<Locale> localeSet = locales.get(j - 1);
                    if (localeSet.isEmpty()) {
                        continue;
                    }
                    String value = getCellValue(row.getCell(j));
                    if (StringUtils.isNotBlank(value)) {
                        for (Locale locale : localeSet) {
                            localeMessageMapTmp.put(key, locale, value);
                            loaded = true;
                        }
                    }
                }
            }
        }
        return loaded;
    }

    private String getCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        return StringUtils.trimToNull(dataFormatter.formatCellValue(cell));
    }
}
