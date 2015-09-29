
package com.open.net;

import android.content.Context;
import android.text.TextUtils;

import java.io.File;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Executor;

import com.open.utils.Log;

public class HttpDownloader {
    
    private static final String TAG = "DownloadManager";
    private static final String DOWNLOAD_TASK_QUEUE_FULL = "下载任务列表已满";

    private static final int MAX_TASK_COUNT = 100;
    private static final String SDCARD_ROOT = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
    public static String FILE_ROOT = SDCARD_ROOT + File.separator + "download";

    private volatile static HttpDownloader sDownloadManager;
    private HttpTaskExecutor mHttpTaskExecutor;
    private String mRootPath = "";

    public static HttpDownloader getInstance(Context context) {
        if (sDownloadManager == null) {
            synchronized (HttpDownloader.class) {
                if (sDownloadManager == null) {
                    sDownloadManager = new HttpDownloader(context, FILE_ROOT);
                }
            }
        }
        return sDownloadManager;
    }

    /**
     * Constructor function
     * @param context
     * @param rootPath
     */
    private HttpDownloader(Context context, String rootPath) {
        try {
            mRootPath = rootPath;
            mHttpTaskExecutor = new HttpTaskExecutor(new Executor() {
                @Override
                public void execute(Runnable task) {
                    HttpThreadPoolExecutor.getInstance().submit(task);
                }
            });
            
            if (rootPath != null && rootPath.length() > 0) {
                File rootFile = new File(rootPath);
                if (!rootFile.exists()) {
                    boolean result = rootFile.mkdirs();
                    if (!result) {
                        if (rootPath.indexOf("sdcard0") != -1) {
                            rootPath = rootPath.replace("sdcard0", "sdcard1");
                            rootFile = new File(rootPath);
                            rootFile.mkdirs();
                            FILE_ROOT = FILE_ROOT.replace("sdcard0", "sdcard1");
                            mRootPath = rootPath;
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.i(TAG, "DownloadManager Exception "+ e.getMessage());
        }
    }

    /**
     * Add the downloading URL into the queue and execute the download task.
     * @param url
     * @param mDownLoadCallback
     */
    public void startDownload(String url, DownLoadCallback mDownLoadCallback) {
        if (TextUtils.isEmpty(url) || mHttpTaskExecutor.isTaskAlreadyInQueue(url)) {
            return;
        }

        if (mHttpTaskExecutor.getCurrentTaskSize() >= MAX_TASK_COUNT) {
            mDownLoadCallback.onFailure(DOWNLOAD_TASK_QUEUE_FULL);
        }

        mHttpTaskExecutor.execute(new HttpTaskWrapper(url, mRootPath,  mDownLoadCallback));
    }

    /**
     * Wrap the Runnable task
     */
    private class RunnableWrapper implements Runnable {
        public HttpTaskWrapper mTask = null;
        public HttpTaskExecutor mExecutor = null;
        public RunnableWrapper(HttpTaskWrapper task, HttpTaskExecutor executor) {
            mTask = task;
            mExecutor = executor;
        }

        @Override
        public void run() {
            try {
                mTask.run();
            } finally {
                mExecutor.scheduleNext();
            }
        }
    }
    
    /**
     * Start to execute HTTP task.
     */
    private class HttpTaskExecutor {
        private Queue<RunnableWrapper> mTasksQueue;
        private Runnable mCurrentTask = null; 
        private Executor mExecutor = null;
        
        public HttpTaskExecutor(Executor executor) {
            mTasksQueue = new LinkedList<RunnableWrapper>();
            mExecutor = executor;
        }
        
        public synchronized void execute(final HttpTaskWrapper task) {
            synchronized(mTasksQueue) {
                mTasksQueue.offer(new RunnableWrapper(task, this));
                if (mCurrentTask == null) {
                    scheduleNext();
                }
            }
        }

        protected synchronized void scheduleNext() {
            synchronized(mTasksQueue) {
                mCurrentTask = mTasksQueue.poll();
            }
            if (mCurrentTask != null) {
                mExecutor.execute(mCurrentTask);
            }
        }
        
        protected boolean isTaskAlreadyInQueue(String url) {
            synchronized(mTasksQueue) {
                for (int i = 0; i < mTasksQueue.size(); i++) {
                    RunnableWrapper wrapper  = ((LinkedList<RunnableWrapper>) mTasksQueue).get(i);
                    if (wrapper.mTask != null && wrapper.mTask.getUrl() != null && wrapper.mTask.getUrl().equals(url)) {
                        return true;
                    }
                }
                return false;
            }
        }
        
        protected int getCurrentTaskSize() {
            synchronized(mTasksQueue) {
                return mTasksQueue.size();
            }
        }
    }
}
