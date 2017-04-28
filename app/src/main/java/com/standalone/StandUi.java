package com.standalone;

import android.content.IntentFilter;
import android.util.Log;
import android.widget.AbsoluteLayout;

import com.wosplayer.Ui.element.uitools.ImageStore;
import com.wosplayer.app.BackRunner;
import com.wosplayer.app.DisplayActivity;
import com.wosplayer.app.Logs;
import com.wosplayer.tool.SdCardTools;

import java.util.ArrayList;

/**
 * Created by lzp on 2017/4/26.
 //监听sd卡内容
 //控制图片和视频的播放
 */
public class StandUi implements OnPlayed{
    private static final String TAG ="单机播放器";
    private StandUi(){
    }
    private static StandUi instands = null;
    public static StandUi getInstands(){
        if (instands == null){
            instands  = new StandUi();
        }
        return instands;
    }
    //activity
    private DisplayActivity mActivity;
    private MimageMvideoSurface layer;
    private MSVideo video;
    //当前播放的文件夹目录
    private String curDirs;
    //当前播放列表
    private ArrayList<String> playList;
    //是否播放
    private boolean isPlay = false;
    //当前播放的位置
    private int curIndex = 0;
    //广播
    private USBBroad broad;

    public void init(DisplayActivity activity){
        Log.e(TAG,"初始化 - 单机UI");
        mActivity = activity;
        playList = new ArrayList<>();
        regest();
        onBackExcute();

    }
    private void regest() {
        if (broad==null){
            broad = new USBBroad(this);
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.MEDIA_EJECT");
            filter.addAction("android.intent.action.MEDIA_MOUNTED");
            filter.addDataScheme("file");
            mActivity.registerReceiver(broad,filter);
        }
    }
    private void unregest(){
        if (broad!=null){
            Log.e(TAG,"取消广播");
            mActivity.unregisterReceiver(broad);
        }
    }
    //后台执行
    private synchronized void onBackExcute() {
        BackRunner.runBackground(new Runnable() {
            @Override
            public void run() {
                try {
                    fountsource();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
    //查找资源
    private void fountsource() {
        long time = System.currentTimeMillis();
        String dir = null;
        //读取 usb内容
        ArrayList<String> pathlist = SdCardTools.getAllStorePath(mActivity);
        Logs.e(TAG,"获取路径: "+ (System.currentTimeMillis()- time) );
        time = System.currentTimeMillis();
        if (pathlist!=null){
            dir = SdCardTools.justPath(pathlist,"usb");
            if (dir==null) dir = SdCardTools.justPath(pathlist,"extsd");
        }
        Logs.e(TAG,"循环耗时: "+ (System.currentTimeMillis()- time) );
        if (dir!=null &&  !dir.equals("")){
            pathlist = new ArrayList<>();
            //读取文件下所有资源文件->更新播放列表
            SdCardTools.getTagerPrefixOnFiles(dir,pathlist,"wmv","3gp","avi","mp4","png","jpg");
//            SdCardTools.getTagerPrefixOnFiles(dir,pathlist,"wmv","3gp","avi","mp4");
            Logs.e(TAG,"获取到可用的资源文件列表:\n"+pathlist);
            if (pathlist.size()==0){
                return;
            }
            isPlay = false;
            playList.clear();
            playList = pathlist;
            curIndex = 0; //初始化播放位置
            curDirs = dir;
            isPlay = true;
            //通知重新开始播放
            onFinished();
        }else{
            //通知找不到路径
            onNotPath();
        }
    }

    private void onNotPath() {
        //没有文件路径
//        OverAppDialog.popWind(mActivity,"找不到可用文件路径",2);
    }

    public void unInin(){
        Log.e(TAG,"unInin");
        onStop();
        //关闭广播
        unregest();
        mActivity = null;
    }
    //开始
    public void onStart(){
        Logs.e(TAG,"############################################# 开始执行");
        if (layer == null){
            layer = new MimageMvideoSurface(mActivity);
            layer.setOnPlayed(this);
            layer.create();
            mActivity.main.addView(layer,new AbsoluteLayout.LayoutParams(-1,-1,0,0));
            if (video==null){
                video = new MSVideo(mActivity);
                mActivity.main.addView(video,new AbsoluteLayout.LayoutParams(-1,-1,0,0));
                layer.setVideo(video);
            }
        }

        //如果可以播放
        if (isPlay && playList.size()>0){

            //播放当前位置的资源
            String source = playList.get(curIndex);
            int type = -1;
            //判断文件类型
            if (source.endsWith("png") || source.endsWith("jpg")){
                //图片显示
                type = 1;
            }
            if (source.endsWith("mp4") || source.endsWith("avi") || source.endsWith("3gp") || source.endsWith("wmv")){
                //视频展示
                type = 2;
            }
           //下标加1
            if (++curIndex>=playList.size())  curIndex = 0;
            layer.onPlayStart(source,type);
        }
    }
    //结束
    public void onStop(){
        if (layer != null){
            layer.destorys();
            if (video!=null){
                mActivity.main.removeView(video);
                video = null;
            }
            mActivity.main.removeView(layer);
            layer = null;
            ImageStore.getInstants().clearCache();//清除缓存
        }
        Logs.e(TAG,"############################################# 结束执行");
    }

    @Override
    public void runMainThread(Runnable task) {
        if (mActivity!=null){
            mActivity.mHandler.post(task);
        }
    }

    @Override
    public void onFinished() {
        if (mActivity != null)
            runMainThread(new Runnable() {
                @Override
                public void run() {
                    onStart();
                }
            });
    }

    @Override
    public void onBroad(final String var) {
        runMainThread(new Runnable() {
            @Override
            public void run() {
                onStop();
                onBackExcute();
            }
        });
    }


}
