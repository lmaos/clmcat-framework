package com.clmcat.basics.commons.lang;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Collection;

public class StringUtils {

    public static final String EMPTY_STRING = "";

    /**
     * 连接字符串，使用 split 分割
     * @param objs 数组对象
     * @param split 分割符号
     * @return
     */
    public static final String join(Object objs, String split) {
        if (objs == null) {
            return null;
        }

        if (objs instanceof Object[]) {
            return join((Object[])objs, split);
        }

        if (objs instanceof Collection) {
            return join((Collection)objs, split);
        }

        if (objs.getClass().isArray()) {

            int length = Array.getLength(objs);
            StringBuilder text = new StringBuilder();
            for (int i = 0; i < length; i++) {
                Object obj = Array.get(objs, i);
                text.append(obj).append(split);
            }
            if (text.length() > 0) {
                return text.substring(0, text.length() - split.length());
            }
            return EMPTY_STRING;
        } else {
            return String.valueOf(objs);
        }
    }

    /**
     * 连接字符串，使用 split 分割
     * @param objs 数组对象
     * @param split 分割符号
     * @return
     */
    public static final String join(Object[] objs, String split) {
        if (objs == null) {
            return null;
        }
        StringBuilder text = new StringBuilder();
        for (Object obj : objs) {
            text.append(obj).append(split);
        }
        if (text.length() > 0) {
            return text.substring(0, text.length() - split.length());
        }
        return EMPTY_STRING;
    }

    /**
     * 连接字符串，使用 split 分割
     * @param objs 集合对象
     * @param split 分割符号
     * @return
     */
    public static final String join(Collection<?> objs, String split) {
        if (objs == null) {
            return null;
        }
        StringBuilder text = new StringBuilder();
        for (Object obj : objs) {
            text.append(obj).append(split);
        }
        if (text.length() > 0) {
            return text.substring(0, text.length() - split.length());
        }
        return EMPTY_STRING;
    }

    public static final boolean isBlank(String str) {
        if (str == null || str.length() == 0) {
            return true;
        }

        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c != ' ') {
                return false;
            }
        }
        return true;
    }

    public static final boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    /**
     * [不推荐使用] 常量反转，会导致所有的依赖当前常量引用的数据全部反转，是一种不安全的访问。
     * @param str
     */
    @Deprecated
    public static final void constReversal(String str) {
        if (str == null || str.isEmpty()) {
            return;
        }
        try {
            Field valueField = String.class.getDeclaredField("value");
            if (!valueField.isAccessible()) {
                valueField.setAccessible(true);
            }
            char[] value = (char[])valueField.get(str);
            for (int i = 0; i < value.length/2; i++) {
                char c = value[i];
                value[i] = value[value.length - 1 - i];
                value[value.length - 1 - i] = c;
            }
        } catch (Exception e) {

        }
    }

    /**
     * 反转字符串
     * @param str
     * @return
     */
    public static final String reversal(String str) {
        char[] value = str.toCharArray();
        for (int i = 0; i < value.length/2; i++) {
            char c = value[i];
            value[i] = value[value.length - 1 - i];
            value[value.length - 1 - i] = c;
        }
        return String.valueOf(value);
    }

	public static boolean equalsAny(String src, String... texts) {
		for (String text : texts) {
			if (src == null && text == null) {
				return true;
			}
			if (src == text || src.equals(text)) {
				return true;
			}
		}
		return false;
	}
    
    public static void main(String[] args){
        // Object object = new Integer[]{111, 222};
        // System.out.println(join(new String[]{"2343", "ddd"}, ","));
        // System.out.println(join(object, ","));
        String str = "12345";
        // str = reversal(str);
        constReversal(str);
        System.out.println(str);
        // System.out.println();
        str = "12345";
        System.out.println(str);

    }

}
