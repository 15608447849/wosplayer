package com.wosplayer.Ui.element.iviewelementImpl;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;

import com.wosplayer.Ui.element.IPlayer;
import com.wosplayer.Ui.element.iviewelementImpl.mycons_view.MyVideoView;
import com.wosplayer.Ui.performer.TimeCalls;
import com.wosplayer.app.DataList;
import com.wosplayer.app.WosApplication;
import com.wosplayer.app.log;
import com.wosplayer.loadArea.otherBlock.fileUtils;

/**
 * Created by Administrator on 2016/7/26.
 * 播放视频
 *
 */

public class IVideoPlayer extends AbsoluteLayout implements IPlayer{
    private static final java.lang.String TAG = IVideoPlayer.class.getName();
    private Context mCcontext;
    private ViewGroup mfatherView = null;
    private int x=0;
    private int y=0;
    private int h=0;
    private int w=0;
    private boolean isExistOnLayout = false;//是否已经布局

    //播放器
    private MyVideoView video = null;
    public IVideoPlayer(Context context,ViewGroup vp) {
        super(context);
        this.mfatherView = vp;
        mCcontext =context;
        video = new MyVideoView(context,this);
    }

    private String singleFileLocalPath = null;
    private String singleFileUri = null;
    private DataList mp = null;
    @Override
    public void loadData(DataList mp, Object ob) {
        try {
        this.mp = mp;
        this.x = mp.GetIntDefualt("x", 0);
        this.y = mp.GetIntDefualt("y", 0);
        this.w = mp.GetIntDefualt("width", 0);
        this.h = mp.GetIntDefualt("height", 0);
        this.singleFileLocalPath = mp.GetStringDefualt("localpath", "");
        this.singleFileUri = mp.GetStringDefualt("getcontents", "");
        video.initVideoView(true);//false 不循环
        }catch (Exception e){
            log.e(TAG, "loaddata() " + e.getMessage());
        }
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
            //设置播放器
            video.setMyLayout(x,y,w,h);
        } catch (Exception e) {
            log.e(TAG,"设置布局:" + e.getMessage());
        }
    }

    /**
     * 当前下标
     */
    private int currentIndex = -1;
    @Override
    public void start() {
        try{
            setlayout();//设置布局
                    if(!fileUtils.checkFileExists(singleFileLocalPath)) {//fileUtils.checkFileExists(filename)
                        //不存在
                        log.e(TAG, "开始 - 视频资源 不存在 - " + singleFileLocalPath);
                        //播放默认视频
                        String del = WosApplication.getConfigValue("defaultVideo");
                        if (!del.equals("")){
                            playVideo(del);
                        }
                        if (timeCalls!=null){
                            timeCalls.playOvers(this);
                        }
                    }else{
                        playVideo(singleFileLocalPath);
                    }
        }catch (Exception e){
            log.e(TAG,"开始:"+e.getMessage());
        }
    }

    @Override
    public void stop() {
        try {
            //移除父视图
            mfatherView.removeView(this);
            isExistOnLayout = false;
        }catch (Exception e){
            log.e(TAG,"停止:"+e.getMessage());
        }
    }

    @Override
    public DataList getDatalist() {
        return mp;
    }

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
