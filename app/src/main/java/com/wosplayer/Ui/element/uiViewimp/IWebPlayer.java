package com.wosplayer.Ui.element.uiViewimp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AbsoluteLayout;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.wosplayer.R;
import com.wosplayer.Ui.element.definedView.MWeb;
import com.wosplayer.Ui.element.interfaces.IPlayer;
import com.wosplayer.Ui.element.interfaces.TimeCalls;
import com.wosplayer.app.DataList;
import com.wosplayer.app.Logs;
import com.wosplayer.command.kernal.CommandCenter;

import java.util.zip.Inflater;

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
    public IWebPlayer(Context context, ViewGroup mfatherView) {
        this.context = context;
        this.superView = mfatherView;
    }
    private String uri = null;
    @Override
    public void loadData(DataList mp, Object ob) {
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
            this.uri = mp.GetStringDefualt("fudianpath","");
            if (this.uri.isEmpty()){
                //http链接
                this.uri = mp.GetStringDefualt("getcontents","http://www.winonetech.com/");
                this.uri = uri.startsWith("http")?uri:"http://" + this.uri;
            }else{
                this.uri = "file://"+uri+"index.html";
                //富癫银行项目
                String remoteResourcePath = mp.GetStringDefualt("getcontents","");
                //发送指令
                sendFFBK(remoteResourcePath);
            }

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
