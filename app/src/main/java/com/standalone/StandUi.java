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

import cn.trinea.android.common.util.FileUtils;

import static com.wosplayer.tool.SdCardTools.justPath;

/**
 * Created by lzp on 2017/4/26.
 //监听sd卡内容
 //控制图片和视频的播放
 */
public class StandUi implements OnPlayed{
    private static final String TAG ="单机播放器";
    private static final String NODE = "/NODE";
    private static final String TRANSLATE_DIRS = "/built-in";
    private static final String[] FILTER_SUFFIX = {"png","jpg","wmv","3gp","avi","mp4","rmvb","mov","fiv","flv","mpeg"};
    private StandUi(){
    }

    private static class Holder{
        static StandUi instants = new StandUi();
    }
    public static StandUi getInstants(){
        return Holder.instants;
    }
    //activity
    private DisplayActivity mActivity;
    private MImageMVideoSurfaceView layer;
    private MSVideo video;
    //当前播放的文件夹目录
    private String curDirs = NODE;
    //当前播放列表
    private ArrayList<String> playList;
    //是否播放
    private boolean isPlay = false;
    //当前播放的位置
    private int curIndex = 0;
    //广播
    private USBBroad broad;
    //等待对话框
    private ProgressDialog dialog;

    private final String[] messageArray = new String[]{
            "正在查询可用资源,请稍后片刻.",
            "文件查询错误:"
    };

    /**
     * 初始化
     */
    public void init(DisplayActivity activity){
        mActivity = activity;
        playList = new ArrayList<>();
        regest();//注册广播
        //创建对话框
        createDialog();
        onBackExecute();
        Log.i(TAG,"初始化完成.");
    }

    private void createDialog() {
        if (dialog==null){
            dialog = new ProgressDialog(mActivity);
            dialog.setTitle("提示");
            dialog.setIndeterminate(true);
            dialog.setCancelable(false);
        }
    }

    /**
     * 广播注册标识
     */
    private boolean isBroadReceiver = false;

    /**
     * 注册广播
     */
    private void regest() {
        unregest();
        if (broad==null && !isBroadReceiver){
            broad = new USBBroad(this);
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.MEDIA_EJECT");
            filter.addAction("android.intent.action.MEDIA_MOUNTED");
            filter.addDataScheme("file");
            filter.setPriority(1000);
            mActivity.registerReceiver(broad,filter);
            isBroadReceiver = true;
            Log.i(TAG,"注册 SD卡 监听广播.");
        }
    }

    /**
     * 取消广播
     */
    private void unregest(){
        try {
        if (broad!=null && isBroadReceiver){
                mActivity.unregisterReceiver(broad);
                isBroadReceiver = false;
            Log.i(TAG,"取消 SD卡 监听广播.");
        }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            broad = null;
        }
    }
    private void waitTime(long time){
        synchronized(this){
            try {
                this.wait(time);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    private void notifyWaitStop() {
        synchronized (this){
            this.notify();
        }
    }

    //后台执行
    private synchronized void onBackExecute() {
        BackRunner.runBackground(new Runnable() {
            @Override
            public void run() {
                try {
                    notifyWaitStop();
                    waitTime(1000);
                    OverAppDialog.showDialog(mActivity,dialog,messageArray[0]);
                    fountSource();
                } catch (Exception e) {
                    OverAppDialog.showDialog(mActivity,dialog,messageArray[1]+e.getMessage());
                }
            }
        });
    }


    //查找资源
    private void fountSource() {
        isPlay = false;
        long time = System.currentTimeMillis();
        String dir;
        ArrayList<String> homeList;
        //读取 usb内容
        homeList = SdCardTools.getAllStorePath(mActivity);

        if (homeList !=null){
            OverAppDialog.showDialog(mActivity,dialog,"获取目录路径耗时: "+ (System.currentTimeMillis()- time) +" , 路径数:"+ homeList.size());
            time = System.currentTimeMillis();
            dir = justPath(homeList,"usb");
            if (dir==null) dir = justPath(homeList,"ext");
            if (dir == null) dir = justPath(homeList,"sd");
            if (dir == null) dir = justPath(homeList,"emu");
        }else{
            throw new IllegalStateException("获取目录路径失败.");
        }
        if (dir!=null && !"".equals(dir)){
            OverAppDialog.showDialog(mActivity,dialog,"选择目录: "+ dir +", 耗时:"+ (System.currentTimeMillis()- time)+",准备遍历文件夹.请稍等");
            time = System.currentTimeMillis();
            final ArrayList<String> fileList = new ArrayList<>();

            //读取文件下所有资源文件->更新播放列表
            SdCardTools.getTaggerPrefixOnFiles(dir, fileList,FILTER_SUFFIX);
            if (!dir.equals(SdCardTools.getSDPath())){
                checkFileTranslation(fileList);
            }

            if (fileList.size()>0){
                OverAppDialog.showDialog(mActivity,dialog,"循环查询资源文件耗时: "+ (System.currentTimeMillis()- time) +"  资源文件数量: " + fileList.size());
                playList = fileList;
                curIndex = 0; //初始化播放位置
                curDirs = dir;
                isPlay = true;
                //通知重新开始播放
                onFinished();
            }else{
                throw new IllegalStateException("找不到有效资源文件,请检查存储设备是否正常.");
            }
        }
    }

    /**
     * 检测文件是否可转移到内置卡
     */
    private void checkFileTranslation(ArrayList<String> fileList) {
        if (fileList==null || fileList.isEmpty()) return;
        final String dirPath = SdCardTools.getSDPath();
        final ArrayList<String> list = new ArrayList<>();
        long fileSize = 0;
        for (String path : fileList){
                if (path.contains(TRANSLATE_DIRS) && !path.contains(dirPath)){
                    list.add(path);
                    fileSize += FileUtils.getFileSize(path);
                }
        }
        if (list.size()==0) return;

        final String dirs = dirPath+TRANSLATE_DIRS;
        if(FileUtils.isFolderExist(dirs)){
            //文件夹存在 - 清空内容
            SdCardTools.clearDir(dirs);
            OverAppDialog.showDialog(mActivity,dialog,"已清理本地目录:"+ dirs);
        }else{
            if (SdCardTools.MkDir(dirs))
            OverAppDialog.showDialog(mActivity,dialog,"文件夹:"+dirs+" 创建失败,资源转移失败.");
            return;
        }

        //获取文件总大小, 获取内置卡总大小
        long freeSize = SdCardTools.getSDFreeSizeBytes();
        OverAppDialog.showDialog(mActivity,dialog,"转移备用资源到内置卡,当前资源总大小: "+ fileSize +", 内置卡空闲空间: " + freeSize);
        if (fileSize - freeSize > 0) {
            OverAppDialog.showDialog(mActivity,dialog,"请手动清理内置卡存储空间.");
            return;
        }

        String path;
        for (int i = 0; i < list.size() ; i++){
            path = list.get(i);
            OverAppDialog.showDialog(mActivity,dialog,"复制文件: "+ path+" 进度: "+ i+"/"+list.size());
            SdCardTools.copyFile(path,dirs+path.substring(path.lastIndexOf("/")));
        }
        OverAppDialog.showDialog(mActivity,dialog,"已成功导入文件到内置卡.");
        waitTime(500);
    }

    /**
     * 还原初始化状态
     */
    public void uninitialized(){
        onStop();
        //关闭广播
        unregest();
        OverAppDialog.closeDialog(mActivity,dialog);
        dialog = null;
        mActivity = null;
        Log.e(TAG,"还原初始化状态");
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
                layer = new MImageMVideoSurfaceView(mActivity);
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
            //文件是否存在
            if (!FileUtils.isFileExist(source) || source.contains("/wosplayer/default") || source.contains("/wosplayer/ffbk") ){
                //移除资源
                removeSource(source);
                isNext = true;
                Logs.e(TAG,"移除资源 "+ source+",并且直接下一次执行");
                OverAppDialog.showDialog(mActivity,dialog,"移除资源:"+source+",文件是否存在:"+FileUtils.isFileExist(source));
            }else{
                //判断文件类型
                if (SdCardTools.justFileSuffix(source,FILTER_SUFFIX,0,2)){
                    //图片显示
                    type = 1;
                }else
                if (SdCardTools.justFileSuffix(source,FILTER_SUFFIX,2,FILTER_SUFFIX.length)){
                    //视频展示
                    type = 2;
                }
//                Logs.e(TAG,"播放资源 "+source);
                OverAppDialog.closeDialog(mActivity,dialog);
                layer.onPlayStart(source,type);
            }
            //下标加1
            if (++curIndex>=playList.size())  curIndex = 0;
            if (isNext) onStart();
        }
        else{
            //通知目录播放失败
            OverAppDialog.showDialog(mActivity,dialog,"不允许播放或播放目录文件内容错误.");
            onStop();

        }
    }
    //移除资源
    private void removeSource(String source) {
        try {
            if (playList!=null){
                playList.remove(source);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //结束
    private void onStop(){
        OverAppDialog.showDialog(mActivity,dialog,"清理资源处理中.");
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
        if (playList!=null) playList.clear();
        playList = null;
        curDirs = NODE;
        //初始化下标
        curIndex = 0;
        OverAppDialog.showDialog(mActivity,dialog,"清理资源完成.");
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
        waitTime(3 * 1000);
        OverAppDialog.showDialog(mActivity,dialog,"开始播放.");
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
                if (var!=null && !curDirs.equals(NODE)) {
                    String[] strarr = var.split("#");
                    if (strarr.length>0 && !curDirs.equals(NODE)){
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
                }
                onStop();
                onBackExecute();
            }
        });
    }


}
