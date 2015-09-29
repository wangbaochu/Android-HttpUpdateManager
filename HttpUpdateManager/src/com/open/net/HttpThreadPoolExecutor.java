package com.open.net;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class HttpThreadPoolExecutor extends ThreadPoolExecutor {

    /** 线程池维护线程的最少数量 */
    private static final int DEFAULT_CORE_POOL_SIZE = 5;
    private static final int DEFAULT_MAXIMUM_POOL_SIZE = 10;
    /** 线程池维护线程所允许的空闲时间 */
    private static final int DEFAULT_KEEP_ALIVETIME = 0;

    private static HttpThreadPoolExecutor sAliThreadPoolExecutor;
    private HttpThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
            BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
    }

    public static HttpThreadPoolExecutor getInstance() {
        if (sAliThreadPoolExecutor == null) {
            sAliThreadPoolExecutor = new HttpThreadPoolExecutor(DEFAULT_CORE_POOL_SIZE,
                    DEFAULT_MAXIMUM_POOL_SIZE, DEFAULT_KEEP_ALIVETIME,
                    TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(3),
                    new HttpThreadPoolExecutor.CallerRunsPolicy());
        }

        return sAliThreadPoolExecutor;
    }
}
