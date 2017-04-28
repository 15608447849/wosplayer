package com.standalone;

/**
 * Created by user on 2017/4/28.
 */

public interface MVideoInterface {

    void attch();
    void deattch();
    void startVideo(String url,VideoEvent event);
    void stopVideo();
    void releaseVideo();
    interface VideoEvent{
        void onComplete();
    }
}
