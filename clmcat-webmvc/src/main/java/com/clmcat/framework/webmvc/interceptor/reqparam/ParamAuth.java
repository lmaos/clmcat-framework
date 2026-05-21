package com.clmcat.framework.webmvc.interceptor.reqparam;

import java.nio.charset.Charset;
import java.util.Base64;

import com.clmcat.basics.commons.util.CodecUtils;
import com.clmcat.framework.webmvc.ResponseStatus;

import lombok.Data;

@Data
public class ParamAuth {
	private String password;
	private String decode;
	private Charset charset = Charset.forName("UTF-8");
	
	public byte[] decodeBody(String body, Charset charset) {
		byte[] data = null;
		if (decode == null || "base64".equals(decode)) {
			data =  Base64.getDecoder().decode(body.getBytes(charset));
		} else if ("hex".equals(decode)){
			data = CodecUtils.hex2byte(body);
		} else {
			ResponseStatus.P_VALUE_ERROR.resEx().setMessage("decode not found");
		}
		return data;
	}
}
