package com.wosplayer.Ui.element.interfaces;

import com.wosplayer.app.DataList;

/**
 * Created by Administrator on 2016/7/24.
 */

public interface IPlayer{
     void loadData(DataList mp,Object ob);
     void start();
     void stop();
    void setTimerCall(TimeCalls timer);
}
