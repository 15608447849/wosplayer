package com.wosplayer.app;

/**
 * Created by 79306 on 2017/3/6.
 */

public class DisPlayInterface {

    //activity 与 fragment 通讯接口
    public interface onFragAction{
        void sendMessage(Object obj);
    }
}
