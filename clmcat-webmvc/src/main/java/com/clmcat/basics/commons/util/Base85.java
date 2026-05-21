package com.clmcat.basics.commons.util;

import java.nio.ByteBuffer;

/**
 * @author zhangxingyu
 *
 * Base85编码解码工具类
 */
public class Base85 {
    // Ascii85字符表（RFC 1924）
    private static final char[] ASCII85_TABLE =
            "!\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~"
                    .toCharArray();

    // Z85字符表（ZeroMQ标准）
    private static final char[] Z85_TABLE =
            "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ.-:+=^!/*?&<>()[]{}@%$#".toCharArray();

    // Ascii85特殊标记
    private static final String ASCII85_START = "<~";
    private static final String ASCII85_END = "~>";
    private static final char ASCII85_ZERO = 'z';
    private static final char ASCII85_SPACE = ' ';

    // Ascii85编码（含Adobe包装标记）
    public static String encodeAscii85(byte[] input) {
        StringBuilder output = new StringBuilder(ASCII85_START);
        int padding = 0;

        for (int i = 0; i < input.length; i += 4) {
            long value = 0;
            int bytesToProcess = Math.min(4, input.length - i);

            // 处理全零块特殊编码
            if (bytesToProcess == 4 &&
                    input[i] == 0 && input[i+1] == 0 &&
                    input[i+2] == 0 && input[i+3] == 0) {
                output.append(ASCII85_ZERO);
                continue;
            }

            // 构建32位整数
            for (int j = 0; j < bytesToProcess; j++) {
                value |= (input[i + j] & 0xFFL) << (24 - j * 8);
            }
            if (bytesToProcess < 4) {
                padding = 4 - bytesToProcess;
            }

            // 转换为85进制
            char[] chunk = new char[5];
            for (int k = 4; k >= 0; k--) {
                chunk[k] = ASCII85_TABLE[(int)(value % 85)];
                value /= 85;
            }

            // 添加非全零块
            output.append(chunk, 0, 5 - padding);
        }
        return output.append(ASCII85_END).toString();
    }

    // Ascii85解码
    public static byte[] decodeAscii85(String input) {
        // 去除包装标记和空白字符
        String trimmed = input.replace(ASCII85_START, "")
                .replace(ASCII85_END, "")
                .replaceAll("\\s", "");

        ByteBuffer buffer = ByteBuffer.allocate(trimmed.length() * 4 / 5);
        int chunkPos = 0;
        long value = 0;

        for (char c : trimmed.toCharArray()) {
            if (c == ASCII85_ZERO) {
                buffer.put(new byte[]{0, 0, 0, 0});
                continue;
            }

            int digit = new String(ASCII85_TABLE).indexOf(c);
            if (digit == -1) {continue;}

            value = value * 85 + digit;
            if (++chunkPos == 5) {
                for (int i = 0; i < 4; i++) {
                    buffer.put((byte)((value >> (24 - i * 8)) & 0xFF));
                }
                value = 0;
                chunkPos = 0;
            }
        }

        // 处理剩余部分
        if (chunkPos > 0) {
            value *= Math.pow(85, 5 - chunkPos);
            for (int i = 0; i < chunkPos - 1; i++) {
                buffer.put((byte)((value >> (24 - i * 8)) & 0xFF));
            }
        }

        byte[] result = new byte[buffer.position()];
        buffer.rewind();
        buffer.get(result);
        return result;
    }

    // Z85编码（要求输入长度是4的倍数）
    public static String encodeZ85(byte[] input) {
        if (input.length % 4 != 0) {
            throw new IllegalArgumentException("Z85输入长度必须是4的倍数");
        }

        StringBuilder output = new StringBuilder();
        for (int i = 0; i < input.length; i += 4) {
            long value = ((input[i] & 0xFFL) << 24) |
                    ((input[i+1] & 0xFFL) << 16) |
                    ((input[i+2] & 0xFFL) << 8) |
                    (input[i+3] & 0xFFL);

            char[] chunk = new char[5];
            for (int j = 4; j >= 0; j--) {
                chunk[j] = Z85_TABLE[(int)(value % 85)];
                value /= 85;
            }
            output.append(chunk);
        }
        return output.toString();
    }

    // Z85解码（要求输入长度是5的倍数）
    public static byte[] decodeZ85(String input) {
        if (input.length() % 5 != 0) {
            throw new IllegalArgumentException("Z85输入长度必须是5的倍数");
        }

        ByteBuffer buffer = ByteBuffer.allocate(input.length() * 4 / 5);
        for (int i = 0; i < input.length(); i += 5) {
            long value = 0;
            for (int j = 0; j < 5; j++) {
                char c = input.charAt(i + j);
                int digit = new String(Z85_TABLE).indexOf(c);
                if (digit == -1) {
                    throw new IllegalArgumentException("非法Z85字符: " + c);
                }
                value = value * 85 + digit;
            }

            buffer.put((byte)((value >> 24) & 0xFF));
            buffer.put((byte)((value >> 16) & 0xFF));
            buffer.put((byte)((value >> 8) & 0xFF));
            buffer.put((byte)(value & 0xFF));
        }
        return buffer.array();
    }
}
