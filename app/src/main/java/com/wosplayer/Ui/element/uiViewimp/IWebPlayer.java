package com.wosplayer.Ui.element.uiViewimp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.wosplayer.R;
import com.wosplayer.Ui.element.definedView.MWeb;
import com.wosplayer.Ui.element.interfaces.IPlayer;
import com.wosplayer.Ui.element.interfaces.TimeCalls;
import com.wosplayer.Ui.performer.UiExcuter;
import com.wosplayer.app.DataList;
import com.wosplayer.app.Logs;
import com.wosplayer.command.kernal.CommandCenter;

/**
 * Created by Administrator on 2016/7/24.
 *
 */
public class IWebPlayer implements IPlayer {
    private static final java.lang.String TAG = "网页显示器";
    private MWeb web;
    private Context context;
    private ViewGroup superView = null;
    AbsoluteLayout.LayoutParams layoutParams;
    private boolean isLayout = false;
    private ProgressBar progress;
    private FrameLayout flayout;
    private int timeLength = 0;//时长
    private Runnable callTo;
    public IWebPlayer(Context context, ViewGroup mfatherView) {
        this.context = context;
        this.superView = mfatherView;
    }
    private String uri = null;
    @Override
    public void loadData(DataList mp) {
        try {
            int x,y,w,h;
            x = mp.GetIntDefualt("x", 0);
            y = mp.GetIntDefualt("y", 0);
            w = mp.GetIntDefualt("width", 0);
            h = mp.GetIntDefualt("height", 0);
            flayout = (FrameLayout) LayoutInflater.from(context).inflate(R.layout.webprogress,null);
            progress = (ProgressBar) flayout.findViewById(R.id.progress);
            layoutParams= new AbsoluteLayout.LayoutParams(w,h,x,y);
            flayout.setLayoutParams(new AbsoluteLayout.LayoutParams(w,AbsoluteLayout.LayoutParams.WRAP_CONTENT,x,y));
//          this.setLayoutParams();
            //0 - 本地网页  1 -远程网页   2 -富滇项目
            int type = mp.GetIntDefualt("type",-1);
            if (type == 1){
                uri = mp.GetStringDefualt("url","http://www.winonetech.com/");

            }
            if (type == 2){
                uri = mp.GetStringDefualt("fudianpath","");
                //发送指令
                sendFFBK(mp.GetStringDefualt("resource",""));
            }
            timeLength = mp.GetIntDefualt("timelength",0);
        } catch (Exception e) {
            Logs.e(TAG, "loaddata() " + e.getMessage());
        }
    }
    //发送广播 - > 到指令
    private void sendFFBK(String url){
        if (url.isEmpty()) return;
        //Logs.d(TAG,"拉取资源文件，发送本地指令FFBK");
        Intent intent = new Intent();
        intent.setAction(CommandCenter.action);
        Bundle b = new Bundle();
        b.putString(CommandCenter.cmd,"FFBK:");
        b.putString(CommandCenter.param,url);
        intent.putExtras(b);
        context.sendBroadcast(intent);
    }



    @Override
    public void setTimerCall(TimeCalls timer) {
        final TimeCalls timeCalls = timer;
        callTo =  new Runnable() {
            @Override
            public void run() {
                if (timeCalls!=null){
                    timeCalls.playOvers(IWebPlayer.this);
                }
            }
        };
    }


    @Override
    public void start() {//主线程执行
        try {
            if (!isLayout) {
                web = new MWeb(context,
                        new MWeb.MwebChrome(progress),
                        null);
                web.setLayoutParams(layoutParams);
                superView.addView(web);
                superView.addView(flayout);
                web.loadUrl(this.uri);
                isLayout = true;
            }
            UiExcuter.getInstancs().runingMainDelayed(callTo, timeLength * 1000);
        } catch (Exception e) {
            Logs.e(TAG, "web start():" + e.getMessage());
        }
    }

    @Override
    public void stop() {//主线程执行
        try {
            if (isLayout){
                web.killSelf();
                superView.removeView(flayout);
                superView.removeView(web);
                isLayout = false;
            }
        } catch (Exception e) {
            Logs.e(TAG, "web stop():" + e.getMessage());
        }
    }
}
