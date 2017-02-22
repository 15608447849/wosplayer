package com.wosplayer.Ui.element.iviewelementImpl;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebViewClient;

import com.wosplayer.Ui.element.IPlayer;
import com.wosplayer.Ui.performer.TimeCalls;
import com.wosplayer.app.DataList;
import com.wosplayer.app.Logs;
import com.wosplayer.cmdBroadcast.CmdPostTaskCenter;

/**
 * Created by Administrator on 2016/7/24.
 *
 */
public class IWebPlayer extends android.webkit.WebView implements IPlayer {
    private static final java.lang.String TAG = IWebPlayer.class.getName();
    private Context context;
    private ViewGroup fView = null;
    private int x = 0;
    private int y = 0;
    private int h = 0;
    private int w = 0;
    private boolean isLayout = false;

    public IWebPlayer(Context context, ViewGroup mfatherView) {
        super(context);
        this.context = context;
        this.fView = mfatherView;
    }
    private String uri = null;
    @Override
    public void loadData(DataList mp, Object ob) {
        try {
            this.x = mp.GetIntDefualt("x", 0);
            this.y = mp.GetIntDefualt("y", 0);
            this.w = mp.GetIntDefualt("width", 0);
            this.h = mp.GetIntDefualt("height", 0);

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
            initParam();
        } catch (Exception e) {
            Logs.e(TAG, "loaddata() " + e.getMessage());
        }
    }
    //发送广播 - > 到指令
    private void sendFFBK(String url){
        if (url.isEmpty()) return;
        Logs.d(TAG,"拉取资源文件，发送本地指令FFBK");
        Intent intent = new Intent();
        intent.setAction(CmdPostTaskCenter.action);
        Bundle b = new Bundle();
        b.putString(CmdPostTaskCenter.cmd,"FFBK:");
        b.putString(CmdPostTaskCenter.param,url);
        intent.putExtras(b);
        context.sendBroadcast(intent);
    }
    //初始化webview参数
    private void initParam() {
        WebSettings webSettings = this.getSettings();
        webSettings.setJavaScriptEnabled(true);//js
        webSettings.setAllowFileAccessFromFileURLs(true);// js读取本地文件内容
 //       this.getSettings().setPluginState(WebSettings.PluginState.ON);//flash 有关系
        webSettings.setDefaultTextEncodingName("UTF-8");//编码
        this.setWebChromeClient(new WebChromeClient());
        this.setWebViewClient(new WebViewClient());
        this.getSettings().setLoadWithOverviewMode(true);
    }
    @Override
    public void setlayout() {
        try {
            if (!isLayout) {
                fView.addView(this);
                isLayout = true;
            }
            LayoutParams lp = (LayoutParams) this.getLayoutParams();
            lp.x = x;
            lp.y = y;
            lp.width = w;
            lp.height = h;
            this.setLayoutParams(lp);
        } catch (Exception e) {
            Logs.e(TAG, "setlayout() " + e.getMessage());
        }
    }

    @Override
    public DataList getDatalist() {
        return null;
    }

    //时间回调
    private TimeCalls timeCalls = null;

    @Override
    public void setTimerCall(TimeCalls timer) {
        timeCalls = timer;
    }

    @Override
    public void unTimerCall() {
        timeCalls = null;
    }

    @Override
    public void start() {//主线程执行
        try {
            setlayout();
            this.onResume();
            this.loadUrl(this.uri);
        } catch (Exception e) {
            Logs.e(TAG, "web start():" + e.getMessage());
        }
    }

    @Override
    public void stop() {//主线程执行
        try {
            this.loadUrl("about:blank");
            this.onPause();
            fView.removeView(IWebPlayer.this);
            isLayout = false;
        } catch (Exception e) {
            Logs.e(TAG, "web stop():" + e.getMessage());
        }
    }
}
