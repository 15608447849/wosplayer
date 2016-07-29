package com.wosplayer.Ui.element.iviewelementImpl.userDefinedView.interactivemode.viewbeans;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;
import android.widget.FrameLayout;

import com.wosplayer.Ui.element.iviewelementImpl.userDefinedView.MyVideoView;
import com.wosplayer.Ui.element.iviewelementImpl.userDefinedView.interactivemode.IviewPlayer;
import com.wosplayer.app.log;
import com.wosplayer.loadArea.excuteBolock.Loader;
import com.wosplayer.loadArea.otherBlock.fileUtils;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;

/**
 * Created by user on 2016/7/4.
 */
public class ActiveVideoPlayer extends AbsoluteLayout implements IviewPlayer,Loader.LoaderCaller {
    private static final java.lang.String TAG = ActiveVideoPlayer.class.getName();
    private MyVideoView video;
    private long mCurrentSeek;
    //构造
    public ActiveVideoPlayer(Context context, String uri, String localpath) {
        super(context);
        InitSettting(uri,localpath);
        video = new MyVideoView(context,this);
        video.setMyLayout(0,0,-1,-1);

    }



    private void mInitStart(String filePath) {
        video.loadRouce(filePath);
        video.start();
    }

    public void mStop(){
//      video.pause();
        video.stopMyPlayer();
    }

    public void mResume(){
        video.start();
    }



    /**
     * ------------------------------------------------我的方法------------------------------------------------------------------------------*
     */


    /**
     * 初始化
     */
    public void InitSettting(String uriPath, String localPath){

        this.UriPath = uriPath;
        this.videoFileLocalPath = localPath;

        //设置布局 属性　
        settingLayout(AbsoluteLayout.LayoutParams.MATCH_PARENT, AbsoluteLayout.LayoutParams.MATCH_PARENT, 0, 0);
        //初始化 资源加载者
        load = new Loader();
        load.settingCaller(this);
    }

    /**
     * 设置 layout
     */
    private int x;
    private int y;
    private int h;
    private int w;

    public void settingLayout(int width, int height, int x, int y) {
        this.w = width;
        this.h = height;
        this.x = x;
        this.y = y;

        //设置宽高坐标
        AbsoluteLayout.LayoutParams params = new AbsoluteLayout.LayoutParams(w, h, x, y);
        this.setLayoutParams(params);
        this.setBackgroundColor(Color.RED);
    }

    private String UriPath;
    private String videoFileLocalPath;//播放的文件路径
    private ViewGroup mFather ; //父容器
    private Loader load ;//资源下载者
    private boolean isloading = false;//是否正在下载中
    private boolean isremove = false;//是否移除
    /**
     * 自动加载资源
     */
    @Override
    public void AotuLoadingResource() {
        load.LoadingUriResource(UriPath,null);
        isloading = true;
    }
    /**
     * 加载视图
     */
    private void loadingMyVideoView() {
        if (mFather == null) {
            return;
        }
        if(isloading){//在下载
            return;
        }
        //  查看本地
        if (fileUtils.checkFileExists(videoFileLocalPath)) {
            //存在
            Call(videoFileLocalPath);
        } else {
            //访问网络
            if (!isloading) {
                load.LoadingUriResource(UriPath,null);
            }
        }
        ;
    }
    /**
     * 释放资源
     */
    public void releasedResource() {
//        mStop();
    }
    private FrameLayout returnBtn ;

    public void setMyreturnBtn(FrameLayout btn){
        returnBtn = btn;
    }
    /**
     * 添加到父容器
     * @param view
     */
    @Override
    public void addMeToFather(View view) {
        if (view != null){
            mFather = (ViewGroup) view;
        }
        if (mFather!=null){
            {
                //容器是个绝对布局的话
                AndroidSchedulers.mainThread().createWorker().schedule(new Action0() {
                    @Override
                    public void call() {
                        mFather.removeView(ActiveVideoPlayer.this);
                        mFather.addView(ActiveVideoPlayer.this);
                        if (returnBtn!=null){
                            mFather.removeView(ActiveVideoPlayer.this.returnBtn);
                            mFather.addView(ActiveVideoPlayer.this.returnBtn);
                        }
                        isremove = false;
                    }
                });

                        //异步加载视图
                        loadingMyVideoView();

            }
        }
    }

    /**
     * 从父容器中移除
     */
    @Override
    public void removeMeToFather() {
        if (mFather != null) {
            if (mFather instanceof AbsoluteLayout) {
                if(isremove){
                    log.e(TAG,"已經被移除");
                    return;
                }
                isremove=true;
                AndroidSchedulers.mainThread().createWorker().schedule(new Action0() {
                    @Override
                    public void call() {
                        //容器是个绝对布局的话
                        mFather.removeView(ActiveVideoPlayer.this);
                        mFather = null;

                    }
                });
                        //异步释放视图
                        releasedResource();

            }
        }
    }

    /**
     * 资源回调
     * @param filePath
     */
    @Override
    public void Call(final String filePath) {
        log.i(TAG, "videoplayer  一个视频 资源 下载结果传递了来了:" + filePath);
        isloading = false; //下载完毕
        if (mFather == null) {
            return;
        }

            //播放.
    AndroidSchedulers.mainThread().createWorker().schedule(new Action0() {
        @Override
        public void call() {
            mInitStart(filePath);
        }
    }) ;

    }




}
