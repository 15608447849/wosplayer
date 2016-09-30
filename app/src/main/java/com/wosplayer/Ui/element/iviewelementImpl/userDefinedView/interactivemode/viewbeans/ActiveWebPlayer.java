package com.wosplayer.Ui.element.iviewelementImpl.userDefinedView.interactivemode.viewbeans;

import android.content.Context;
import android.graphics.Color;
import android.net.http.SslError;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AbsoluteLayout;
import android.widget.ImageButton;

import com.wosplayer.R;
import com.wosplayer.Ui.element.iviewelementImpl.IinteractionPlayer;
import com.wosplayer.Ui.element.iviewelementImpl.userDefinedView.interactivemode.IviewPlayer;
import com.wosplayer.app.log;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;

/**
 * Created by Administrator on 2016/6/29.
 */

public class ActiveWebPlayer extends WebView implements IviewPlayer {


    private static final java.lang.String TAG = ActiveWebPlayer.class.getName();
    private String uri;//主页
    private View mFather; //承载我的 容器视图

    public ActiveWebPlayer(Context context, String uri) {

        super(context);
        this.uri = uri;
        //设置我的　布局属性
        this.setLayoutParams(new AbsoluteLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT,0,0));

        View linerlayout = LayoutInflater.from(context).inflate(R.layout.webview_top_linear, null);
        linerlayout.setLayoutParams(
                new AbsoluteLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                        50,0,0));
        linerlayout.setBackgroundColor(Color.WHITE);
        initBtnViewEvent(linerlayout);
        InitParam();
        this.addView(linerlayout);
//        this.requestFocusFromTouch();//请求　触摸焦点　

    }

    private void initBtnViewEvent(View view) {
        //按钮 事件　初始化
        ImageButton back = (ImageButton) view.findViewById(R.id.back);
        back.setOnClickListener(ev);

        ImageButton go = (ImageButton) view.findViewById(R.id.go);
        go.setOnClickListener(ev);

        ImageButton refresh = (ImageButton) view.findViewById(R.id.refresh);
        refresh.setOnClickListener(ev);

        ImageButton home = (ImageButton) view.findViewById(R.id.home);
        home.setOnClickListener(ev);

        ImageButton close = (ImageButton) view.findViewById(R.id.close);
        close.setOnClickListener(ev);

    }

    OnClickListener ev = new OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            if (id == R.id.back) {
                ActiveWebPlayer.this.goBack();
            }
            if (id == R.id.go) {
                ActiveWebPlayer.this.goForward();
            }
            if (id == R.id.refresh) {
                ActiveWebPlayer.this.reload();

            }
            if (id == R.id.home) {
                ActiveWebPlayer.this.loadUrl(ActiveWebPlayer.this.uri);
            }
            if (id == R.id.close) {
             //   removeMeToFather();
            }
        }
    };


    /**
     * 添加到我的父视图
     */
    @Override
    public void addMeToFather(View Father){
        if(Father!=null){
            this.mFather = Father;
        }

        if (mFather!=null){
            if (mFather instanceof AbsoluteLayout){
                //容器是个绝对布局的话
                AndroidSchedulers.mainThread().createWorker().schedule(new Action0() {
                    @Override
                    public void call() {
                        ((AbsoluteLayout)mFather).removeView(ActiveWebPlayer.this);
                        ((AbsoluteLayout)mFather).addView(ActiveWebPlayer.this);
                        isRemove=false;
                    }
                });

                AotuLoadingResource();
            }
        }
    }

    @Override
    public void addMeToFather(View view, boolean f) {
        //NULL
        addMeToFather(view);
    }

    boolean isRemove = false;
    /**
     * 从父视图把自己移除
     */
    public void removeMeToFather(){
        if (isRemove){
            return;
        }

        if (mFather!=null){
            if (mFather instanceof AbsoluteLayout){
                isRemove=true;
                //容器是个绝对布局的话
                AndroidSchedulers.mainThread().createWorker().schedule(new Action0() {
                    @Override
                    public void call() {
                        ((AbsoluteLayout)mFather).removeView(ActiveWebPlayer.this);
                        if (mFather instanceof IinteractionPlayer){
                            ((IinteractionPlayer)mFather).returnPrevionsView();
                        }

                        mFather = null;
                    }
                });

            }
        }
    }

    @Override
    public void removeMeToFather(boolean f) {

    }

    @Override
    public void AotuLoadingResource() { //自动加载试图
//        InitParam();
    }

    private void InitParam() {
        if (uri==null){
            return;
        }

        this.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(android.webkit.WebView view,
                                                    String url) {
                try {
                    view.loadUrl(url);// 不开启新的浏器页面
                } catch (Exception e) {
                    log.e(TAG, "--------------------." + e.getMessage());
                }
                return true;
            }

            @Override
            public void onReceivedSslError(android.webkit.WebView view,
                                           SslErrorHandler handler, SslError error) {
                super.onReceivedSslError(view, handler, error);
                try {
                    log.e(TAG, "onReceivedSslError0." + error.toString());
                    handler.cancel(); // 默认的处理方式，WebView变成空白页
                } catch (Exception e) {
                    log.e(TAG, "onReceivedSslError1." + e.getMessage());
                }
            }
        });

        this.getSettings().setJavaScriptEnabled(true);//java
        this.getSettings().setPluginState(WebSettings.PluginState.ON);
        this.setWebChromeClient(new WebChromeClient());
        this.getSettings().setDefaultTextEncodingName("UTF-8");
        this.getSettings().setLoadWithOverviewMode(true);
        uri = uri.startsWith("http://")? uri : "http://"+uri;
        this.loadUrl(uri);
    }

}
