package com.wosplayer.Ui.element.iviewelementImpl;

import android.content.Context;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebViewClient;

import com.wosplayer.Ui.element.IPlayer;
import com.wosplayer.Ui.performer.TimeCalls;
import com.wosplayer.app.DataList;
import com.wosplayer.app.Logs;

/**
 * Created by Administrator on 2016/7/24.
 */

public class IWebPlayer extends android.webkit.WebView implements IPlayer {
    private static final java.lang.String TAG = IWebPlayer.class.getName();

    private Context mCcontext;
    private ViewGroup mfatherView = null;
    private int x = 0;
    private int y = 0;
    private int h = 0;
    private int w = 0;
    private boolean isExistOnLayout = false;

    public IWebPlayer(Context context, ViewGroup mfatherView) {
        super(context);
        mCcontext = context;
        this.mfatherView = mfatherView;
    }

    private DataList mp = null;
    private String uri = null;

    @Override
    public void loadData(DataList mp, Object ob) {
        try {
            this.mp = mp;
            this.x = mp.GetIntDefualt("x", 0);
            this.y = mp.GetIntDefualt("y", 0);
            this.w = mp.GetIntDefualt("width", 0);
            this.h = mp.GetIntDefualt("height", 0);
            this.uri = mp.GetStringDefualt("getcontents",
                    "http://www.winonetech.com/");
           this.uri = uri.startsWith("http")?uri:"http://" + this.uri;



            initParam();
        } catch (Exception e) {
            Logs.e(TAG, "loaddata() " + e.getMessage());
        }
    }

    private void initParam() {
        this.getSettings().setJavaScriptEnabled(true);//js
        this.getSettings().setPluginState(WebSettings.PluginState.ON);//flash 有关系
//      this.setWebChromeClient(new WebChromeClient());
        this.getSettings().setDefaultTextEncodingName("UTF-8");//编码
        this.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(android.webkit.WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                if (newProgress == 100) {
                    // 加载完成
//                    log.i(TAG, "页面加载完成");
                } else {
                    // 加载进度
//                    log.i(TAG, "页面加载中..." + newProgress);
                }
            }
        });
        this.setWebViewClient(new WebViewClient());
        this.getSettings().setLoadWithOverviewMode(true);
    }

    @Override
    public void setlayout() {
        try {
            if (!isExistOnLayout) {
                mfatherView.addView(this);
                isExistOnLayout = true;
            }

            LayoutParams lp = (LayoutParams) this
                    .getLayoutParams();
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
        return mp;
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
            this.loadUrl(this.uri);
        } catch (Exception e) {
            Logs.e(TAG, "web start():" + e.getMessage());
        }
    }

    @Override
    public void stop() {//主线程执行
        try {
            mfatherView.removeView(IWebPlayer.this);
            isExistOnLayout = false;
        } catch (Exception e) {
            Logs.e(TAG, "web stop():" + e.getMessage());
        }
    }
}
