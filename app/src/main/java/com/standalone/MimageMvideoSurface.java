package com.standalone;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.wosplayer.Ui.element.uitools.ImageTools;
import com.wosplayer.app.SystemConfig;

/**
 * Created by user on 2017/4/27.
 */

public class MimageMvideoSurface extends SurfaceView implements Runnable,MVideoInterface.VideoEvent{
    private static final String TAG = "单机图片显示层";
    private Object lock = new Object();
    private OnPlayed notify;
    private int sourceType = -1; // 图片1 视频2
    private String url;
    private Thread mThread;
    private boolean flag;
    private TimeCount count;
    private Bitmap bitmap;
    private MVideoInterface video;
    private int imageTime = 10;
    public MimageMvideoSurface(Context context) {
        super(context);
    }

    //初始化
    public void create(){
        //设置图片播放时间
        imageTime = SystemConfig.get().GetIntDefualt("SingImageTime",10);
        //白色背景
        SurfaceHolder holder = this.getHolder();
        holder.setSizeFromLayout();//设置surface大小来自布局
        holder.setType(SurfaceHolder.SURFACE_TYPE_NORMAL);
        createThread();
    }

    public void destorys(){
        stopThread();
        drawClear();
        if (video!=null){
            video.stopVideo();
            video.releaseVideo();
            video = null;
        }
        if (notify!=null) notify=null;
    }

    public void setVideo(MVideoInterface video){
        this.video = video;
    }

    //设置回调
    public void setOnPlayed(OnPlayed play){
        this.notify = play;
    }

    //创建线程
    public void createThread(){
        //创建
        if (count==null){
            count = new TimeCount();
            count.mStart();
        }
        if (mThread==null){
            mThread = new Thread(this);
            flag = true;
            mThread.start();
        }
    }
    //停止线程
    public void stopThread(){
        //销毁
        if (count!=null){
            count.mStort();
            count=null;
        }
        if (mThread!=null){
            flag = false;
            unlockThread();
            mThread = null;
        }
    }
    @Override
    public void run() {
        //关于图片的绘制的控制
        while (flag){
            try {
                switch (sourceType){
                    case 1:drawImage();break;
                    case 2:showVideo();break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void onPlayStart(String url,int type){
        this.sourceType = type;
        this.url = url;
    }
    //解锁
    private void unlockThread(){
        synchronized (lock){
            lock.notify();
        }
    }
    //锁定线程
    private void lockThread(){
        try {
            synchronized (lock){
                lock.wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    private void onPlayStop(){
        this.sourceType = -1;
        this.url = null;
        if (notify!=null) notify.onFinished();
    }
    private void drawClear(){
        //锁定画布
        Canvas mCanvas = getHolder().lockCanvas();
        if (mCanvas!=null){
            mCanvas.drawColor(Color.WHITE);
            getHolder().unlockCanvasAndPost(mCanvas);
        }

    }
    //绘制图片
    private void drawImage() {
        if (!flag) return;
        //先根据资源地址 获取 bitmap
        Bitmap bitmap = ImageTools.getBitmap(null,url,null);
        if (bitmap!=null && getHolder() != null){
                    try {
                        //锁定画布
                        Canvas mCanvas = getHolder().lockCanvas();
                        if (mCanvas==null){
                            onPlayStop();
                            return;
                        }
                        //展示
                        Paint paint = new Paint();
                        paint.setAntiAlias(true);
                        paint.setStyle(Paint.Style.STROKE);
                        Rect mSrcRect, mDestRect;
                        mSrcRect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
                        mDestRect = new Rect(0, 0, getWidth(), getHeight());
                        mCanvas.drawBitmap(bitmap, mSrcRect, mDestRect, paint);
                        //解锁画布
                        getHolder().unlockCanvasAndPost(mCanvas);
                            //休眠指定秒数
                            count.setTime(imageTime, new TimeAction() {
                                @Override
                                public void over() {
                                    unlockThread();
                                }
                            });
                        lockThread();
                        onPlayStop();
                    } catch (Exception e) {
                        e.printStackTrace();
                        onPlayStop();
                    }
        }else{
            onPlayStop();
        }
    }
    //播放视频
    private void showVideo() {
        if (!flag) return;
        if (video == null || notify==null || url==null || url.equals("")){
            onPlayStop();
        }else{
            notify.runMainThread(new Runnable() {
                @Override
                public void run() {

                    MimageMvideoSurface.this.setVisibility(GONE);
                    video.attch();
                    video.startVideo(url,MimageMvideoSurface.this);
                }
            });
            //锁定线程
            lockThread();
            onPlayStop();
        }
    }

    @Override
    public void onComplete() {
        //视频播放完成
//        Log.i(TAG,"VideoEvent onComplete : "+video);
        if(getVisibility() == GONE){
           this.setVisibility(VISIBLE);
        }
        if (video!=null){
            video.stopVideo(); //停止播放
            video.deattch(); //解除绑定
            unlockThread(); //解锁线程
        }
    }
    //时间计时器
    private class TimeCount extends Thread{
        private boolean isStart;
        private int sleepTime;
        private TimeAction action = null;
        public void setTime(int time, TimeAction action){
            sleepTime = time<=0?0:time;
            this.action = action;
        }
        public void mStart(){
            isStart = true;
            this.start();
        }
        public void mStort(){
            isStart = false;
        }
        @Override
        public void run() {
            try {
                while (isStart){
                    if (sleepTime>0){
                        sleep(sleepTime * 1000);
                        if (action!=null){
                            action.over();
                            action = null;
                            sleepTime = 0;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    //时间回调器
    private interface TimeAction{
        void over();
    }
}
