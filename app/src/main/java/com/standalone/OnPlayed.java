package com.standalone;

/**
 * Created by user on 2017/4/27.
 */

public interface OnPlayed {
    void runMainThread(Runnable task);
    void onFinished();
    void onBroad(String var);
}
