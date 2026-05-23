package com.clmcat.basics.commons.util;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

/**
 * @author zhangxingyu
 *
 * Base85编码解码工具类
 */
public class Base85 {
    // Adobe ASCII85字符表：'!'(33) 到 'u'(117)
    private static final char[] ASCII85_TABLE =
            "!\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstu"
                    .toCharArray();
    private static final int[] ASCII85_REVERSE = createReverseLookup(ASCII85_TABLE);

    // Z85字符表（ZeroMQ标准）
    private static final char[] Z85_TABLE =
            "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ.-:+=^!/*?&<>()[]{}@%$#".toCharArray();
    private static final int[] Z85_REVERSE = createReverseLookup(Z85_TABLE);

    // Ascii85特殊标记
    private static final String ASCII85_START = "<~";
    private static final String ASCII85_END = "~>";
    private static final char ASCII85_ZERO = 'z';
    private static final int ASCII85_BASE = 85;
    private static final long UINT32_MAX = 0xFFFFFFFFL;

    // Ascii85编码（含Adobe包装标记）
    public static String encodeAscii85(byte[] input) {
        StringBuilder output = new StringBuilder(ASCII85_START);
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

            // 转换为85进制
            char[] chunk = new char[5];
            for (int k = 4; k >= 0; k--) {
                chunk[k] = ASCII85_TABLE[(int) (value % ASCII85_BASE)];
                value /= ASCII85_BASE;
            }

            int outputLength = bytesToProcess < 4 ? bytesToProcess + 1 : 5;
            output.append(chunk, 0, outputLength);
        }
        return output.append(ASCII85_END).toString();
    }

    // Ascii85解码
    public static byte[] decodeAscii85(String input) {
        if (input == null) {
            throw new IllegalArgumentException("Ascii85输入不能为空");
        }
        String trimmed = input.trim();
        boolean hasStart = trimmed.startsWith(ASCII85_START);
        boolean hasEnd = trimmed.endsWith(ASCII85_END);
        if (hasStart != hasEnd) {
            throw new IllegalArgumentException("Ascii85包装标记不完整");
        }
        if (hasStart) {
            trimmed = trimmed.substring(ASCII85_START.length(), trimmed.length() - ASCII85_END.length());
        }

        ByteArrayOutputStream buffer = new ByteArrayOutputStream(trimmed.length());
        int chunkPos = 0;
        long value = 0;

        for (char c : trimmed.toCharArray()) {
            if (Character.isWhitespace(c)) {
                continue;
            }
            if (c == ASCII85_ZERO) {
                if (chunkPos != 0) {
                    throw new IllegalArgumentException("Ascii85字符'z'只能单独表示一个4字节零块");
                }
                buffer.write(0);
                buffer.write(0);
                buffer.write(0);
                buffer.write(0);
                continue;
            }

            int digit = decodeAscii85Digit(c);
            if (digit < 0) {
                throw new IllegalArgumentException("非法Ascii85字符: " + c);
            }

            value = value * ASCII85_BASE + digit;
            if (++chunkPos == 5) {
                writeDecodedChunk(buffer, value, 4);
                value = 0;
                chunkPos = 0;
            }
        }

        // 处理剩余部分
        if (chunkPos == 1) {
            throw new IllegalArgumentException("Ascii85尾块长度非法，至少需要2个字符");
        }
        if (chunkPos > 1) {
            for (int i = chunkPos; i < 5; i++) {
                value = value * ASCII85_BASE + (ASCII85_BASE - 1);
            }
            writeDecodedChunk(buffer, value, chunkPos - 1);
        }

        return buffer.toByteArray();
    }

    private static void writeDecodedChunk(ByteArrayOutputStream buffer, long value, int outputBytes) {
        if (value < 0 || value > UINT32_MAX) {
            throw new IllegalArgumentException("Ascii85编码块超出32位无符号整数范围");
        }
        for (int i = 0; i < outputBytes; i++) {
            buffer.write((byte) ((value >> (24 - i * 8)) & 0xFF));
        }
    }

    private static int decodeAscii85Digit(char c) {
        if (c >= ASCII85_REVERSE.length) {
            return -1;
        }
        return ASCII85_REVERSE[c];
    }

    private static int[] createReverseLookup(char[] table) {
        int[] reverse = new int[128];
        for (int i = 0; i < reverse.length; i++) {
            reverse[i] = -1;
        }
        for (int i = 0; i < table.length; i++) {
            char c = table[i];
            if (c < reverse.length) {
                reverse[c] = i;
            }
        }
        return reverse;
    }

    private static int decodeZ85Digit(char c) {
        if (c >= Z85_REVERSE.length) {
            return -1;
        }
        return Z85_REVERSE[c];
    }

    private static void checkDecodedUint32(long value, String type) {
        if (value < 0 || value > UINT32_MAX) {
            throw new IllegalArgumentException(type + "编码块超出32位无符号整数范围");
        }
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
                int digit = decodeZ85Digit(c);
                if (digit == -1) {
                    throw new IllegalArgumentException("非法Z85字符: " + c);
                }
                value = value * ASCII85_BASE + digit;
            }
            checkDecodedUint32(value, "Z85");

            buffer.put((byte) ((value >> 24) & 0xFF));
            buffer.put((byte) ((value >> 16) & 0xFF));
            buffer.put((byte) ((value >> 8) & 0xFF));
            buffer.put((byte) (value & 0xFF));
        }
        return buffer.array();
    }
}
