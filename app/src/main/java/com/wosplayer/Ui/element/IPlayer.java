package com.wosplayer.Ui.element;

import com.wosplayer.app.DataList;
import com.wosplayer.loadArea.excuteBolock.Loader;

/**
 * Created by Administrator on 2016/7/24.
 */

public interface IPlayer extends Loader.LoaderCaller {

    public void loadData(DataList mp);
    public void start();
    public void stop();
    public void setlayout ();
    public DataList getDatalist();

}