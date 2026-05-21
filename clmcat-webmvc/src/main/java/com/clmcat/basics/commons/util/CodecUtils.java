package com.clmcat.basics.commons.util;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class CodecUtils {
	
	public static class SHA256 {
		
		public static String encryptToHex(String data) {
			return byte2hex(encrypt(data));
		}
		public static byte[] encrypt(String data) {
			return encrypt(data.getBytes(Charset.forName("UTF-8")));
		}

		public static byte[] encrypt(byte[] data) {
			MessageDigest messageDigest;
			try {
				messageDigest = MessageDigest.getInstance("SHA-256");
				messageDigest.update(data);
				return messageDigest.digest();
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
			return null;
		}
	}

	/**
	 * MD5
	 * 
	 * @author zhangxingyu
	 *
	 */
	public static class MD5 {

		public static byte[] md5bytes(byte[] data) {
			byte[] digest = null;
			try {
				MessageDigest md5 = MessageDigest.getInstance("md5");
				digest = md5.digest(data);
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
			return digest;
		}

		public static String md5(String str) {
			byte[] digest = null;
			try {
				MessageDigest md5 = MessageDigest.getInstance("md5");
				digest = md5.digest(str.getBytes("utf-8"));
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			return byte2hex(digest);
		}

		public static String md5_16b(String str) {
			byte[] digest = null;
			try {
				MessageDigest md5 = MessageDigest.getInstance("md5");
				digest = md5.digest(str.getBytes("utf-8"));
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			return byte2hex(digest, 4, 12);
		}
	}

	/**
	 * AES
	 * 
	 * @author zhangxingyu
	 * AES/CBC/PKCS5Padding, iv=密钥的前16字节
	 */
	public static class AES {

		private static final String KEY_AES = "AES";
		/// AES/CBC/PKCS5Padding
		private static final String KEY_CIPHER_AES = "AES/CBC/PKCS5Padding";

		/**
		 * AES AES/CBC/PKCS5Padding 加密, iv=密钥的前16字节
		 * @param data 数据
		 * @param key 密钥 key.getBytes()
		 * @param charset data.getBytes(charset)
		 */
		public static byte[] encrypt(String data, String key, Charset charset) throws Exception {
			if (key == null || key.length() != 16 && key.length() != 32) {
				throw new Exception("key不满足条件");
			}
			byte[] raw = key.getBytes();
			byte[] bytes = data.getBytes(charset);
			return encrypt(bytes, raw);
		}

		/**
		 * AES AES/CBC/PKCS5Padding 加密, iv=密钥的前16字节
		 * @param data 数据
		 * @param key 密钥 key.getBytes()
		 *
		 */
		public static byte[] encrypt(byte[] data, String key) throws Exception {
			if (key == null || key.length() != 16 && key.length() != 32) {
				throw new Exception("key不满足条件");
			}
			byte[] raw = key.getBytes();
			return encrypt(data, raw);
		}
		/**
		 * AES AES/CBC/PKCS5Padding 加密, iv=密钥的前16字节
		 * @param data 数据
		 * @param key 密钥
		 */
		public static byte[] encrypt(byte[] data, byte[] key) throws Exception {
			if (key == null || key.length != 16 && key.length != 32) {
				throw new Exception("key不满足条件");
			}
			/// 密钥的前16字节
			byte[] iv = new byte[16];
			System.arraycopy(key, 0, iv, 0, 16);
			IvParameterSpec ivSpec = new IvParameterSpec(iv);
			SecretKeySpec skeySpec = new SecretKeySpec(key, KEY_AES);
			Cipher cipher = Cipher.getInstance(KEY_CIPHER_AES);
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec, ivSpec);
			return cipher.doFinal(data);
		}

		/**
		 * AES AES/CBC/PKCS5Padding 解密, iv=密钥的前16字节
		 * @param encrypt 加密的内容
		 * @param key 密钥: key.getBytes()
		 * @param charset 编码 encrypt.getBytes(charset)
		 */
		public static String decrypt(byte[] encrypt, String key, Charset charset) throws Exception {
			if (key == null || key.length() != 16 && key.length() != 32) {
				throw new Exception("key不满足条件");
			}
			byte[] raw = key.getBytes();
			byte[] original = decrypt(encrypt, raw);
			return new String(original, charset);
		}

		/**
		 * AES AES/CBC/PKCS5Padding 解密, iv=密钥的前16字节
		 * @param encrypt 加密的内容
		 * @param key 密钥: key.getBytes()
		 */
		public static byte[] decrypt(byte[] encrypt, String key) throws Exception {
			if (key == null || key.length() != 16 && key.length() != 32) {
				throw new Exception("key不满足条件");
			}
			byte[] raw = key.getBytes();
			return decrypt(encrypt, raw);
		}

		/**
		 * AES AES/CBC/PKCS5Padding 解密, iv=密钥的前16字节
		 * @param encrypt 加密的内容
		 * @param key 密钥
		 */
		public static byte[] decrypt(byte[] encrypt, byte[] key) throws Exception {
			if (key == null || key.length != 16 && key.length != 32) {
				throw new Exception("key不满足条件");
			}
			/// 密钥的前16字节
			byte[] iv = new byte[16];
			System.arraycopy(key, 0, iv, 0, 16);
			IvParameterSpec ivSpec = new IvParameterSpec(iv);
			SecretKeySpec skeySpec = new SecretKeySpec(key, KEY_AES);
			Cipher cipher = Cipher.getInstance(KEY_CIPHER_AES);
			cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivSpec);
			return cipher.doFinal(encrypt);
		}
	}

	public static byte[] hex2byte(String strhex) {
		if (strhex == null) {
			return null;
		}
		int l = strhex.length();
		if ((l & 1) == 1) {
			return null;
		}
		byte[] b = new byte[l / 2];
		char c0; // l
		char c1; // r
		byte b0 = 0; // l
		byte b1 = 0; // r
		for (int i = 0, j = 0; j < l; i++, j += 2) {

			c0 = strhex.charAt(j);
			c1 = strhex.charAt(j + 1);
			if (c0 >= '0' && c0 <= '9') {
				b0 = (byte) (c0 - '0');
			} else if (c0 >= 'A' && c0 <= 'F') {
				b0 = (byte) (c0 - 'A' + 10);
			} else if (c0 >= 'a' && c0 <= 'f') {
				b0 = (byte) (c0 - 'a' + 10);
			} else {
				throw new RuntimeException("字符错误!! 不是有效的hex: " + c0);
			}

			if (c1 >= '0' && c1 <= '9') {
				b1 = (byte) (c1 - '0');
			} else if (c1 >= 'A' && c1 <= 'F') {
				b1 = (byte) (c1 - 'A' + 10);
			} else if (c1 >= 'a' && c1 <= 'f') {
				b1 = (byte) (c1 - 'a' + 10);
			} else {
				throw new RuntimeException("字符错误!! 不是有效的hex: " + c1);
			}
			b[i] = (byte) ((b0 << 4) & 0xFF | b1);
		}
		return b;
	}

	public final static char HEXS[] = "0123456789ABCDEF".toCharArray();

	public static String byte2hex(byte[] b) {
		char[] values = new char[b.length * 2];
		int index = 0;
		int l = 0;
		int r = 0;
		for (int n = 0; n < b.length; n++, index += 2) {
			l = (b[n] >> 4) & 0xF;
			r = b[n] & 0xF;
			values[index] = HEXS[l];
			values[index + 1] = HEXS[r];
		}
		return new String(values);
	}

	public static String byte2hex(byte[] b, int start, int end) {
		int length = end - start; // 长度 , 例如: [ end = 10 , start = 0, 长度 = 10 ]
		char[] values = new char[length * 2];
		int index = 0;
		int l = 0;
		int r = 0;
		for (int n = start; n < end; n++, index += 2) {
			l = (b[n] >> 4) & 0xF;
			r = b[n] & 0xF;
			values[index] = HEXS[l];
			values[index + 1] = HEXS[r];
		}
		return new String(values);
	}

}
