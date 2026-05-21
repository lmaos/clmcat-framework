package com.clmcat.basics.commons.lang;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class RandomStringUtils {

    public static String random(int count, String chars) {
        Random random = ThreadLocalRandom.current();
        char[] values = new char[count];
        for (int i = 0; i < count; i++) {
            values[i] = chars.charAt(random.nextInt(chars.length()));
        }
        return new String(values);
    }
    
    public static String random(int count, char[] chars) {
        Random random = ThreadLocalRandom.current();
        char[] values = new char[count];
        for (int i = 0; i < count; i++) {
            values[i] = chars[random.nextInt(chars.length)];
        }
        return new String(values);
    }
}
