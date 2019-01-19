package com.example.moader.executor;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class MoaderExecutor {
    private static  int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static  int CORE_POOL_SIZE = CPU_COUNT + 1;
    private static  int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1;
    private static  long KEEP_ALIVE_TIME = 10L;
    private static Executor INSTANCE = null;

    private static ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "Moader#" + mCount.getAndIncrement());
        }
    };

    public static Executor getInstance() {
        if (INSTANCE == null) {
            synchronized (MoaderExecutor.class) {
                INSTANCE = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE,
                        KEEP_ALIVE_TIME, TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>(), sThreadFactory);
            }
        }
        return INSTANCE;
    }

}
