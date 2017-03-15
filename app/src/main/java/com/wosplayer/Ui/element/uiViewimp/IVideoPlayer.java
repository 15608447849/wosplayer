package com.wosplayer.Ui.element.uiViewimp;

import android.content.Context;
import android.media.MediaPlayer;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;

import com.wosplayer.Ui.element.interfaces.IPlayer;
import com.wosplayer.Ui.element.definedView.Mvideo;
import com.wosplayer.Ui.element.interfaces.TimeCalls;
import com.wosplayer.Ui.performer.UiExcuter;
import com.wosplayer.app.DataList;
import com.wosplayer.app.Logs;
import com.wosplayer.download.util.DownloadFileUtil;

import cn.trinea.android.common.util.FileUtils;

/**
 * Created by Administrator on 2016/7/26.
 * 播放视频
 *
 */

public class IVideoPlayer implements IPlayer{
    private static final java.lang.String TAG = "IVideoPlayer";
    private ViewGroup superView = null;
    private boolean isLayout = false;//是否已经布局

    private AbsoluteLayout.LayoutParams layoutParams;
    //播放器
    private Mvideo video = null;
    public IVideoPlayer(Context context,ViewGroup vp) {
        this.superView = vp;
        video = new Mvideo(context);
    }

    private String localPath = null;

    @Override
    public void loadData(DataList mp, Object ob) {
        try {
        int x,y,h,w;
        x = mp.GetIntDefualt("x", 0);
        y = mp.GetIntDefualt("y", 0);
        w = mp.GetIntDefualt("width", 0);
        h = mp.GetIntDefualt("height", 0);
         layoutParams = new AbsoluteLayout.LayoutParams(w,h,x,y);

        localPath = mp.GetStringDefualt("localpath", "");
        //Logs.d(TAG,"加载视频 : x="+x+";y="+y+";width="+w+";height="+h+"\nfile:"+localPath);

        video.setLayoutParams(layoutParams);
        video.setLoop(false);//false 不循环
            video.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    //播放完毕 通知上一级
                    callTo();
                }
            });

        }catch (Exception e){
            Logs.e(TAG, "loaddata() " + e.getMessage());
        }
    }

    private void callTo() {
        if (timeCalls!=null){
            timeCalls.playOvers(this);
        }
    }

 

    @Override
    public void start() {
        try{

            if (FileUtils.isFileExist(localPath)){
                video.setMedioFilePath(localPath);
            }else{
                video.setMedioFilePath(UiExcuter.getInstancs().defVideoPath);
            }
            if (!isLayout){
                superView.addView(video);
                isLayout = true;
            }

        }catch (Exception e){
            Logs.e(TAG,"开始:"+e.getMessage());
        }
    }

    @Override
    public void stop() {
        try {
            if (isLayout){
                //移除父视图
                superView.removeView(video);
                isLayout = false;
            }

        }catch (Exception e){
            Logs.e(TAG,"停止:"+e.getMessage());
        }
    }
    //时间回调
    private TimeCalls timeCalls = null;
    @Override
    public void setTimerCall(TimeCalls timer) {
        timeCalls = timer;
    }


}
