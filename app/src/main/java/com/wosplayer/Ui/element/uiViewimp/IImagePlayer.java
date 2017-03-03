package com.wosplayer.Ui.element.uiViewimp;

import android.content.Context;
import android.graphics.Canvas;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;
import android.widget.ImageView;

import com.wosplayer.R;
import com.wosplayer.Ui.element.interfaces.IPlayer;
import com.wosplayer.Ui.element.uitools.ImageAttabuteAnimation;
import com.wosplayer.Ui.element.uitools.ImageViewPicassocLoader;
import com.wosplayer.Ui.element.interfaces.TimeCalls;
import com.wosplayer.app.DataList;
import com.wosplayer.app.Logs;
import com.wosplayer.download.util.DownloadFileUtil;

/**
 * Created by Administrator on 2016/7/24.
 */

public class IImagePlayer extends ImageView implements IPlayer{


    private static final java.lang.String TAG = "图片显示器 ";//IImagePlayer.class.getName();
    private Context context;
    private ViewGroup superView = null;


    private boolean isLayout = false;

    public IImagePlayer(Context context, ViewGroup superView) {
        super(context);
        this.context =context;
        this.superView = superView;
    }
    private int x,y,w,h=0;
    private String localpath = null;//文件本地路径
    private String uri = null;
    @Override
    public void loadData(DataList mp, Object ob) {
        try {

            x = mp.GetIntDefualt("x", 0);
            y = mp.GetIntDefualt("y", 0);
            w = mp.GetIntDefualt("width",0 );
            h = mp.GetIntDefualt("height", 0);
            //Logs.d(TAG,"设置布局:"+x+"-"+y+"-"+w+"-"+h);
            this.setLayoutParams(new AbsoluteLayout.LayoutParams(w,h,x,y));

            this.localpath = mp.GetStringDefualt("localpath", "");
            this.uri = mp.GetStringDefualt("getcontents", "");//网络路径
        }catch (Exception e){
            Logs.e(TAG, "loaddata() " + e.getMessage());
        }
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


    //主线程中执行
    @Override
    public void start() {
        try{
            if (!isLayout){
                superView.addView(this);
                isLayout = true;
            }
            loadMyImage();
        }catch (Exception e){
            Logs.e(TAG,"开始:"+e.getMessage());
        }
    }
    //加载图片
    private void loadMyImage() {
        //先判断文件是不是存在
        if (DownloadFileUtil.checkFileExists(localpath)){
            picassoLoaderImager(localpath);
        }else{
            Logs.e(TAG,"图片路径不存在 - "+localpath);
            this.setImageResource(R.drawable.error);//设置错误图片地址-需要修改
            if (timeCalls!=null){
                timeCalls.playOvers(this);
            }
        }
    }
    //主线程中执行
    @Override
    public void stop() {
        try {
            if (isLayout){
                //移除视图
                superView.removeView(this);
                isLayout = false;
            }
        }catch (Exception e){
            Logs.e(TAG,"停止:"+e.getMessage());
        }
    }

    private void picassoLoaderImager(String filePath) {
        //设置图片切换方式
        ImageAttabuteAnimation.SttingAnimation(context,this,new int[]{x,y,w,h});
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
        } catch (Exception e) {
            Logs.e(TAG,"试图引用　一个　回收的图片 ["+e.getMessage()+"-----"+e.getCause()+"]");
            loadMyImage();
        }
    }
    @Override
    protected void onDetachedFromWindow() {
        try {
            super.onDetachedFromWindow();
//            setImageDrawable(null);
        }catch (Exception e){
            Logs.e(TAG,"onDetachedFromWindow:"+e.getMessage());
        }
    }
}
