package com.wosplayer.Ui.element;

import com.wosplayer.Ui.performer.TimeCalls;
import com.wosplayer.app.DataList;

/**
 * Created by Administrator on 2016/7/24.
 */

public interface IPlayer{

    public void loadData(DataList mp,Object ob);
    public void start();
    public void stop();
    public void setlayout ();
    public DataList getDatalist();
    void setTimerCall(TimeCalls timer);
    void unTimerCall();
}
