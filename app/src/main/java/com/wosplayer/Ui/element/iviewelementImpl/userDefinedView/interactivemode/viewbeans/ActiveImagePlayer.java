package com.wosplayer.Ui.element.iviewelementImpl.userDefinedView.interactivemode.viewbeans;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.wosplayer.R;
import com.wosplayer.Ui.element.iviewelementImpl.IImagePlayer;
import com.wosplayer.Ui.element.iviewelementImpl.uitools.ImageViewPicassocLoader;
import com.wosplayer.Ui.element.iviewelementImpl.userDefinedView.interactivemode.IviewPlayer;
import com.wosplayer.app.log;
import com.wosplayer.loadArea.otherBlock.fileUtils;

import java.io.File;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;

/**
 * Created by user on 2016/6/30.
 */
public class ActiveImagePlayer extends ImageView implements IviewPlayer {


    private static final java.lang.String TAG = "_ActiveImagePlayer";//.class.getName()";
    private boolean isloading = false;//是否正在下载中
    private String uriPath;  //网络 uri 地址
    private String localPath; // 本地文件路径
    private Context mcontext;

    public ActiveImagePlayer(Context context, String uriPath, String localPath) {
        super(context);
        this.mcontext = context;
        this.uriPath = uriPath;
        this.localPath = localPath;

        //设置布局 属性　
        settingLayout(AbsoluteLayout.LayoutParams.MATCH_PARENT, AbsoluteLayout.LayoutParams.MATCH_PARENT, 0, 0);

        //设置 图片显示 方式
//        this.setScaleType(ImageView.ScaleType.FIT_XY);

        //初始化 资源加载者
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

    /**
     * 添加自己到父控件
     */
    private View mFather;
    public boolean isShow = true;

    @Override
    public void addMeToFather(View Father) {
        if (this.mFather != null) {
            ((AbsoluteLayout) mFather).removeView(this);
            this.mFather = null;
        }
        if (Father == null) {
           return;
        }
        this.mFather = Father;
        AndroidSchedulers.mainThread().createWorker().schedule(new Action0() {
            @Override
            public void call() {
                if (mFather != null) {
                    if (mFather instanceof AbsoluteLayout) {
                        //容器是个绝对布局的话
                        ((AbsoluteLayout) mFather).removeView(ActiveImagePlayer.this);
                        ((AbsoluteLayout) mFather).addView(ActiveImagePlayer.this);
                    }
                }
            }
        });
        if (isShow) {
            //异步加载视图
            loadingMyImageView();
        }
    }

    @Override
    public void addMeToFather(View view, boolean f) {
        isShow = false;
        addMeToFather(view);
    }


    /**
     * 从父控件 移除自己
     */
    @Override
    public void removeMeToFather() {

        if (mFather != null) {
            if (mFather instanceof ViewGroup) {


                AndroidSchedulers.mainThread().createWorker().schedule(new Action0() {
                    @Override
                    public void call() {
                        //容器是个绝对布局的话
                        try {
                            releativePlayBtn();//释放 播放 按钮
                            ((ViewGroup) mFather).removeView(ActiveImagePlayer.this);
                        } catch (Exception e) {
                            log.e(TAG, " " + mFather + "- " + this + " & " + e.getMessage());
                        }
                        mFather = null;
                        log.e(TAG, "视图移除自己成功");
                        //异步释放视图
                        releaseImageViewResouce();

                    }
                });


            }
        }
    }

    @Override
    public void removeMeToFather(boolean f) {


        AndroidSchedulers.mainThread().createWorker().schedule(new Action0() {
            @Override
            public void call() {
                //异步释放视图
                releaseImageViewResouce();
            }
        });


    }

    @Override
    public int getPlayDration(IviewPlayer iviewPlayer) {
        return 15 * 1000;
    }

    @Override
    public void otherMother(Object object) {

    }

    private boolean existLoaddingBg = false;
    Bitmap bitmap_Loading = null;

    /**
     * 加载视图
     */
    private void loadingMyImageView() {

        if (mFather == null) {
            return;
        }
        if (isloading) {

            AndroidSchedulers.mainThread().createWorker().schedule(new Action0() {
                @Override
                public void call() {
                    if (bitmap_Loading == null) {
                        try {
                            bitmap_Loading = BitmapFactory.decodeResource(ActiveImagePlayer.this.getResources(), R.drawable.loadding);
                        } catch (Exception e) {
                            log.e("下载 时 图片 异常:" + e.getMessage());
//                            return;
                            bitmap_Loading = BitmapFactory.decodeResource(ActiveImagePlayer.this.getResources(), R.drawable.error);
                        }
                    }
                    BitmapDrawable bd = new BitmapDrawable(ActiveImagePlayer.this.getResources(), bitmap_Loading);
                    ActiveImagePlayer.this.setImageDrawable(bd);
                    existLoaddingBg = true;
                }
            });


            return;
        }

        //  查看本地
        if (fileUtils.checkFileExists(localPath)) {
            //存在
            picassoLoaderImager(localPath);
        } else {
            log.e(TAG,"互动 图片 资源 不存在 - "+localPath +" \nurl "+uriPath );
            setImageResource(R.drawable.error);
        }
    }

    /**
     * 自动下载资源
     */
    @Override
    public void AotuLoadingResource() {

    }

    /**
     * 资源释放
     */
    private void releaseImageViewResouce() {
        log.i(TAG, "----------------------互动图片-------------清理资源---------------------Thread: " + Thread.currentThread().getName());
        IImagePlayer.removeMyImage(this);
        log.i(TAG, "----------------------互动图片 end-------------清理资源---------------------Thread: " + Thread.currentThread().getName());
    }



    /**
     * Android中有四种，分别是：
     * ALPHA_8：每个像素占用1byte内存
     * ARGB_4444:每个像素占用2byte内存
     * ARGB_8888:每个像素占用4byte内存
     * RGB_565:每个像素占用2byte内存
     *
     * @param filePath
     */
    private void picassoLoaderImager(String filePath) {

//        ImageAttabuteAnimation.SttingAnimation(mcontext,this,null);

        log.e(TAG, "互动 -------  loadimageing ------- " + filePath);

        ImageViewPicassocLoader.loadImage(mcontext, this, new File(filePath), null);
        log.e(TAG, "互动 -------  loadimage end -------");
    }



    //重写系统方法

    @Override
    protected void onDraw(Canvas canvas) {
        try {
            super.onDraw(canvas);
        } catch (Exception e) {
            log.i(TAG, "试图引用　一个　回收的图片 [" + e.getMessage() + "-" + e.getCause() + "]");
           picassoLoaderImager(localPath);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        // setImageDrawable(null);
    }

    /**
     * 加载一个  播放按钮
     */
    private Button playVideoBtn;
    private RelativeLayout relative;
    public Button getPlayVideoBtn() {
        if (mFather != null) {
            if (playVideoBtn == null) {
                playVideoBtn = new Button(mcontext);

                Bitmap bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.play);
                BitmapDrawable bd = new BitmapDrawable(this.getResources(), bitmap);
                playVideoBtn.setBackgroundDrawable(bd); //背景图

                if (relative == null) {
                    relative = new RelativeLayout(mcontext);
                    relative.setLayoutParams(new AbsoluteLayout.LayoutParams(
                            AbsoluteLayout.LayoutParams.MATCH_PARENT,
                            AbsoluteLayout.LayoutParams.MATCH_PARENT,
                            0, 0));
                }
                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);
                lp.addRule(RelativeLayout.CENTER_IN_PARENT);
                playVideoBtn.setLayoutParams(lp);
                 relative.addView(playVideoBtn); //添加到相对布局中
            }
            ((ViewGroup) ActiveImagePlayer.this.mFather).addView(relative);
        }
        return playVideoBtn;
    }


    /**
     * 释放 播放 按钮
     */
    private void releativePlayBtn() {
        if (playVideoBtn == null || mFather==null) {
            return;
        }
        //如果返回按钮存在. 移除
        Drawable drawable = playVideoBtn.getBackground();
        if (drawable != null) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
                if (!bitmapDrawable.getBitmap().isRecycled()) {
                    bitmapDrawable.getBitmap().recycle();
                    bitmapDrawable.setCallback(null);
                    playVideoBtn.setBackgroundResource(0);
                }
        }
                if (relative != null) {
                    relative.removeView(playVideoBtn);
                    playVideoBtn = null;
                    ((ViewGroup) ActiveImagePlayer.this.mFather).removeView(relative);
                    relative = null;
                }
    }
}
