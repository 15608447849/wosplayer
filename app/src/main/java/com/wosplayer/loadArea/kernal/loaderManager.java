package com.wosplayer.loadArea.kernal;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.wosplayer.app.Logs;
import com.wosplayer.cmdBroadcast.Command.OtherCmd.UPDCbroad;
import com.wosplayer.loadArea.TASKLIST.Task;
import com.wosplayer.loadArea.TASKLIST.TaskQueue;
import com.wosplayer.loadArea.excuteBolock.LoaderCall;

import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by user on 2016/7/21.
 *  1.是一个intentService
 *  2.维护一个count , 当count 与 当前所需要下载的任务相同的时候 发送一个 下载完成广播
 *
 */
public class loaderManager extends IntentService
{

    public static final String KEY_TYPE="type";
    public static final int KEY_TYPE_SCHDULE = 1;
    public static final int KEY_TYPE_UPDATE_APK = 2;
    public static final int KEY_TYPE_FFBK = 3;


    public static final String KEY_TERMINAL_NUM="terminalNo";
    public static final String KEY_SAVE_PATH="savepath";
    public static final String KEY_ALIAS="alias";
    public static final String KEY_TASK_LIST="loaderTaskArr";
    public static final String KEY_TASK_SINGLE="loaderTasksingle";

    private static final String TAG = "_loaderManager";
    public loaderManager() {
        super("loaderManager");
    }
    private static ReentrantLock lock = new ReentrantLock();
    @Override
    public void onCreate() {
        super.onCreate();
        Logs.i("--------------------------------------------loaderManager create--------------------------------------------------------");
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        Logs.i("-------------------------------------------loaderManager destroy----------------------------------------------------------");
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        Bundle bundle = intent.getExtras();
        int loadType = bundle.getInt(KEY_TYPE);
        String terminalNo = bundle.getString(KEY_TERMINAL_NUM,"");//终端id
        String savepath = bundle.getString(KEY_SAVE_PATH,"");//保存路径
        String alias = bundle.getString(KEY_ALIAS,"");//别名
        ArrayList<CharSequence> taskList = bundle.getCharSequenceArrayList(KEY_TASK_LIST);//任务列表
        String singUrl = bundle.getString(KEY_TASK_SINGLE,"");//单一任务


        if (loadType == KEY_TYPE_SCHDULE){
            //排期发来的资源下载列表
            scheduleSourceLoad(terminalNo,savepath,taskList);
        }else
        if (loadType == KEY_TYPE_UPDATE_APK){
            //更新apk
            updateAPK(terminalNo,savepath,singUrl);
        }else
        if (loadType == KEY_TYPE_FFBK){
            //富滇银行 资源文件下载
            loadResource(terminalNo,savepath,singUrl,alias);
        }else{
           Logs.d(TAG, "onHandleIntent: 错误的下载任务类型");
        }
    }

    //下载富癫银行的资源文件
    private void loadResource(String terminalNo, String savepath, String singUrl, String alias) {
        Logs.i(TAG,"下载 web resouce - "+singUrl+"\n savepath - "+savepath+"\n terminalNo - "+terminalNo+"\n alias: "+alias);
        Task task = new Task(savepath,terminalNo,singUrl,null);
        task.setFileName(alias);
        TaskQueue.getInstants().addTask(task);
    }

    //更新apk
    private void updateAPK(String terminalNo, String savepath, String singUrl) {
        Logs.i(TAG,"下载 apk - "+singUrl+"\n savepath - "+savepath+"\n terminalNo - "+terminalNo);
        TaskQueue.getInstants().addTask(new Task(savepath, terminalNo, singUrl, new LoaderCall() {
            @Override
            public void downloadResult(String filePath, String state) {
                try {
                    Logs.i(TAG,"下载apk成功  - 路径- "+ filePath );
                    Intent intent = new Intent();
                    intent.setAction(UPDCbroad.ACTION);
                    Bundle bundle = new Bundle();
                    bundle.putString(UPDCbroad.key,filePath);
                    intent.putExtras(bundle);
                    getApplicationContext().sendBroadcast(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }));

    }

    private void scheduleSourceLoad(String terminalNo,String savepath,ArrayList<CharSequence> taskList) {
        Log.i(TAG,"收到一个 排期资源下载队列, 队列大小:"+taskList.size()+"\n terminalNo="+terminalNo+"\n savepath="+savepath);
        for (int i = 0;i<taskList.size();i++){
            TaskQueue.getInstants().addTask(new Task(savepath,terminalNo,(String)taskList.get(i),null));
        }
        Intent intent = new Intent();
        intent.setAction(completeTaskListBroadcast.action);
        getApplicationContext().sendBroadcast(intent);
    }


}
