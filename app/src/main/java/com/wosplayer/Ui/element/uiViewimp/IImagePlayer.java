package com.wosplayer.Ui.element.uiViewimp;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;

import com.wosplayer.Ui.element.definedView.Mimage;
import com.wosplayer.Ui.element.interfaces.IPlayer;
import com.wosplayer.Ui.element.interfaces.TimeCalls;
import com.wosplayer.Ui.element.uitools.ImageAttabuteAnimation;
import com.wosplayer.Ui.element.uitools.ImageTools;
import com.wosplayer.Ui.performer.UiExcuter;
import com.wosplayer.app.DataList;
import com.wosplayer.app.Logs;

import cn.trinea.android.common.util.FileUtils;

/**
 * Created by Administrator on 2016/7/24.
 */

public class IImagePlayer implements IPlayer{
    private static final java.lang.String TAG = "图片显示器 ";//IImagePlayer.class.getName();
    private ViewGroup superView = null;
    private boolean isLayout = false;
    private Mimage image;
    private int[] sizearr;
    private String imageFile = null;//文件本地路径
    private int timeLength = 0;//时长
    private Runnable callTo;


    public IImagePlayer(Context context, ViewGroup superView) {
        this.superView = superView;
        image = new Mimage(context);
    }
    @Override
    public void loadData(DataList mp) {
        try {

            int x = mp.GetIntDefualt("x", 0);
            int y = mp.GetIntDefualt("y", 0);
            int w = mp.GetIntDefualt("width",0 );
            int h = mp.GetIntDefualt("height", 0);
            sizearr = new int[]{x,y,w,h};
            image.setLayoutParams(new AbsoluteLayout.LayoutParams(w,h,x,y));
            imageFile = mp.GetStringDefualt("localpath", "");
            timeLength = mp.GetIntDefualt("timelength",0);
        }catch (Exception e){
            Logs.e(TAG, "loaddata() " + e.getMessage());
        }
    }

    @Override
    public void setTimerCall(TimeCalls timer) {
        final TimeCalls timeCalls = timer;
        callTo =  new Runnable() {
            @Override
            public void run() {
                if (timeCalls!=null){
                    timeCalls.playOvers(IImagePlayer.this);
                }
            }
        };
    }

    //主线程中执行
    @Override
    public void start() {
        try{
            if (!isLayout){
                superView.addView(image);
                isLayout = true;
            }
            if (FileUtils.isFileExist(imageFile)){
//                image.start(imageFile);
            ImageTools.getBitmap(imageFile,image);
            }else{
//                image.start();
                ImageTools.getBitmap(UiExcuter.getInstancs().defImagePath,image);
            }
            ImageAttabuteAnimation.SttingAnimation(null,image,sizearr);//属性动画

            //开始一个计时器
            UiExcuter.getInstancs().runingMainDelayed(callTo, timeLength * 1000);

        }catch (Exception e){
            Logs.e(TAG,"开始:"+e.getMessage());
        }
    }
    //主线程中执行
    @Override
    public void stop() {
        try {
            if (isLayout){
                //移除视图
                superView.removeView(image);
                isLayout = false;
            }
            UiExcuter.getInstancs().removeMain(callTo);
        }catch (Exception e){
            Logs.e(TAG,"停止:"+e.getMessage());
        }
    }
}
