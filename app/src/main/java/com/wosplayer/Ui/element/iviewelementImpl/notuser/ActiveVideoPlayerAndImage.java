package com.wosplayer.Ui.element.iviewelementImpl.notuser;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;
import android.widget.Button;
import android.widget.FrameLayout;

import com.wosplayer.Ui.element.iviewelementImpl.IinteractionPlayer;
import com.wosplayer.Ui.element.iviewelementImpl.userDefinedView.interactivemode.IviewPlayer;
import com.wosplayer.Ui.element.iviewelementImpl.userDefinedView.interactivemode.viewbeans.ActiveImagePlayer;
import com.wosplayer.Ui.element.iviewelementImpl.userDefinedView.interactivemode.viewbeans.ActiveVideoPlayer;
import com.wosplayer.activity.DisplayActivity;
import com.wosplayer.app.log;
import com.wosplayer.loadArea.otherBlock.fileUtils;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;

/**
 * Created by user on 2016/7/4.
 */
public class ActiveVideoPlayerAndImage extends AbsoluteLayout implements IviewPlayer {

    private static final java.lang.String TAG = "_ActiveVideoPlayerAndImage";//ActiveVideoPlayerAndImage.class.getName();
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
    ActiveVideoPlayer vplayer = null;

    private String UriPath;
    private String videoFileLocalPath;//播放的文件路径
    private ViewGroup mFather ; //父容器

//    private boolean isloading = false;//是否正在下载中
    private boolean isremove = false;//是否移除

    /**
     * 图片加载者
     */
    private ActiveImagePlayer imager_One;
    private boolean image_Src_isOK = false;//视频资源是否加载成功
    private OnClickListener ImaerClickEvent = new OnClickListener() {
        @Override
        public void onClick(View v) {
            log.e("准备播放一个视频,当前资源状态:" + image_Src_isOK);
            if (mFather==null){
                return;
            }

            int iw = -1;
            int ih = -1;
            int ix = 0;
            int iy = 0;
            //获取互动模块的执行者
            if (mFather.getParent() instanceof ActiveViewPagers){
                if(((ActiveViewPagers)mFather.getParent()).getParent() instanceof IinteractionPlayer){
                    AbsoluteLayout.LayoutParams lp = (AbsoluteLayout.LayoutParams) ((IinteractionPlayer)((ActiveViewPagers)mFather.getParent()).getParent()).getLayoutParams();
                    iw = lp.width;
                    ih = lp.height;
                    ix = lp.x;
                    iy = lp.y;
                }
            }

           AbsoluteLayout.LayoutParams param = new AbsoluteLayout.LayoutParams(iw,ih,ix,iy);
//           param.gravity = Gravity.CENTER;

            if (!image_Src_isOK){ //如果资源没有下载完  不加载视频
                log.e(TAG,"--- 资源没有加载完 ---");
                DisplayActivity.activityContext.visibleLayoutDialog(true,param); //资源如果没有加载完 显示图层 传递true
                return;
            }
           ViewGroup vp = DisplayActivity.frame_main;
            log.e("this Vp:"+mFather.getMeasuredHeight());
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

    }
    private Button playbtn = null;
    /**
     * 加载视图
     */
    private void loadingMyVideoView() {
        if (mFather == null) {
            log.e(TAG,"loadingMyVideoView father is null");
            return;
        }
        //图片 播放者  出来吧!!!
        if (imager_One == null){
            log.d(TAG,"制作第一帧图片");
            imager_One = new ActiveImagePlayer(mcontext,imageUriPath,imageLoalPath);//加载图片
            imager_One.addMeToFather(ActiveVideoPlayerAndImage.this);//添加到绝对布局
        }
        if (playbtn==null){
            playbtn = imager_One.getPlayVideoBtn();//播放按钮
            playbtn.setOnClickListener(ImaerClickEvent);//播放事件
        }

        //  查看本地
        if (fileUtils.checkFileExists(videoFileLocalPath)) {
            //存在
            image_Src_isOK = true;
        } else {
            image_Src_isOK = false;
            log.e(TAG,"资源不存在");
        }
    }
    /**
     * 释放资源
     */
    public void releasedResource() {
        log.e(TAG,"释放资源中... ");
        if (imager_One != null){
            imager_One.removeMeToFather();
            imager_One = null;
        }
        if (playbtn!=null){
            playbtn = null;
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
                        loadingMyVideoView();
                    }
                });
            }
        }
    }

    @Override
    public void addMeToFather(View view, boolean f) {
        //null
        addMeToFather(view);
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

    @Override
    public void removeMeToFather(boolean f) {

    }

    @Override
    public int getPlayDration(IviewPlayer iviewPlayer) {
        return 1;
    }

    @Override
    public void otherMother(Object object) {

    }


}
