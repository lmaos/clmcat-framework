package com.clmcat.basics.commons.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadExecutors {

    public static final ExecutorService EXEC = createExecutor();

    public static ExecutorService createExecutor() {
        try {
            // ===== JDK21+ 虚拟线程（反射，不影响编译）=====
            Object executor = Executors.class.getMethod("newVirtualThreadPerTaskExecutor").invoke(null);
            return (ExecutorService) executor;
        } catch (Throwable ignore) {
            // ===== 低版本 JDK：降级为缓存线程池 =====
            return Executors.newCachedThreadPool(r -> {
                Thread thread = new Thread(r, "global-threadExecutors");
                thread.setDaemon(true);
                return thread;
            });
        }
    }
}