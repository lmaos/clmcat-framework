package com.clmcat.basics.commons.localex;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import com.clmcat.basics.commons.lang.StringUtils;

public class LocaleMap {

	private Map<String, LocaleMessage> locales = new HashMap<>();
	private boolean ignoreCase = true;
	private String symbols = "-";
	public LocaleMap() {

	}

	public LocaleMap(boolean ignoreCase) {
		super();
		this.ignoreCase = ignoreCase;
	}
	
	public void setSymbols(String symbols) {
		this.symbols = symbols;
	}

	public void putAll(LocaleMap localeMap) {
		for(Entry<String, LocaleMessage> entry : localeMap.locales.entrySet()) {
			String locale = entry.getKey();
			LocaleMessage localeMessage = entry.getValue();
			
			if (localeMessage != null && localeMessage.msg != null)
			{
				localeMessage.msg.forEach((key, value) -> {
					put(locale, key, value);
				});
			}
		}
	}
	
	public void put(Locale locale, String key, String message) {
		if (StringUtils.isNotBlank(locale.getCountry())) {
			put(locale.getLanguage()+symbols+locale.getCountry(), key, message);
		} else {
			put(locale.getLanguage(), key, message);
		}
	}

	public void put(String locale, String key, String message) {
		LocaleMessage localeMessage = locales.get(locale);
		if (localeMessage == null) {
			localeMessage = new LocaleMessage();
			locales.putIfAbsent(locale, localeMessage);
			localeMessage = locales.get(locale);
		}
		if (ignoreCase) {
			localeMessage.msg.put(key.toUpperCase(), message);
		} else {
			localeMessage.msg.put(key, message);
		}
	}

	public String get(Locale locale, String key) {
		LocaleMessage localeMessage = null;
		if (StringUtils.isNotBlank(locale.getCountry())) {
			localeMessage = locales.get(locale.getLanguage() + symbols + locale.getCountry());
		}
		if (localeMessage == null) {
			localeMessage = locales.get(locale.getLanguage());
		}
		if (localeMessage == null) {
			return null;
		}
		if (ignoreCase) {
			return localeMessage.msg.get(key.toUpperCase());
		} else {
			return localeMessage.msg.get(key);
		}
	}

	private static class LocaleMessage {
		private Map<String, String> msg = new HashMap<>();
		void putAll(LocaleMessage localeMessage) {
			msg.putAll(localeMessage.msg);
		}

	}
}
