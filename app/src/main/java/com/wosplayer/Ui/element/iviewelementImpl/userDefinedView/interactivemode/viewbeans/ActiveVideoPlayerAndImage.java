package com.wosplayer.Ui.element.iviewelementImpl.userDefinedView.interactivemode.viewbeans;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;
import android.widget.Button;
import android.widget.FrameLayout;

import com.wosplayer.Ui.element.iviewelementImpl.IinteractionPlayer;
import com.wosplayer.Ui.element.iviewelementImpl.userDefinedView.interactivemode.IviewPlayer;
import com.wosplayer.activity.DisplayActivity;
import com.wosplayer.app.log;
import com.wosplayer.loadArea.excuteBolock.Loader;
import com.wosplayer.loadArea.otherBlock.fileUtils;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;

/**
 * Created by user on 2016/7/4.
 */
public class ActiveVideoPlayerAndImage extends AbsoluteLayout implements IviewPlayer,Loader.LoaderCaller {

    private static final java.lang.String TAG = ActiveVideoPlayerAndImage.class.getName();
    private Context mcontext ;
    //构造
    public ActiveVideoPlayerAndImage(Context context, String imageUri, String imageLoalPath, String uriPath, String localPath) {
        super(context);
        mcontext = context;
        InitSettting(imageUri,imageLoalPath,uriPath,localPath);
    }

    /**
     * ------------------------------------------------我的方法------------------------------------------------------------------------------*
     */

    /**
     * 初始化
     */
    public void InitSettting(String imageUri, String imageLoalPath, String uriPath, String localPath){
        this.imageUriPath = imageUri;//视频第一帧地址
        this.imageLoalPath =imageLoalPath;

        this.UriPath = uriPath; //视频的 播放 地址
        this.videoFileLocalPath = localPath;

        //设置布局 属性　
        settingLayout(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 0, 0);
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
        LayoutParams params = new LayoutParams(w, h, x, y);
        this.setLayoutParams(params);
        this.setBackgroundColor(Color.RED);
    }

    private String imageUriPath;//视频第一帧图片
    private String imageLoalPath;//本地地址

    /**
     * 视频播放者
     */
    ActiveVideoPlayer vplayer ;

    private String UriPath;
    private String videoFileLocalPath;//播放的文件路径
    private ViewGroup mFather ; //父容器
    private Loader load ;//资源下载者
    private boolean isloading = false;//是否正在下载中
    private boolean isremove = false;//是否移除

    /**
     * 图片加载者
     */
    private ActiveImagePlayer imager;
    private boolean image_Src_isOK = false;//视频资源是否加载成功
    private OnClickListener ImaerClickEvent = new OnClickListener() {
        @Override
        public void onClick(View v) {

           DisplayActivity.activityContext.visibleLayoutDialog(!image_Src_isOK); //资源如果没有加载完 显示图层 传递true

            if (!image_Src_isOK){ //如果资源没有下载完  不加载视频
                return;
            }

           ViewGroup vp = DisplayActivity.frame_main;
            if (vplayer == null){
                vplayer = new ActiveVideoPlayer(DisplayActivity.activityContext,UriPath,videoFileLocalPath);
            }
            vplayer.addMeToFather(vp);
        }
    };

    /**
     * 自动加载资源
     */
    @Override
    public void AotuLoadingResource() {
        load.LoadingUriResource(UriPath,null);//视频资源
        isloading = true;
    }
    /**
     * 加载视图
     */
    private void loadingMyVideoView() {
        if (mFather == null) {
            return;
        }

        //图片 播放者  出来吧!!!
        if (imager == null){
            imager = new ActiveImagePlayer(mcontext,imageUriPath,imageLoalPath);

        }
        imager.addMeToFather(ActiveVideoPlayerAndImage.this);//添加到绝对布局
        Button btn = imager.getPlayVideoBtn();
        btn.setOnClickListener(ImaerClickEvent);


        if(isloading){//继续下载视频资源
            return;
        }
        //  查看本地
        if (fileUtils.checkFileExists(videoFileLocalPath)) {
            //存在
            Call(videoFileLocalPath);//回调 视频资源
        } else {
            //访问网络
            if (!isloading) {
                //不在下载中
                load.LoadingUriResource(UriPath,null);
            }
        }
        ;
    }
    /**
     * 释放资源
     */
    public void releasedResource() {

        if (imager != null){
            imager.removeMeToFather();
            imager = null;
        }
        if (vplayer != null){
            vplayer.removeMeToFather();
            vplayer = null;
        }

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
                        mFather.removeView(ActiveVideoPlayerAndImage.this);
                        mFather.addView(ActiveVideoPlayerAndImage.this);

                        if (returnBtn!=null){

                            mFather.removeView(ActiveVideoPlayerAndImage.this.returnBtn);
                            mFather.addView(ActiveVideoPlayerAndImage.this.returnBtn);

                        }
                        isremove = false;
                    }
                });

                IinteractionPlayer.worker.schedule(new Action0() {
                    @Override
                    public void call() {
                        //异步加载视图
                        loadingMyVideoView();
                    }
                });

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
                    return;
                }
                isremove=true;
                AndroidSchedulers.mainThread().createWorker().schedule(new Action0() {
                    @Override
                    public void call() {
                        //容器是个绝对布局的话
                        mFather.removeView(ActiveVideoPlayerAndImage.this);
                        mFather = null;

                    }
                });

               IinteractionPlayer.worker.schedule(new Action0() {
                    @Override
                    public void call() {
                        //异步释放视图
                        releasedResource();
                    }
                });

            }
        }
    }

    /**
     * 资源回调
     * @param filePath
     */
    @Override
    public void Call(final String filePath) {
        log.i(TAG, "一个视频 资源 下载结果传递了来了:" + filePath);
        isloading = false; //下载完毕

        if (mFather == null) {
            return;
        }
        image_Src_isOK = true;
    }
}
