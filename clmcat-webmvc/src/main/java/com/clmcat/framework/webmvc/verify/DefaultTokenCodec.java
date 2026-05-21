package com.clmcat.framework.webmvc.verify;

import com.clmcat.basics.commons.lang.NumberUtils;
import org.slf4j.Logger;

import java.net.InetAddress;
import java.util.Base64;
import java.util.concurrent.ThreadLocalRandom;

public class DefaultTokenCodec {
	public final static String TOKEN_INFO_KEY = "Token-Info";
	public final static String TOKEN_STATUS_KEY = "Token-Status";

	private static long timeout = 1 * 1000 * 60 * 60 * 24;

	static Logger logger = org.slf4j.LoggerFactory.getLogger(DefaultTokenCodec.class);

	public static String createToken(long userId, String remoteAddr) {
		// userId 64 8
		// ipv6 128 16
		// ipvv 32 4
		try {

			long invalidTime = System.currentTimeMillis() + timeout;
			InetAddress inetAddress = InetAddress.getAllByName(remoteAddr)[0];
			byte[] addressBytes = inetAddress.getAddress();
			ThreadLocalRandom localRandom = ThreadLocalRandom.current();
			int random1 = localRandom.nextInt();
			int random2 = localRandom.nextInt();

			// 28(IPV4) - 40(IPV6)
			int length = 12 + addressBytes.length + 12;

			byte[] userIdBytes = NumberUtils.toBytesByLongB(userId);
			byte[] randomBytes1 = NumberUtils.toBytesByIntB(random1);
			byte[] randomBytes2 = NumberUtils.toBytesByIntB(random2);
			byte[] invalidTimeBytes = NumberUtils.toBytesByLongB(invalidTime);

			byte[] tokenBytes = new byte[length];
			int index = 0;
			int rindex = 0;

			for (int i = 0; i < userIdBytes.length; i++) {
				tokenBytes[index++] = userIdBytes[i];
				if ((i & 1) == 0) {
					tokenBytes[index++] = randomBytes1[rindex++];
				}
			}
			rindex = 0;
			if (addressBytes.length == 16) {
				for (int i = 0; i < addressBytes.length; i++) {
					tokenBytes[index++] = addressBytes[i];
					if ((i & 3) == 0) {
						tokenBytes[index++] = randomBytes2[rindex++];
					}
				}
			} else {
				for (int i = 0; i < addressBytes.length; i++) {
					tokenBytes[index++] = addressBytes[i];
					tokenBytes[index++] = randomBytes2[rindex++];
				}
			}
			for (byte invalidTimeByte : invalidTimeBytes) {
				tokenBytes[index++] = invalidTimeByte;
			}

			return Base64.getEncoder().encodeToString(tokenBytes);

		} catch (Exception e) {
			logger.error("创建token 失败", e);
			return null;
		}
	}

	public static TokenInfo parse(String token) {
		if (token == null) {
			return null;
		}
		try {
			byte[] tokenBytes = Base64.getDecoder().decode(token);

			if (tokenBytes.length != 28 && tokenBytes.length != 40) {
				return null;
			}
			byte[] userIdBytes = new byte[8];
			byte[] remoteAddrBytes = null;
			byte[] invalidTimeBytes = new byte[8];

			int index = 0;

			for (int i = 0; i < userIdBytes.length; i++) {
				userIdBytes[i] = tokenBytes[index++];
				if ((i & 1) == 0) {
					index++;
				}
			}

			// IPV4
			if (tokenBytes.length == 28) {
				remoteAddrBytes = new byte[4];
				for (int i = 0; i < remoteAddrBytes.length; i++) {
					remoteAddrBytes[i] = tokenBytes[index++];
					index++;
				}
			} else {
				// ipV6
				remoteAddrBytes = new byte[16];
				for (int i = 0; i < remoteAddrBytes.length; i++) {
					remoteAddrBytes[i] = tokenBytes[index++];
					if ((i & 3) == 0) {
						index++;
					}
				}
			}

			for (int i = 0; i < invalidTimeBytes.length; i++) {
				invalidTimeBytes[i] = tokenBytes[index++];
			}

			long userId = NumberUtils.toLongByBytesB(userIdBytes);
			long invalidTime = NumberUtils.toLongByBytesB(invalidTimeBytes);
			InetAddress remoteAddr = InetAddress.getByAddress(remoteAddrBytes);

			return new DefaultTokenInfo(userId, remoteAddr, invalidTime);
		} catch (Exception e) {
			logger.error("解析token失败", e);
			return null;
		}
	}
	public static void main(String[] args) {
		System.out.println(createToken(12345, "127.0.0.1"));
	}
}
