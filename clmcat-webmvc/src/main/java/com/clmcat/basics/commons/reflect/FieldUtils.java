package com.clmcat.basics.commons.reflect;

import java.lang.reflect.Field;
import java.util.LinkedList;

/**
 * java 反射 操作成员属性
 */
public class FieldUtils {

    /**
     * 获得当前字段的值
     *
     * @param obj 对象
     * @param fieldName 字段名
     * @return
     * @throws NoSuchFieldException 找不到当前字段时候发生错误
     */
    public static final Object get(Object obj, String fieldName) throws NoSuchFieldException {
        Field field = obj.getClass().getDeclaredField(fieldName);
        return get(obj, field);
    }

    /**
     * 获得当前字段的值
     *
     * @param obj 对象
     * @param field 字段
     * @return
     */
    public static final Object get(Object obj, Field field) {
        if (obj == null) {
            return null;
        }
        try {
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            return field.get(obj);
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 给字段设置值
     * @param obj 对象
     * @param fieldName 字段名
     * @param value 设置的内容
     * @return
     * @throws NoSuchFieldException 找不到当前字段时候发生错误
     */
    public static final boolean set(Object obj, String fieldName, Object value) throws NoSuchFieldException {
        Field field = obj.getClass().getDeclaredField(fieldName);
        return set(obj, field, value);
    }

    /**
     * 设置字段的值
     * @param obj
     * @param field
     * @param value
     * @return
     */
    public static final boolean set(Object obj, Field field, Object value) {
        if (obj == null) {
            return false;
        }
        try {
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            field.set(obj, value);
            return true;
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
    }

    public static final Field getDeclaredField(Class<?> beanType, String fieldName, boolean superFind) throws NoSuchFieldException {
        if (!superFind) {
            return beanType.getDeclaredField(fieldName);
        } else {
            LinkedList<Class<?>> types = new LinkedList<>();
            types.add(beanType);
            while (!types.isEmpty()) {

                Class<?> type = types.removeFirst();
                try {
                    return type.getDeclaredField(fieldName);
                } catch (Exception e) { }

                Class<?> superClass = type.getSuperclass();
                if (superClass != Object.class) {
                    types.addLast(superClass);
                }
            }

            throw new NoSuchFieldException(beanType.getName() + "." + fieldName);
        }
    }



}
