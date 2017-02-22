package com.wosplayer.app;

import android.os.Process;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by 79306 on 2017/2/21.
 */

public class BackRunner {

    public static final ThreadFactory THREAD_FACTORY = new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setPriority(Process.THREAD_PRIORITY_DEFAULT+Process.THREAD_PRIORITY_LESS_FAVORABLE);
            return thread;
        }
    };

    private static ThreadPoolExecutor mThreadpool = new ThreadPoolExecutor(0,Runtime.getRuntime().availableProcessors(),2, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(),THREAD_FACTORY);

    public static void runBackground(Runnable runnable){
        mThreadpool.execute(runnable);
    }
    public static void removeBackground(Runnable runnable){
        mThreadpool.remove(runnable);
    }
}
