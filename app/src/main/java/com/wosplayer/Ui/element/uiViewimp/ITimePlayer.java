package com.wosplayer.Ui.element.uiViewimp;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;

import com.wosplayer.Ui.element.definedView.Mtimes;
import com.wosplayer.Ui.element.interfaces.IPlayer;
import com.wosplayer.Ui.element.interfaces.TimeCalls;
import com.wosplayer.app.DataList;
import com.wosplayer.app.Logs;

import static com.wosplayer.app.CrashHandler.TAG;

/**
 * Created by user on 2017/3/28.
 */

public class ITimePlayer implements IPlayer {
    private Mtimes tis;
    private ViewGroup viewGroup = null;
    private boolean isLayout = false;
    public ITimePlayer(Context context, ViewGroup vp){
        this.viewGroup = vp;
        //创建 surfaceview
        tis = new Mtimes(context);
    }
    @Override
    public void loadData(DataList mp) {
        //属性参数
        int x,y,w,h;
        x = mp.GetIntDefualt("x", 0);
        y = mp.GetIntDefualt("y", 0);
        w = mp.GetIntDefualt("width", 0);
        h = mp.GetIntDefualt("height", 0);
        AbsoluteLayout.LayoutParams layoutParams = new AbsoluteLayout.LayoutParams(w,h,x,y);
        tis.setLayoutParams(layoutParams);

        //背景色
        String bgcolor = mp.GetStringDefualt("bgcolor","#FFFFFF");
        //背景透明度
        double bgalpha = mp.GetdoubleDefualt("bgalpha",0.0);
        //文本颜色
        String fontcolor = mp.GetStringDefualt("fontcolor","#000000");
        //文本透明度
        double fontalpha = mp.GetdoubleDefualt("fontalpha",1.0);
        //文本大小
        int fontsize = mp.GetIntDefualt("fontsize",20)*3;
        tis.setBgAlpha(bgalpha);
        tis.setBgColor(bgcolor);
        tis.setTimeSize(fontsize);
        tis.setTimeAlpha(fontalpha);
        tis.setTimeColor(fontcolor);

    }

    @Override
    public void start() {
        try{
            if (!isLayout){
                //添加到上面去
                viewGroup.addView(tis);
                isLayout = true;
            }
        }catch (Exception e){
            Logs.e(TAG, "start :"+ e.getMessage());
        }
    }

    @Override
    public void stop() {
        try{
            if (isLayout){
                viewGroup.removeView(tis);
                isLayout=false;
            }
        }catch (Exception e){
            Logs.e(TAG, "stop :"+ e.getMessage());
        }
    }

    @Override
    public void setTimerCall(TimeCalls timer) {

    }
}
