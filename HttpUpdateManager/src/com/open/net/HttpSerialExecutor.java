package com.open.net;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.Executor;

public class HttpSerialExecutor implements Executor {
    private final Queue<Runnable> mTasksQueue = new ArrayDeque<Runnable>();
    private Executor mExecutor;
    private Runnable mIsActive;

    public Executor getExecutor() {
        return mExecutor;
    }
    
    public void setExecutor(Executor executor) {
        mExecutor = executor;
    }

    public synchronized void execute(final Runnable r) {
        mTasksQueue.offer(new Runnable() {
            public void run() {
                try {
                    r.run();
                } finally {
                    scheduleNext();
                }
            }
        });
        if (mIsActive == null) {
            scheduleNext();
        }
    }

    private synchronized void scheduleNext() {
        if ((mIsActive = mTasksQueue.poll()) != null) {
            mExecutor.execute(mIsActive);
        }
    }
}

