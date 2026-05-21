package com.clmcat.framework.webmvc.interceptor;

public class LocaleParameterProperty {

	private boolean disabledHeader;

	private String encrypt; // 加密
	private String encryptCipher; // 加密密码
	private String decipherCipher; // 解密密码

	public boolean isDisabledHeader() {
		return disabledHeader;
	}

	public void setDisabledHeader(boolean disabledHeader) {
		this.disabledHeader = disabledHeader;
	}

	public String getEncrypt() {
		return encrypt;
	}

	public void setEncrypt(String encrypt) {
		this.encrypt = encrypt;
	}

	public String getEncryptCipher() {
		return encryptCipher;
	}

	public void setEncryptCipher(String encryptCipher) {
		this.encryptCipher = encryptCipher;
	}

	public String getDecipherCipher() {
		return decipherCipher;
	}

	public void setDecipherCipher(String decipherCipher) {
		this.decipherCipher = decipherCipher;
	}

}
