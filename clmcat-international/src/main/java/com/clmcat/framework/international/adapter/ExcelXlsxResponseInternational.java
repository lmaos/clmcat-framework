package com.clmcat.framework.international.adapter;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.clmcat.basics.commons.lang.StringUtils;
import com.clmcat.framework.international.localemap.LocalLocalMessageMap;

import java.io.InputStream;
import java.net.URL;
import java.util.*;

@Slf4j
public class ExcelXlsxResponseInternational extends AbstractResponseInternationalization   {

    public final String DEFAULT_FILE_NAME = "message.xlsx";

    public ExcelXlsxResponseInternational() {
    }


    @Override
    public boolean doLoad(LocalLocalMessageMap localeMessageMapTmp) {
        parseExcel(DEFAULT_FILE_NAME, localeMessageMapTmp);
        /// 其他的配置文件
        if(properties != null && properties.getConfigs() != null) {
            for (String config : properties.getConfigs()) {
                parseExcel(config, localeMessageMapTmp);
            }
        }
        return true;
    }


    private void parseExcel(String fileName,  LocalLocalMessageMap localeMessageMapTmp) {
        if(!fileName.endsWith(".xlsx")) {
            log.error("无法解析, 文件后缀不正确！： {}" , fileName);
            return;
        }

        try {
            Enumeration<URL> resources = ExcelXlsxResponseInternational.class.getClassLoader().getResources(fileName);
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();

                try (InputStream inputStream = url.openStream();) {
                    Workbook workbook = new XSSFWorkbook(inputStream);
                    Sheet sheet = workbook.getSheetAt(0);
                    parseSheet(sheet, localeMessageMapTmp);
                } catch (Exception e) {
                    log.error("无法解析： {}" , url, e);
                }

            }
        } catch (Exception e) {
            log.error("无法解析： {}" , fileName, e);
        }
    }

    private void parseSheet(Sheet sheet, LocalLocalMessageMap localeMessageMapTmp) {
        Row row = sheet.getRow(0);
        List<Set<Locale>> locales = new ArrayList<>();
        for (int i = 1; i < row.getLastCellNum(); i++) {
            /// en,en-US
            String localeStr = row.getCell(i).getStringCellValue().trim();
            Set<Locale> localeSet = new HashSet<>();
            locales.add(localeSet);
            if (StringUtils.isNotBlank(localeStr)) {
                String[] splits = localeStr.split(",");
                for (String split : splits) {
                    split = split.trim();
                    if (StringUtils.isNotBlank(split)) {
                        Locale locale = Locale.forLanguageTag(split);
                        localeSet.add(locale);
                    }
                }
            }
        }

        /// 第二行开始解析
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            row = sheet.getRow(i);
            Cell keyCell = row.getCell(0);
            String key = keyCell.getStringCellValue().trim();
            if (StringUtils.isNotBlank(key)) {
                for (int j = 1; j < row.getLastCellNum(); j++) {
                    Set<Locale> localeSet = locales.get(j - 1);
                    Cell valueCell = row.getCell(j);
                    String value = valueCell.getStringCellValue();
                    if (StringUtils.isNotBlank(value)) {
                        for (Locale locale : localeSet) {
                            localeMessageMapTmp.put(key, locale, value);
                        }
                    }
                }
            }
        }
    }
}
