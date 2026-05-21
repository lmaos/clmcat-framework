package com.clmcat.framework.international.demo;

import com.clmcat.framework.international.adapter.ExcelXlsxResponseInternational;
import com.clmcat.framework.webmvc.ResponseInternationalization;

import java.util.Locale;

public class ExcelXlsxResponseInternationalDemo {
    public static void main(String[] args) throws Exception {
        ExcelXlsxResponseInternational excelXlsxResponseInternational = new ExcelXlsxResponseInternational();
        excelXlsxResponseInternational.afterPropertiesSet();
        ResponseInternationalization.Message message = new ResponseInternationalization.Message();
        message.setLocaleMessage("hello");
        message.setLocale(Locale.forLanguageTag("en"));
        String msg = excelXlsxResponseInternational.convertResponseMessage(message);
        System.out.println(msg);
    }
}
