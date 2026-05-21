package com.clmcat.basics.commons.localex;

import java.io.IOException;
import java.io.InputStream;

public interface LocaleFileLoader {

	LocaleMap parse(InputStream in, String filePath, boolean ignoreCase) throws IOException;
}
