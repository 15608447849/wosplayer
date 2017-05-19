package com.standalone;

import android.app.ProgressDialog;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.AbsoluteLayout;

import com.wosplayer.Ui.element.uitools.ImageStore;
import com.wosplayer.app.BackRunner;
import com.wosplayer.app.DisplayActivity;
import com.wosplayer.app.Logs;
import com.wosplayer.app.OverAppDialog;
import com.wosplayer.tool.SdCardTools;

import java.util.ArrayList;
import java.util.Iterator;

import cn.trinea.android.common.util.FileUtils;

import static com.wosplayer.tool.SdCardTools.justPath;

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
    //等待对话框
    ProgressDialog dialog;

    public void init(DisplayActivity activity){
        Log.e(TAG,"初始化 - 单机UI");
        mActivity = activity;
        playList = new ArrayList<>();
        regest();//注册广播
        //创建等待对话框
        createWaiteDialog();
        onBackExcute();
    }

    private void createWaiteDialog() {
        if (dialog==null){
            dialog = new ProgressDialog(mActivity);
            dialog.setTitle("提示");
            dialog.setMessage("正在查询可用文件主目录,请稍后.请勿连续拔插外置存储设备."); //
            dialog.setIndeterminate(true);
            dialog.setCancelable(false);
        }
    }

    private void regest() {
        if (broad==null){
            broad = new USBBroad(this);
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.MEDIA_EJECT");
            filter.addAction("android.intent.action.MEDIA_MOUNTED");
            filter.addDataScheme("file");
            filter.setPriority(1000);
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
                    OverAppDialog.showWaitingDialog(mActivity,dialog);
                    synchronized(this){
                        wait(1000);
                    }
                    fountsource();
                } catch (Exception e) {
                    e.printStackTrace();
                }finally {
                    OverAppDialog.closeWaitingDialog(mActivity,dialog);
                }
            }
        });
    }
    //查找资源
    private void fountsource() {
        isPlay = false;
        long time = System.currentTimeMillis();
        String dir = null;
        ArrayList<String> pathlist = null;
        ArrayList<String> filelist = null;

        //读取 usb内容
        pathlist = SdCardTools.getAllStorePath(mActivity);
        Logs.e(TAG,"获取路径 耗时: "+ (System.currentTimeMillis()- time) +", 路径列表:\n"+pathlist);
        if (pathlist!=null){
            time = System.currentTimeMillis();
            dir = justPath(pathlist,"usb");
            if (dir==null) dir = justPath(pathlist,"ext");
            if (dir == null) dir = justPath(pathlist,"sd");
            if (dir == null) dir = justPath(pathlist,"emu");
            Logs.e(TAG,"循环根目录路径耗时: "+ (System.currentTimeMillis()- time) );
        }

        if (dir!=null && !"".equals(dir)){
            time = System.currentTimeMillis();
            filelist = new ArrayList<>();
            //读取文件下所有资源文件->更新播放列表
            SdCardTools.getTagerPrefixOnFiles(dir,filelist,"wmv","3gp","avi","mp4","rmvb","mov","fiv","flv","mpeg","png","jpg");
            Logs.e(TAG,"循环资源文件耗时: "+ (System.currentTimeMillis()- time) +"  资源文件列表:\n" +filelist);
            if (filelist.size()>0){
                playList = filelist;
                curIndex = 0; //初始化播放位置
                curDirs = dir;
                isPlay = true;
                //通知重新开始播放
                onFinished();
                return;
            }
        }
        if (!isPlay){
            //通知找不到路径
            onFailt("找不到可用文件主目录或资源",5);
        }
    }

    private void onFailt(String var,int time) {
        //没有文件路径
        OverAppDialog.popWind(mActivity,var,3);
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
//        Logs.e(TAG,"############################################# 开始执行");

        //如果可以播放
        if (isPlay && playList!=null && playList.size()>0){

            if (video==null){
                video = new MSVideo(mActivity);
                mActivity.main.addView(video,new AbsoluteLayout.LayoutParams(-1,-1,0,0));
                Logs.e(TAG,"创建视频层");
            }
            if (layer == null){
                layer = new MimageMvideoSurface(mActivity);
                layer.setOnPlayed(this);
                layer.create();
                mActivity.main.addView(layer,new AbsoluteLayout.LayoutParams(-1,-1,0,0));
                layer.setVideo(video);
                Logs.e(TAG,"创建图片层");
            }

            int type = -1;
            boolean isNext = false;



            //播放当前位置的资源
            String source = playList.get(curIndex);
            //文件是否不存在
            if (!FileUtils.isFileExist(source) || source.contains("/wosplayer/default") || source.contains("/wosplayer/ffbk/") ){
                //移除资源
                removeSource(source);
                isNext = true;
                Logs.e(TAG,"移除资源 "+ source+",并且直接下一次执行");
            }else{
                //判断文件类型
                if (source.endsWith("png") || source.endsWith("jpg")){
                    //图片显示
                    type = 1;
                }else
                if (source.endsWith("mp4") || source.endsWith("avi")
                        || source.endsWith("3gp") || source.endsWith("wmv")
                        || source.endsWith("fiv")  || source.endsWith("rmvb")
                        || source.endsWith("mov") || source.endsWith("flv")
                        || source.endsWith("mpeg")){
                    //视频展示
                    type = 2;
                }
                Logs.e(TAG,"播放资源 "+source);
                layer.onPlayStart(source,type);
            }
            //下标加1
            if (++curIndex>=playList.size())  curIndex = 0;
            if (isNext) onStart();
        }
        else{
            //通知目录播放失败
            onFailt("不允许播放或播放目录文件内容错误",10);
        }
    }
    //移除资源
    private void removeSource(String source) {
        Iterator<String> itr = playList.iterator();
        while (itr.hasNext()){
            if (source.equals(itr.next())) itr.remove();
        }
    }

    //结束
    private void onStop(){
        if (layer != null){
            layer.destorys();
            mActivity.main.removeView(layer);
            layer = null;
            Logs.e(TAG,"移除图片层");
        }
        if (video!=null){
            mActivity.main.removeView(video);
            video = null;
            Logs.e(TAG,"移除视频层");
        }
        //移除缓存
        ImageStore.getInstants().clearCache();//清除缓存
        //移除播放列表
        playList = null;
        curDirs = null;
        //初始化下标
        curIndex = 0;
//        Logs.e(TAG,"############################################# 结束执行");
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
                String[] strarr = var.split("#");
                if (strarr.length>=0 ){
                    if (strarr[0].equals("out") && !strarr[1].contains(curDirs)){
                            return;
                    }
                    if (strarr[0].equals("in")){
                        //如果是当前目录为usb - 不改变
                        if (curDirs.contains("usb")){
                            return;
                        }
                    }
                }
                onStop();
                onBackExcute();
            }
        });
    }


}
