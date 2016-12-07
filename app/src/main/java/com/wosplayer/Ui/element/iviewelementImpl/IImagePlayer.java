package com.wosplayer.Ui.element.iviewelementImpl;

import android.content.Context;
import android.graphics.Canvas;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsoluteLayout;
import android.widget.ImageView;

import com.wosplayer.R;
import com.wosplayer.Ui.element.IPlayer;
import com.wosplayer.Ui.element.iviewelementImpl.uitools.ImageAttabuteAnimation;
import com.wosplayer.Ui.element.iviewelementImpl.uitools.ImageViewPicassocLoader;
import com.wosplayer.Ui.performer.TimeCalls;
import com.wosplayer.app.DataList;
import com.wosplayer.app.log;
import com.wosplayer.loadArea.otherBlock.fileUtils;

/**
 * Created by Administrator on 2016/7/24.
 */

public class IImagePlayer extends ImageView implements IPlayer{

    private WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
    private static final java.lang.String TAG = "_iimagePlayer ";//IImagePlayer.class.getName();
    private Context mCcontext;
    private ViewGroup mfatherView = null;
    private int x=0;
    private int y=0;
    private int h=0;
    private int w=0;
    private int defaultWidth = wm.getDefaultDisplay().getWidth();
    private boolean isExistOnLayout = false;

    public IImagePlayer(Context context, ViewGroup mfatherView) {
        super(context);
        mCcontext =context;
        this.mfatherView = mfatherView;
    }

    private String localpath = null;
    private String uri = null;
    private DataList mp = null;
    @Override
    public void loadData(DataList mp, Object ob) {
        try {
            this.mp = mp;
            this.x = mp.GetIntDefualt("x", 0);
            this.y = mp.GetIntDefualt("y", 0);
            this.w = mp.GetIntDefualt("width",defaultWidth );
            this.h = mp.GetIntDefualt("height", 0);
            this.localpath = mp.GetStringDefualt("localpath", "");
            this.uri = mp.GetStringDefualt("getcontents", "");

        }catch (Exception e){
            log.e(TAG, "loaddata() " + e.getMessage());
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
    public void setlayout() {
        try {
            if (!isExistOnLayout){
                mfatherView.addView(this);
                isExistOnLayout = true;
            }
            AbsoluteLayout.LayoutParams lp = (AbsoluteLayout.LayoutParams) this
                    .getLayoutParams();
            lp.x = x;
            lp.y = y;
            lp.width = w;
            lp.height = h;
            this.setLayoutParams(lp);
        } catch (Exception e) {
          log.e(TAG,"设置布局:" + e.getMessage());
        }
    }
    //主线程中执行
    @Override
    public void start() {
        try{
            setlayout();//设置布局
            loadMyImage();
        }catch (Exception e){
            log.e(TAG,"开始:"+e.getMessage());
        }
    }
    //加载图片
    private void loadMyImage() {
        //先判断文件是不是存在
        if (fileUtils.checkFileExists(localpath)){
            picassoLoaderImager(localpath);
        }else{
            log.e(TAG,"图片 路径 不存在 - "+localpath);
            this.setImageResource(R.drawable.error);
            if (timeCalls!=null){
                timeCalls.playOvers(this);
            }
        }
    }
    //主线程中执行
    @Override
    public void stop() {
        try {
            //移除父视图
            mfatherView.removeView(this);
            isExistOnLayout = false;
            //移除存在的图片资源
//            removeMyImage(this);
        }catch (Exception e){
            log.e(TAG,"停止:"+e.getMessage());
        }
    }
//    //放入主线程
//    public static void removeMyImage(ImageView imageView) {
//        log.i(TAG, "----------------------准备 释放资源----------------------------------" );
//        //资源回调的地方
//        Bitmap bitmap = null;
//        Drawable drawable = imageView.getDrawable();
//        if (drawable == null){
//            drawable = imageView.getBackground();
//            if (drawable == null){
//                imageView.setDrawingCacheEnabled(true);
//                    bitmap = imageView.getDrawingCache();
//                imageView.setDrawingCacheEnabled(false);
//                    if (bitmap==null){
//                        return;
//                    }else{
//                        log.i(TAG,"getDrawingCache() :"+bitmap.toString());
//                    }
//            }
//        }
//        if (drawable != null && drawable instanceof BitmapDrawable) {
//            log.e(TAG,"释放资源:" + drawable.toString());
//            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
//            bitmap = bitmapDrawable.getBitmap();
//        }
//
//        if (bitmap != null && !bitmap.isRecycled()) {
//            log.i(TAG,"-- bitmap is exitt --");
//            if(drawable!=null){
//                drawable.setCallback(null);
//            }
//            bitmap.recycle();
//            log.i(TAG, " 释放资源 success" );
//            return;
//        }
//        imageView.setBackgroundResource(0);
//        imageView.setImageDrawable(null);
//        log.e(TAG, " 释放资源 failt" );
//    }

    private void picassoLoaderImager(String filePath) {
        //设置图片切换方式
        ImageAttabuteAnimation.SttingAnimation(mCcontext,this,new int[]{x,y,w,h});
        /**
         *getMeasuredHeight()返回的是原始测量高度，与屏幕无关，getHeight()返回的是在屏幕上显示的高度。实际上在当屏幕可以包裹内容的时候，
         * 他们的值是相等的，只有当view超出屏幕后，才能看出他们的区别。
         * 当超出屏幕后，getMeasuredHeight()等于getHeight()加上屏幕之外没有显示的高度。
         */
        ImageViewPicassocLoader.getBitmap(filePath,this);
//        log.i(TAG," ---------------------------------loader image --------------------------------- over");
    }

    //重写系统方法
    @Override
    protected void onDraw(Canvas canvas) {
        try {
            super.onDraw(canvas);
            //log.i(TAG,"onDraw()被调用");
        } catch (Exception e) {
            log.e(TAG,"试图引用　一个　回收的图片 ["+e.getMessage()+"-----"+e.getCause()+"]");
            loadMyImage();
        }
    }
    @Override
    protected void onDetachedFromWindow() {
        try {
            super.onDetachedFromWindow();
           // log.i(TAG,"onDetachedFromWindow()被调用");
            setImageDrawable(null);
        }catch (Exception e){
            log.e(TAG,"onDetachedFromWindow:"+e.getMessage());
        }
    }

}
