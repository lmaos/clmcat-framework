package com.clmcat.basics.commons.localex;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import com.clmcat.basics.commons.lang.StringUtils;

public class TsvLocaleFileLoader implements LocaleFileLoader {

	private byte[] read(InputStream in) throws IOException {
		ByteArrayOutputStream bout = new ByteArrayOutputStream(10240);
		byte[] data = new byte[1024];
		int len = 0;
		while ((len = in.read(data)) > 0) {
			bout.write(data, 0, len);
		}
		return bout.toByteArray();
	}
	
	@Override
	public LocaleMap parse(InputStream in, String filePath, boolean ignoreCase) throws IOException {
		byte[] buf = read(in);
		
		String text = new String(buf, Charset.forName("UTF-8"));
		String lines[] = text.split("\n");
		LocaleMap localeMap = new LocaleMap();
		if (lines.length > 1) {
			String titles[] = lines[0].trim().split("\t");

			for (int i = 1; i < lines.length; i++) {
				String colls[] = lines[i].trim().split("\t");
				String key = colls[0];
				for (int j = 1; j < colls.length && j < titles.length; j++) {
					String localeName = titles[j].trim();
					String message = colls[j];
					if (StringUtils.equalsAny(message, "", "未翻译")) {
						
					} else {
						message = message.replace("\\n", "\n");
						String[] localeNameItems = localeName.split(",");
						for (String localeNameItem : localeNameItems) {
							localeMap.put(localeNameItem, key, message);
						}
					}
				}
			}
		}

		return localeMap;
	}

}
