package com.clmcat.basics.commons.reflect;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 方法工具
 */
public class MethodUtils {


    public final Object invoke(Object obj, Method method, Object... args) throws Throwable {
        if (obj == null) {
            return null;
        }
        if (method.isAccessible()) {
            method.setAccessible(true);
        }
        return method.invoke(obj, args);
    }


    public final Method getMethod(Class<?> beanType, String methodName, Class<?>[] paramTypes, boolean superFind) throws NoSuchMethodException {
        if (!superFind) {
            Method declaredMethod = beanType.getDeclaredMethod(methodName, paramTypes);
            return declaredMethod;
        } else {
            Class<?> type = beanType;
            while (type != Object.class) {
                try {
                    return beanType.getDeclaredMethod(methodName, paramTypes);
                } catch (Throwable e) {}
                type = type.getSuperclass();
            }
            throw new NoSuchMethodException(beanType.getName() + "." + methodName);
        }
    }

    public final List<Method> findMethods(Class<?> beanType, MatchMethod match, boolean superFind) {
        if (!superFind) {
            List<Method> methods = new ArrayList<>();
            Method[] declaredMethods = beanType.getDeclaredMethods();

            for (Method declaredMethod : declaredMethods) {
                if (match.match(declaredMethod)) {
                    methods.add(declaredMethod);
                }
            }
            return methods;
        } else {
            List<Method> methods = new ArrayList<>();

            Class<?> type = beanType;
            while (type != Object.class) {
                Method[] declaredMethods = type.getDeclaredMethods();
                for (Method declaredMethod : declaredMethods) {
                    if (match.match(declaredMethod)) {
                        methods.add(declaredMethod);
                    }
                }
                type = type.getSuperclass();
            }

            return methods;
        }
    }

    @FunctionalInterface
    public static interface MatchMethod {
        boolean match(Method method);
    }
}
