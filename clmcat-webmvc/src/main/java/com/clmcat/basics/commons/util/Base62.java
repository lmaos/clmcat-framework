package com.clmcat.basics.commons.util;

import java.math.BigInteger;
import java.util.Arrays;

/**
 * Base62 编码工具类，字符表顺序为 0-9 a-z A-Z。
 */
public class Base62 {
    private static final char[] ALPHABET =
            "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    private static final int[] REVERSE = createReverseLookup();
    private static final BigInteger BASE = BigInteger.valueOf(ALPHABET.length);

    public static String encode(long value) {
        if (value < 0) {
            throw new IllegalArgumentException("Base62不支持负数编码");
        }
        if (value == 0) {
            return String.valueOf(ALPHABET[0]);
        }
        return encodePositive(BigInteger.valueOf(value));
    }

    public static long decodeToLong(String input) {
        BigInteger value = decodeToBigInteger(input);
        if (value.bitLength() > Long.SIZE - 1) {
            throw new IllegalArgumentException("Base62值超出long范围");
        }
        return value.longValueExact();
    }

    public static String encode(byte[] input) {
        if (input == null) {
            throw new IllegalArgumentException("Base62输入不能为空");
        }
        if (input.length == 0) {
            return "";
        }
        int leadingZeroBytes = countLeadingZeroBytes(input);
        byte[] magnitudeBytes = Arrays.copyOfRange(input, leadingZeroBytes, input.length);

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < leadingZeroBytes; i++) {
            builder.append(ALPHABET[0]);
        }
        if (magnitudeBytes.length > 0) {
            builder.append(encodePositive(new BigInteger(1, magnitudeBytes)));
        }
        return builder.toString();
    }

    public static byte[] decode(String input) {
        String normalized = normalizeInput(input);
        if (normalized.isEmpty()) {
            return new byte[0];
        }

        int leadingZeroChars = countLeadingZeroChars(normalized);
        String payload = normalized.substring(leadingZeroChars);
        byte[] magnitudeBytes = payload.isEmpty() ? new byte[0] : toUnsignedBytes(decodeToBigInteger(payload));

        byte[] result = new byte[leadingZeroChars + magnitudeBytes.length];
        System.arraycopy(magnitudeBytes, 0, result, leadingZeroChars, magnitudeBytes.length);
        return result;
    }

    public static BigInteger decodeToBigInteger(String input) {
        String normalized = normalizeInput(input);
        if (normalized.isEmpty()) {
            return BigInteger.ZERO;
        }

        BigInteger value = BigInteger.ZERO;
        for (int i = 0; i < normalized.length(); i++) {
            int digit = decodeDigit(normalized.charAt(i));
            if (digit < 0) {
                throw new IllegalArgumentException("非法Base62字符: " + normalized.charAt(i));
            }
            value = value.multiply(BASE).add(BigInteger.valueOf(digit));
        }
        return value;
    }

    private static String encodePositive(BigInteger value) {
        StringBuilder builder = new StringBuilder();
        BigInteger current = value;
        while (current.signum() > 0) {
            BigInteger[] divRem = current.divideAndRemainder(BASE);
            builder.append(ALPHABET[divRem[1].intValue()]);
            current = divRem[0];
        }
        return builder.reverse().toString();
    }

    private static int countLeadingZeroBytes(byte[] input) {
        int count = 0;
        while (count < input.length && input[count] == 0) {
            count++;
        }
        return count;
    }

    private static int countLeadingZeroChars(String input) {
        int count = 0;
        while (count < input.length() && input.charAt(count) == ALPHABET[0]) {
            count++;
        }
        return count;
    }

    private static String normalizeInput(String input) {
        if (input == null) {
            throw new IllegalArgumentException("Base62输入不能为空");
        }
        return input.trim();
    }

    private static byte[] toUnsignedBytes(BigInteger value) {
        byte[] bytes = value.toByteArray();
        if (bytes.length > 1 && bytes[0] == 0) {
            return Arrays.copyOfRange(bytes, 1, bytes.length);
        }
        if (bytes.length == 1 && bytes[0] == 0) {
            return new byte[0];
        }
        return bytes;
    }

    private static int decodeDigit(char c) {
        if (c >= REVERSE.length) {
            return -1;
        }
        return REVERSE[c];
    }

    private static int[] createReverseLookup() {
        int[] reverse = new int[123];
        Arrays.fill(reverse, -1);
        for (int i = 0; i < ALPHABET.length; i++) {
            reverse[ALPHABET[i]] = i;
        }
        return reverse;
    }
}
