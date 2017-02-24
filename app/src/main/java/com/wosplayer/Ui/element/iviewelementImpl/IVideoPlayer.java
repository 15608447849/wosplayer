package com.wosplayer.Ui.element.iviewelementImpl;

import android.content.Context;
import android.media.MediaPlayer;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;

import com.wosplayer.Ui.element.interfaces.IPlayer;
import com.wosplayer.Ui.element.iviewelementImpl.mycons_view.MyVideoView;
import com.wosplayer.Ui.performer.TimeCalls;
import com.wosplayer.app.DataList;
import com.wosplayer.app.DisplayerApplication;
import com.wosplayer.app.Logs;
import com.wosplayer.download.util.DownloadFileUtil;

/**
 * Created by Administrator on 2016/7/26.
 * 播放视频
 *
 */

public class IVideoPlayer extends AbsoluteLayout implements IPlayer{
    private static final java.lang.String TAG = "IVideoPlayer";
    private Context context;
    private ViewGroup superView = null;

    private boolean isLayout = false;//是否已经布局

    //播放器
    private MyVideoView video = null;
    public IVideoPlayer(Context context,ViewGroup vp) {
        super(context);
        this.superView = vp;
        this.context =context;

    }

    private String localPath = null;
    public String singleFileUri = null;
    private DataList mp = null;
    @Override
    public void loadData(DataList mp, Object ob) {
        try {

            video = new MyVideoView(context,this);
            int x,y,h,w;
        x = mp.GetIntDefualt("x", 0);
        y = mp.GetIntDefualt("y", 0);
        w = mp.GetIntDefualt("width", 0);
        h = mp.GetIntDefualt("height", 0);
        this.setLayoutParams(new AbsoluteLayout.LayoutParams(w,h,x,y));
        video.setLayoutParam(x,y,w,h);
        this.localPath = mp.GetStringDefualt("localpath", "");
        this.singleFileUri = mp.GetStringDefualt("getcontents", "");
        Logs.d(TAG,"获取到一个视频 - "+x+y+"-"+w+","+h+"  - "+localPath);
        video.initVideoView(true);//false 不循环
            video.setOnCompletionListener_(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
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

            if (!isLayout){
                superView.addView(this);
                isLayout = true;
            }
                    if(!DownloadFileUtil.checkFileExists(localPath)) {
                        //不存在
                        Logs.e(TAG, "视频资源 不存在 - " + localPath);
                        callTo();
//                        //播放默认视频
                        String del = DisplayerApplication.getConfigValue("defaultVideo");
                        if (!del.equals("")){
                            playVideo(del);
                        }
                    }else{
                        playVideo(localPath);
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
                superView.removeView(this);
                isLayout = false;
            }

        }catch (Exception e){
            Logs.e(TAG,"停止:"+e.getMessage());
        }
    }


    //开始播放视频
    private void playVideo(String filename){
        video.loadRouce(filename);//第一个开始 有多个的话每播放完一个 播下一个,到最后 跳到第一个
        video.start();
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

}
