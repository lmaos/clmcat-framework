package com.clmcat.framework.webmvc.interceptor.reqparam;

import java.nio.charset.Charset;
import java.util.Base64;

import com.clmcat.basics.commons.util.CodecUtils;
import com.clmcat.framework.webmvc.ResponseStatus;

public class ParamAuth {
	private String password;
	private String decode;
	private Charset charset = Charset.forName("UTF-8");

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getDecode() {
		return decode;
	}

	public void setDecode(String decode) {
		this.decode = decode;
	}

	public Charset getCharset() {
		return charset;
	}

	public void setCharset(Charset charset) {
		this.charset = charset;
	}

	public byte[] decodeBody(String body, Charset charset) {
		byte[] data = null;
		if (decode == null || "base64".equals(decode)) {
			data = Base64.getDecoder().decode(body.getBytes(charset));
		} else if ("hex".equals(decode)) {
			data = CodecUtils.hex2byte(body);
		} else {
			ResponseStatus.P_VALUE_ERROR.resEx().setMessage("decode not found");
		}
		return data;
	}
}
