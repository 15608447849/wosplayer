package com.wosplayer.Ui.element.iviewelementImpl;

import android.content.Context;
import android.view.SurfaceView;
import android.view.ViewGroup;

import com.wosplayer.Ui.element.IPlayer;
import com.wosplayer.app.DataList;

/**
 * Created by user on 2016/7/26.
 */
public class ITextPlayer extends SurfaceView implements IPlayer {



    private static final java.lang.String TAG = ITextPlayer.class.getName();

    private Context mCcontext;
    private ViewGroup mfatherView = null;
    private int x=0;
    private int y=0;
    private int h=0;
    private int w=0;
    private boolean isExistOnLayout = false;



    private String bgcolor;//背景
    private String fontcolor;//前景
    private int FontSize;//文字大小
    private int speed;//速度
    private String Font;//字体
    private String text;//文本

    public ITextPlayer(Context context,ViewGroup mfatherView) {
        super(context);
        mCcontext =context;
        this.mfatherView = mfatherView;
    }





//-------------------------------------------------------------------------------------------------------

























    //-------------------------------------------------------------------------------------------------------
    @Override
    public void loadData(DataList mp) {

    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void setlayout() {

    }

    @Override
    public DataList getDatalist() {
        return null;
    }

    @Override
    public void Call(String filePath) {
        //not use
    }
}
