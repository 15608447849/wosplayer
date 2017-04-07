package com.wosplayer.Ui.element.definedView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.wosplayer.app.DisplayActivity;
import com.wosplayer.app.Logs;

import java.lang.ref.WeakReference;

/**
 * Created by 79306 on 2017/3/9.
 */

public class MWeb extends android.webkit.WebView{
    private static final String TAG = "网页";
    private WebChromeClient wCclient;
    private WebViewClient wVclient;
    public MWeb(Context context) {
      this(context,null);
    }
    public MWeb(Context context,WebChromeClient wCclient) {
     this(context,wCclient,null);
    }
    public MWeb(Context context,WebChromeClient wCclient,WebViewClient wVclient) {
        super(context);
        this.wCclient = wCclient;
        this.wVclient = wVclient;
        initParam();
    }
    private void initParam() {
//        Log.e(TAG,"初始化"+this);
        /*
         • LAYER_TYPE_NONE:view按一般方式绘制，不使用离屏缓冲．这是默认的行为．
         • LAYER_TYPE_HARDWARE:如果应用被硬加速了，view会被绘制到一个硬件纹理中．如果应用没被硬加速，此类型的layer的行为同于LAYER_TYPE_SOFTWARE．
         • LAYER_TYPE_SOFTWARE:view被绘制到一个bitmap中
         */
        this.setLayerType(View.LAYER_TYPE_HARDWARE, null);//关闭软件加速
//        if (this.isHardwareAccelerated()){
//            Log.i(TAG, "硬件加速中");
//        }else {
//            Log.i(TAG, "非硬件加速");
//        }
        this.setBackgroundColor(Color.TRANSPARENT);
        this.onResume();
        WebSettings webSettings = this.getSettings();
        webSettings.setJavaScriptEnabled(true);//js
        webSettings.setAllowFileAccess(true); //设置可以访问文件
        webSettings.setAllowFileAccessFromFileURLs(true);// js读取本地文件内容
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true); //支持通过JS打开新窗口

        webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);  //提高渲染的优先级

        webSettings.setDefaultTextEncodingName("UTF-8");//设置编码格式
        webSettings.setSupportZoom(false); //支持缩放，默认为true
        webSettings.setBuiltInZoomControls(false); //设置内置的缩放控件。若为false，则该WebView不可缩放
        webSettings.setDisplayZoomControls(true); //隐藏原生的缩放控件

        //设置自适应屏幕，两者合用
        webSettings.setUseWideViewPort(true); //将图片调整到适合webview的大小
        webSettings.setLoadWithOverviewMode(true); // 缩放至屏幕的大小

        webSettings.setAppCacheEnabled(true); //启用应用缓存
        webSettings.setDomStorageEnabled(true); //启用或禁用DOM缓存。
        webSettings.setDatabaseEnabled(true); //启用或禁用DOM缓存。
        //缓存模式如下：
        //LOAD_CACHE_ONLY: 不使用网络，只读取本地缓存数据
        //LOAD_DEFAULT: （默认）根据cache-control决定是否从网络上取数据。
        //LOAD_NO_CACHE: 不使用缓存，只从网络获取数据.
        //LOAD_CACHE_ELSE_NETWORK，只要本地有，无论是否过期，或者no-cache，都使用缓存中的数据
        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);

        webSettings.setLoadsImagesAutomatically(true); //支持自动加载图片

        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN); //支持内容重新布局
        webSettings.supportMultipleWindows();  //多窗口
        //webSettings.setPluginsEnabled(true);  //支持插件
//        webSettings.setPluginState(WebSettings.PluginState.ON);//flash 有关系
        this.setWebChromeClient(wCclient==null?new WebChromeClient():wCclient);
        this.setWebViewClient(wVclient==null?new WebViewClient():wVclient);

    }

    public void killSelf(){
//        Log.e(TAG,"销毁中"+this);
//        this.loadUrl("about:blank");
        this.stopLoading();
        this.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
        this.clearHistory();//清除当前webview访问的历史记录
        this.clearCache(true);//由于内核缓存是全局的因此这个方法不仅仅针对webview而是针对整个应用程序.
        this.clearFormData();//清除自动完成填充的表单数据
        this.clearView();
        this.freeMemory();
//        this.pauseTimers();
        this.onPause();
        ((ViewGroup) this.getParent()).removeView(this);
        this.destroy();
    }









    public static class MwebChrome extends WebChromeClient{
        WeakReference<ProgressBar> progress;

        public MwebChrome(ProgressBar progress) {
            this.progress =  new WeakReference<ProgressBar>(progress);
        }

        @Override
            public void onConsoleMessage(String message, int lineNumber, String sourceID) {
                //Logs.e("控制台1", message + "(" +sourceID  + ":" + lineNumber+")");
                //super.onConsoleMessage(message, lineNumber, sourceID);
            }

            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
//                Logs.e("控制台2", "["+consoleMessage.messageLevel()+"] "+ consoleMessage.message() + "(" +consoleMessage.sourceId()  + ":" + consoleMessage.lineNumber()+")");
//                boolean flag = super.onConsoleMessage(consoleMessage);
//                Logs.e("控制台2",flag+"");
                return true;
            }
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (progress.get()==null) return;
                if(newProgress==100 ){
                    progress.get().setVisibility(View.GONE);//加载完网页进度条消失
                }
                else{
                    progress.get().setVisibility(View.VISIBLE);//开始加载网页时显示进度条
                    progress.get().setProgress(newProgress);//设置进度值
                }
            }

    }








}
