package com.wosplayer.Ui.element.uiViewimp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AbsoluteLayout;

import com.wosplayer.Ui.element.interfaces.IPlayer;
import com.wosplayer.Ui.element.interfaces.TimeCalls;
import com.wosplayer.app.DataList;
import com.wosplayer.app.Logs;
import com.wosplayer.command.kernal.CommandCenter;

/**
 * Created by Administrator on 2016/7/24.
 *
 */
public class IWebPlayer implements IPlayer {
    private static final java.lang.String TAG = "网页显示器";
    private android.webkit.WebView webView;
    private Context context;
    private ViewGroup superView = null;
    AbsoluteLayout.LayoutParams layoutParams;
    private boolean isLayout = false;

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
            layoutParams= new AbsoluteLayout.LayoutParams(w,h,x,y);
//            this.setLayoutParams();
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
        Logs.d(TAG,"拉取资源文件，发送本地指令FFBK");
        Intent intent = new Intent();
        intent.setAction(CommandCenter.action);
        Bundle b = new Bundle();
        b.putString(CommandCenter.cmd,"FFBK:");
        b.putString(CommandCenter.param,url);
        intent.putExtras(b);
        context.sendBroadcast(intent);
    }
    private void undestory(){
        if (webView!=null){
            webView.clearHistory();
            webView.clearCache(true);
            webView.loadUrl("about:blank");
            webView.clearView();
            webView.freeMemory();
            webView.pauseTimers();
            webView.destroy();
            webView = null;
            Log.i(TAG, "销毁WebView.");
        }
    }
    //初始化webview参数
    private void initParam() {
        webView = new WebView(context);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);//js
        webSettings.setAllowFileAccess(true); //设置可以访问文件
        webSettings.setAllowFileAccessFromFileURLs(true);// js读取本地文件内容

        webSettings.setSupportZoom(false); //支持缩放，默认为true
        webSettings.setBuiltInZoomControls(false); //设置内置的缩放控件。若为false，则该WebView不可缩放
        webSettings.setDisplayZoomControls(true); //隐藏原生的缩放控件

        //设置自适应屏幕，两者合用
        webSettings.setUseWideViewPort(true); //将图片调整到适合webview的大小
        webSettings.setLoadWithOverviewMode(true); // 缩放至屏幕的大小

//        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK); //webview中缓存
        //缓存模式如下：
        //LOAD_CACHE_ONLY: 不使用网络，只读取本地缓存数据
        //LOAD_DEFAULT: （默认）根据cache-control决定是否从网络上取数据。
        //LOAD_NO_CACHE: 不使用缓存，只从网络获取数据.
        //LOAD_CACHE_ELSE_NETWORK，只要本地有，无论是否过期，或者no-cache，都使用缓存中的数据
        //不使用缓存:
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webSettings.setLoadsImagesAutomatically(true); //支持自动加载图片
 //       webView.getSettings().setPluginState(WebSettings.PluginState.ON);//flash 有关系
        webSettings.setDefaultTextEncodingName("UTF-8");//设置编码格式
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient());
        webView.getSettings().setLoadWithOverviewMode(true);
        Log.i(TAG, "创建WebView.");
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
            if (!isLayout) {
                initParam();
                webView.setLayoutParams(layoutParams);
                superView.addView(webView);
                isLayout = true;
            }
            webView.loadUrl(this.uri);
        } catch (Exception e) {
            Logs.e(TAG, "web start():" + e.getMessage());
        }
    }

    @Override
    public void stop() {//主线程执行
        try {
            if (isLayout){
                superView.removeView(webView);
                undestory();
                isLayout = false;
            }
        } catch (Exception e) {
            Logs.e(TAG, "web stop():" + e.getMessage());
        }
    }
}
