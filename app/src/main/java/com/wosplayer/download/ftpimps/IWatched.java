package com.wosplayer.download.ftpimps;

/**
 * Created by user on 2017/4/17.
 */

public interface IWatched {
    void onStart();
    void onSuccess();
    void onFailt(Exception e);
    void transBuff(float progress,float speed);//进度 ,每秒下载量
}
