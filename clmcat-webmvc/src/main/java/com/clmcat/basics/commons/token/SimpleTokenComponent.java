package com.clmcat.basics.commons.token;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Base64;

import com.clmcat.basics.commons.lang.NumberUtils;
import com.clmcat.basics.commons.token.entity.SimpleToken;
import com.clmcat.basics.commons.util.CodecUtils;

/**
 * 简单 TOKEN SimpleTokenComponent <br>
 * 
 * SimpleTokenComponent stc = new SimpleTokenComponent(password); // 创建简单TOKEN组建 <br>
 * 
 * String token = stc.getToken(simpleToken, clientCheck);  // 获得TOKEN <br>
 * 
 * boolean ok = stc.verifyToken(token, clientCheck);  // 验证TOKEN有效 <br>
 * 
 * SimpleToken simpleToken = stc.parseToken(token); // 解析TOKEN <br>
 * 
 * @author zhangxingyu
 * <br>
 * 
 * <br>
 * 
 * | check_md5 (16) | user_id (8) | invalid_time (8) | aes_text[current_time, client_check] (16 + n) |
 *
 */
public class SimpleTokenComponent implements TokenComponent<SimpleToken> {
	/**
	 * 密码
	 */
	private byte passwordBytes[];
	private short mod = TokenMod.simple;

	public SimpleTokenComponent(String password) {
		passwordBytes = CodecUtils.MD5.md5bytes(password.getBytes(Charset.forName("UTF-8")));
	}

	// clientCheck 客户端校验码, 每个客户端唯一(客户端传递,可以是设备号.)
	public String getToken(SimpleToken simpleToken, String clientCheck) throws TokenComponentException {
		try {
			long userId = simpleToken.getUserId(); // 用户ID
			long invalidTime = simpleToken.getInvalidTime(); // 过期时间
			long currentTime = System.currentTimeMillis();
			byte[] textBytes;
			if (clientCheck != null && clientCheck.length() > 0) {
				byte[] ccccc = clientCheck.getBytes(Charset.forName("UTF-8"));
				byte[] currentTimeBytes = NumberUtils.toBytesByLongB(currentTime);
				textBytes = new byte[8 + ccccc.length];
				System.arraycopy(currentTimeBytes, 0, textBytes, 0, 8);
				System.arraycopy(ccccc, 0, textBytes, 8, ccccc.length);
			} else {
				textBytes = NumberUtils.toBytesByLongB(currentTime);
			}
			// 加密的内容 AES(current, clientCheck);
			byte[] aesBytes = CodecUtils.AES.encrypt(textBytes, passwordBytes);
			
			byte[] userIdBytes = NumberUtils.toBytesByLongB(userId);
			byte[] invalidTimeBytes = NumberUtils.toBytesByLongB(invalidTime);

			int contentLength = 16 + aesBytes.length;
			byte[] tokenContents = new byte[contentLength + 16];
			System.arraycopy(userIdBytes, 0, tokenContents, 0, 8);
			System.arraycopy(invalidTimeBytes, 0, tokenContents, 8, 8);
			System.arraycopy(aesBytes, 0, tokenContents, 16, aesBytes.length);
			System.arraycopy(passwordBytes, 0, tokenContents, contentLength, 16);

			// CHECK(userId,invalidTime,aes_text,password)
			byte[] checkBytes = CodecUtils.MD5.md5bytes(tokenContents);
			
			// CHECK(userId,invalidTime,aes_text,password), userId,invalidTime, AES_aes_test(currentTime+ip, password)
			byte[] token = new byte[checkBytes.length + contentLength + 2];
			token[0] = (byte) ((mod >> 8) & 0xFF);
			token[1] = (byte) ((mod >> 0) & 0xFF);
			System.arraycopy(checkBytes, 0, token, 2, 16);
			System.arraycopy(tokenContents, 0, token, 18, contentLength);
			return Base64.getUrlEncoder().encodeToString(token);
		} catch (Exception e) {
			throw new TokenComponentException(e);
		}
	}
	/**
	 * 解析token 如果校验失败则发生 TokenComponentException 异常.
	 */
	@Override
	public SimpleToken parseToken(String token) throws TokenComponentException {
		byte[] tokenBytes = Base64.getUrlDecoder().decode(token);
		ByteBuffer byteBuffer = ByteBuffer.wrap(tokenBytes).order(ByteOrder.BIG_ENDIAN);
		// 校验版本
		short modValue = byteBuffer.getShort();
		if (modValue != mod) {
			throw new TokenComponentException("mod error");
		}
		// MD5校验码 -- CHECK(userId,invalidTime,aes_text,password)
		byte[] checkBytes = new byte[16];
		byteBuffer.get(checkBytes); 
		
		// (userId, invalidTime, aes_text, password)
		byte[] contentValue = new byte[byteBuffer.remaining() + 16];
		System.arraycopy(tokenBytes, 18, contentValue, 0, byteBuffer.remaining());
		System.arraycopy(passwordBytes, 0, contentValue, byteBuffer.remaining(), 16);
		byte[] checkValue = CodecUtils.MD5.md5bytes(contentValue);
		// 比较是否一致- 快速验证TOKEN是否被篡改.
		if (!Arrays.equals(checkValue, checkBytes)) {
			throw new TokenComponentException("check error");
		}
		long userId = byteBuffer.getLong();
		long invalidTime = byteBuffer.getLong();

		return new SimpleToken(userId, invalidTime);
	}

	@Override
	public boolean verifyToken(String token, String clientCheck) {
		byte[] tokenBytes = Base64.getUrlDecoder().decode(token);
		short modValue = (short) (((tokenBytes[0] & 0xFF) << 8) | (tokenBytes[1] & 0xFF));
		if (modValue != mod) {
			return false;
		}
		// MD5校验码 -- CHECK(userId,invalidTime,aes_text,password)
		byte[] checkBytes = new byte[16];
		System.arraycopy(tokenBytes, 2, checkBytes, 0, 16);
		int contentLength = tokenBytes.length - 18;
		// (userId, invalidTime, aes_text, password)
		byte[] contentValue = new byte[contentLength + 16];
		System.arraycopy(tokenBytes, 18, contentValue, 0, contentLength);
		System.arraycopy(passwordBytes, 0, contentValue, contentLength, 16);
		byte[] checkValue = CodecUtils.MD5.md5bytes(contentValue);
		// 比较是否一致- 快速验证TOKEN是否被篡改.
		if (!Arrays.equals(checkValue, checkBytes)) {
			return false;
		}

		// 校验超时时间-token是否过期, invalidTime == 0 永久有效.
		byte[] invalidTimeBytes = new byte[8];
		System.arraycopy(tokenBytes, 26, invalidTimeBytes, 0, 8);
		long invalidTime = NumberUtils.toLongByBytesB(invalidTimeBytes);
		if (invalidTime > 0 && invalidTime < System.currentTimeMillis()) {
			return false;
		}

		// 进行TOKEN客户端码校验--必须是这个客户端才能用. 非这个客户端则不可使用.
		if (clientCheck != null) {
			try {
				// aes_test
				byte[] aesValueBytes = new byte[contentLength - 16];
				System.arraycopy(contentValue, 16, aesValueBytes, 0, aesValueBytes.length);
				// (currentTime, clientCheck)
				byte[] textValue = CodecUtils.AES.decrypt(aesValueBytes, passwordBytes);
				byte[] clientCheckBytes = clientCheck.getBytes(Charset.forName("UTF-8"));
				// 比较客户端校验码是否一致
				if (textValue.length == 8 + clientCheckBytes.length) {
					for (int i = 0; i < clientCheckBytes.length; i++) {
						if (textValue[i + 8] != clientCheckBytes[i]) {
							return false;
						}
					}
				} else {
					return false;
				}
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}

		}
		return true;
	}

	public byte[] getAddr(String ip) {
		try {
			byte[] addr = InetAddress.getByName(ip).getAddress();
			return addr;
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * (没校验) 不进行校验-直接获取 SimpleToken
	 * @param token
	 * @return
	 */
	public SimpleToken getTokenBeanNoVerify(String token) {
		byte[] tokenBytes = Base64.getUrlDecoder().decode(token);
		ByteBuffer byteBuffer = ByteBuffer.wrap(tokenBytes).order(ByteOrder.BIG_ENDIAN);
		short modValue = byteBuffer.getShort();
		if (modValue != mod) {
			return null;
		}
		long userId = byteBuffer.getLong();
		long invalidTime = byteBuffer.getLong();
		return new SimpleToken(userId, invalidTime);
	}
	
	

	public static void main(String[] args) throws TokenComponentException {
		SimpleTokenComponent stc = new SimpleTokenComponent("12345678976");
		String token = stc.getToken(new SimpleToken(1234, System.currentTimeMillis() + 10000), "hello");
		System.out.println(token);
		System.out.println(stc.verifyToken(token, "hello"));
		System.out.println(stc.parseToken(token));
		// 测试性能 结论: 验证有效性带 clientCheck时候的性能单核 75w/s, 不校验 clientCheck 时的性能 170w/s
//		long time = System.currentTimeMillis();
//		int size = 10000000;
//		for (int i = 0; i < size; i++) {
//			stc.verifyToken(token, "hello");
//		}
//		time = System.currentTimeMillis() - time;
//		System.out.println(time+"ms");
//		System.out.println(BigDecimal.valueOf(time).divide(BigDecimal.valueOf(size), 10, RoundingMode.HALF_UP));
//		System.out.println(BigDecimal.valueOf(size).divide(BigDecimal.valueOf(time), 10, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(1000)));

	}

}
