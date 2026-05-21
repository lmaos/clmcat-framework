package com.clmcat.basics.commons.localex;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class LocalFileParser implements LocaleMapParser {

	private LocaleMap localeMap;
	private String paths;
	private LocaleFileLoader localeFileLoader;
	private String localeSymbols = "-";
	public LocalFileParser(String path, LocaleFileLoader localeFileLoader) {
		this.paths = path;
		this.localeFileLoader = localeFileLoader;
	}


	private void reset(LocaleService localeService) {
		boolean ignoreCase = localeService.isIgnoreCase();
		LocaleMap localeMap = new LocaleMap(ignoreCase);
		localeMap.setSymbols(localeSymbols);
		
		if (paths != null && localeFileLoader != null) {
			String patharr[] = paths.split(",");
			for (String path : patharr) {
				try {
					URL url = getURL(path.trim());
					if (url != null) {
						try (InputStream in = url.openStream()) {
							LocaleMap tmp = localeFileLoader.parse(in, path, ignoreCase);
							tmp.setSymbols(localeSymbols);
							localeMap.putAll(tmp);
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		this.localeMap = localeMap;
	}

	public void setLocaleSymbols(String localeSymbols) {
		this.localeSymbols = localeSymbols;
		localeMap.setSymbols(localeSymbols);
	}
	
	public String getLocaleSymbols() {
		return localeSymbols;
	}

	private URL getURL(String path) {
		if (path == null || path.isEmpty()) {
			return null;
		}
		try {
			if (path.startsWith("classpath:")) {
				String file = path.substring("classpath:".length());
				return getClass().getClassLoader().getResource(file);
			} else {
				return new File(path).toURI().toURL();
			}
		} catch (Exception e) {
			return null;
		}
	}

	public LocaleMap parserLocaleMap(LocaleService localeService) {
		reset(localeService);
		return localeMap;
	}
}
