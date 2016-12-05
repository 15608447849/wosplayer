package com.wosplayer.Ui.element.iviewelementImpl.userDefinedView.interactivemode.viewbeans;

import android.content.Context;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;

import com.wosplayer.Ui.element.iviewelementImpl.mycons_view.MyVideoView;
import com.wosplayer.Ui.element.iviewelementImpl.userDefinedView.interactivemode.IviewPlayer;
import com.wosplayer.app.WosApplication;
import com.wosplayer.app.log;
import com.wosplayer.loadArea.excuteBolock.Loader;
import com.wosplayer.loadArea.otherBlock.fileUtils;

/**
 * Created by user on 2016/7/4.
 */
public class ActiveVideoPlayer extends AbsoluteLayout implements IviewPlayer {
    private static final java.lang.String TAG = "_VIDEOPLAYER";//ActiveVideoPlayer.class.getName();
    private MyVideoView video;
    private long mCurrentSeek;

    //构造
    public ActiveVideoPlayer(Context context, String uri, String localpath) {
        super(context);
        log.e(TAG, "互动 视频播放者 创建");
        InitSettting(uri, localpath);
        video = new MyVideoView(context, this);
}

    //互动接口对象  回调对象 附加功能
    private IviewPlayer iviewPlayer = null;

    //初始化开始
    private void mInitStart(String filePath) {
        video.setMyLayout(0, 0, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);//设置布局
        video.initVideoView(false);//不循环
        video.setOnPreparedListener_(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                if (iviewPlayer != null) {
                    iviewPlayer.otherMother(mp.getDuration());
                }
            }
        });
        video.loadRouce(filePath);
        video.start();
    }

    public void mStop() {
        video.stopMyPlayer();
    }

    public void mResume() {
        video.start();
    }
    /**
     * ------------------------------------------------我的方法------------------------------------------------------------------------------*
     */


    /**
     * 初始化
     */
    public void InitSettting(String uriPath, String localPath) {

        this.UriPath = uriPath;
        this.videoFileLocalPath = localPath;

        //设置布局 属性　
        this.settingLayout(AbsoluteLayout.LayoutParams.MATCH_PARENT, AbsoluteLayout.LayoutParams.MATCH_PARENT, 0, 0);
        log.e(TAG, "互动 视频 初始化:" + uriPath + "\n " + localPath);
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
    }

    private String UriPath;
    private String videoFileLocalPath;//播放的文件路径
    private ViewGroup mFather; //父容器
    private Loader load;//资源下载者
    private boolean isloading = false;//是否正在下载中
    private boolean isremove = false;//是否移除

    /**
     * 自动加载资源
     */
    @Override
    public void AotuLoadingResource() {

    }

    /**
     * 加载视图
     */
    private void loadingMyVideoView() {
        if (mFather == null) {
            log.e(TAG, "video viewGroup is null");
            return;
        }
        //  查看本地
        if (fileUtils.checkFileExists(videoFileLocalPath)) {
            //存在
            mInitStart(videoFileLocalPath);
        } else {
            log.e(TAG, "视频资源不存在 - " + videoFileLocalPath);
            String del = WosApplication.getConfigValue("defaultVideo");
            if (!del.equals("")) {
                mInitStart(del);
            }
        }
    }

    /**
     * 释放资源
     */
    public void releasedResource() {
        mStop();
    }





    /**
     * 添加到父容器
     *
     * @param view
     */
    @Override
    public void addMeToFather(View view) {
        if (view != null) {
            mFather = (ViewGroup) view;
        }
        if (mFather != null) {
            {
                //容器是个绝对布局的话
//                AndroidSchedulers.mainThread().createWorker().schedule(new Action0() {
//                    @Override
//                    public void call() {
//                      mFather.removeView(ActiveVideoPlayer.this);
                        ActiveVideoPlayer.this.setBackgroundColor(Color.RED);
                        mFather.addView(ActiveVideoPlayer.this);
                        isremove = false;
                        log.e(TAG, "互动 video player 添加到 视图 ,video 还没有添加");
                        //加载视图
                        loadingMyVideoView();
//                    }
//                });
            }
        }
    }

    @Override
    public void addMeToFather(View view, boolean f) {
        //NULL
    }

    /**
     * 从父容器中移除
     */
    @Override
    public void removeMeToFather() {
        if (mFather != null) {
            if (mFather instanceof AbsoluteLayout) {
//                AndroidSchedulers.mainThread().createWorker().schedule(new Action0() {
//                    @Override
//                    public void call() {
                        if (isremove) {
                            log.e(TAG, "已經被移除");
                            return;
                        }
                        //释放视图
                        releasedResource();
                        //容器是个绝对布局的话
                        mFather.removeView(ActiveVideoPlayer.this);
                        mFather = null;
                        isremove = true;
//                    }
//                });

            }
        }
    }

    @Override
    public void removeMeToFather(boolean f) {

    }

    //获取 时长
    @Override
    public int getPlayDration(IviewPlayer iviewPlayer) {
        this.iviewPlayer = iviewPlayer;
        return -1;
    }

    @Override
    public void otherMother(Object object) {
    }


}
