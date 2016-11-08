package com.wosplayer.Ui.element;

import com.wosplayer.app.DataList;
import com.wosplayer.loadArea.excuteBolock.LoaderCall;

/**
 * Created by Administrator on 2016/7/24.
 */

public interface IPlayer extends LoaderCall {

    public void loadData(DataList mp,Object ob);
    public void start();
    public void stop();
    public void setlayout ();
    public DataList getDatalist();

}
